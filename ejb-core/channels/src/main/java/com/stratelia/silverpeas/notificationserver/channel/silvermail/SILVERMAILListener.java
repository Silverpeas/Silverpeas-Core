/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Date;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.jms.Message;

import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.silverpeas.notificationserver.channel.AbstractListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

public class SILVERMAILListener extends AbstractListener
{

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public SILVERMAILListener() {}

	/**
	 * Method declaration
	 *
	 *
	 * @throws CreateException
	 *
	 * @see
	 */
	public void ejbCreate() {}

	/**
	 * listener of NotificationServer JMS message
	 */
	public void onMessage(Message msg)
	{
		try
		{
			SilverTrace.info("silvermail", "SILVERMAILListener.onMessage()", "root.MSG_GEN_PARAM_VALUE", "JMS Message = " + msg.toString());
			processMessage(msg);
		}
		catch (NotificationServerException e)
		{
			SilverTrace.error("silvermail", "SILVERMAILListener.onMessage()", "silvermail.EX_CANT_PROCESS_MSG", "JMS Message = " + msg.toString(),e);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param p_Message
	 *
	 * @throws NotificationServerException
	 *
	 * @see
	 */
	public void send(NotificationData p_Message) throws NotificationServerException
	{
		try
		{
			Hashtable		  keyValue = p_Message.getTargetParam();
			String			  tmpSubjectString = (String) keyValue.get("SUBJECT");  // retrieves the SUBJECT key value.
			String			  tmpSourceString = (String) keyValue.get("SOURCE");  // retrieves the SOURCE key value.
			String			  tmpUrlString = (String) keyValue.get("URL");  // retrieves the URL key value.
			Date			  tmpDate = (Date) keyValue.get("DATE");  // retrieves the DATE key value.

			SILVERMAILMessage sm = new SILVERMAILMessage();

			sm.setUserId(Integer.parseInt(p_Message.getTargetReceipt()));
			sm.setSenderName(p_Message.getSenderName());
			sm.setSubject(tmpSubjectString);
			sm.setUrl(tmpUrlString);
			sm.setSource(tmpSourceString);
			sm.setDate(tmpDate);
			sm.setBody(p_Message.getMessage());
			SILVERMAILPersistence.addMessage(sm);
		}
		catch (Exception e)
		{
			throw new NotificationServerException("SILVERMAILListener.send()", SilverpeasException.ERROR, "silvermail.EX_CANT_ADD_MESSAGE",e);
		}
	}

}
