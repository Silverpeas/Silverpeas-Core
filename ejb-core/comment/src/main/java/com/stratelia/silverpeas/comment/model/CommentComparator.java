package com.stratelia.silverpeas.comment.model;

import java.util.Comparator;
import java.util.HashMap;

public class CommentComparator implements Comparator {

	HashMap mapComment;
	
	public CommentComparator(HashMap mapComment) {
		this.mapComment = mapComment;
	}

	public int compare(Object indexFirst, Object indexSecond) {
		// TODO Auto-generated method stub
		int a,b;
		a= ((Integer) this.mapComment.get((Integer)indexFirst)).intValue();
		b= ((Integer) this.mapComment.get((Integer)indexSecond)).intValue();
		return a-b;
	}

}
