package com.silverpeas.external.filesharing.servlets;

import static com.silverpeas.external.filesharing.servlets.FileSharingConstants.PARAM_KEYFILE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.silverpeas.external.filesharing.model.DownloadDetail;
import com.silverpeas.external.filesharing.model.FileSharingFactory;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.web.servlet.RestRequest;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class GetLinkFileServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3386956747183311695L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		reply(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		reply(req, resp);
	}

	protected void reply(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		RestRequest rest = new RestRequest(request, "myFile");
		String keyFile = rest.getElementValue(PARAM_KEYFILE);
		TicketDetail ticket = FileSharingFactory.getFileSharing().getTicket(
				keyFile);
		if (ticket.isValid()) {
			// recherche des infos sur le fichier...
			String filePath = null;
			String fileType = null;
			String fileName = null;
			long fileSize = 0;
			if (!ticket.isVersioning())
			{
				AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(""	+ ticket.getFileId()));
				filePath = FileRepositoryManager.getAbsolutePath(attachment.getInstanceId()+ File.separator 
					+ FileRepositoryManager.getRelativePath(FileRepositoryManager.getAttachmentContext(attachment.getContext())))
					+ attachment.getPhysicalName();
				fileType = attachment.getType();
				fileName = attachment.getLogicalName();
				fileSize = attachment.getSize();
			}
			else
			{
				DocumentVersion version = new VersioningUtil().getLastPublicVersion(new DocumentPK(ticket.getFileId(), ticket.getComponentId()));
				filePath = FileRepositoryManager.getAbsolutePath(ticket.getComponentId())+ File.separator 
						+ "Versioning" + File.separator
						+ version.getPhysicalName();
				fileType = version.getMimeType();
				fileName = version.getLogicalName();
				fileSize = version.getSize();
			}
			File realFile = new File(filePath);
			BufferedInputStream input = null;
			OutputStream out = response.getOutputStream();
			try {
				response.setContentType(fileType);
				response.setHeader("Content-Disposition","inline; filename=\""+fileName+"\"");
				response.setContentLength(new Long(fileSize).intValue());
				input = new BufferedInputStream(FileUtils
						.openInputStream(realFile));
				IOUtils.copy(input, out);
				DownloadDetail download = new DownloadDetail(keyFile, new Date(), request.getRemoteAddr());
				FileSharingFactory.getFileSharing().addDownload(download);
				return;
			} catch(Exception ex){
				ex.printStackTrace();
			}finally {
				if (input != null) {
					IOUtils.closeQuietly(input);
				}
				IOUtils.closeQuietly(out);
			}
		}
		getServletContext().getRequestDispatcher("/fileSharing/jsp/invalidTicket.jsp")
				.forward(request, response);
	}

}