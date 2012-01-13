package de.siteof.resource.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {


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


}
