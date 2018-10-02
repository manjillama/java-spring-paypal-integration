package edu.manjiltamang.paypal.utils;

import javax.servlet.http.HttpServletRequest;

public class URLLocation {
	// Nothing fancy here just extracting the base url e.g. http://localhost:8080/
	public static String getBaseUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>SCHEME>>>>>>"+ scheme);
		
		String serverName = request.getServerName();
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>SERVER-NAME>>>>>>"+ serverName);
		
		int serverPort = request.getServerPort();
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>SERVER-PORT>>>>>>"+ serverPort);
		
		String contextPath = request.getContextPath();
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>CONTEXT-PATH>>>>>>"+ contextPath);
		
		StringBuffer url = new StringBuffer();
		url.append(scheme).append("://").append(serverName);
		
		if((serverPort!=80) && (serverPort!= 443)) {
			url.append(":").append(serverPort);
		}
		url.append(contextPath);
		
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>URL 1>>>>>>"+ url.toString());
		
		if(url.toString().endsWith("/")) {
			url.append("/");
		}
		
		//System.err.println(">>>>>>>>>>>>>>>>>>>>>>>FInal URL>>>>>>"+ url.toString());
		
		return url.toString();
	}

}