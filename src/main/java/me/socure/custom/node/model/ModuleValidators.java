////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.model;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.identity.sm.ServiceAttributeValidator;

/**
 * Validate the configuration of ID+ and Docv
 */
public class ModuleValidators implements ServiceAttributeValidator {

    private final Logger logger = LoggerFactory.getLogger(ModuleValidators.class);
    private final Set<String> modules = Set.of("emailrisk", "phonerisk", "fraud", "addressrisk"
        , "synthetic", "decision", "kyc");

    @Override
    public boolean validate(Set<String> set) {
        if (set.isEmpty()) return true;
        logger.info("Modules configured {}", set);
        for (String s : set) if (!modules.contains(s.toLowerCase())) return false;
        return true;
    }

    @Override
    public boolean validate(Set<String> values, String orgName) {
        return ServiceAttributeValidator.super.validate(values, orgName);
    }
}

