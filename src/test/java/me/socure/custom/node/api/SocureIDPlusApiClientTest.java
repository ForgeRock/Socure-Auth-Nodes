////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.api;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import me.socure.custom.node.model.SocureIDPlusRequestVO;

public class SocureIDPlusApiClientTest {

    @Test
    @Disabled
    void execute() throws IOException, InterruptedException {
        SocureIDPlusApiClient apiClient = new SocureIDPlusApiClient();
        SocureIDPlusRequestVO requestObj = new SocureIDPlusRequestVO();
        requestObj.setEmail("test@socure.com");

        requestObj.setFirstName("Jane");
        requestObj.setSurName("Doe");
        requestObj.setModules(List.of("kyc", "emailrisk", "decision"));
        String url = "http://localhost:8080/responder";
        String apiKey = "SocureApiKey apiKey";
        JsonNode resp = apiClient.execute(requestObj, url, apiKey);
        System.out.println(resp.toPrettyString());
        Assertions.assertTrue(null != resp);
    }
}