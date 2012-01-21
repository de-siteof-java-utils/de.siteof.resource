package de.siteof.resource;

import java.net.HttpCookie;
import java.util.List;

public interface ICookieManager {
	
	String[] getCookiesForName(String name);
	
	void setSessionCookie(HttpCookie cookie);

	void setSessionCookies(List<HttpCookie> cookies);

}
