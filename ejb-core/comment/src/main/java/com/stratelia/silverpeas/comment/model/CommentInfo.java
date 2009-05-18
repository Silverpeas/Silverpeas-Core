package com.stratelia.silverpeas.comment.model;

import java.io.Serializable;

public class CommentInfo implements Serializable{
	
	private int commentCount;
	private String componentId;
	private String elementId;
	
	public CommentInfo(int commentCount, String componentId, String elementId ) {
		this.commentCount = commentCount;
		this.componentId = componentId;
		this.elementId= elementId;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public String getComponentId() {
		return componentId;
	}

	public String getElementId() {
		return elementId;
	}
	
	

}
