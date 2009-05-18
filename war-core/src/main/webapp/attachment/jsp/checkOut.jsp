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
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentException"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	String url 			= request.getParameter("Url");
    String idAttachment = request.getParameter("IdAttachment");
    String userId		= request.getParameter("UserId");
  	String fileLanguage = request.getParameter("FileLanguage");
  	
    boolean checkOutOK = AttachmentController.checkoutFile(idAttachment, userId, fileLanguage);
    
    if (checkOutOK)
    	response.sendRedirect(URLManager.getApplicationURL()+url);
    else
    {
    	if (url.indexOf("?") == -1)
	        response.sendRedirect(URLManager.getApplicationURL()+url+"?CheckOutStatus=1");
	    else
	    	response.sendRedirect(URLManager.getApplicationURL()+url+"&CheckOutStatus=1");
    }
%>