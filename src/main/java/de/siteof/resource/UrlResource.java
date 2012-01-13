package de.siteof.resource;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.task.ITaskManager;

public class UrlResource extends AbstractResource {

	public static class HttpClientResponseInputStream extends FilterInputStream {

		private HttpMethod method;

		private static final Log log	= LogFactory.getLog(UrlResource.class);


		public HttpClientResponseInputStream(InputStream in, HttpMethod method) {
			super(in);
			this.method	= method;
		}

		public int read() throws IOException {
			int result	= super.read();
			return result;
		}

		public int read(byte[] buffer, int offset, int count) throws IOException {
			int result	= super.read(buffer, offset, count);
			return result;
		}

		public int read(byte[] buffer) throws IOException {
			return this.read(buffer, 0, buffer.length);
		}

		/* (non-Javadoc)
		 * @see java.io.FilterInputStream#close()
		 */
		public void close() throws IOException {
			try {
				super.close();
			} catch (Exception e) {
				log.debug("failed to close input stream - " + e, e);
			}
			try {
				method.abort();
			} catch (Exception e) {
				log.debug("failed to abort connection - " + e, e);
			}
			try {
				method.releaseConnection();
			} catch (Exception e) {
				log.debug("failed to release connection - " + e, e);
			}
		}


	}


	private static HttpClient httpClient;

	private static Object initLock	= new Object();

	private final ICookieManager cookieManager;


	private transient HttpMethod method;


	public UrlResource(String name, ICookieManager cookieManager, ITaskManager taskManager) {
		super(name, taskManager);
		this.cookieManager = cookieManager;
	}


	private static void init() {
		if (httpClient == null) {
			synchronized(initLock) {
				if (httpClient == null) {
					MultiThreadedHttpConnectionManager connectionManager =
						new MultiThreadedHttpConnectionManager();
					HttpConnectionManagerParams params = connectionManager.getParams();
					if (params == null) {
						params = new HttpConnectionManagerParams();
					}
					params.setSoTimeout(5000);
					httpClient		= new HttpClient(connectionManager);
				}
			}
		}
	}


	public static void disposeAll() {
		if (httpClient != null) {
			synchronized(initLock) {
				if (httpClient != null) {
					// see also http://jakarta.apache.org/httpcomponents/httpclient-3.x//threading.html
					MultiThreadedHttpConnectionManager connectionManager =
						(MultiThreadedHttpConnectionManager) httpClient.getHttpConnectionManager();
					if (connectionManager != null) {
						connectionManager.shutdown();
					}
					httpClient		= null;
				}
			}
		}
	}


	public boolean exists() throws IOException {
		// may need to get re-implemented
		InputStream in	= getResourceAsStream();
		if (in == null) {
			return false;
		}
		in.close();
		return true;
	}


	public InputStream getResourceAsStream() throws IOException {
		InputStream in	= null;
//		URL url	= new URL(getName());
//		URLConnection connection	= url.openConnection();
//		in	= connection.getInputStream();
		String url	= this.getName();
		HttpClient client = httpClient;
		if (httpClient == null) {
			init();
			//client.getHttpConnectionManager().getParams().setMaxTotalConnections(50);
			client	= httpClient;
		}
		String name = this.getName();
		HttpMethod method = new GetMethod(name);
		this.method = method;
		try {
			if (cookieManager != null) {
				String[] cookies = cookieManager.getCookiesForName(name);
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						method.setRequestHeader("Cookie", cookies[i]);
					}
				}
			}
//			method.setRequestHeader("Cookie", "userinfo=a%3A7%3A%7Bs%3A8%3A%22uniqueid%22%3Bs%3A32%3A%2216507a6e28b45a6d395108727d7b56ab%22%3Bs%3A10%3A%22visitcount%22%3Bi%3A1%3Bs%3A9%3A%22visittime%22%3Bi%3A1197939822%3Bs%3A8%3A%22hitcount%22%3Bi%3A7%3Bs%3A10%3A%22firstvisit%22%3Bi%3A1197939822%3Bs%3A8%3A%22username%22%3Bs%3A11%3A%22merlins-end%22%3Bs%3A9%3A%22authtoken%22%3Bs%3A32%3A%2240b05eae93059c1c64d7065bff5b02da%22%3B%7D");
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("unable to open URL=\"" + url + "\", status: " + method.getStatusLine());
			}

			in = new HttpClientResponseInputStream(method.getResponseBodyAsStream(), method);
		} finally {
			if (in == null) {
				method.releaseConnection();
			}
		}
		return in;
	}

	public long getSize() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see de.siteof.webpicturebrowser.loader.AbstractResource#abort()
	 */
	@Override
	public void abort() {
		//super.abort();
		HttpMethod method = this.method;
		if (method != null) {
			this.method = null;
			method.abort();
		}
	}

}
