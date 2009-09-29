<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@ page import="com.stratelia.webactiv.util.ClientBrowserUtil"%>
<%@ include file="checkAttachment.jsp"%>
<%
  String sURI = request.getRequestURI();
  String sRequestURL = request.getRequestURL().toString();
  String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
      - request.getRequestURI().length());

  ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");
  ResourceLocator generalSettings = GeneralPropertiesManager
      .getGeneralResourceLocator();

  String pathInstallerJre = generalSettings.getString("pathInstallerJre");
  if (pathInstallerJre != null && !pathInstallerJre.startsWith("http")) {
    pathInstallerJre = m_sAbsolute + pathInstallerJre;
  }
  
  ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
  String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
  String maxFileSizeForApplet = maximumFileSize.substring(0, maximumFileSize.length() - 3);
	
  //Example: http://myserver
  String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);
	
  String onlineEditingFolder = settings.getString("OnlineEditingFolder",
      "C:\\\\Documents Silverpeas\\\\");
  boolean onlineEditingEnable = m_MainSessionCtrl.getPersonalization()
      .getOnlineEditingStatus()
      && settings.getBoolean("OnlineEditingEnable", false);
  boolean webdavEditingEnable = m_MainSessionCtrl.getPersonalization()
      .getWebdavEditingStatus()
      && settings.getBoolean("OnlineEditingEnable", false);
  boolean dragAndDropEnable = m_MainSessionCtrl.getPersonalization()
      .getDragAndDropStatus()
      && settings.getBoolean("DragAndDropEnable", false);

  //initialisation des variables
  String id = request.getParameter("Id");
  String componentId = request.getParameter("ComponentId");
  String context = request.getParameter("Context");
  String url = request.getParameter("Url");
  String indexIt = request.getParameter("IndexIt"); //indexIt can be 0 or 1 or notdefined (used only by kmelia actually)
  String checkOutStatus = request.getParameter("CheckOutStatus");
  String contentLanguage = request.getParameter("Language");
  String xmlForm = request.getParameter("XMLFormName");
  
  if (!StringUtil.isDefined(contentLanguage))
    contentLanguage = null;

  String profile = request.getParameter("Profile");
  if (!StringUtil.isDefined(profile))
    profile = "user";

  boolean originWysiwyg = false;
  if (request.getParameter("OriginWysiwyg") != null)
    originWysiwyg = (new Boolean(request.getParameter("OriginWysiwyg")))
        .booleanValue();

  if (!StringUtil.isDefined(indexIt))
    indexIt = "1";

  boolean openUrl = false;
  if (request.getParameter("OpenUrl") != null)
    openUrl = (new Boolean(request.getParameter("OpenUrl"))).booleanValue();
  
  String dNdVisible = request.getParameter("DNDVisible");

  //récupération des fichiers attachés à un événement
  //create foreignKey with componentId and customer id
  //use AttachmentPK to build the foreign key of customer object.
  AttachmentPK foreignKey = new AttachmentPK(id, componentId);

  Vector vectAttachment = AttachmentController
      .searchAttachmentByPKAndContext(foreignKey, context);
  Iterator itAttachment = vectAttachment.iterator();

  Window window = gef.getWindow();
  Board board = gef.getBoard();
%>
<link type="text/css" rel="stylesheet" href="<%=m_Context%>/util/styleSheets/modal-message.css">

<script type="text/javascript" src="<%=m_Context%>/util/javaScript/modalMessage/ajax-dynamic-content.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/modalMessage/modal-message.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/modalMessage/ajax.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/animation.js"></script>
<script language='Javascript'>
var activex = true;
var attachmentId_bis;
var filename_bis;

