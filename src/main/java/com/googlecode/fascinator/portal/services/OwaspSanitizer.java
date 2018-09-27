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

import com.googlecode.fascinator.common.JsonSimple;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.common.StorageDataUtil;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.owasp.esapi.ESAPI;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Matt Mulholland
 * @date 29/3/17
 */
public class OwaspSanitizer {
    private static final StorageDataUtil storageDataUtil = new StorageDataUtil();

    private static final Logger LOG = LoggerFactory.getLogger(OwaspSanitizer.class);
    public static final JSONArray whitelist = getWhitelist();


    private static JSONArray getWhitelist() {
        try {
            JsonSimpleConfig jsonSimpleConfig = getConfig();
            if (jsonSimpleConfig != null) {
                Object whitelist = jsonSimpleConfig.getPath("owasp", "whitelist");
                if (whitelist instanceof JSONArray) {
                    return (JSONArray)whitelist;
                } else {
                    LOG.info("No owasp whitelist was found in system config");
                }
            } else {
                LOG.warn("Unable to load system config. Returning an empty list...");
            }
        } catch (Exception e) {
            LOG.error("There was a problem with getting the owasp whitelist. Returning an empty list...", e);
        }
        return new JSONArray();
    }

    private static JsonSimpleConfig getConfig() {
        JsonSimpleConfig jsonSimpleConfig = null;
        try {
            jsonSimpleConfig = new JsonSimpleConfig();
        } catch (IOException e) {
            LOG.warn("Problem with configuration loading.");
        }
        return jsonSimpleConfig;
    }

    public static void sanitizeTfPackage(JsonSimple tfpackage) {
        sanitizeTfPackageNumberedFieldAndShadow(tfpackage, "dc:description", "text", "shadow");
        LOG.debug("tfpackage after sanitized is: {}", tfpackage);
    }

    public static void sanitizeTfPackageField(JsonSimple tfpackage, String baseKey) {
        LOG.debug("field to sanitize: {}", baseKey);
        String text = tfpackage.getString("", baseKey);
        String sanitized = sanitizeHtml(baseKey, text);
        updateTfPackageSanitizedKeyValue(tfpackage, sanitized, baseKey, text);
    }

    /**
     * tfpackage numbered description text also has escaped value: shadow that needs to be handled
     */
    public static void sanitizeTfPackageNumberedFieldAndShadow(JsonSimple tfpackage, String baseKey, String suffixKey, String suffixShadowKey) {
        Map<String, Object> map = storageDataUtil.getList(tfpackage, baseKey);
        LOG.debug("numbered fields to sanitize: {}", map);
        for (Map.Entry<String, Object> entrySet : map.entrySet()) {
            String number = entrySet.getKey();
            Map<String, Object> field = (Map) entrySet.getValue();
            LOG.debug("processing: {}", number);
            String text = (String) field.get(suffixKey);
            String sanitized = sanitizeHtml(baseKey, text);
            String sanitizedShadow = escapeHtml(sanitized);
            String keyToUpdate = baseKey + "." + number + "." + suffixKey;
            String shadowKeyToUpdate = baseKey + "." + number + "." + suffixShadowKey;
            updateTfPackageSanitizedKeyValue(tfpackage, sanitized, keyToUpdate, text);
            updateTfPackageSanitizedKeyValue(tfpackage, sanitizedShadow, shadowKeyToUpdate, text);
        }
    }

    public static void sanitizeTfPackageNumberedField(JsonSimple tfpackage, String baseKey, String suffixKey) {
        Map<String, Object> map = storageDataUtil.getList(tfpackage, baseKey);
        LOG.debug("numbered fields to sanitize: {}", map);
        for (Map.Entry<String, Object> entrySet : map.entrySet()) {
            String number = entrySet.getKey();
            Map<String, Object> field = (Map) entrySet.getValue();
            LOG.debug("processing: {}", number);
            String text = (String) field.get(suffixKey);
            String sanitized = OwaspSanitizer.sanitizeHtml(baseKey, text);
            String keyToUpdate = baseKey + "." + number + "." + suffixKey;
            updateTfPackageSanitizedKeyValue(tfpackage, sanitized, keyToUpdate, text);
        }
    }

    public static void sanitizeMapNumberedField(Map<String, Object> map, String subKey) {
        LOG.debug("pre-sanitised map is: {}", map);
        for (Map.Entry<String, Object> entrySet : map.entrySet()) {
            String number = entrySet.getKey();
            Map<String, Object> field = (Map) entrySet.getValue();
            LOG.debug("processing outer map key: {}", number);
            String text = (String) field.get(subKey);
            String sanitized = sanitizeHtml(subKey, text);
            field.put(subKey, sanitized);
        }
        LOG.debug("post-sanitized map is: {}", map);
    }

    public static String escapeHtml(String value) {
        LOG.debug("incoming value before escaped: {}", value);
        String escaped = ESAPI.encoder().encodeForHTML(StringUtils.defaultIfEmpty(value, ""));
        LOG.debug("outgoing value after escaped: {}", escaped);
        return escaped;
    }

    private static void updateTfPackageSanitizedKeyValue(JsonSimple tfpackage, String sanitized, String keyToUpdate, String text) {
        if (!text.equals(sanitized)) {
            LOG.debug("updating key: {} to post-sanitized value: {}", keyToUpdate, sanitized);
            tfpackage.getJsonObject().put(keyToUpdate, sanitized);
        }
    }

    /**
     * a basic sanitizer with default whitelist sanitizers
     */
    public static String sanitizeHtml(String field, String value) {
        if (whitelist.contains(field)) {
            LOG.warn("Field {} present in owasp whitelist. {} will not be sanitized.", field, value);
            return value;
        } else {
            return sanitizeCustomHtml(StringUtils.defaultIfEmpty(value, ""), getDefaultPolicy());
        }
    }

    public static String sanitizeCustomHtml(String value, PolicyFactory sanitizer) {
        LOG.debug("pre-sanitized: {}", value);
        String sanititized = sanitizer.sanitize(StringUtils.defaultIfEmpty(value, ""));
        if (!value.equals(sanititized)) {
            LOG.info("post-sanitized: {}", sanititized);
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
