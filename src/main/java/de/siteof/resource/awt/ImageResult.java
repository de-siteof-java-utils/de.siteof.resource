package de.siteof.resource.awt;

import java.awt.Image;

import de.siteof.task.ITask;

public class ImageResult implements IImageResult {

	private String name;
	private Image image;
	private boolean finished;
	private boolean failed;
	private boolean loading;
	private ITask loadingTask;

	public Image getImage() {
		return image;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public ITask getLoadingTask() {
		return loadingTask;
	}

	public void setLoadingTask(ITask loadingTask) {
		this.loadingTask = loadingTask;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
