////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import me.socure.custom.node.model.SocureIDPlusRequestVO;

/**
 * SocureIDPlusApiClient
 */
@Singleton
public class SocureIDPlusApiClient {
    public static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "content-type";
    public static final String ACCEPT = "accept";
    private static final String KEY_PREFIX = "SocureApiKey ";
    private ObjectMapper objectMapper = null;

    public SocureIDPlusApiClient() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * JsonNode execute.
     *
     * @param requestObj requestObj
     * @return JsonNode JsonNode
     */
    public JsonNode execute(final SocureIDPlusRequestVO requestObj, final String uri,
                            final String apiKey)
    throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header(AUTHORIZATION, KEY_PREFIX.concat(apiKey))
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestObj)))
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

}

