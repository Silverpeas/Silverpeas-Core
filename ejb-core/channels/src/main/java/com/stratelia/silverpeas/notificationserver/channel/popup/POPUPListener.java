package com.stratelia.silverpeas.notificationserver.channel.popup;

import java.util.*;

import javax.jms.*;
import com.stratelia.silverpeas.notificationserver.*;
import com.stratelia.silverpeas.notificationserver.channel.*;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.silverpeas.silvertrace.*;

/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2001
 * Société :
 * @author eDurand
 * @version 1.0
 */

public class POPUPListener extends AbstractListener
{
  public POPUPListener()
  {
  }
  public void ejbCreate () { }

  /**
   * listener of NotificationServer JMS message
   */
  public void onMessage(
    Message msg )
  {
    SilverTrace.info( "popup", "POPUPListener.onMessage()", "root.MSG_GEN_PARAM_VALUE", "JMS message = "+msg);
    try
    {
      processMessage( msg );
    }
    catch(NotificationServerException e)
    {
      SilverTrace.error( "popup", "POPUPListener.onMessage()", "popup.EX_CANT_PROCESS_MSG", "",e);
    }
  }

  /**
   *
   */
  public void send( NotificationData p_Message )
  throws NotificationServerException
  {
		try
		{
	 	  StringBuffer message = new StringBuffer();
	 	  if (p_Message.getTargetParam().get("SOURCE")!=null)
	 	  {
	 		message.append("Source : " + p_Message.getTargetParam().get("SOURCE") + "\n");
	 	  }
	 	  if (p_Message.getTargetParam().get("DATE")!=null)
	 	  {
	 		message.append("Date : " + DateUtil.dateToString( ( (Date) p_Message.getTargetParam().get("DATE") ), "") + "\n");
	 	  }
	 	  message.append ( p_Message.getMessage() );
	 	 SilverMessageFactory.push ( p_Message.getTargetReceipt(), p_Message);
	 	  
		}
		catch (Exception e)
		{
			throw new NotificationServerException("POPUPListener.send()", SilverpeasException.ERROR, "popup.EX_CANT_ADD_MESSAGE",e);
		}
  }

}