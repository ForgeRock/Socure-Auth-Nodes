/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */

package me.socure.custom.node;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.sm.RequiredValueValidator;
import me.socure.custom.node.api.SocureIDPlusApiClient;
import me.socure.custom.node.model.DocvCallbackVO;
import me.socure.custom.node.model.SocureIDPlusRequestVO;
import me.socure.custom.node.utils.Constants;
import me.socure.custom.node.utils.Decision;

/**
 *A node that initiates the document verification process, allows the user to scan the documents using their mobile device, performs selfie matching, and authenticates the processed document
 */
@Node.Metadata(outcomeProvider = SocureDocumentVerificationNode.OutcomeProvider.class,
    configClass = SocureDocumentVerificationNode.DocumentVerificationConfig.class, tags = {
    "Document Verification", "marketplace", "trustnetwork"})
public class SocureDocumentVerificationNode extends AbstractDecisionNode implements Node {

    public static final String CURRENT_STEP = "current_step";
    public static final Gson GSON = new Gson();
    public static final String DECISION = "decision";
    public static final String VALUE = "value";
    public static final String DOCUMENT_VERIFICATION = "documentVerification";
    public static final String DOCUMENT_DATA = "documentData";
    private final Logger logger = LoggerFactory.getLogger(SocureDocumentVerificationNode.class);
    private final DocumentVerificationConfig config;
    private final Realm realm;
    private String loggerPrefix = "[SocureDocumentVerification Node][Marketplace] ";
    private static final String CALLBACK_DOCV_ID = "docvdata";
    private static final String CALLBACK_DEVICE_ID = "device_id";
    /* Pattern to set custom docv parameter to customize the docv */
    private static final Pattern pattern =
        Pattern.compile("(#flow|#send_message|#lang|#redirect_method|#url|#doctype)");
    @Inject
    private SocureIDPlusApiClient apiClient;

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm  The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public SocureDocumentVerificationNode(@Assisted DocumentVerificationConfig config, @Assisted Realm realm)
    throws NodeProcessException {
        this.config = config;
        this.realm = realm;
    }

    /**
     * Processes Socure ID+ and Document Verification Request.
     *
     * @param context
     * @return Action Action
     * @throws NodeProcessException
     */
    @Override
    public Action process(TreeContext context)  {
         return processDocumentVerification(context);
    }

    private Action processDocumentVerification(TreeContext context) {
        try {
            List<HiddenValueCallback> hiddenValues = context.getCallbacks(HiddenValueCallback.class);

            if (hiddenValues.isEmpty()) {
                logger.warn(loggerPrefix + "Callbacks are missing. Restart the document verification");
                return buildDocvCallback(context);
            }
            String docvData = getHiddenCallbackValue(CALLBACK_DOCV_ID,context) ;
            String deviceId = getHiddenCallbackValue(CALLBACK_DEVICE_ID,context) ;
            logger.info(loggerPrefix + "Document Data {}", docvData);
            DocvCallbackVO docvCallbackVO = GSON.fromJson(docvData, DocvCallbackVO.class);

            SocureIDPlusRequestVO requestObj = new SocureIDPlusRequestVO();
            requestObj.setModules(SocureIDPlusDocvModules.toList());
            requestObj.setDocumentUuid(docvCallbackVO.getDocumentUuid());
            requestObj.setDeviceSessionId(deviceId);
            requestObj.setIpAddress(context.request.clientIp);

            JsonNode resp = apiClient.execute(requestObj, this.config.SocureApiEndpoint(),
                new String(this.config.SocureApiKey()));
            logger.debug(loggerPrefix + "Response {}", resp.toPrettyString());
            context.getStateFor(this).putShared(Constants.DOCUMENT_DATA, resp);
            final String decisionValue = resp.get(DECISION).get(VALUE).asText();
            storeDocumentAttributesToLDAP(context, resp);
            return goToAction(Decision.from(decisionValue))
                .withHeader("Document verified successfully")
                .putSessionProperty(DOCUMENT_VERIFICATION, decisionValue)
                .build();
        } catch (Exception e) {
            logger.error(loggerPrefix + "Error processing document callback", e);
            context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            context.getStateFor(this).putShared(loggerPrefix + "StackTrace", new Date() + ": " + sw.toString());
            return goToAction(Decision.Error)
                .withHeader("Error Processing request")
                .withErrorMessage(e.getMessage())
                .build();
        }

    }

    private String getHiddenCallbackValue(String callBackId, TreeContext context){
        return
            context.getCallbacks(HiddenValueCallback.class)
            .stream()
            .filter(h -> h.getId().equals(callBackId))
            .findFirst().map(HiddenValueCallback::getValue)
            .orElse(null);
    }
    private Action buildDocvCallback(final TreeContext context) {
        logger.info(loggerPrefix + "Sending script output");

        String decvScript = getScriptAsString(context);
        ScriptTextOutputCallback scriptCallback = new ScriptTextOutputCallback(decvScript);
        HiddenValueCallback hiddenDocvData = new HiddenValueCallback(CALLBACK_DOCV_ID);
        HiddenValueCallback hiddenDeviceId = new HiddenValueCallback(CALLBACK_DEVICE_ID);
        ImmutableList<Callback> callbacks =
            ImmutableList.of(scriptCallback, hiddenDocvData, hiddenDeviceId);
        context.getStateFor(this).putShared(CURRENT_STEP, "document_verification");
        return Action.send(callbacks).build();
    }

