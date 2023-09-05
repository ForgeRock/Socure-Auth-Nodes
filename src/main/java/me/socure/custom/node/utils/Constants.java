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


package me.socure.custom.node.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    private static final Logger logger = LoggerFactory.getLogger(Constants.class);
    public static final String DOCUMENT_DATA = "document_data";
    public static final String OBJECT_ATTRIBUTES = "objectAttributes";

    public static String fileContent = "    try {\n" +
        "        let loginButton = document.getElementById('loginButton_0');\n" +
        "        let submitButton = document.querySelector(\"button[type='submit']\");\n" +
        "        let newUiContainer = document.querySelector(\"#body-append-el\");\n" +
        "\n" +
        "        if (null != loginButton) loginButton.style.display = 'none';\n" +
        "        if (null != submitButton) submitButton.style.display = 'none';\n" +
        "\n" +
        "        var socure_div = document.createElement(\"div\");\n" +
        "        socure_div.setAttribute(\"id\", \"socure\");\n" +
        "\n" +
        "        var config = {\n" +
        "            onProgress: onProgress, //callback method for reading the progress status\n" +
        "            onSuccess: onSuccess, //callback method to read the success response\n" +
        "            onError: onError, //callback method to read the error response\n" +
        "            qrCodeNeeded: true //toggle the QR code display\n" +
        "        };\n" +
        "\n " +
        "   function clearSession() {\n" +
        "            try{  \n //Socure.cleanup();\n" +
        "                   // Socure.reset(); \n " +
        "            window.sessionStorage.removeItem('documentVerificationToken');\n" +
        "            window.sessionStorage.removeItem('publicApiKey');\n" +
        "            window.localStorage.removeItem('devicer_id');\n" +
        "            console.log(\"Socure DocV Session cleaned!\"); } catch(e){ console.log(e) }\n" +
        "        } \n" +
        "     function onProgress(e) { console.log(e) };\n" +
        "        function onError(e) { console.log(e) };\n" +
        "\n" +
        "        function onSuccess(e) {\n" +
        "            console.log(e);\n" +
        "            document.getElementById('docvdata').value = JSON.stringify(e);\n" +
        "            document.getElementById('device_id').value = JSON.parse(window.localStorage.getItem('devicer_id'))?.value;   \n" +
        "            if (null != loginButton) {" +
        "             // loginButton.style.display = 'inline';" +
        "             // loginButton.attributes.removeNamedItem('hidden');\n" +
        "              loginButton.click();\n " +
        "             }\n" +
        "            if (null != submitButton) submitButton.style.display = 'inline';\n" +
        "            socure_div.style.display = 'none'\n" +
        "        };\n" +
        "\n" +
        "\n" +
        "        var script = document.createElement('script');\n" +
        "        script.setAttribute(\"src\", \"%s\");\n" +
        "\n" +
        "        if (null != newUiContainer) {\n" +
        "            newUiContainer.appendChild(socure_div)\n" +
        "        } else {\n" +
        "            console.log(socure_div);\n" +
        "            document.body.appendChild(socure_div);\n" +
        "        }\n" +
        "\n" +
        "         const removeEmpty = (obj) => {\n" +
        "            Object.entries(obj).forEach(([key, val]) =>\n" +
        "                (val && typeof val === 'object') && removeEmpty(val) ||\n" +
        "                (val === null || val === \"\") && delete obj[key]\n" +
        "            );\n" +
        "            return obj;\n" +
        "        }; \n" +
        "        document.body.appendChild(script);\n" +
        "\n" +
        "       script.onload = function () {\n" +
        "       clearSession(); \n" +
        " var modifyExperience = { \n" +
        " \"sendMessage\": \"#send_message\",\n" +
        "    \"flow\": { \n" +
        "    \"name\": \"#flow\" \n" +
        " },\n" +
        "\"language\": \"#lang\",  \n" +
        "   \"redirect\": {\n" +
        "    \"method\": \"#redirect_method\",\n" +
        "    \"url\": \"#url\"  \n" +
        "},\n" +
        "    \"documentType\": \"#doctype\"\n" +
        " }; \n" +
        " removeEmpty(modifyExperience); \n" +
        "       SocureInitializer.init(\"%s\").then(lib => {\n" +
        "                lib.init(\"%s\", \"#socure\", config).then(function () {\n" +
        "                    lib.start(1,null,modifyExperience);\n" +
        "                });\n" +
        "            });\n" +
        "        };\n" +
        "    } catch (error) {\n" +
        "        console.error(error);\n" +
        "    }\n";

}

