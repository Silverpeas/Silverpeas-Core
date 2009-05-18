package com.stratelia.silverpeas.util;

/**
 * Title: userPanelPeas
 * Description: this is an object pair of pair object 
 * Copyright:    Copyright (c) 2002
 * Company:      Silverpeas
 * @author J-C Groccia
 * @version 1.0
 */


public class  PairObject
{
	private Object first = null;
	private Object second = null;


	/**
	 * constructor
	 */
	 public PairObject(Object first, Object second){
		this.first = first;
		this.second = second;
	 }

	 public Object getFirst(){
		return first;
	}
	public Object getSecond(){
		return second;
	}
}
