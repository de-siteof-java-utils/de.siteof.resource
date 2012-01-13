package de.siteof.resource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.siteof.resource.util.IOUtil;
import de.siteof.resource.util.SpeedLimitInputStream;
import de.siteof.task.ITaskManager;

public class SpeedLimitResource extends ResourceProxy {

	private int unitLength;
	private long minReadDuration;

	private static final Log log	= LogFactory.getLog(SpeedLimitResource.class);


	public SpeedLimitResource(IResource resource, int unitLength, long minReadDuration,
			ITaskManager taskManager) {
		super(resource, taskManager);
		this.unitLength		= unitLength;
		this.minReadDuration	= minReadDuration;
	}

	public InputStream getResourceAsStream() throws IOException {
		InputStream in	= super.getResourceAsStream();
		if (in != null) {
			in	= new SpeedLimitInputStream(in, unitLength, minReadDuration);
		}
		return in;
	}

	public byte[] getResourceBytes() throws IOException {
		InputStream in	= getResourceAsStream();
		if (in == null) {
			log.warn("resource not found: " + this.getName());
			return null;
		}
		try {
			log.debug("loading data from resource, name=" + this.getName());
			return IOUtil.readAllFromStream(in);
		} finally {
			in.close();
		}
	}


}
