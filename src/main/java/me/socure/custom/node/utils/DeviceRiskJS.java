////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.utils;

public class DeviceRiskJS {

    public static final String CONTENT = "try {\n" +
        "    let loginButton = document.getElementById('loginButton_0');\n" +
        "    let submitButton = document.querySelector(\"button[type='submit']\");\n" +
        "    if (null != loginButton) loginButton.style.display = 'none';\n" +
        "    if (null != submitButton) submitButton.style.display = 'none';\n" +
        "\n" +
        "    function onSuccess(e) {\n" +
        "        document.getElementById('device_id').value = e || \"null\";\n" +
        "        if (null != loginButton) {\n" +
        "            loginButton.click();\n" +
        "        }\n" +
        "    };\n" +
        "\n" +
        "    var script = document.createElement('script');\n" +
        "    script.setAttribute(\"src\", \"%s\");\n" +
        "    script.setAttribute(\"data-public-key\", \"%s\");\n" +
        "    script.setAttribute(\"context\", \"login\");\n" +
        "    document.body.appendChild(script);\n" +
        "\n" +
        "    script.onload = function () {\n" +
        "        SigmaDeviceManager.getDeviceSessionId().then((r) => {\n" +
        "            console.log(r);\n" +
        "            onSuccess(r);\n" +
        "        });\n" +
        "        \n" +
        "    };\n" +
        "} catch (error) {\n" +
        "    console.error(error);\n" +
        "    if (null != loginButton) {\n" +
        "        loginButton.click();\n" +
        "    }\n" +
        "}\n";
}

