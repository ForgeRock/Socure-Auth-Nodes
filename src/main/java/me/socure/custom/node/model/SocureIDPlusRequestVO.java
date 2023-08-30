package me.socure.custom.node.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * SocureIDPlusRequestVO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SocureIDPlusRequestVO {
    private List<String> modules;
    private String previousReferenceId;
    private String firstName;
    private String surName;
    private String fullName;
    private String email;
    private String deviceSessionId;
    private String country;
    private String physicalAddress;
    private String physicalAddress2;
    private String city;
    private String state;
    private String zip;
    private String mobileNumber;
    private String ipAddress;
    private String geocode;
    private String nationalId;
    private String companyName;
    private String userId;
    private String dob;
    private String documentUuid;
    private boolean userConsent = true;

}


