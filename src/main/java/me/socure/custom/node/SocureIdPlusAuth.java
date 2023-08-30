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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.TextOutputCallback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.NodeState;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.authentication.callbacks.StringAttributeInputCallback;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;
import me.socure.custom.node.api.SocureIDPlusApiClient;
import me.socure.custom.node.model.AttributesMapping;
import me.socure.custom.node.model.SocureIDPlusRequestVO;
import me.socure.custom.node.utils.DateUtils;
import me.socure.custom.node.utils.Decision;

/**
 * A node that verifies the user attributes
 */
@Node.Metadata(outcomeProvider = SocureIdPlusAuth.OutcomeProvider.class,
    configClass = SocureIdPlusAuth.Config.class, tags = {"Identify Verification", "marketplace","trustnetwork"})
public class SocureIdPlusAuth extends AbstractDecisionNode implements Node {

    public static final String CURRENT_STEP = "current_step";
    public static final Gson GSON = new Gson();
    public static final String OBJECT_ATTRIBUTES = "objectAttributes";
    public static final String DECISION = "decision";
    public static final String VALUE = "value";
    public static final String STATUS = "status";
    public static final String MSG = "msg";
    public static final String ERROR = "Error";
    public static final String MISSING_INPUT = "missing_input";
    public static final String ID_VERIFICATION = "id-verification";
    public static final String ID_PLUS_DECISION = "idPlus_decision";
    public static final String SUCCESS = "success";
    private static final String CALLBACK_DOCV_ID = "docvdata";
    private static final String CALLBACK_DEVICE_ID = "device_id";
    private final Logger logger = LoggerFactory.getLogger(SocureIdPlusAuth.class);
    private final Config config;
    private final Realm realm;
    private final String loggerPrefix = "[SocureIdPlus Node][Marketplace] ";

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
    public SocureIdPlusAuth(@Assisted Config config, @Assisted Realm realm)
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
    public Action process(TreeContext context) {
        return processIdVerification(context);
    }

    private Action processIdVerification(TreeContext context) {

        if (context.hasCallbacks()) {
            Map<String, Object> attributesObj =
                context.getStateFor(this).get(OBJECT_ATTRIBUTES).asMap();
            for (StringAttributeInputCallback stic : context.getCallbacks(
                StringAttributeInputCallback.class)) {
                attributesObj.put(stic.getName(), stic.getValue());
            }
        }
        NodeState sharesState = context.getStateFor(this);
        SocureIDPlusRequestVO requestObj = buildRequest(context);
        List<Callback> attributeCallbacks = validateRequest(requestObj);
        if (!attributeCallbacks.isEmpty()) {
            sharesState.putShared(CURRENT_STEP, MISSING_INPUT);
            attributeCallbacks.add(0,
                new TextOutputCallback(2, "Please enter required information"));
            return Action.send(attributeCallbacks).build();
        }

        logger.info(loggerPrefix + "Setting modules {}", config.modules());
        requestObj.setModules(config.modules());
        JsonNode resp = null;
        try {
            resp = apiClient.execute(requestObj, this.config.SocureApiEndpoint(),
                new String(this.config.SocureApiKey()));
            logger.debug(loggerPrefix + resp.toPrettyString());
            if(resp.has(STATUS) && ERROR.equals(resp.get(STATUS).asText())){
                throw new IllegalArgumentException(resp.get(MSG).asText());
            }
            final String decisionValue = resp.get(DECISION).get(VALUE).asText();
            logger.info(loggerPrefix + "Id+ Decision : {}", decisionValue);
            sharesState.putShared(ID_PLUS_DECISION,decisionValue);
            return goToAction(Decision.from(decisionValue))
                .withHeader("Identity verified successfully")
                .putSessionProperty(ID_VERIFICATION, SUCCESS)
                .build();
        } catch (Exception e) {
            logger.error(loggerPrefix + "Error processing request", e);
            context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            context.getStateFor(this).putShared(loggerPrefix + "StackTrace", new Date() + ": " + sw.toString());
            return goToAction(Decision.Error)
                .withErrorMessage(e.getMessage())
                .build();
        }
    }

    protected Action.ActionBuilder goToAction(Decision outcome) {
        return Action.goTo(outcome.name());
    }

    private Optional<String> getSharedStateValue(TreeContext tree, String parameter) {
        if (!tree.getStateFor(this).isDefined(parameter)) return Optional.empty();
        return Optional.ofNullable(tree.getStateFor(this).get(parameter).asString());
    }