function handleError() {
	activex = false;
    window.onerror = null;
    checkinOfficeFile(attachmentId_bis, filename_bis);
}

	function openFile(fileURL, fileName, attachmentId, classicalFileURL)
	{
		if (navigator.appName == 'Microsoft Internet Explorer')
		{
			ucPass.Download(fileName, '<%=m_sAbsolute%>'+fileURL, '<%=onlineEditingFolder%>', '<%=m_sAbsolute+m_Context%>/FileUploader/upload', 'non', '<%=userId%>', attachmentId, '<%=language%>', '<%=contentLanguage%>');
			location.href="<%=m_Context+url%>";
		}
		else
		{
			SP_openWindow(classicalFileURL, "test", "600", "240", "scrollbars, resizable, alwaysRaised");
		}
	}

	function checkoutOfficeFile(attachmentId)
	{
		document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkOut.jsp";
    document.attachmentForm.IdAttachment.value = attachmentId;
		document.attachmentForm.submit();
	}

	function checkinOfficeFile(attachmentId, fileName)
	{
		attachmentId_bis = attachmentId;
        filename_bis = fileName;

		if (navigator.appName == 'Microsoft Internet Explorer' && <%=onlineEditingEnable%> && activex)
		{
			window.onerror = handleError;
			var yesno = ucPass.CheckIn(fileName, '<%=onlineEditingFolder%>', '<%=m_sAbsolute+m_Context%>/FileUploader/upload', 'non', '<%=userId%>', attachmentId, '<%=language%>', '<%=contentLanguage%>');
			window.onerror = null;
			if (yesno == 'oui')
				location.href="<%=m_Context+url%>";
			else
			{
				document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkIn.jsp";
				document.attachmentForm.IdAttachment.value = attachmentId;
				document.attachmentForm.submit();
			}
		}
		else
		{
			document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkIn.jsp";
			document.attachmentForm.IdAttachment.value = attachmentId;
			document.attachmentForm.submit();
		}
	}
  
  function checkinOpenOfficeFile(attachmentId, fileName) {
    attachmentId_bis = attachmentId;
    filename_bis = fileName;
    if(confirm('<%=messages.getString("confirm.checkin.message")%>')) {
      document.attachmentForm.update_attachment.value='true';
    }
    document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/checkIn.jsp";
    document.attachmentForm.IdAttachment.value = attachmentId;
    document.attachmentForm.submit();
  }
	function AddAttachment()
	{
		<%
			String winAddHeight = "240";
			if (I18NHelper.isI18N)
				winAddHeight = "270";
		%>
		SP_openWindow("<%=m_Context%>/attachment/jsp/addAttFiles.jsp?Id=<%=id%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&Url=<%=URLEncoder.encode(url)%>", "test", "600", "<%=winAddHeight%>","scrollbars=no, resizable, alwaysRaised");
	}

	/*function DeleteConfirmAttachment(t, id)
	{
	    if (window.confirm("<%=messages.getString("suppressionConfirmation")%> '" + t + "' ?")){
	          document.attachmentForm.IdAttachment.value = id;
	          document.attachmentForm.submit();
	    }
	}*/

	function UpAttachment(id)
	{
		document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/moveUp.jsp";
	    document.attachmentForm.IdAttachment.value = id;
	    document.attachmentForm.submit();;
	}

	function DownAttachment(id)
	{
		document.attachmentForm.action = "<%=m_Context%>/attachment/jsp/moveDown.jsp";
	    document.attachmentForm.IdAttachment.value = id;
	    document.attachmentForm.submit();;
	}

	function EditXmlForm(id, lang)
	{
		SP_openWindow("<%=m_Context%>/RformTemplate/jsp/Edit?ObjectId="+id+"&ObjectLanguage="+lang+"&ComponentId=<%=componentId%>&IndexIt=<%=indexIt%>&ObjectType=Attachment&XMLFormName=<%=URLEncoder.encode(xmlForm)%>&Url=<%=URLEncoder.encode(url)%>", "test", "600", "400","scrollbars=yes, resizable, alwaysRaised");
	}

	function updateAttachment(id)
	{
		<%
			String winHeight = "220";
			if (I18NHelper.isI18N)
				winHeight = "240";
		%>
		var url = "<%=m_Context%>/attachment/jsp/toUpdateFile.jsp?IdAttachment="+id+"&Id=<%=id%>&ComponentId=<%=componentId%>&Context=<%=context%>&IndexIt=<%=indexIt%>&Url=<%=URLEncoder.encode(url)%>";
		if (dNdVisible)
			url += "&DNDVisible=true";
		SP_openWindow(url, "test", "600", "<%=winHeight%>","scrollbars=no, resizable, alwaysRaised");
	}

	function uploadCompleted(s)
	{
		location.href="<%=m_Context%><%=url%>";
	}

	function SelectFile( fileUrl )
	{
		window.opener.SetUrl( fileUrl ) ;
		window.close() ;
	}
	
	messageObj = new DHTML_modalMessage();	// We only create one object of this class
	messageObj.setShadowOffset(5);	// Large shadow

	function closeMessage()
	{
		messageObj.close();	
	}

	function closeMessage(force)
	{
    document.attachmentForm.force_release.value=force;
		messageObj.close(); 
	}
	
	var attachmentId 	= "-1";
	var attachmentName	= "";
	
	function deleteAttachment()
	{
	    document.attachmentForm.IdAttachment.value = id;
	    document.attachmentForm.submit();
	}
	
	function DeleteConfirmAttachment(t, id, languages)
	{
		attachmentId 	= id;
		attachmentName	= t;
		
		if (languages.length > 4) //at least two translations
			messageObj.setSize(500,200);
		else
			messageObj.setSize(500,100);
		messageObj.setCssClassMessageBox(false);
		messageObj.setSource('<%=m_Context%>/attachment/jsp/suppressionDialog.jsp?ComponentId=<%=componentId%>&Id=<%=id%>&Url=<%=url%>&IdAttachment='+id+'&Name='+t+'&Languages='+languages+'&IndexIt=<%=indexIt%>');
		messageObj.setShadowDivVisible(false);	// Disable shadow for these boxes	
		messageObj.display();
	}
	
	function ShareAttachment(id)
	{
		var url = "<%=m_Context%>/RfileSharing/jsp/NewTicket?FileId="+id+"&ComponentId=<%=componentId%>";
		SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
	}
	
	function displayWarning()
	{
		messageObj.setSize(300,80);
		messageObj.setCssClassMessageBox(false);   
		messageObj.setSource('<%=m_Context%>/attachment/jsp/warning_locked.jsp?profile=<%=profile%>' );
		messageObj.setShadowDivVisible(false);  // Disable shadow for these boxes
		messageObj.display();
	}

