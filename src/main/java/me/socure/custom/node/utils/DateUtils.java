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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * The type Date utils.
 */
public class DateUtils {
    private static DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Is valid dob boolean.
     *
     * @param dob the dob
     * @return the boolean
     */
    public static boolean isValidDob(String dob) {
        if (isValidFormat(dob) && !isFutureDate(dob)) {
            return true;
        }
        return false;
    }

    /**
     * Is valid format boolean.
     *
     * @param dob the dob
     * @return the boolean
     */
    public static boolean isValidFormat(String dob) {
        try {
            LocalDate.parse(dob, dateFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Is valid dob boolean.
     *
     * @param dob the dob
     * @return the boolean
     */
    public static boolean isFutureDate(String dob) {
        try {
            LocalDate dobDate = LocalDate.parse(dob, dateFormatter);
            return dobDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

