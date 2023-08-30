package me.socure.custom.node.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * The type Claim holder attributes.
 */
public class AttributesMapping {

    /**
     * Attribute map map.
     *
     * @return the map
     */
    public static Map<String, BiConsumer<SocureIDPlusRequestVO, String>> attributeMap() {
        Map<String, BiConsumer<SocureIDPlusRequestVO, String>> attribute = new HashMap<>();

        attribute.putAll(
            Map.of("firstName", SocureIDPlusRequestVO::setFirstName,
                "lastName", SocureIDPlusRequestVO::setSurName,
                "streetAddress", SocureIDPlusRequestVO::setPhysicalAddress,
                "dob", SocureIDPlusRequestVO::setDob,
                "state", SocureIDPlusRequestVO::setState,
                "mobilePhone", SocureIDPlusRequestVO::setMobileNumber,
                "city", SocureIDPlusRequestVO::setCity,
                "email", SocureIDPlusRequestVO::setEmail,
                "ssn", SocureIDPlusRequestVO::setNationalId,
                "zipCode", SocureIDPlusRequestVO::setZip));
        attribute.put("ipAddress", SocureIDPlusRequestVO::setIpAddress);
        attribute.put("countryCode", SocureIDPlusRequestVO::setCountry);
        return attribute;
    }
}
