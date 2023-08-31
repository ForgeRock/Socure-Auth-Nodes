////////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2022 Socure Inc.
// All rights reserved.
////////////////////////////////////////////////////////////////////////////////

package me.socure.custom.node.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleValidatorsTest {

    @Test
    void validate() {
        ModuleValidators moduleValidators = new ModuleValidators();
        boolean result = moduleValidators.validate(Set.of("emailRisk"));
        Assertions.assertTrue(result);
    }
}