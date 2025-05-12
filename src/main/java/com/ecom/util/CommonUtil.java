package com.ecom.util;

import jakarta.servlet.http.HttpServletRequest;

public class CommonUtil {

	public static Boolean sendMail() {
		return false;
	}

	public static String generateUrl(HttpServletRequest request) {
		
		String siteUrl = request.getRequestURI().toString();
		return siteUrl.replace(request.getServletPath(), "");
	}
}
