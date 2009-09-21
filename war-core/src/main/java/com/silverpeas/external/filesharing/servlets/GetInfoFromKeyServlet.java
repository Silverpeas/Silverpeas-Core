package com.silverpeas.external.filesharing.servlets;

import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_ATTACHMENT;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_DOCUMENT;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_DOCUMENTVERSION;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_TICKET;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.PARAM_KEYFILE;
import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.ATT_KEYFILE;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.external.filesharing.model.FileSharingFactory;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

public class GetInfoFromKeyServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String keyFile = request.getParameter(PARAM_KEYFILE);
		TicketDetail ticket = FileSharingFactory.getFileSharing().getTicket(keyFile);
		request.setAttribute(ATT_TICKET, ticket);
		if(!ticket.isValid()) {
			 getServletContext().getRequestDispatcher("/fileSharing/jsp/invalidTicket.jsp").forward(request, response);
			 return;
		}
		if (!ticket.isVersioning())
			request.setAttribute(ATT_ATTACHMENT, AttachmentController.searchAttachmentByPK(new AttachmentPK("" + ticket.getFileId())));
		else
		{
			VersioningUtil versioningUtil = new VersioningUtil();
			DocumentPK documentPK = new DocumentPK(ticket.getFileId(), ticket.getComponentId());
			Document document = versioningUtil.getDocument(documentPK);
			DocumentVersion version = versioningUtil.getLastPublicVersion(documentPK);
			
			request.setAttribute(ATT_DOCUMENT, document);
			request.setAttribute(ATT_DOCUMENTVERSION, version);
		}
		request.setAttribute(ATT_KEYFILE, keyFile);
		getServletContext().getRequestDispatcher("/fileSharing/jsp/displayTicketInfo.jsp").forward(request, response);

	}	
}