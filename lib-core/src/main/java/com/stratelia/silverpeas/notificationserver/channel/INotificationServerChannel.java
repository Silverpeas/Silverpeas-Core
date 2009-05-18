/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver.channel;

import com.stratelia.silverpeas.notificationserver.*;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

public interface INotificationServerChannel
{

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
	void send(NotificationData p_Message) throws NotificationServerException;
}
