package de.siteof.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.util.IOUtil;

public class CookieManager implements ICookieManager {

	private static class CookieCacheEntry {
		private long lastModified;
		private String[] cookies;

		/**
		 * @return the lastModified
		 */
		public long getLastModified() {
			return lastModified;
		}
		/**
		 * @param lastModified the lastModified to set
		 */
		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}
		/**
		 * @return the cookies
		 */
		public String[] getCookies() {
			return cookies;
		}
		/**
		 * @param cookies the cookies to set
		 */
		public void setCookies(String[] cookies) {
			this.cookies = cookies;
		}
	}

	private static class UrlInfo {
		private final String domainName;
		private final String path;

		public UrlInfo(String domainName, String path) {
			this.domainName = domainName;
			this.path = path;
		}

		public String getDomainName() {
			return domainName;
		}

		public String getPath() {
			return path;
		}
	}

	private static class CookieKey {
	    private final String name;        // NAME= ... "$Name" style is reserved
	    private final String domain;      // Domain=VALUE ... domain that sees cookie
	    private final String path;        // Path=VALUE ... URLs that see the cookie

		public CookieKey(HttpCookie cookie) {
			this.name = cookie.getName();
			this.domain = cookie.getDomain();
			this.path = cookie.getPath();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((domain == null) ? 0 : domain.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CookieKey other = (CookieKey) obj;
			if (domain == null) {
				if (other.domain != null)
					return false;
			} else if (!domain.equals(other.domain))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CookieKey [name=" + name + ", domain=" + domain + ", path="
					+ path + "]";
		}
	}


	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String PROTOCOL_SEPARATOR = "://";

	private static final Log log = LogFactory.getLog(CookieManager.class);

	private final File directory;

	private final Map<String, CookieCacheEntry> cookieCacheMap = new HashMap<String, CookieCacheEntry>();

	private final Map<CookieKey, HttpCookie> sessionCookieMap = new HashMap<CookieKey, HttpCookie>();

	public CookieManager(File directory) {
		this.directory = directory;
	}

	public CookieManager() {
		this(null);
	}

	private UrlInfo parseUrl(String name) {
		UrlInfo result = null;
		if ((name != null) && (name.length() > 0)) {
			int protocolSeparator = name.indexOf(PROTOCOL_SEPARATOR);
			if (protocolSeparator >= 0) {
				int domainNameIndex = protocolSeparator + PROTOCOL_SEPARATOR.length();
				int pathSeparator = name.indexOf('/', domainNameIndex);
				String domainName;
				String path;
				if (pathSeparator >= 0) {
					domainName = name.substring(domainNameIndex, pathSeparator);
					path = name.substring(pathSeparator);
					int portSeparator = domainName.indexOf(':');
					if (portSeparator >= 0) {
						domainName = domainName.substring(0, portSeparator);
					}
				} else {
					domainName = name.substring(domainNameIndex);
					path = "/";
				}
				result = new UrlInfo(domainName, path);
			}
		}
		return result;
	}

	private Collection<HttpCookie> getSessionCookies(String domainName, String path) {
		Map<String, HttpCookie> map = new HashMap<String, HttpCookie>();
		synchronized (sessionCookieMap) {
			for (HttpCookie cookie: sessionCookieMap.values()) {
				if ((domainName.endsWith(cookie.getDomain())) && (path.startsWith(cookie.getPath()))) {
					HttpCookie otherCookie = map.get(cookie.getName());
					if ((otherCookie == null) ||
							(otherCookie.getDomain().length() < cookie.getDomain().length()) ||
							((otherCookie.getDomain().length() == cookie.getDomain().length()) &&
									(otherCookie.getPath().length() < cookie.getPath().length()))) {
						map.put(cookie.getName(), cookie);
					}
				}
			}
		}
		return map.values();
	}

	@Override
	public String[] getCookiesForName(String name) {
		String[] result = null;
		UrlInfo urlInfo = this.parseUrl(name);
		String domainName = urlInfo.getDomainName();
		if ((directory != null) && (domainName != null) && (domainName.length() > 0)) {
			File f = new File(directory, domainName + ".txt");
			while ((f != null) && (!f.exists())) {
				int index = domainName.indexOf('.');
				if (index >= 0) {
					domainName = domainName.substring(index + 1);
					f = new File(directory, domainName + ".txt");
				} else {
					f = null;
				}
			}
			if (f != null) {
				CookieCacheEntry entry;
				synchronized (cookieCacheMap) {
					entry = cookieCacheMap.get(domainName);
				}
				long fileLastModified = f.lastModified();
				if ((entry != null) && (entry.getLastModified() == fileLastModified)) {
					result = entry.getCookies();
				} else {
					try {
						InputStream in = new FileInputStream(f);
						try {
							byte[] data = IOUtil.readAllFromStream(in);
							BufferedReader r = new BufferedReader(new StringReader(new String(data)));
							List<String> cookies = new ArrayList<String>();
							while (true) {
								String line = r.readLine();
								if (line == null) {
									break;
								}
								if (line.length() > 0) {
									cookies.add(line);
								}
							}
							if (entry == null) {
								entry = new CookieCacheEntry();
							}
							entry.setCookies((String[]) cookies.toArray(EMPTY_STRING_ARRAY));
							entry.setLastModified(fileLastModified);
							synchronized (cookieCacheMap) {
								cookieCacheMap.put(domainName, entry);
							}
							result = entry.getCookies();
						} finally {
							in.close();
						}
					} catch (IOException e) {
						log.warn("Failed to load cookie file (" + f.getName() + ") - " + e, e);
					}
				}
			}
		}

		// use the original domain name / path
		Collection<HttpCookie> sessionCookies = getSessionCookies(
				urlInfo.getDomainName(),
				urlInfo.getPath());
		if (!sessionCookies.isEmpty()) {
			// TODO session cookies are currently not correctly overriding permanent cookies
			String[] sessionCookieStrings = new String[sessionCookies.size()];
			int index = 0;
			for (HttpCookie cookie: sessionCookies) {
				sessionCookieStrings[index++] = cookie.toString();
			}
			if ((result != null) && (result.length > 0)) {
				result = Arrays.copyOf(result, result.length + sessionCookieStrings.length);
				System.arraycopy(sessionCookieStrings, 0, result,
						result.length - sessionCookieStrings.length, sessionCookieStrings.length);
			} else {
				result = sessionCookieStrings;
			}
		}

		if (result == null) {
			result = EMPTY_STRING_ARRAY;
		}
		if (log.isDebugEnabled()) {
			log.debug("domainName=[" + urlInfo.getDomainName() + "], path=[" + urlInfo.getPath() + "], cookies=" +
					Arrays.asList(result));
		}
		return result;
	}


	@Override
	public void setSessionCookie(HttpCookie cookie) {
		this.setSessionCookies(Collections.singletonList(cookie));
	}

	@Override
	public void setSessionCookies(List<HttpCookie> cookies) {
		synchronized (sessionCookieMap) {
			for (HttpCookie cookie: cookies) {
				sessionCookieMap.put(new CookieKey(cookie), cookie);
			}
		}
	}

}
