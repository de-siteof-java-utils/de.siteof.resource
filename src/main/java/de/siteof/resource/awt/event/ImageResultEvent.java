package de.siteof.resource.awt.event;

import java.util.EventObject;

import de.siteof.resource.awt.IImageResult;

public class ImageResultEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int FAILED	= -1;
	public static final int LOADED	= 1;

	private int eventId;
	private IImageResult imageResult;

	public ImageResultEvent(Object source, int eventId, IImageResult imageResult) {
		super(source);
		this.eventId	= eventId;
		this.imageResult	= imageResult;
	}

	public int getEventId() {
		return eventId;
	}

	public IImageResult getImageResult() {
		return imageResult;
	}

}
