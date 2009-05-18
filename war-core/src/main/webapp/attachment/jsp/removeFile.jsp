<!--Définition des informations globales de la page --->
<%@ page language="java" %>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.util.Collection, java.util.ArrayList, java.util.Iterator, java.util.Date"%>
<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.WAPrimaryKey"%>

<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentPK"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetail"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentException"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>


<%  
        String 		componentId 		= request.getParameter("ComponentId");
        String 		id 					= request.getParameter("Id");
        String 		url 				= request.getParameter("Url");
        String 		idAttachment 		= request.getParameter("IdAttachment");
        String[]	languagesToRemove	= request.getParameterValues("languagesToDelete");
        String		sIndexIt			= request.getParameter("IndexIt");
        
        boolean indexIt = true;
        if ("1".equals(sIndexIt))
  		  indexIt = true;
  	  	else if ("0".equals(sIndexIt))
  		  indexIt = false;
        
        out.print("<BR>idAttachment="+idAttachment);
        
        //create AttachmentPK with id and componentId
        AttachmentPK atPK = new AttachmentPK(idAttachment, componentId);
        
        if (languagesToRemove != null && languagesToRemove.length > 0)
        {
        	if (languagesToRemove[0].equals("all"))
        	{
        		//suppresion de l'objet
    	        AttachmentController.deleteAttachment(atPK);
        	}
        	else
        	{
	        	AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(atPK);
	        	if (languagesToRemove.length >= attachment.getTranslations().size())
	        	{
	        		//suppresion de l'objet
	    	        AttachmentController.deleteAttachment(atPK);
	        	}
	        	else
	        	{
		        	for (int i=0; i<languagesToRemove.length; i++)
		        	{
		        		attachment.setRemoveTranslation(true);
		        		
		        		//search translation id according to language
		        		AttachmentDetailI18N translation = (AttachmentDetailI18N) attachment.getTranslation(languagesToRemove[i]);
		        		attachment.setLanguage(languagesToRemove[i]);
		        		attachment.setTranslationId(Integer.toString(translation.getId()));
		        		
		        		AttachmentController.updateAttachment(attachment, indexIt);
		        	}
	        	}
        	}
        }
        else
        {
	        //suppresion de l'objet
	        AttachmentController.deleteAttachment(atPK);
        }
        
        if (url.indexOf('?') >= 0)
        {
            response.sendRedirect(URLManager.getApplicationURL()+ url + "&Id="+id);
        }
        else
        {
            response.sendRedirect(URLManager.getApplicationURL()+ url + "?Id="+id);
        }
%>