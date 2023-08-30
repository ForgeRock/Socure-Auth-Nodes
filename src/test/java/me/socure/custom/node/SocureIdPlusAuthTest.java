////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.OutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.util.i18n.PreferredLocales;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import me.socure.custom.node.api.SocureIDPlusApiClient;

/**
 *
 */
public class SocureIdPlusAuthTest {


    private SocureIdPlusAuth socureIDplusNode;
    private SocureIDPlusApiClient apiClient = new SocureIDPlusApiClient();


    @BeforeEach
    public void setup() throws NodeProcessException, NoSuchFieldException, IllegalAccessException {
       socureIDplusNode = new SocureIdPlusAuth(new MyConfig(),new Alpha());
       System.out.println(socureIDplusNode.getClass().getFields());
       Field fld = socureIDplusNode.getClass().getDeclaredField("apiClient");
       fld.setAccessible(true);
       fld.set(socureIDplusNode,apiClient);
  }


    @Test
    public void process() throws NodeProcessException {


        Map map = Map.of("sn","Jane","cn","Doe");
        JsonValue sharedState = new JsonValue(new HashMap<>());
        sharedState.add("objectAttributes", map);

        ExternalRequestContext request = new ExternalRequestContext.Builder()
            .authId(UUID.randomUUID().toString())
            .hostName("localhost")
            .build();

        TreeContext context = new TreeContext(sharedState,request,new ArrayList<>(),
            Optional.ofNullable(UUID.randomUUID().toString()));

       Action actionResult = socureIDplusNode.process(context);
       System.out.println(actionResult.outcome);
    }

    @Test
    void testOutcomes() {
        SocureIdPlusAuth.OutcomeProvider outcomeProvider = new SocureIdPlusAuth.OutcomeProvider();
        List<OutcomeProvider.Outcome>
            outcomes = outcomeProvider.getOutcomes(new PreferredLocales(),null);
        System.out.println(outcomes);
    }

    public static class MyConfig implements SocureIdPlusAuth.Config {

    }
    public static class Alpha implements Realm{

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
}