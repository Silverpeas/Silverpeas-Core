<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@page import="java.io.IOException"%>
<%@ include file="checkAttachment.jsp"%>

<script type="text/javascript" src="<%=m_Context%>/attachment/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/upload_applet.js"></script>

<script type="text/javascript" src="<%=m_Context%>/util/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/yui/container/container_core-min.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/yui/animation/animation-min.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/yui/menu/menu-min.js"></script>
<link rel="stylesheet" type="text/css" href="<%=m_Context%>/util/yui/menu/assets/menu.css"/>

<%
      //initialisation des variables
      String id = request.getParameter("Id");
      String componentId = request.getParameter("ComponentId");
      String context = request.getParameter("Context");
      boolean fromAlias = StringUtil.getBooleanValue(request.getParameter("Alias"));
      String profile = request.getParameter("Profile");
      String sIndexIt = request.getParameter("IndexIt");
      String callbackURL = request.getParameter("CallbackUrl");
      String xmlForm = m_MainSessionCtrl.getOrganizationController().getComponentParameterValue(
          componentId, "XmlFormForFiles");

      boolean useXMLForm = StringUtil.isDefined(xmlForm);
      boolean useFileSharing = (isFileSharingEnable(m_MainSessionCtrl, componentId) && "admin".
          equalsIgnoreCase(profile));
      boolean contextualMenuEnabled = ("admin".equalsIgnoreCase(profile) || "publisher".
          equalsIgnoreCase(profile) || "writer".equalsIgnoreCase(profile));
      String iconStyle = "";
      if (contextualMenuEnabled) {
        iconStyle = "style=\"cursor:move\"";
      }

      boolean webdavEditingEnable = m_MainSessionCtrl.isWebDAVEditingEnabled() && attSettings.
          getBoolean("OnlineEditingEnable", false);
      boolean dragAndDropEnable = m_MainSessionCtrl.isDragNDropEnabled() && attSettings.getBoolean(
          "DragAndDropEnable", false);

      boolean displayUniversalLinks = URLManager.displayUniversalLinks();

      String attachmentPosition = "right";
      boolean showTitle = true;
      boolean showFileSize = true;
      boolean showDownloadEstimation = true;
      boolean showInfo = true;
      boolean showIcon = true;
      boolean showMenuNotif = StringUtil.getBooleanValue(request.getParameter("ShowMenuNotif"));

      if (request.getParameter("AttachmentPosition") != null) {
        attachmentPosition = request.getParameter("AttachmentPosition");
      }
      if (request.getParameter("ShowTitle") != null) {
        showTitle = Boolean.parseBoolean(request.getParameter("ShowTitle"));
      }
      if (request.getParameter("ShowFileSize") != null) {
        showFileSize = Boolean.parseBoolean(request.getParameter("ShowFileSize"));
      }
      if (request.getParameter("ShowDownloadEstimation") != null) {
        showDownloadEstimation =
            Boolean.parseBoolean(request.getParameter("ShowDownloadEstimation"));
      }
      if (request.getParameter("ShowInfo") != null) {
        showInfo = Boolean.parseBoolean(request.getParameter("ShowInfo"));
      }
      if (request.getParameter("ShowIcon") != null) {
        showIcon = Boolean.parseBoolean(request.getParameter("ShowIcon"));
      }

      String contentLanguage = request.getParameter("Language");
      if (!StringUtil.isDefined(contentLanguage)) {
        contentLanguage = null;
      }

      boolean spinfireViewerEnable = attSettings.getBoolean("SpinfireViewerEnable", false);

      String sURI = request.getRequestURI();
      String sRequestURL = request.getRequestURL().toString();
      String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length()
          - request.getRequestURI().length());

      session.setAttribute("Silverpeas_Attachment_ObjectId", id);
      session.setAttribute("Silverpeas_Attachment_ComponentId", componentId);
      session.setAttribute("Silverpeas_Attachment_Context", context);
      session.setAttribute("Silverpeas_Attachment_Profile", profile);

      boolean indexIt = StringUtil.getBooleanValue(sIndexIt);
      session.setAttribute("Silverpeas_Attachment_IndexIt", new Boolean(indexIt));

      //Example: http://myserver
      String httpServerBase = GeneralPropertiesManager.getGeneralResourceLocator().getString(
          "httpServerBase", m_sAbsolute);

      AttachmentPK foreignKey = new AttachmentPK(id, componentId);

      Vector attachments = AttachmentController.searchAttachmentByPKAndContext(foreignKey, context);
      Iterator itAttachments = attachments.iterator();

      if (itAttachments.hasNext() || (StringUtil.isDefined(profile) && !profile.equals("user"))) {
        Board board = gef.getBoard();
        out.println(board.printBefore());

        int nbAttachmentPerLine = 3;

        if (attachmentPosition != null && "right".equals(attachmentPosition)) {
          out.println("<table class=\"attachments\">");
          out.println(
              "<tr><td class=\"header\"><img src=\"" + m_Context + "/util/icons/attachedFiles.gif\" class=\"picto\"/></td></tr>");
        } else {
          out.println("<TABLE border=\"0\">");
          out.println(
              "<tr><td align=\"center\" colspan=\"" + (2 * nbAttachmentPerLine - 1) + "\"><img src=\"" + m_Context + "/util/icons/attachedFiles.gif\"/></td></tr>");
        }

        AttachmentDetail attachmentDetail = null;
        String author = "";
        String title = "";
        String info = "";
        String url = "";
        int a = 1;
        out.println("<tr><td>");
        out.println("<ul id=\"attachmentList\">");
        while (itAttachments.hasNext()) {
          attachmentDetail = (AttachmentDetail) itAttachments.next();
          title = attachmentDetail.getTitle(contentLanguage);
          if (!StringUtil.isDefined(title) || !showTitle) {
            title = attachmentDetail.getLogicalName(contentLanguage);
          }
          info = attachmentDetail.getInfo(contentLanguage);
          if (StringUtil.isDefined(attachmentDetail.getAuthor(contentLanguage))) {
            author = "<br/><i>" + attachmentDetail.getAuthor(contentLanguage) + "</i>";
          }

          if ("bottom".equals(attachmentPosition) && a == 1) {
            out.println("<tr id=\"attachment" + attachmentDetail.getPK().getId() + "\">");
          } else if ("right".equals(attachmentPosition)) {
            out.println(
                "<li id=\"attachment_" + attachmentDetail.getPK().getId() + "\" class=\"attachmentListItem\">");
          }
          out.print("<span class=\"lineMain\">");
          if (showIcon) {
            out.println(
                "<img id=\"img_" + attachmentDetail.getPK().getId() + "\" src=\"" + attachmentDetail.
                getAttachmentIcon(contentLanguage) + "\" width=\"20\" " + iconStyle + ">");
          }

          url = m_Context +  attachmentDetail.getAttachmentURL(contentLanguage);
          if (fromAlias) {
            url = attachmentDetail.getAliasURL(contentLanguage);
          }

          out.println(
              "<a id=\"url" + attachmentDetail.getPK().getId() + "\" href=\"" + url + "\" target=\"_blank\">" + title + "</a>");

          if (displayUniversalLinks) {
            String link = URLManager.getSimpleURL(URLManager.URL_FILE, attachmentDetail.getPK().
                getId());
            String linkIcon = m_Context + "/util/icons/link.gif";
            out.print(
                " <a href=\"" + link + "\"><img src=\"" + linkIcon + "\" border=\"0\" alt=\"" + attResources.
                getString("CopyLink") + "\" title=\"" + attResources.getString("CopyLink") + "\"></a>");
          }
          out.print("</span>");

          if (contextualMenuEnabled) {
            com.silverpeas.attachment.MenuHelper.displayActions(attachmentDetail, useXMLForm,
                useFileSharing, webdavEditingEnable, userId, contentLanguage, attResources,
                httpServerBase, showMenuNotif, out);
			
            out.println("<br/>");
            if (attachmentDetail.isReadOnly()) {
              out.println("<div id=\"worker" + attachmentDetail.getPK().getId() + "\" style=\"visibility:visible\">" + attResources.
                  getString("readOnly") + " " + m_MainSessionCtrl.getOrganizationController().
                  getUserDetail(attachmentDetail.getWorkerId()).getDisplayedName() + " " + attResources.
                  getString("at") + " " + attResources.getOutputDate(attachmentDetail.
                  getReservationDate()) + "</div>");
            } else {
              out.println(
                  "<div id=\"worker" + attachmentDetail.getPK().getId() + "\" style=\"visibility:hidden\"></div>");
            }
          } else {
            out.println("<br/>");
          }
          

          out.println("<span class=\"lineSize\">");
          if (showFileSize) {
            out.print(attachmentDetail.getAttachmentFileSize(contentLanguage));
          }
          if (showFileSize && showDownloadEstimation) {
            out.print(" / ");
          }
          if (showDownloadEstimation) {
            out.print(attachmentDetail.getAttachmentDownloadEstimation(contentLanguage));
          }
          out.println(" - " + attResources.getOutputDate(attachmentDetail.getCreationDate()));
          out.println("</span>");
          if (StringUtil.isDefined(attachmentDetail.getTitle(contentLanguage)) && showTitle) {
            out.println("<br/>" + attachmentDetail.getLogicalName(contentLanguage));
          }
          if (StringUtil.isDefined(info) && showInfo) {
            out.println("<br/><i>" + EncodeHelper.javaStringToHtmlParagraphe(info) + "</i>");
          }

          if (StringUtil.isDefined(attachmentDetail.getXmlForm(contentLanguage))) {
            String xmlURL = m_Context + "/RformTemplate/jsp/View?width=400&ObjectId=" + attachmentDetail.
                getPK().getId() + "&ObjectLanguage=" + contentLanguage + "&ComponentId=" + componentId + "&ObjectType=Attachment&XMLFormName=" + URLEncoder.encode(attachmentDetail.getXmlForm(contentLanguage), "UTF-8");
%>
<br/><a rel="<%=xmlURL%>" href="#" title="<%=title%>"><%=attResources.getString("attachment.xmlForm.View")%></a>
<%
          }

          if (attachmentDetail.isSpinfireDocument(contentLanguage) && spinfireViewerEnable) {
%>

<div id="switchView" name="switchView" style="display: none">
  <a href="#" onClick="changeView3d(<%=attachmentDetail.getPK().getId()%>)"><img name="iconeView<%=attachmentDetail.getPK().getId()%>" valign="top" border="0" src="<%=URLManager.getApplicationURL()%>/util/icons/masque3D.gif"></a>
</div>
<div id="<%=attachmentDetail.getPK().getId()%>" style="display: none">
  <OBJECT classid="CLSID:A31CCCB0-46A8-11D3-A726-005004B35102"
          width="300" height="200" id="XV" >
    <PARAM NAME="ModelName" VALUE="<%=url%>">
    <PARAM NAME="BorderWidth" VALUE="1">
    <PARAM NAME="ReferenceFrame" VALUE="1">
    <PARAM NAME="ViewportActiveBorder" VALUE="FALSE">
    <PARAM NAME="DisplayMessages" VALUE="TRUE">
    <PARAM NAME="DisplayInfo" VALUE="TRUE">
    <PARAM NAME="SpinX" VALUE="0">
    <PARAM NAME="SpinY" VALUE="0">
    <PARAM NAME="SpinZ" VALUE="0">
    <PARAM NAME="AnimateTransitions" VALUE="0">
    <PARAM NAME="ZoomFit" VALUE="1">
  </OBJECT>
</div>
<br/>
<%
        }

        if ("bottom".equals(attachmentPosition) && a < nbAttachmentPerLine) {
          out.println("<td width=\"30\">&nbsp;</td>");
        } else if ("right".equals(attachmentPosition)) {
            out.println("</li>");
          }

        if ("bottom".equals(attachmentPosition) && a == nbAttachmentPerLine) {
          out.println("</tr>");
          if (itAttachments.hasNext()) {
            out.println(
                "<tr><td colspan=\"" + (2 * nbAttachmentPerLine - 1) + "\">&nbsp;</td></tr>");
          }
        }
          
        author = "";
        if (a == 3) {
          a = 1;
        } else {
          a++;
        }
      }
      out.println("</ul>");
      out.println("</td></tr>");
%>
<% if (contextualMenuEnabled && dragAndDropEnable) {
      ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
      String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
%>
<tr><td class="dragNdrop">
    <a href="javascript:showHideDragDrop('<%=httpServerBase + m_Context%>/DragAndDrop/drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&PubId=<%=id%>&IndexIt=<%=indexIt%>&Context=<%=context%>','<%=httpServerBase + m_Context%>/upload/explanationShort_<%=language%>.html','<%=attResources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_Context%>','<%=attResources.getString("GML.DragNDropExpand")%>','<%=attResources.getString("GML.DragNDropCollapse")%>')" id="dNdActionLabel">Déposer rapidement un fichier...</a>
    <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding: 0px" align="top"></div>
  </td>
</tr>
  <% }%>
  <% if (contextualMenuEnabled && !dragAndDropEnable) {%>
<tr><td class="dragNdrop"><br/><a href="javascript:AddAttachment();"><%=attResources.getString("GML.add")%>...</a></td></tr>
    <% }%>
    <%
            out.println("</table>");
            out.println(board.printAfter());
          }
    %>
