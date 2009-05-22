/*
 * Created on 15 déc. 2004
 *
 */
package com.stratelia.webactiv.calendar.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author neysseri
 *
 */
public class HolidayDetail implements Serializable {

	private Date 	holidayDate = null;
	private String 	userId 	= null;

	public HolidayDetail(Date holidayDate, String userId) {
		setDate(holidayDate);
		setUserId(userId);
	}

	/**
	 * @return
	 */
	public Date getDate() {
		return holidayDate;
	}

	/**
	 * @return
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param date
	 */
	public void setDate(Date date) {
		holidayDate = date;
	}

	/**
	 * @param string
	 */
	public void setUserId(String string) {
		userId = string;
	}
	
}
