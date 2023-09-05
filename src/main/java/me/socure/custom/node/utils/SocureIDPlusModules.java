////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.utils;

import me.socure.custom.node.SocureIdPlusAuth;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SocureIDPlus Modules
 */
public enum SocureIDPlusModules {
    /**
     * Emailrisk socure id plus modules.
     */
    emailrisk,
    /**
     * Phonerisk socure id plus modules.
     */
    phonerisk,
    /**
     * Fraud socure id plus modules.
     */
    fraud,
    /**
     * Addressrisk socure id plus modules.
     */
    addressrisk,
    /**
     * Synthetic socure id plus modules.
     */
    synthetic,
    /**
     * Decision socure id plus modules.
     */
    decision,
    /**
     * Kyc socure id plus modules.
     */
    kyc;
    /**
     * To list of String.
     *
     * @return the list
     */
    public static List<String> toList() {
        return Arrays.stream(values())
            .map(SocureIDPlusModules::name)
            .collect(Collectors.toList());
    }
}

