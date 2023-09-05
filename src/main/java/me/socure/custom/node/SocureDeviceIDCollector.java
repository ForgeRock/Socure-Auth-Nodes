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
import java.util.Optional;
import java.util.ResourceBundle;
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

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.sm.RequiredValueValidator;
import me.socure.custom.node.utils.Decision;
import me.socure.custom.node.utils.DeviceRiskJS;

/**
 * A node that initiates the document verification process, allows the user to scan the documents using their mobile device, performs selfie matching, and authenticates the processed document
 */
@Node.Metadata(outcomeProvider = SocureDeviceIDCollector.OutcomeProvider.class,
    configClass = SocureDeviceIDCollector.Config.class, tags = {
    "Device Risk Device Id collector", "marketplace", "trustnetwork"})
public class SocureDeviceIDCollector extends AbstractDecisionNode implements Node {

    private static final String CALLBACK_DEVICE_ID = "device_id";
    private final Logger logger = LoggerFactory.getLogger(SocureDeviceIDCollector.class);
    private final Config config;
    private final Realm realm;
    private final String loggerPrefix = "[SocureDeviceRisk Node][Marketplace] ";

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm  The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public SocureDeviceIDCollector(@Assisted Config config, @Assisted Realm realm)
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
        return collectDeviceId(context);
    }

    private Action collectDeviceId(TreeContext context) {
        try {
            List<HiddenValueCallback> hiddenValues =
                context.getCallbacks(HiddenValueCallback.class);

            if (hiddenValues.isEmpty()) {
                logger.warn(loggerPrefix + "Callbacks are missing. Restart the deviceRisk");
                return buildDocvCallback(context);
            }

            String deviceId = getHiddenCallbackValue(CALLBACK_DEVICE_ID, context);
            context.getStateFor(this).putShared(CALLBACK_DEVICE_ID, deviceId);
            return goToAction(Decision.Accept)
                .withHeader("deviceId capture successfully")
                .putSessionProperty(CALLBACK_DEVICE_ID, deviceId)
                .build();
        } catch (Exception e) {
            logger.error(loggerPrefix + "Error processing document callback", e);
            context.getStateFor(this)
                .putShared(loggerPrefix + "Exception", new Date() + ": " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            context.getStateFor(this)
                .putShared(loggerPrefix + "StackTrace", new Date() + ": " + sw);
            return goToAction(Decision.Error)
                .withHeader("Error Processing request")
                .withErrorMessage(e.getMessage())
                .build();
        }

    }

    private String getHiddenCallbackValue(String callBackId, TreeContext context) {
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

        HiddenValueCallback hiddenDeviceId = new HiddenValueCallback(CALLBACK_DEVICE_ID);
        ImmutableList<Callback> callbacks =
            ImmutableList.of(scriptCallback, hiddenDeviceId);
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
        String tempString = String.format(DeviceRiskJS.CONTENT, config.websdkUrl(),
            new String(config.websdkKey()));
        return tempString;

    }

    /**
     * Configuration for the node.
     */
    public interface Config {

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

    }

    public static final class OutcomeProvider
        implements org.forgerock.openam.auth.node.api.OutcomeProvider {

        private static final String BUNDLE = SocureDeviceIDCollector.class.getName();

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            final ResourceBundle bundle =
                locales.getBundleInPreferredLocale(BUNDLE, OutcomeProvider.class.getClassLoader());
            return Arrays.asList("Error", "Accept")
                .stream()
                .map(d -> buildOutcome(bundle, d))
                .collect(Collectors.toUnmodifiableList());
        }

        private Outcome buildOutcome(ResourceBundle bundle, String key) {
            return new Outcome(key, key);
        }
    }
}
