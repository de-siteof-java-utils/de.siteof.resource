package de.siteof.resource.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpeedLimitInputStream extends FilterInputStream {

	private int unitLength;
	private long minReadDuration;

	private int readCounter;
	private long lastTimeStamp;

	private static final Log log	= LogFactory.getLog(SpeedLimitInputStream.class);


	public SpeedLimitInputStream(InputStream in, int unitLength, long minReadDuration) {
		super(in);
		this.unitLength		= unitLength;
		this.minReadDuration	= minReadDuration;
	}

	protected void sleep(long duration) {
		try {
			log.debug("sleeping: " + duration);
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			log.debug("sleep interrupted - " + e, e);
		}
	}

	protected void doOnBeforeRead(int maxCount) {
		if (lastTimeStamp == 0) {
			lastTimeStamp	= System.currentTimeMillis();
		}
	}

	protected void doOnRead(int count) {
		readCounter	+= count;
		while (readCounter >= unitLength) {
			sleep(minReadDuration);
			lastTimeStamp	= System.currentTimeMillis();
			readCounter	-= unitLength;
		}
		if (readCounter < 0) {
			readCounter	= 0;
		}
		readCounter++;
	}

	public int read() throws IOException {
		doOnBeforeRead(1);
		int result	= super.read();
		if (result >= 0) {
			doOnRead(1);
		}
		return result;
	}

	public int read(byte[] buffer, int offset, int count) throws IOException {
		doOnBeforeRead(count);
		int result	= super.read(buffer, offset, count);
		if (result > 0) {
			doOnRead(result);
		}
		return result;
	}

	public int read(byte[] buffer) throws IOException {
		return this.read(buffer, 0, buffer.length);
	}


}