    private SocureIDPlusRequestVO buildRequest(TreeContext context) {
        StringJoiner stringJoiner = new StringJoiner(" | ");
        try {
            stringJoiner.add("Reading shared state");

            Map<String, String> attrMap = this.config.attributes();
            final Map<String, BiConsumer<SocureIDPlusRequestVO, String>> attributeMap =
                AttributesMapping.attributeMap();
            NodeState state = context.getStateFor(this);
            logger.debug(loggerPrefix + "Shared State {} ", GSON.toJson(state));
            logger.debug(loggerPrefix + "Keys {}", state.keys());
            Map<String, Object> attributesObj = state.get(OBJECT_ATTRIBUTES).asMap();
            logger.debug(loggerPrefix + "attributesObj {}", attributesObj);
            SocureIDPlusRequestVO requestVO = new SocureIDPlusRequestVO();
            requestVO.setIpAddress(context.request.clientIp);
            attrMap.entrySet().forEach(entry -> {
                String key = entry.getKey();
                stringJoiner.add("Key").add(key);
                String ldapAttribute = entry.getValue();
                stringJoiner.add("ldapAttribute").add(ldapAttribute);
                String value = attributesObj.getOrDefault(ldapAttribute, "").toString();
                if (attributeMap.containsKey(key)) attributeMap.get(key).accept(requestVO, value);
            });
            logger.debug(loggerPrefix + "Input data {} ", GSON.toJson(requestVO));
            return requestVO;
        } finally {
            logger.info(loggerPrefix + stringJoiner);
        }
    }

    private List<Callback> validateRequest(SocureIDPlusRequestVO requestVO) {
        String dob = requestVO.getDob();
        List<Callback> callbacks = new ArrayList<>();
        Map<String,String> attributeMap = config.attributes();
        if (null != dob && !DateUtils.isValidDob(dob)) {
            StringAttributeInputCallback textInputCallback =
                new StringAttributeInputCallback(attributeMap.get("dob"), "Date of Birth", "", true);
            callbacks.add(textInputCallback);
        }
        String ssn = requestVO.getNationalId();
        if (null != ssn && !(ssn.length() == 4 || ssn.length() == 9)) {
            StringAttributeInputCallback textInputCallback =
                new StringAttributeInputCallback(attributeMap.get("ssn"), "Social Security Number", "", true);
            callbacks.add(textInputCallback);
        }
        String zipCode = requestVO.getZip();
        if(null != zipCode && !zipCode.matches("^[0-9]{5}(?:-[0-9]{4})?$")){
            StringAttributeInputCallback textInputCallback =
                new StringAttributeInputCallback(attributeMap.get("zipCode"), "Postal Code", "", true);
            callbacks.add(textInputCallback);
        }
        String mobileNumber = requestVO.getMobileNumber();
        if(null != mobileNumber && !mobileNumber.matches("^[0-9]{10}$")){
            StringAttributeInputCallback textInputCallback =
                new StringAttributeInputCallback(attributeMap.get("mobilePhone"), "Mobile Phone", "", true);
            callbacks.add(textInputCallback);
        }
        return callbacks;
    }

    /**
     * SocureIDPlus Modules
     */
    public enum SocureIDPlusModules {
        emailrisk, phonerisk, fraud, addressrisk, synthetic, decision, kyc;

        public static List<String> toList() {
            return Arrays.stream(values())
                .map(SocureIDPlusModules::name)
                .collect(Collectors.toList());
        }
    }

    public static final class OutcomeProvider
        implements org.forgerock.openam.auth.node.api.OutcomeProvider {

        private static final String BUNDLE = SocureIdPlusAuth.class.getName();

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            final ResourceBundle bundle =
                locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return Arrays.stream(Decision.values())
                .map(d -> buildOutcome(bundle, d.name()))
                .collect(Collectors.toUnmodifiableList());
        }

        private Outcome buildOutcome(ResourceBundle bundle, String key) {
            return new Outcome(key, key);
        }
    }

    /**
     * Configuration for the node.
     */
    public static interface Config {

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
         * The list of modules should be used with the identity verification call.
         * identity must be in.
         */
        @Attribute(order = 300, validators = {RequiredValueValidator.class})
        default List<String> modules() {
            return SocureIDPlusModules.toList();
        }

        /**
         * Map for picking ForgeRockLDAP Attributes to ID+ Api. The KEY should be the Socure attribute JSON key and the VALUE should be the corresponding ForgeRock LDAP Attribute.
         */
        @Attribute(order = 400)
        default Map<String, String> attributes() {
            Map<String, String> attributeMap = new HashMap<>();
            attributeMap.put("ssn", "ssn");
            attributeMap.put("mobilePhone", "telephoneNumber");
            attributeMap.putAll(Map.of("firstName", "givenName",
                "lastName", "sn",
                "streetAddress", "postalAddress",
                "city", "city",
                "state", "stateProvince",
                "zipCode", "postalCode",
                "countryCode", "country",
                "email", "mail",
                "dob", "dob"
            ));
            return attributeMap;

        }

    }
}