<div id="attachmentModalDialog" style="display: none"/>
<% if (spinfireViewerEnable) {%>
<script type="text/javascript">
  if (navigator.appName=='Microsoft Internet Explorer')
  {
    for (i=0; i<document.getElementsByName("switchView").length; i++)
      document.getElementsByName("switchView")[i].style.display = '';
  }
  function changeView3d(objectId)
  {
    if (document.getElementById(objectId).style.display == 'none')
    {
      document.getElementById(objectId).style.display = '';
      eval("iconeView"+objectId).src = '<%=URLManager.getApplicationURL()%>/util/icons/visible3D.gif';
    }
    else
    {
      document.getElementById(objectId).style.display = 'none';
      eval("iconeView"+objectId).src = '<%=URLManager.getApplicationURL()%>/util/icons/masque3D.gif';
    }
  }
</script>
<% }%>

<script type="text/javascript">
  // Create the tooltips only on document load
  $(document).ready(function()
  {
    // Use the each() method to gain access to each elements attributes
    $('a[rel]').each(function()
    {
      $(this).qtip(
      {
        content: {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text: '<img class="throbber" src="<%=m_Context%>/util/icons/inProgress.gif" alt="Loading..." />',
          url: $(this).attr('rel'), // Use the rel attribute of each element for the url to load
          title: {
            text: '<%=attResources.getString("attachment.xmlForm.ToolTip")%> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
            button: '<%=attResources.getString("GML.close")%>' // Show a close link in the title
          }
        },
        position: {
          corner: {
            target: 'leftMiddle', // Position the tooltip above the link
            tooltip: 'rightMiddle'
          },
          adjust: {
            screen: true // Keep the tooltip on-screen at all times
          }
        },
        show: {
          when: 'click',
          solo: true // Only show one tooltip at a time
        },
        hide: 'unfocus',
        style: {
          tip: true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
          border: {
            width: 0,
            radius: 4
          },
          name: 'light', // Use the default light style
          width: 570 // Set the tooltip width
        }
      })
    });
  });

  <% if (contextualMenuEnabled) {%>

    function checkout(id, webdav, edit, download)
    {
      if (id > 0) {
        $.get('<%=m_Context%>/Attachment', {Id:id,FileLanguage:'<%=contentLanguage%>',Action:'Checkout'},
        function(data){
			if(data == 'ok') {
          		var oMenu = eval("oMenu"+id);
				oMenu.getItem(3).cfg.setProperty("disabled", false);
		        oMenu.getItem(0).cfg.setProperty("disabled", true);
		        oMenu.getItem(1).cfg.setProperty("disabled", true);
          		if (!webdav)
          		{
            		oMenu.getItem(2).cfg.setProperty("disabled", true);
          		}
		        //disable delete
				<% if (useXMLForm) {%>
					oMenu.getItem(2,1).cfg.setProperty("disabled", true);
				<% } else {%>
				    oMenu.getItem(1,1).cfg.setProperty("disabled", true);
				<% }%>
				$('#worker'+id).html("<%=attResources.getString("readOnly")%> <%=m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()%> <%=attResources.getString("at")%> <%=DateUtil.getOutputDate(new Date(), language)%>");
          		$('#worker'+id).css({'visibility':'visible'});

          		if (edit) {
					var url = "<%=httpServerBase + m_Context%>/attachment/jsp/launch.jsp?documentUrl="+eval("webDav"+id);
    				window.open(url,'_self');
    			} else if (download) {
    				var url = $('#url'+id).attr('href');
    				window.open(url);
    			}
        	} else {
        		alert("<%=attResources.getString("attachment.dialog.checkout.nok")%>");
          		window.location.href=window.location.href;
        	}
        });
      }
    }

    function checkoutAndDownload(id, webdav)
    {
      checkout(id, webdav, false, true);
    }

    function checkoutAndEdit(id)
    {
      checkout(id, true, true, false);
    }

    function checkin(id,webdav,forceRelease)
    {
      if (id > 0) {
        var webdavUpdate = 'false';
        if (webdav)
        {
          if(confirm('<%=attResources.getString("confirm.checkin.message")%>')) {
            webdavUpdate='true';
          }
        }
        if(forceRelease == 'true'){
          closeMessage();
        }
        $.get('<%=m_Context%>/Attachment', {Id:id,FileLanguage:'<%=contentLanguage%>',Action:'Checkin',update_attachment:webdavUpdate,force_release:forceRelease},
        function(data){
          data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
          if (data == "locked")
          {
            displayWarning(id);
            //TODO - voir avec nico pour afficher le menu avec force
          }
          else
          {
            if (data == "ok")
            {
              menuCheckin(id);
            }
          }
        }, "html");
      }
    }

    function menuCheckin(id)
    {
      var oMenu = eval("oMenu"+id);
      oMenu.getItem(3).cfg.setProperty("disabled", true);
      oMenu.getItem(0).cfg.setProperty("disabled", false);
      oMenu.getItem(1).cfg.setProperty("disabled", false);
      oMenu.getItem(2).cfg.setProperty("disabled", false);

      //enable delete
  <% if (useXMLForm) {%>
      oMenu.getItem(2,1).cfg.setProperty("disabled", false);
  <% } else {%>
      oMenu.getItem(1,1).cfg.setProperty("disabled", false);
  <% }%>
	
      $('#worker'+id).html("");
      $('#worker'+id).css({'visibility':'hidden'});
    }
    
     function notifyAttachment(attachmentId)
    {
		alertUsersAttachment(attachmentId); //dans publication.jsp
    }

    function AddAttachment()
    {
  <%
       String winAddHeight = "240";
       if (I18NHelper.isI18N) {
         winAddHeight = "270";
       }
  %>
      SP_openWindow("<%=m_Context%>/attachment/jsp/addAttFiles.jsp", "test", "600", "<%=winAddHeight%>","scrollbars=no, resizable, alwaysRaised");
    }

    function deleteAttachment(attachmentId)
    {
      var url = "<%=m_Context%>/attachment/jsp/suppressionDialog.jsp?IdAttachment="+attachmentId;
      $("#attachmentModalDialog").dialog("open").load(url);
    }

    function removeAttachment(attachmentId)
    {
      var sLanguages = "";
      var boxItems = document.removeForm.languagesToDelete;
      if (boxItems != null){
        //at least one checkbox exists
        var nbBox = boxItems.length;
        //alert("nbBox = "+nbBox);
        if ( (nbBox == null) && (boxItems.checked) ){
          //there's only once checkbox
          sLanguages += boxItems.value+",";
        } else{
          for (i=0;i<boxItems.length ;i++ ){
            if (boxItems[i].checked){
              sLanguages += boxItems[i].value+",";
            }
          }
        }
      }

      //alert("sLanguages = "+sLanguages);
	
      $.get('<%=m_Context%>/Attachment', { id:attachmentId,Action:'Delete',languagesToDelete:sLanguages},
      function(data){
        data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
        if (data == "attachmentRemoved")
        {
          $('#attachment_'+attachmentId).remove();
        }
        else
        {
          if (data == "translationsRemoved")
          {
            reloadIncludingPage();
          }
        }
        closeMessage();
      });
    }

    function reloadIncludingPage()
    {
  <% if (!StringUtil.isDefined(callbackURL)) {%>
      document.location.reload();
  <% } else {%>
      document.location.href = "<%=m_sAbsolute + m_Context + callbackURL%>";
  <% }%>
    }

    function updateAttachment(attachmentId)
    {
  <%
       String winHeight = "220";
       if (I18NHelper.isI18N) {
         winHeight = "240";
       }
  %>
      var url = "<%=m_Context%>/attachment/jsp/toUpdateFile.jsp?IdAttachment="+attachmentId;
      SP_openWindow(url, "test", "650", "<%=winHeight%>","scrollbars=no, resizable, alwaysRaised");
    }

  <% if (useXMLForm) {%>
    function EditXmlForm(id, lang)
    {
      SP_openWindow("<%=m_Context%>/RformTemplate/jsp/Edit?ObjectId="+id+"&ObjectLanguage="+lang+"&ComponentId=<%=componentId%>&IndexIt=<%=indexIt%>&ObjectType=Attachment&XMLFormName=<%=URLEncoder.encode(xmlForm)%>&ReloadOpener=true", "test", "600", "400","scrollbars=yes, resizable, alwaysRaised");
    }
  <% }%>

    function closeMessage() {
    	$("#attachmentModalDialog").dialog("close");
    }

    function displayWarning(attachmentId) {
    	var url = "<%=m_Context%>/attachment/jsp/warning_locked.jsp?id=" + attachmentId;
        $("#attachmentModalDialog").dialog("open").load(url);
    }  

    $(document).ready(function(){
      $("#attachmentList").sortable({opacity: 0.4, axis: 'y', cursor: 'hand', handle: 'img'});

      $("#attachmentModalDialog").dialog({
    	  autoOpen: false,
          modal: true,
          title: "<%=attResources.getString("attachment.dialog.delete")%>",
          height: 'auto',
          width: 400});
    });

    $('#attachmentList').bind('sortupdate', function(event, ui) {
      var reg=new RegExp("attachment", "g");
	
      var data = $('#attachmentList').sortable('serialize');
      data += "#";
      var tableau=data.split(reg);
      var param = "";
      for (var i=0; i<tableau.length; i++)
      {
        if (i != 0)
          param += ","
				
        param += tableau[i].substring(3, tableau[i].length-1);
      }
      sortAttachments(param);
    });

    function sortAttachments(orderedList)
    {
      //alert(orderedList);
      $.get('<%=m_Context%>/Attachment', { orderedList:orderedList,Action:'Sort'},
      function(data){
        data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
        if (data == "error")
        {
          alert("Une erreur s'est produite !");
        }
      });
    }

    function uploadCompleted(s)
    {
      reloadIncludingPage();
    }

    function ShareAttachment(id)
    {
      var url = "<%=m_Context%>/RfileSharing/jsp/NewTicket?FileId="+id+"&ComponentId=<%=componentId%>";
      SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
    }
  <% }%>
</script>
