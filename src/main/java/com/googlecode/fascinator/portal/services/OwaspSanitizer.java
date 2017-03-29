/*
 *
 *  * The Fascinator - Portal - Security
 *  * Copyright (C) 2017. Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation; either version 2 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, write to the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package com.googlecode.fascinator.portal.services;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matt Mulholland
 * @date 29/3/17
 */
public class OwaspSanitizer {
    private static final Logger LOG = LoggerFactory.getLogger(OwaspSanitizer.class);

    /**
     * a basic sanitizer with default whitelist sanitizers
     */
    public static String sanitizeHtml(String value) {
        return sanitizeCustomHtml(value, getDefaultPolicy());
    }

    public static String sanitizeCustomHtml(String value, PolicyFactory sanitizer) {
        LOG.debug("pre-sanitized: " + value);
        String sanititized = sanitizer.sanitize(value);
        if (!value.equals(sanititized)) {
            LOG.info("post-sanitized: " + sanititized);
        }
        return sanititized;
    }

    /**
     * convenience method for building on default policy
     */
    public static PolicyFactory getDefaultPolicy() {
        return Sanitizers.FORMATTING.and
                (Sanitizers.BLOCKS).and(Sanitizers.IMAGES).and(Sanitizers.LINKS).and
                (Sanitizers.STYLES).and(Sanitizers.TABLES);
    }
}
