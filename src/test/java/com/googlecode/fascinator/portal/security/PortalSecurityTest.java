package com.googlecode.fascinator.portal.security;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

public class PortalSecurityTest {

    @Test
    public void testPattern() {
        String url1 = "/redbox/default/detail/7af2a55aae2a3cae29da4ae2122b1c58/";
        Assert.assertTrue("Wrong pattern defined.",
                isMatchto("/**/detail/*", url1));
        Assert.assertFalse("Wrong pattern defined.",
                isMatchto("/**/workflows/simpleworkflow*", url1));
    }

    private boolean isMatchto(String urlPattern, String testingUrl) {

        return new AntPathMatcher().match(urlPattern, testingUrl);
    }
}
