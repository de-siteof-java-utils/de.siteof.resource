package de.siteof.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
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


	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String PROTOCOL_SEPARATOR = "://";

	private static final Log log	= LogFactory.getLog(CookieManager.class);

	private final File directory;

	private Map<String, CookieCacheEntry> cookieCacheMap = new HashMap<String, CookieCacheEntry>();

	public CookieManager(File directory) {
		this.directory = directory;
	}


	public String[] getCookiesForName(String name) {
		String[] result = null;
		String domainName = null;
		if ((name != null) && (name.length() > 0)) {
			int protocolSeparator = name.indexOf(PROTOCOL_SEPARATOR);
			if (protocolSeparator >= 0) {
				int domainNameIndex = protocolSeparator + PROTOCOL_SEPARATOR.length();
				int pathSeparator = name.indexOf('/', domainNameIndex);
				if (pathSeparator >= 0) {
					domainName = name.substring(domainNameIndex, pathSeparator);
				} else {
					domainName = name.substring(domainNameIndex);
				}
			}
		}
		if ((domainName != null) && (domainName.length() > 0)) {
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
				CookieCacheEntry entry = cookieCacheMap.get(domainName);
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
							cookieCacheMap.put(domainName, entry);
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
		if (result == null) {
			result = EMPTY_STRING_ARRAY;
		}
		return result;
	}

}
