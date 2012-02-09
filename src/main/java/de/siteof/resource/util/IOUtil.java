package de.siteof.resource.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IOUtil {

	private static final Log log = LogFactory.getLog(IOUtil.class);

	public static byte[] readAllFromStream(InputStream in) throws IOException {
		ByteArrayOutputStream out	= new ByteArrayOutputStream(in.available());
		byte[] buffer	= new byte[4096];
		while (true) {
			int readCount	= in.read(buffer);
			if (readCount <= 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
		return out.toByteArray();
	}

	public static boolean close(InputStream in) {
		boolean result = false;
		if (in != null) {
			try {
				in.close();
				result = true;
			} catch (IOException e) {
				log.warn("close failed due to " + e, e);
			}
		}
		return result;
	}

}
