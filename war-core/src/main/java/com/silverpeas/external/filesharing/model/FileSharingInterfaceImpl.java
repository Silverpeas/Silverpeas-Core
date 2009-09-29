/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.filesharing.model;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.external.filesharing.dao.TicketDAO;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

/** 
 * @author
 */
 public class FileSharingInterfaceImpl implements FileSharingInterface 
 {
	 private TicketDAO dao;
	 
	public FileSharingInterfaceImpl()
	{
		dao = new TicketDAO();
	}
	 
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#getTicketsByUser(java.lang.String)
	 */
	public List<TicketDetail> getTicketsByUser(String userId) throws RemoteException
	{
		Connection con = initCon();
		try
		{
			/*OrganizationController organizationController = new OrganizationController();
			String[] componentIds = organizationController.getAllComponentIdsRecur(null, userId, "kmelia", false, true);
			List<String> ids = new ArrayList();
			for (int c=0; c<componentIds.length; c++)
			{
				String[] profiles = organizationController.getUserProfiles(userId, componentIds[c]);
				boolean isAdmin = false;
				for (int p=0; !isAdmin && p<profiles.length; p++)
				{
					isAdmin = "admin".equalsIgnoreCase(profiles[p]);
				}
				if (isAdmin)
					ids.add(componentIds[c]);
			}
			return dao.getTicketsByComponentIds(con, ids);*/
			return dao.getTicketsByUser(con, userId);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.getTicketsByUser()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKETS_NOT_EXIST", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#getTicketsByFile(java.lang.String)
	 */
	public void deleteTicketsByFile(String fileId, boolean versioning) throws RemoteException
	{
		Connection con = initCon();
		try
		{
			dao.deleteTicketsByFile(con, fileId, versioning);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.getTicketsByFile()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKETS_NOT_EXIST", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#getTicket(java.lang.String)
	 */
	public TicketDetail getTicket(String key) throws RemoteException
	{
		Connection con = initCon();
		try
		{
			return dao.getTicket(con,key);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.getTicket()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_TICKET_NOT_EXIST", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#createTicket(com.silverpeas.external.filesharing.model.TicketDetail)
	 */
	public String createTicket(TicketDetail ticket)
	{
		Connection con = initCon();
		try
		{
			return dao.createTicket(con,ticket);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.createTicket()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_CREATION_NOT_POSSIBLE", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#addDownload(com.silverpeas.external.filesharing.model.DownloadDetail)
	 */
	public void addDownload(DownloadDetail download)
	{
		Connection con = initCon();
		try
		{
			TicketDetail ticket = dao.getTicket(con, download.getKeyFile());
			ticket.setNbAccess(ticket.getNbAccess() + 1);
			dao.addDownload(con, download);
			dao.updateTicket(con, ticket);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.addDownload()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_ADD_DOWNLOAD_NOT_POSSIBLE", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#updateTicket(com.silverpeas.external.filesharing.model.TicketDetail)
	 */
	public void updateTicket(TicketDetail ticket)
	{
		Connection con = initCon();
		try
		{			
			dao.updateTicket(con,ticket);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.updateTicket()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_UPDATE_NOT_POSSIBLE", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	 
	/* (non-Javadoc)
	 * @see com.silverpeas.external.filesharing.model.FileSharingInterface#deleteTicket(java.lang.String)
	 */
	public void deleteTicket(String key)
	{
		Connection con = initCon();
		try
		{
			dao.deleteTicket(con,key);
		}
		catch (Exception e)
		{
			throw new FileSharingRuntimeException("FileSharingInterface.deleteTicket()", 
					SilverpeasRuntimeException.ERROR, "filsSharing.MSG_DELETE_NOT_POSSIBLE", e);
		}
		finally
		{
			// fermer la connexion
			fermerCon(con);
		}
	}
	
	 
	private Connection initCon()
	{
		Connection con = null;
		// initialisation de la connexion
		try
		{
			con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
		}
		catch (UtilException e)
		{
			// traitement des exceptions
			throw new FileSharingRuntimeException("FileSharingInterface.initCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
		}
		return con;
	}
	 
	private void fermerCon(Connection con)
	{		
		try
		{
			if(con != null) {
				con.close();
			}
		}
		catch (SQLException e)
		{
			// traitement des exceptions
			throw new FileSharingRuntimeException("FileSharingInterface.fermerCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
		}
	}
}
