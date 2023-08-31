package me.socure.custom.node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import lombok.extern.slf4j.Slf4j;
import me.socure.custom.node.api.SocureIDPlusApiClient;
import me.socure.custom.node.model.SocureIDPlusRequestVO;
import me.socure.custom.node.utils.Constants;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.security.auth.callback.Callback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class SocureDocumentVerificationNodeTest {

    private SocureDocumentVerificationNode docvNode;
    private SocureIDPlusApiClient apiClient = new DummyRest();

    @BeforeEach
    void setUp() throws NoSuchFieldException, NodeProcessException, IllegalAccessException {
        docvNode = new SocureDocumentVerificationNode(new SocureDocumentVerificationNodeTest.MyConfig(),new SocureIdPlusAuthTest.Alpha());
        Field fld = docvNode.getClass().getDeclaredField("apiClient");
        fld.setAccessible(true);
        fld.set(docvNode,apiClient);
    }

    @Test
    void testProcess() {
        Map map = Map.of("givenName","Jane","sn","Doe","telephoneNumber","+15129208145");
        JsonValue sharedState = new JsonValue(new HashMap<>());
        sharedState.add("objectAttributes", map);

        ExternalRequestContext request = new ExternalRequestContext.Builder()
            .authId(UUID.randomUUID().toString())
            .hostName("localhost")
            .build();

        TreeContext context = new TreeContext(sharedState,request,new ArrayList<>(),
            Optional.ofNullable(UUID.randomUUID().toString()));

        Action actionResult = docvNode.process(context);
        Assertions.assertNotNull(actionResult,"Action result must be not null");
        Assertions.assertNotNull(actionResult.callbacks,"callbacks must be not null");
    }

    @Test
    void testCallbacks() {
        Map map = new HashMap<>();
        JsonValue sharedState = new JsonValue(new HashMap<>());
        sharedState.add("objectAttributes", map);

        ScriptTextOutputCallback scriptCallback = new ScriptTextOutputCallback(Constants.fileContent);

        HiddenValueCallback hiddenDocvData = new HiddenValueCallback("docvdata");
        hiddenDocvData.setValue("{\"documentUuid\":\"00000000000\"}");

        HiddenValueCallback hiddenDeviceId = new HiddenValueCallback("device_id");
        hiddenDeviceId.setValue(UUID.randomUUID().toString());

        ImmutableList<Callback>
            callbacks = ImmutableList.of(scriptCallback, hiddenDocvData,hiddenDeviceId);

        ExternalRequestContext request = new ExternalRequestContext.Builder()
            .authId(UUID.randomUUID().toString())
            .hostName("localhost")
            .build();

        TreeContext context = new TreeContext(sharedState,request,callbacks,
            Optional.ofNullable(UUID.randomUUID().toString()));

        Action actionResult = docvNode.process(context);
        Assertions.assertNotNull(actionResult,"Action result must be not null");
        Assertions.assertNotNull(actionResult.callbacks,"callbacks must be not null");
    }

    public static class MyConfig implements SocureDocumentVerificationNode.DocumentVerificationConfig {

    }
    public static class Alpha implements Realm {

        @Override
        public String asPath() {
            return "/alpha";
        }

        @Override
        public String asRoutingPath() {
            return "/alpha";
        }

        @Override
        public String asDN() {
            return "alpha";
        }
    }

    private static class DummyRest extends SocureIDPlusApiClient{
        public JsonNode execute(final SocureIDPlusRequestVO requestObj, final String uri,
                                final String apiKey){
            try {
                return new ObjectMapper().readTree("{\n" +
                    "  \"documentVerification\": {\n" +
                    "    \"documentData\": {\n" +
                    "      \"firstName\": \"Jane\",\n" +
                    "      \"surName\": \"Doe\",\n" +
                    "      \"address\": \"330, NY 10001\",\n" +
                    "      \"documentNumber\": \"000000000\",\n" +
                    "      \"dob\": \"1980-01-01\",\n" +
                    "      \"expirationDate\": \"2020-01-01\",\n" +
                    "      \"issueDate\": \"2015-01-01\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"decision\": {\n" +
                    "    \"value\": \"accept\"\n" +
                    "  }\n" +
                    "}");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}