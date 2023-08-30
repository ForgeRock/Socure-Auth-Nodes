////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.api;

import com.fasterxml.jackson.databind.JsonNode;
import me.socure.custom.node.model.SocureIDPlusRequestVO;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SocureIDPlusApiClientTest {

    @Test
    void execute() throws IOException, InterruptedException {
        SocureIDPlusApiClient apiClient = new SocureIDPlusApiClient();
        SocureIDPlusRequestVO requestObj = new SocureIDPlusRequestVO();
        requestObj.setEmail("test@socure.com");

        requestObj.setFirstName("Jane");
        requestObj.setSurName("Doe");
        requestObj.setModules(List.of("kyc","emailrisk", "decision"));
        String url = "https://sandbox.socure.com/api/3.0/EmailAuthScore";
        String apiKey = "SocureApiKey c966f77b-7ce0-4063-8e5f-de875d33304f";
        JsonNode resp = apiClient.execute(requestObj, url, apiKey);
        System.out.println(resp.toPrettyString());
        Assertions.assertTrue(null != resp);
    }
}