package com.silverpeas.external.filesharing.control;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import com.silverpeas.external.filesharing.model.FileSharingInterface;
import com.silverpeas.external.filesharing.model.FileSharingInterfaceImpl;
import com.silverpeas.external.filesharing.model.FileSharingRuntimeException;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class FileSharingSessionController extends AbstractComponentSessionController
{
    /**
     * Standard Session Controller Constructeur
     *
     *
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     *
     * @see
     */
	public FileSharingSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		super(mainSessionCtrl, componentContext,  "com.silverpeas.external.filesharing.multilang.fileSharingBundle", "com.silverpeas.external.filesharing.settings.fileSharingIcons");
	}
	
	public List<TicketDetail> getTicketsByUser() throws RemoteException
	{
		return getFileSharingInterface().getTicketsByUser(getUserId());
	}
	
	public String createTicket(TicketDetail ticket)
	{
		TicketDetail newTicket = ticket;
		UserDetail user = getUserDetail();
		newTicket.setCreatorId(user.getId());
		newTicket.setCreatorName(user.getDisplayedName());
		return getFileSharingInterface().createTicket(newTicket);
	}
	
	public void updateTicket(TicketDetail ticket)
	{
		TicketDetail newTicket = ticket;
		newTicket.setUpdateId(getUserId());
		newTicket.setUpdateName(getUserDetail().getDisplayedName());
		newTicket.setUpdateDate(new Date());
		getFileSharingInterface().updateTicket(newTicket);
	}
	
	public void deleteTicket(String key)
	{
		getFileSharingInterface().deleteTicket(key);
	}
	
	public TicketDetail getTicket(String key) throws RemoteException
	{
		TicketDetail ticket = getFileSharingInterface().getTicket(key);
		ticket.setCreatorName(getUserDetail(ticket.getCreatorId()).getDisplayedName());
		if (StringUtil.isDefined(ticket.getUpdateId()))
			ticket.setUpdateName(getUserDetail(ticket.getUpdateId()).getDisplayedName());
		return ticket;
	}
	
	private FileSharingInterface getFileSharingInterface()
	{
		FileSharingInterface fileSharingInterface = null;
		try
		{
			fileSharingInterface = new FileSharingInterfaceImpl();
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingSessionController.getFileSharingInterface()", SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
		return fileSharingInterface;
	}

}