</script>
<script type="text/javascript" src="<%=m_Context%>/attachment/jsp/javaScript/dragAndDrop.js"></script>
<CENTER>
<%
  if ("1".equals(checkOutStatus)) {
    out.println(board.printBefore());
    out
        .println("<BR><table border=\"0\" width=\"100%\"><tr><td align=\"center\"><b>");
    out.println(messages
        .getString("attachment.SameFileNameAlreadyCheckout"));
    out.println("</b></td></tr></table><BR>");
    out.println(board.printAfter());
    out.println("<BR>");
  }
%> <%
   out.println(board.printBefore());
 %>
<table border="0" width="100%">
<% if (dragAndDropEnable) { %>
  <tr>
    <td align="right">
	<a href="javascript:showHideDragDrop('<%=httpServerBase+m_Context%>/DragAndDrop/drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&PubId=<%=id%>&IndexIt=<%=indexIt%>&Context=<%=context%>','<%=httpServerBase%>/weblib/dragAnddrop/explanation_<%=language%>.html','<%=httpServerBase%>/weblib/dragAnddrop/radupload.properties','<%=pathInstallerJre%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>')" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
    <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px" align="top"></div>
    </td>
  </tr>
<% } //end if dragAndDropEnable %>
  <tr>
    <td><!--formulaire de gestion des fichiers joints -->
    <table border="0" cellspacing="3" cellpadding="0" width="100%">
      <form name="attachmentForm" action="<%=m_Context%>/attachment/jsp/removeFile.jsp" method="POST">
        <input type="hidden" name="Id" value="<%=id%>" />
        <input type="hidden" name="ComponentId" value="<%=componentId%>" />
        <input type="hidden" name="Context" value="<%=context%>" />
        <input type="hidden" name="Url" value="<%=url%>" />
        <input type="hidden" name="IndexIt" value="<%=indexIt%>" /> 
        <input type="hidden" name="IdAttachment" value="" /> 
        <input type="hidden" name="DocumentId"/> 
        <input type="hidden" name="PubId" value="<%=id%>" />
        <input type="hidden" name="UserId" value="<%=userId%>" /> 
        <input type="hidden" name="FileLanguage" value="<%=contentLanguage%>" />
        <input type="hidden" name="update_attachment" value="false" />
        <input type="hidden" name="force_release" value="false" />
      <tr>
        <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>"></td>
      </tr>
      <tr>
        <td align="center"><b><%=messages.getString("type")%></b></td>
        <td align="left"><b><%=resources.getString("GML.file")%></b></td>
        <td align="left"><b><%=messages.getString("Title")%></b></td>
        <td align="left"><b><%=resources.getString("GML.description")%></b></td>
        <td align="left"><b><%=resources.getString("GML.size")%></b></td>
        <td align="left"><b><%=resources.getString("uploadDate")%></b></td>
        <td align="center"><b><%=resources.getString("GML.operations")%></b></td>
      </tr>
      <tr>
        <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>"></td>
      </tr>
      <tr>
        <%
          String nameWritten;
          String lastDirContext = "";
          String htmlisation = "true";
          String zip = "true";
          String zipIcone = URLManager.getApplicationURL()
              + "/util/icons/fileType/gif.gif";
          boolean isFirst = true;
          AttachmentDetail attachmentDetail = null;

          while (itAttachment.hasNext()) {
            attachmentDetail = (AttachmentDetail) (itAttachment.next());
            String urlAttachment = attachmentDetail
                .getAttachmentURL(contentLanguage);
            String onlineURL = attachmentDetail.getOnlineURL(contentLanguage);
            String logicalName = attachmentDetail.getLogicalName(contentLanguage);
            String attachmentId = attachmentDetail.getPK().getId();

            //String urlAttachment_zip 	= attachmentDetail.getAttachmentURL()+"&zip="+zip+"&fileName="+attachmentDetail.getLogicalName();
        %>
        <td class="odd" align="center">
        <%
          if (attachmentDetail.isReadOnly()
                && attachmentDetail.isOfficeDocument(contentLanguage)
                && onlineEditingEnable
                && ClientBrowserUtil.isInternetExplorer(request)
                && ClientBrowserUtil.isWindows(request)
                && (userId.equals(attachmentDetail.getWorkerId()) || profile
                    .equals("admin"))) {
        %> <a id="msoffice"
          href="javaScript:openFile('<%=Encode.javaStringToJsString(onlineURL)%>', '<%=Encode.javaStringToJsString(logicalName)%>', '<%=attachmentId%>', '<%=Encode.javaStringToJsString(urlAttachment)%>');"><img
          src="<%=attachmentDetail.getAttachmentIcon(contentLanguage)%>" border="0"></a></td>
        <%
          } else if (attachmentDetail.isReadOnly()
                && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                && webdavEditingEnable
                && (userId.equals(attachmentDetail.getWorkerId()) || profile
                    .equals("admin"))) {
              String ooUrl = httpServerBase;
              pageContext.setAttribute("httpServerBase", httpServerBase+m_Context);
              pageContext.setAttribute("ooo_url", ooUrl
                  + attachmentDetail.getWebdavUrl(contentLanguage));
        %>
        <c:url var="webdavUrl" value="${pageScope.httpServerBase}/attachment/jsp/launch.jsp">
          <c:param name="documentUrl" value="${pageScope.ooo_url}" />
        </c:url>
        <a href="<c:out value="${webdavUrl}"/>" id="webdav"><img src="<%=attachmentDetail.getAttachmentIcon(contentLanguage)%>" border="0"></a>
        </td>
        <%
          } else {
        %>
        <a id="other" href="<%=urlAttachment%>" target="_blank"><img src="<%=attachmentDetail.getAttachmentIcon(contentLanguage)%>" border="0"></a>
        </td>
        <%
          }
        %>
        <td class="odd" align="left">
        <%
          if (attachmentDetail.isAttachmentOffset(lastDirContext)) {
              out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            } else if (attachmentDetail.getAttachmentGroup() == AttachmentDetail.GROUP_DIR) {
              lastDirContext = attachmentDetail.getContext();
            } else {
              lastDirContext = "";
            }
            if (originWysiwyg) {
        %> <a href="javascript:SelectFile('<%=Encode.javaStringToJsString(urlAttachment)%>');"><%=attachmentDetail.getLogicalName(contentLanguage)%></a> <%
   } else {
       if (attachmentDetail.isReadOnly()
           && attachmentDetail.isOfficeDocument(contentLanguage)
           && onlineEditingEnable
           && ClientBrowserUtil.isInternetExplorer(request)
           && ClientBrowserUtil.isWindows(request)
           && (userId.equals(attachmentDetail.getWorkerId()) || profile
               .equals("admin"))) {
 %> <a id="msoffice_name"
          href="javaScript:openFile('<%=Encode.javaStringToJsString(onlineURL)%>', '<%=Encode.javaStringToJsString(logicalName)%>', '<%=attachmentId%>', '<%=Encode.javaStringToJsString(urlAttachment)%>');"><%=logicalName%></a>
        <%
          } else if (attachmentDetail.isReadOnly()
                  && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                  && webdavEditingEnable
                  && (userId.equals(attachmentDetail.getWorkerId()) || profile
                      .equals("admin"))) {
                %>
                <a href="<c:out value="${webdavUrl}"/>" id="webdav_name"><%=logicalName%></a> <%
   } else {
 %> <a href="<%=urlAttachment%>" target="_blank"><%=logicalName%></a> <%
   }
     }
     if (attachmentDetail.isReadOnly()) {
       out.println("<br>(" + messages.getString("readOnly"));
       String displayedName = "?????";
       if (attachmentDetail.getWorkerId() != null) {
         UserDetail worker = AttachmentController
             .getUserDetail(attachmentDetail.getWorkerId());
         if (worker != null)
           displayedName = worker.getDisplayedName();
       }

       out.print(displayedName + " ");
       out.print(messages.getString("at") + " "
           + resources.getOutputDate(attachmentDetail.getReservationDate()));
       if (StringUtil.isDefined(resources.getOutputDate(attachmentDetail
           .getExpiryDate())))
         out.print(" " + messages.getString("until") + " "
             + resources.getOutputDate(attachmentDetail.getExpiryDate()));
       out.println(")");
       out.print("<br>");
     }
 %>
        </td>
        <td class="odd" align="left">
        <%
          String title = attachmentDetail.getTitle(contentLanguage);
            if (title != null && title.length() > 0)
              out.println(title);
            else
              out.println("&nbsp;");
        %>
        </td>
        <td class="odd" align="center">
        <%
          String info = attachmentDetail.getInfo(contentLanguage);
            if (info != null && info.length() > 0) {
              IconPane descriptionIP = gef.getIconPane();
              Icon descIcon = descriptionIP.addIcon();
              descIcon.setProperties(m_Context + "/util/icons/info.gif", info);
              out.println(descriptionIP.print());
            } else
              out.println("&nbsp;");
        %>
        </td>
        <td class="odd" align="left"><%=attachmentDetail.getAttachmentFileSize(contentLanguage)%></td>
        <td class="odd" align="left"><%=DateUtil.getOutputDate(attachmentDetail
                .getCreationDate(contentLanguage), language)%></td>
        <td class="odd" align="right">
        <%
          IconPane iconPane = gef.getIconPane();
            if (!attachmentDetail.isReadOnly()) {
              //Checkout allowed
              Icon checkoutIcon = iconPane.addIcon();
              checkoutIcon.setProperties(
                  m_Context + "/util/icons/checkoutFile.gif", messages
                      .getString("checkOut"),
                  "javascript:onClick=checkoutOfficeFile(" + attachmentId + ")");
            } else if (attachmentDetail.isReadOnly()
                && attachmentDetail.isOfficeDocument()
                && ClientBrowserUtil.isInternetExplorer(request)
                && ClientBrowserUtil.isWindows(request)
                && (userId.equals(attachmentDetail.getWorkerId()) ||
                    "admin".equals(profile))
                && (onlineEditingEnable || !webdavEditingEnable)) {
              //Checkin allowed
              Icon checkinIcon = iconPane.addIcon();
              checkinIcon.setProperties(m_Context + "/util/icons/checkinFile.gif",
                  messages.getString("checkIn"),
                  "javascript:onClick=checkinOfficeFile('" + attachmentId + "','"
                      + Encode.javaStringToJsString(logicalName) + "');");
            } else if (attachmentDetail.isReadOnly()
                && attachmentDetail.isOpenOfficeCompatible(contentLanguage)
                && webdavEditingEnable
                && (userId.equals(attachmentDetail.getWorkerId()) || profile
                    .equals("admin"))) {
              Icon checkinIcon = iconPane.addIcon();
              checkinIcon.setProperties(m_Context + "/util/icons/checkinFile.gif",
                  messages.getString("checkIn"),
                  "javascript:onClick=checkinOpenOfficeFile('" + attachmentId
                      + "','" + Encode.javaStringToJsString(logicalName) + "');");
            } else if (attachmentDetail.isReadOnly()
                && (userId.equals(attachmentDetail.getWorkerId()) || profile
                    .equals("admin"))) {
              Icon checkinIcon = iconPane.addIcon();
              checkinIcon.setProperties(m_Context + "/util/icons/checkinFile.gif",
                  messages.getString("checkIn"),
                  "javascript:onClick=checkinOfficeFile('" + attachmentId + "');");
            }

            Icon updateIcon = iconPane.addIcon();
            Icon deleteIcon = iconPane.addIcon();
            Icon shareIcon 	= iconPane.addIcon();
            
            String attLanguages = "";
            Iterator itAttLanguages = attachmentDetail.getLanguages();
            while (itAttLanguages.hasNext())
            {
            	attLanguages += itAttLanguages.next()+",";
            }

            if (attachmentDetail.isReadOnly()) {
              if (userId.equals(attachmentDetail.getWorkerId())) {
                updateIcon.setProperties(m_Context + "/util/icons/update.gif",
                    resources.getString("GML.modify"),
                    "javascript:onClick=updateAttachment('" + attachmentId + "');");
                deleteIcon.setProperties(ArrayPnoColorPix, "", "");
                shareIcon.setProperties(ArrayPnoColorPix, "", "");
              } else {
                updateIcon.setProperties(ArrayPnoColorPix, "", "");
                deleteIcon.setProperties(ArrayPnoColorPix, "", "");
                shareIcon.setProperties(ArrayPnoColorPix, "", "");
              }
            } else {
              updateIcon.setProperties(m_Context + "/util/icons/update.gif",
            		  resources.getString("GML.modify"),
                  "javascript:onClick=updateAttachment('" + attachmentId + "')");
              deleteIcon.setProperties(m_Context + "/util/icons/delete.gif", resources.getString("GML.delete"),
                  "javascript:onClick=DeleteConfirmAttachment('"+ Encode.javaStringToJsString(logicalName) + "','"
                      + attachmentId + "', '"+attLanguages+"')");
              
              if (isFileSharingEnable(m_MainSessionCtrl, componentId) && "admin".equalsIgnoreCase(profile))
              {
              	shareIcon.setProperties(m_Context + "/util/icons/webLink.gif", messages.getString("attachment.share"), 
            		  "javascript:onClick=ShareAttachment('"+ attachmentId +"')");
              }
              else
              {
            	  shareIcon.setProperties(ArrayPnoColorPix, "", "");
              }
            }
            
            if (StringUtil.isDefined(xmlForm))
            {
	            Icon xmlIcon = iconPane.addIcon();
	            xmlIcon.setProperties(m_Context + "/util/icons/add.gif",
	                    messages.getString("attachment.xmlForm.Edit"), "javascript:onClick=EditXmlForm('"
	                        + attachmentId + "','"+contentLanguage+"')");
            }

            Icon downIcon = iconPane.addIcon();
            if (itAttachment.hasNext())
              downIcon.setProperties(m_Context + "/util/icons/arrow/arrowDown.gif",
                  messages.getString("Down"), "javascript:onClick=DownAttachment('"+ attachmentId + "')");
            else
              downIcon.setProperties(ArrayPnoColorPix, "", "");

            Icon upIcon = iconPane.addIcon();
            if (isFirst)
              upIcon.setProperties(ArrayPnoColorPix, "", "");
            else
              upIcon.setProperties(m_Context + "/util/icons/arrow/arrowUp.gif",
                  messages.getString("Up"), "javascript:onClick=UpAttachment('"
                      + attachmentId + "')");
            out.println(iconPane.print());
            isFirst = false;
        %>
        </td>
      </tr>
      <%
        }
      %>
      </form>
      <tr>
        <td colspan="8" align="center" class="intfdcolor" height="1"><img src="<%=noColorPix%>"></td>
      </tr>
    </table>
    </td>
  </tr>
</table>
<%
  out.println(board.printAfter());
%> <br>
<%
  ButtonPane buttonPane2 = gef.getButtonPane();
  buttonPane2.addButton((Button) gef.getFormButton(resources.getString("GML.add"), "javascript:AddAttachment()", false));
  out.println(buttonPane2.print());
%>
</CENTER>
<% if (onlineEditingEnable) { %>
	<OBJECT ID="ucPass" CLASSID="CLSID:60FFD28D-9C2B-41ED-9928-05ABDA287AEC" CODEBASE="/weblib/onlineEditing/SilverpeasOnlineEdition.CAB#version=4,1,0,0"></OBJECT>
<% } %>

<%
	if ("true".equalsIgnoreCase(dNdVisible))
	{
		%>
			<script language="JavaScript">
				showHideDragDrop();
			</script>
		<%
	}
%>
<%
String warning = request.getParameter("warning");
if ("locked".equalsIgnoreCase(warning)) { %>
  <script type="text/javascript" >
  		setTimeout("displayWarning();", 500);
  </script>
<% } %>