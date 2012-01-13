package de.siteof.resource;

import java.io.IOException;

import de.siteof.task.ITaskManager;

public class SpeedLimitResourceLoader extends AbstractResourceLoader {

	private final IResourceLoader resourceLoader;
	private int unitLength;
	private long minReadDuration;

	public SpeedLimitResourceLoader(IResourceLoader resourceLoader, int unitLength, long minReadDuration,
			ITaskManager taskManager) {
		super(resourceLoader, taskManager);
		this.resourceLoader	= resourceLoader;
		this.unitLength		= unitLength;
		this.minReadDuration	= minReadDuration;
	}

	public boolean isSpeedLimit(String name) {
		return true;
	}

	public IResource getResource(String name) throws IOException {
		IResource resource	= resourceLoader.getResource(name);
		if ((resource != null) && (this.isSpeedLimit(name))) {
			resource	= new SpeedLimitResource(resource, unitLength, minReadDuration, this.getTaskManager());
		}
		return resource;
	}

}
