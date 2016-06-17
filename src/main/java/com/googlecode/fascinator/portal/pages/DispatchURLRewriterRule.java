package com.googlecode.fascinator.portal.pages;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

public class DispatchURLRewriterRule implements URLRewriterRule {
	
	/**
	 * Redirect all pages through our Dispatch class
	 */
	@Override
	public Request process(Request request) {
		//Grab the real HttpServletRequest via reflection to get the RequestURI
		HttpServletRequest req;
		try {
			Field privateRequestField = request.getClass().getDeclaredField("request");
			privateRequestField.setAccessible(true);
			 req = (HttpServletRequest)privateRequestField.get(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
      String ctxPath = request.getContextPath();
      String uri = req.getRequestURI();
      request.setAttribute("RequestURI",
              uri.substring(ctxPath.length() + 1));
      // forward all requests to the main dispatcher
      String path = request.getPath();
      String[] parts = path.substring(1).split("/");
      if (parts.length > 0) {
          String start = parts[0];
          if (!"assets".equals(start) && !"dispatch".equals(start)) {
              path = "/dispatch" + path;
          }
      } else {
          path = "/dispatch";
      }
      return new SimpleRequestWrapper(request, path);
	}

}
