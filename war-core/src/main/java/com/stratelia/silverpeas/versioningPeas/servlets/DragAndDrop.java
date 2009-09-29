package com.stratelia.silverpeas.versioningPeas.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.silverpeas.versioning.importExport.VersionsType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class DragAndDrop extends HttpServlet {
  HttpSession session;
  PrintWriter out;

  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("versioningPeas", "DragAndDrop.init",
          "attachment.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
        
        ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
        boolean runOnUnix = settings.getBoolean("runOnSolaris", false);
        SilverTrace.info("importExportPeas", "DragAndDrop", "root.MSG_GEN_PARAM_VALUE", "runOnUnix = "+runOnUnix);
        
        try
        {
        	String componentId 	= req.getParameter("ComponentId");
    		SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "componentId = "+componentId);
    		String id 			= req.getParameter("Id");
    		SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "id = "+id);
    		int userId 		= Integer.parseInt(req.getParameter("UserId"));
    		SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "userId = "+userId);
    		int 	versionType 		= Integer.parseInt(req.getParameter("Type"));
    		String indexIt		= req.getParameter("IndexIt");
    		boolean bIndexIt = false;
    		if ("1".equals(indexIt))
	    		bIndexIt = true;
    		
    		String documentId = req.getParameter("DocumentId");

        	List<FileItem> items = FileUploadUtil.parseRequest(req);
    		
    		VersioningImportExport vie = new VersioningImportExport();
    		int majorNumber = 0;
			int minorNumber = 0;
    		
    		String	fullFileName = null;
    		for (int i=0; i<items.size(); i++)
    		{
    			FileItem item = items.get(i);
    			SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item #"+i+" = "+item.getFieldName());
    			SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item #"+i+" = "+item.getName());
    			
    			if (!item.isFormField())
    			{
    				fullFileName = item.getName();
    				if (fullFileName != null && runOnUnix)
        			{
    					fullFileName = fullFileName.replace('\\', File.separatorChar);
        				SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "fullFileName on Unix = "+fullFileName);
        			}
    				  				
    				String fileName = fullFileName.substring(fullFileName.lastIndexOf(File.separator)+1, fullFileName.length());
        			SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "file = "+fileName);
        			
        			long size = item.getSize();
    				SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item #"+i+" size = "+size);
        			
        			String type 		= FileRepositoryManager.getFileExtension(fileName);
        			String mimeType 	= AttachmentController.getMimeType(fileName);
					String physicalName = new Long(new Date().getTime()).toString() + "." +type;
					
        			item.write(new File(vie.getVersioningPath(componentId)+physicalName));
        			
        			DocumentPK documentPK = new DocumentPK(-1, componentId);
        			if (StringUtil.isDefined(documentId))
        			{
        				documentPK.setId(documentId);
        			}
        			
        			DocumentVersionPK versionPK = new DocumentVersionPK(-1, documentPK);
        			ForeignPK foreignPK = new ForeignPK(id, componentId);
        			
        			Document document = new Document(documentPK, foreignPK, fileName, null, Document.STATUS_CHECKINED, userId, null, null, componentId, null, null, 0, 0);
					
        			DocumentVersion version = new DocumentVersion(versionPK, documentPK, majorNumber, minorNumber, userId, new Date(), null, versionType, DocumentVersion.STATUS_VALIDATION_NOT_REQ, physicalName, fileName, mimeType, new Long(size).intValue(), componentId);
        			
        			List<DocumentVersion> versions = new ArrayList<DocumentVersion>();
        			versions.add(version);
        			VersionsType versionsType = new VersionsType();
        			versionsType.setListVersions(versions);
        			document.setVersionsType(versionsType);
        			
        			List<Document> documents = new ArrayList<Document>();
        			documents.add(document);
        			
    				vie.importDocuments(foreignPK, documents, userId, bIndexIt);
    			}
    		}
        }
        catch (Exception e)
        {
			SilverTrace.error("versioningPeas", "DragAndDrop.doPost", "ERREUR", e);
        }
    }
}