/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

public class NotificationServer
{
	//private String	  m_JmsFactory = NotificationServerConstant.JMS_FACTORY;
	//private String	  m_JmsQueue = NotificationServerConstant.JMS_QUEUE;

	private String	  m_JmsFactory			= JNDINames.JMS_FACTORY;
	private String	  m_JmsQueue			= JNDINames.JMS_QUEUE;
	private String	  m_JmsHeaderChannel	= JNDINames.JMS_HEADER_CHANNEL;
	private Hashtable m_JmsHeaders;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public NotificationServer()
	{
		m_JmsHeaders = new Hashtable();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////  getNotificationPendingCount

	/**
	 * Description of the method. Method name should begin with a lower case.
	 * 
	 * @author Name (Name of the method's creator) facultatif
	 * @see ClassName (Link to a related class name) facultatif
	 * @see ClassName#member (Link to a related class member) facultatif
	 * @see functionName (Link to a related function)facultatif
	 * @version Text (Information version) facultatif
	 * 
	 * @param pValue Description (parameter name should be prefixed by "p").
	 * @return Description
	 * @exception MyException* Description
	 */
	public long addNotification(NotificationData pData) throws NotificationServerException
	{
		long   notificationid = 0;  // a gérer plus tard (nécessite une database)
		String notificationAsXML;

		m_JmsHeaders.clear();
		m_JmsHeaders.put(m_JmsHeaderChannel, pData.getTargetChannel());

		pData.setNotificationId(notificationid);

		notificationAsXML = NotificationServerUtil.convertNotificationDataToXML(pData);
		try
		{
			jmsSendToQueue(notificationAsXML, m_JmsFactory, m_JmsQueue, m_JmsHeaders);
		}
		catch (Exception e)
		{
			throw new NotificationServerException("NotificationServer.addNotification()", SilverpeasException.ERROR, "notificationServer.EX_CANT_SEND_TO_JSM_QUEUE", notificationAsXML, e);
		}
		return notificationid;
	}

	/**
	 * Send the NotificationMessage in a JMS Queue
	 * 
	 */
	private static void jmsSendToQueue(String notificationMessage, String jmsFactory, String jmsQueue, Hashtable p_JmsHeaders) throws JMSException, NamingException, IOException
	{
		QueueConnectionFactory qconFactory;
		QueueConnection		   qcon;
		QueueSession		   qsession;
		QueueSender			   qsender;
		Queue				   queue;

		InitialContext		   ic = new InitialContext();

		qconFactory = (QueueConnectionFactory) ic.lookup(jmsFactory);
		qcon = qconFactory.createQueueConnection();
		qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ic.lookup(jmsQueue);
		qsender = qsession.createSender(queue);

		TextMessage textMsg;

		textMsg = qsession.createTextMessage();
		qcon.start();

		// Add notificationMessage as message
		textMsg.setText(notificationMessage);
		// Add property
		for (Enumeration e = p_JmsHeaders.keys(); e.hasMoreElements(); )
		{
			Object key = e.nextElement();

			textMsg.setStringProperty((String) key, (String) p_JmsHeaders.get((String) key));
		}

		qsender.send(textMsg);

		qsender.close();
		qsession.close();
		qcon.close();
	}

}
