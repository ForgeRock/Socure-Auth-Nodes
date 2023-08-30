package me.socure.custom.node.utils;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ConstantsTest {

    @Test
    public void testStringFormat() {
        String result = String.format(Constants.fileContent,"https://someurl", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        System.out.println(result);
    }
}