package com.silverpeas.external.filesharing.model;

import java.rmi.RemoteException;
import java.util.List;


public interface FileSharingInterface {

	public List<TicketDetail> getTicketsByUser(String userId) throws RemoteException;

	public void deleteTicketsByFile(String fileId, boolean versioning) throws RemoteException;

	public TicketDetail getTicket(String key) throws RemoteException;

	public String createTicket(TicketDetail ticket);

	/**
	 * mise à jour des téléchargements
	 * @param download
	 */
	public void addDownload(DownloadDetail download);

	public void updateTicket(TicketDetail ticket);

	public void deleteTicket(String key);


}

