package me.socure.custom.node.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
@Slf4j
public class ConstantsTest {

    @Test
    public void testStringFormat() {
        String result = String.format(Constants.fileContent,"https://someurl", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        String patternString = "(#flow|#send_message)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(result);
        Map<String,String> tokens = Map.of("#flow","socure_default","#send_message","false");
        StringBuilder sb = new StringBuilder();
        while(matcher.find()) {
            matcher.appendReplacement(sb, tokens.get(matcher.group(0)));
        }
        matcher.appendTail(sb);

        log.info(sb.toString());
        log.info(DeviceRiskJS.CONTENT);
        Assertions.assertTrue(sb.toString().contains("socure_default"));
    }


}