    protected Action.ActionBuilder goToAction(Decision outcome) {
        return Action.goTo(outcome.name());
    }

    private Optional<String> getSharedStateValue(TreeContext tree, String parameter) {
        if (!tree.getStateFor(this).isDefined(parameter)) return Optional.empty();
        return Optional.ofNullable(tree.getStateFor(this).get(parameter).asString());
    }

    /**
     * Socure Web SDK loading java script.
     *
     * @return String java script code block
     */
    private String getScriptAsString(TreeContext context) {
        String tempString = String.format(Constants.fileContent, config.websdkUrl(),
            new String(config.websdkKey()), new String(config.websdkKey()));

        Matcher matcher = pattern.matcher(tempString);

        Map<String, String> tokens =
            Map.of("#flow", Optional.ofNullable(config.flow()).orElse("socure_default")
                , "#send_message", config.sendMessage() ? "true" : "false"
                , "#lang", context.request.locales.getPreferredLocale().getLanguage()
                , "#redirect_method", Optional.ofNullable(config.redirectMethod()).orElse("")
                , "#url", Optional.ofNullable(config.redirect()).orElse(""),
                "#doctype", Optional.ofNullable(config.defaultDocumentType()).orElse(""));
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    private void storeDocumentAttributesToLDAP(TreeContext context, JsonNode resp) {
        Map<String, String> docAttribMap = this.config.docAttributes();
        if (null == docAttribMap || docAttribMap.isEmpty()) return;
        Map<String, Object> attributesObj =
            context.getStateFor(this).get(Constants.OBJECT_ATTRIBUTES).asMap();
        boolean hasDocData = resp.hasNonNull(DOCUMENT_VERIFICATION)
            && resp.get(DOCUMENT_VERIFICATION).hasNonNull(DOCUMENT_DATA);
        if (!hasDocData) return;
        JsonNode documentData = resp.get(DOCUMENT_VERIFICATION).get(DOCUMENT_DATA);
        for (Map.Entry<String, String> entry : docAttribMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                attributesObj.put(entry.getValue(), documentData.get(entry.getKey()).asText());
            }
        }
    }



    /**
     * SocureIDPlus Modules
     */
    public enum SocureIDPlusDocvModules {
        documentverification, devicerisk, decision;

        public static List<String> toList() {
            return Arrays.stream(values())
                .map(SocureIDPlusDocvModules::name)
                .collect(Collectors.toList());
        }
    }

    /**
     * Configuration for the node.
     */
    public static interface DocumentVerificationConfig {

        /**
         * The SocureApiEndpoint.
         */
        @Attribute(order = 100, validators = {RequiredValueValidator.class})
        default String SocureApiEndpoint() {
            return "";
        }

        /**
         * The api key should be used with the identity verification call.
         * identity must be in.
         */
        @Attribute(order = 200, validators = {RequiredValueValidator.class})
        @Password
        default char[] SocureApiKey() {
            return "".toCharArray();

        }

        /**
         * The websdk key should be used with the Document verification SDK.
         */
        @Attribute(order = 300, validators = {RequiredValueValidator.class})
        @Password
        default char[] websdkKey() {
            return "".toCharArray();
        }

        /**
         * The websdk javascript endpoint.
         */
        @Attribute(order = 400, validators = {RequiredValueValidator.class})
        default String websdkUrl() {
            return "";
        }

        /**
         * Mapping between PII collected from document and ForgeRockLDAP Attributes. The KEY should be the document attribute and the VALUE should be the corresponding ForgeRock LDAP Attribute.
         */
        @Attribute(order = 500)
        default Map<String, String> docAttributes() {
            return Map.of("firstName", "givenName",
                "surName", "sn",
                "address", "postalAddress",
                "documentNumber", "",
                "dob", "dob",
                "expirationDate", "",
                "issueDate", ""
            );
        }

        @Attribute(order = 600)
        default String flow() {
            return "socure_default";
        }

        @Attribute(order = 700)
        default boolean sendMessage() {
            return true;
        }

        @Attribute(order = 800)
        default String redirect() {
            return "";
        }

        @Attribute(order = 900)
        default String redirectMethod() {
            return "";
        }

        @Attribute(order = 1000)
        default String defaultDocumentType() {
            return "";
        }
    }

    public static final class OutcomeProvider
        implements org.forgerock.openam.auth.node.api.OutcomeProvider {

        private static final String BUNDLE = SocureDocumentVerificationNode.class.getName();

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            final ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return Arrays.stream(Decision.values())
                .map(d -> buildOutcome(bundle, d.name()))
                .collect(Collectors.toUnmodifiableList());
        }

        private Outcome buildOutcome(ResourceBundle bundle, String key) {
            return new Outcome(key, key);
        }
    }
}
