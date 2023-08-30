package me.socure.custom.node.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The type Date utils test.
 */
public class DateUtilsTest {

    /**
     * Is valid dob.
     */
    @Test
    void isValidDob() {
        Assertions.assertTrue(DateUtils.isValidDob("2000-01-01"));
        Assertions.assertTrue(DateUtils.isValidDob("2000-12-01"));
        Assertions.assertFalse(DateUtils.isValidDob("2050-12-01"));
        Assertions.assertFalse(DateUtils.isValidDob("2050-02-30"));
        Assertions.assertFalse(DateUtils.isValidDob("2050-00-30"));
    }
}