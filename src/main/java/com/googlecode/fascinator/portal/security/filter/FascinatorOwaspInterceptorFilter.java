/* 
 * The Fascinator - Portal
 * Copyright (C) 2017 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.portal.security.filter;

import com.googlecode.fascinator.common.JsonSimpleConfig;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.filters.SecurityWrapperResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Filter designed to audit against owasp recommentations for web security - incorporating these in addition to
 * those already added in spring's web-security default headers:
 * <a href>http://docs.spring.io/spring-security/site/docs/3.2.10.RELEASE/reference/htmlsingle/html5/#default-security-headers</a>
 *
 * @author matt@redboxresearchdata.com.au
 */
public class FascinatorOwaspInterceptorFilter extends
        OncePerRequestFilter {

    private static Logger LOG = LoggerFactory.getLogger(FascinatorOwaspInterceptorFilter.class);
    private JsonSimpleConfig config = new JsonSimpleConfig();

    public FascinatorOwaspInterceptorFilter() throws IOException {
        LOG.info("starting filter for owasp...");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
//         from: https://www.owasp.org/index.php/HttpOnly
//         if errors exist then create a sanitized cookie header and continue
        checkConfigReload();
        SecurityWrapperResponse securityWrapperResponse = new SecurityWrapperResponse(response, "sanitize");
        checkHttpOnly(request, securityWrapperResponse);
        filterChain.doFilter(request, response);
    }

    private void checkConfigReload() {
        if (config.getBoolean(false, "reload")) {
            try {
                this.config = new JsonSimpleConfig();
            } catch (IOException e) {
                LOG.warn("Problem with configuration loading. Reloading disabled.");
            }
        }
    }

    private void checkHttpOnly(HttpServletRequest request, SecurityWrapperResponse securityWrapperResponse) {
        if (config.getBoolean(false, "owasp", "httponly")) {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                // ESAPI.securityConfiguration().getHttpSessionIdName() returns JSESSIONID by default configuration
                if (ESAPI.securityConfiguration().getHttpSessionIdName().equals(cookie.getName())) {
                    securityWrapperResponse.addCookie(cookie);
                }
            }
        }
    }
}
