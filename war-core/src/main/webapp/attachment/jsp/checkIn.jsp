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
        String url = request.getParameter("Url");
        String idAttachment = request.getParameter("IdAttachment");
        String fileLanguage = request.getParameter("FileLanguage");
        boolean update = Boolean.valueOf(request.getParameter("update_attachment")).booleanValue();
        boolean force = Boolean.valueOf(request.getParameter("force_release")).booleanValue();
        if(!AttachmentController.checkinFile(idAttachment, false, update, force, fileLanguage)) {
          if(url.indexOf('?') > 0) {
            url = url + '&';
          }else {
            url = url + '?';
          }
          url = url + "warning=locked";
        }
        response.sendRedirect(URLManager.getApplicationURL()+url);
%>