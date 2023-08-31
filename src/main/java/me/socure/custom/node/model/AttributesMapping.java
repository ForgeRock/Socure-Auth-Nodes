package me.socure.custom.node.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import me.socure.custom.node.utils.SocureIDPlusModules;

/**
 * The type Claim holder attributes.
 */
public class AttributesMapping {

    private final Map<SocureIDPlusModules, List<IDPlusAttributes>> moduleAttribute =
        Map.of(SocureIDPlusModules.emailrisk,
            List.of(IDPlusAttributes.email),
            SocureIDPlusModules.phonerisk,
            List.of(IDPlusAttributes.mobileNumber, IDPlusAttributes.country),
            SocureIDPlusModules.addressrisk,
            List.of(IDPlusAttributes.firstName, IDPlusAttributes.surName,
                IDPlusAttributes.physicalAddress, IDPlusAttributes.country)
        );

    /**
     * Attribute map map.
     *
     * @return the map
     */
    public static Map<String, BiConsumer<SocureIDPlusRequestVO, String>> attributeMap() {
        Map<String, BiConsumer<SocureIDPlusRequestVO, String>> attribute = new HashMap<>();

        attribute.putAll(
            Map.of(IDPlusAttributes.firstName.name(), SocureIDPlusRequestVO::setFirstName,
                IDPlusAttributes.surName.name(), SocureIDPlusRequestVO::setSurName,
                IDPlusAttributes.physicalAddress.name(), SocureIDPlusRequestVO::setPhysicalAddress,
                IDPlusAttributes.dob.name(), SocureIDPlusRequestVO::setDob,
                IDPlusAttributes.state.name(), SocureIDPlusRequestVO::setState,
                IDPlusAttributes.mobileNumber.name(), SocureIDPlusRequestVO::setMobileNumber,
                IDPlusAttributes.city.name(), SocureIDPlusRequestVO::setCity,
                IDPlusAttributes.email.name(), SocureIDPlusRequestVO::setEmail,
                IDPlusAttributes.nationalId.name(), SocureIDPlusRequestVO::setNationalId,
                IDPlusAttributes.zip.name(), SocureIDPlusRequestVO::setZip));

        attribute.put(IDPlusAttributes.ipAddress.name(), SocureIDPlusRequestVO::setIpAddress);
        attribute.put(IDPlusAttributes.country.name(), SocureIDPlusRequestVO::setCountry);
        attribute.put(IDPlusAttributes.deviceSessionId.name(),
            SocureIDPlusRequestVO::setDeviceSessionId);
        return attribute;
    }

    /**
     * IDPlusAttributes
     */

    public enum IDPlusAttributes {
        firstName("givenName"),
        surName("sn"),
        physicalAddress("postalAddress"),
        physicalAddress2,
        city("city"),
        state("stateProvince"),
        zip("postalCode"),
        country("country"),
        nationalId("ssn"),
        dob("dob"),
        mobileNumber("telephoneNumber"),
        email("mail"),
        companyName,
        ipAddress,
        orderAmount,
        submissionDate,
        prevOrderCount,
        lastOrderDate,
        accountCreationDate,
        customerUserId,
        geocode,
        previousReferenceId,
        deviceSessionId;
        private final String ldapAttribute;

        IDPlusAttributes() {
            this.ldapAttribute = null;
        }

        IDPlusAttributes(String ldapAttribute) {
            this.ldapAttribute = ldapAttribute;
        }

        public static Map<String, String> getMapping() {
            Map<String, String> idPlusLdapMap = new HashMap<>();
            for (IDPlusAttributes val : values()) {
                if (null != val.ldapAttribute) idPlusLdapMap.put(val.name(), val.ldapAttribute);
            }
            return idPlusLdapMap;
        }

        public static Map<String, String> getDocvMapping() {
            Map<String, String> idPlusLdapMap = new HashMap<>();
            for (IDPlusAttributes val : values()) {
                if (null != val.ldapAttribute) idPlusLdapMap.put(val.name(), val.ldapAttribute);
            }
            return idPlusLdapMap;
        }
    }

}
