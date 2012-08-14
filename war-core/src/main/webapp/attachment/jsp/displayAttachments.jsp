<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="org.silverpeas.attachment.AttachmentServiceFactory" %>
<%@ page import="com.silverpeas.util.ForeignPK" %>
<%@ page import="org.silverpeas.attachment.model.SimpleDocument" %>
<%@ include file="checkAttachment.jsp"%>

<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" />
<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}" />

<view:includePlugin name="qtip"/>
<view:includePlugin name="iframepost"/>
<script type="text/javascript" src='<c:url value="/attachment/jsp/javaScript/dragAndDrop.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/javaScript/upload_applet.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/yahoo-dom-event/yahoo-dom-event.js" /> '></script>
<script type="text/javascript" src='<c:url value="/util/yui/container/container_core-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/animation/animation-min.js" />' ></script>
<script type="text/javascript" src='<c:url value="/util/yui/menu/menu-min.js" />' ></script>

<link rel="stylesheet" type="text/css" href='<c:url value="/util/yui/menu/assets/menu.css" />'/>
<script>
<view:settings var="spinfireViewerEnable"  settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="SpinfireViewerEnable" />
<view:setConstant var="spinfire" constant="com.silverpeas.util.MimeTypes.SPINFIRE_MIME_TYPE" />
<view:setConstant var="mainSessionControllerAtt" constant="com.stratelia.silverpeas.peasCore.MainSessionController.MAIN_SESSION_CONTROLLER_ATT" />
<c:set var="mainSessionController" value="${sessionScope[mainSessionControllerAtt]}" />
<view:settings var="onlineEditingEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="OnlineEditingEnable" />
<view:settings var="dAndDropEnable" settings="org.silverpeas.util.attachment.Attachment" defaultValue="${false}" key="DragAndDropEnable" />
<c:set var="webdavEditingEnable" value="${mainSessionController.webDAVEditingEnabled && onlineEditingEnable}" />
<c:set var="dragAndDropEnable" value="${mainSessionController.dragNDropEnabled && dAndDropEnable}" />

<c:set var="userProfile" value="${fn:toLowerCase(param.Profile)}" scope="page"/>
<c:set var="contextualMenuEnabled" value="${'admin' eq userProfile || 'publisher' eq userProfile || 'writer' eq userProfile}" scope="page" />
<view:componentParam var="xmlForm" componentId="${param.ComponentId}" parameter="XmlFormForFiles" />
<view:componentParam var="useFileSharingParam" componentId="${param.ComponentId}" parameter="useFileSharing" />
<c:set var="useFileSharing" value="${'yes' eq fn:toLowerCase(useFileSharingParam) && 'admin' eq userProfile }" />
<c:choose>
  <c:when test="${contextualMenuEnabled}">
    <c:set var="iconStyle" scope="page" value="${'style=\"cursor:move\"'}" />
  </c:when>
  <c:otherwise>
    <c:set var="iconStyle" scope="page" value="${''}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.AttachmentPosition != null}">
    <c:set var="attachmentPosition" scope="page" value="${param.AttachmentPosition}" />
  </c:when>
  <c:otherwise>
    <c:set var="attachmentPosition" scope="page" value="${'right'}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${'right' eq attachmentPosition}">
    <c:set var="isAttachmentPositionRight" scope="page" value="${true}" />
    <c:set var="isAttachmentPositionBottom" value="${false}" />
  </c:when>
  <c:otherwise>
    <c:set var="isAttachmentPositionRight" scope="page" value="${false}" />
    <c:choose>
      <c:when test="${'bottom' eq attachmentPosition}">
        <c:set var="isAttachmentPositionBottom" value="${true}" />
      </c:when>
      <c:otherwise>
        <c:set var="isAttachmentPositionBottom" scope="page" value="${false}" />
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.ShowTitle != null}">
    <c:set var="showTitle" scope="page" value="${view:booleanValue(param.ShowTitle)}" />
  </c:when>
  <c:otherwise>
    <c:set var="showTitle" scope="page" value="${true}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.ShowFileSize != null}">
    <c:set var="showFileSize" scope="page" value="${view:booleanValue(param.ShowFileSize)}" />
  </c:when>
  <c:otherwise>
    <c:set var="showFileSize" scope="page" value="${true}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.ShowDownloadEstimation != null}">
    <c:set var="showDownloadEstimation" scope="page" value="${view:booleanValue(param.ShowDownloadEstimation)}" />
  </c:when>
  <c:otherwise>
    <c:set var="showDownloadEstimation" scope="page" value="${true}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.ShowInfo != null}">
    <c:set var="showInfo" scope="page" value="${view:booleanValue(param.ShowInfo)}" />
  </c:when>
  <c:otherwise>
    <c:set var="showInfo" scope="page" value="${true}" />
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${param.ShowIcon != null}">
    <c:set var="showIcon" scope="page" value="${view:booleanValue(param.ShowIcon)}" />
  </c:when>
  <c:otherwise>
    <c:set var="showIcon" scope="page" value="${true}" />
  </c:otherwise>
</c:choose>
<c:set var="fromAlias" value="${view:booleanValue(param.Alias)}" />
<c:set var="useXMLForm" value="${view:isDefined(xmlForm)}" />
<c:set var="indexIt" value="${view:booleanValue(param.IndexIt)}" />
<c:set var="showMenuNotif" value="${view:booleanValue(param.ShowMenuNotif)}" />
<c:set var="displayUniversalLinks"><%=URLManager.displayUniversalLinks()%></c:set>

<c:set var="Silverpeas_Attachment_ObjectId" value="${param.Id}" scope="session" />
<c:set var="Silverpeas_Attachment_ComponentId" value="${param.ComponentId}" scope="session" />
<c:set var="Silverpeas_Attachment_Context" value="${param.Context}" scope="session" />
<c:set var="Silverpeas_Attachment_Profile" value="${userProfile}" scope="session" />
<c:set var="Silverpeas_Attachment_IndexIt" value="${indexIt}" />
<c:choose>
  <c:when test="${! view:isDefined(param.Language)}">
    <c:set var="contentLanguage" value="${null}" />
  </c:when>
  <c:otherwise>
    <c:set var="contentLanguage" value="${param.Language}" />
  </c:otherwise>
</c:choose>
<c:set var="componentId" value="${param.ComponentId}" />
</script>

<%
  List<SimpleDocument> attachments = AttachmentServiceFactory.getAttachmentService().searchAttachmentsByExternalObject(
              new ForeignPK(request.getParameter("Id"), request.getParameter("ComponentId")), (String) pageContext.getAttribute("contentLanguage"));
  pageContext.setAttribute("attachments", attachments);
%>
<c:if test="${!empty pageScope.attachments  && view:isDefined(userProfile) && ('user' != userProfile)}">
  <div class="attachments bgDegradeGris">
    <div class="bgDegradeGris  header"><h4 class="clean"><fmt:message key="GML.attachments" /></h4></div>
    <ul id="attachmentList">
      <c:forEach items="${pageScope.attachments}" var="attachment" >
        <c:if test="${isAttachmentPositionRight}">
          <li id='attachment_<c:out value="${attachment.oldSilverpeasId}"/>' class='attachmentListItem' <c:out value="${iconStyle}" escapeXml="false"/> >
        </c:if>
        <c:if test="${contextualMenuEnabled}">
          <%com.silverpeas.attachment.MenuHelper.displayActions((SimpleDocument)pageContext.getAttribute("attachment"), (Boolean)pageContext.getAttribute("useXMLForm"),
                (Boolean)pageContext.getAttribute("useFileSharing"), (Boolean)pageContext.getAttribute("webdavEditingEnable"), userId, (String)pageContext.getAttribute("contentLanguage"), attResources,
                URLManager.getServerURL(request), (Boolean)pageContext.getAttribute("showMenuNotif"), (Boolean)pageContext.getAttribute("useContextualMenu"), out); %>              
        </c:if>
        <span class="lineMain">
          <c:if test="${contextualMenuEnabled && !pageScope.useContextualMenu}">
            <img id='edit_<c:out value="${attachment.oldSilverpeasId}"/>' src='<c:url value="/util/icons/arrow/menuAttachment.gif" />' class="moreActions"/>
          </c:if>
          <c:if test="${showIcon}">
            <img id='img_<c:out value="${attachment.oldSilverpeasId}"/>' src='<c:out value="${attachment.displayIcon}" />' class="icon" />
          </c:if>
          <c:choose>
            <c:when test="${fromAlias}">
              <c:set var="attachmentUrl" value="${attachment.aliasURL}" />
            </c:when>
            <c:otherwise>
              <c:url var="attachmentUrl" value="${attachment.attachmentURL}" />
            </c:otherwise>
          </c:choose>
          <c:choose>
            <c:when test="${! view:isDefined(attachment.title) || ! showTitle}">
              <c:set var="title" value="${attachment.filename}" />
            </c:when>
            <c:otherwise>
              <c:set var="title" value="${attachment.title}" />
            </c:otherwise>
          </c:choose>
          <a id='url_<c:out value="${attachment.oldSilverpeasId}"/>' href='<c:out value="${attachmentUrl}" escapeXml="false"/>' target="_blank"><c:out value="${title}" /></a>
        </span>
        <span class="lineSize">
          <c:if test="${displayUniversalLinks}">
            <a href='<c:out value="${attachment.universalURL}" escapeXml="false" />'><img src='<c:url value="/util/icons/link.gif"/>' border="0" alt="<fmt:message key="CopyLink"/>" title="<fmt:message key="CopyLink"/>" /></a>
          </c:if>
          <c:if test="${showFileSize}">
            <c:out value="${view:humanReadableSize(attachment.size)}" />
          </c:if>
          <c:if test="${showFileSize && showDownloadEstimation}"> / </c:if>
          <c:if test="${showDownloadEstimation}">
            <c:out value="${view:estimateDownload(attachment.size)}" />
          </c:if> - <view:formatDate value="${attachment.created}" />
        </span>
        <c:if test="${view:isDefined(attachment.title) && showTitle}">
          <span class="fileName"><c:out value="${attachment.filename}" /></span>
        </c:if>
        <c:if test="${view:isDefined(attachment.description) && showInfo}">
          <span class="description"><view:encodeHtml string="${attachment.description}" /></span>
        </c:if>
        <c:if test="${view:isDefined(attachment.xmlFormId)}"> 
          <br/><a rel='<c:url value="/RformTemplate/jsp/View">
            <c:param name="width" value="400"/>
            <c:param name="ObjectId" value="${attachment.id}"/>            
            <c:param name="ObjectLanguage" value="${contentLanguage}"/>
            <c:param name="ComponentId" value="${componentId}"/>
            <c:param name="ObjectType" value="${'Attachment'}"/>
            <c:param name="XMLFormName" value="${attachment.xmlFormId}"/>
                  </c:url>' href="#" title='<c:out value="${title}"/>' ><fmt:message key="attachment.xmlForm.View" /></a>
        </c:if>  
          <c:if test="${contextualMenuEnabled}">
            <c:choose>
              <c:when test="${attachment.readOnly}">
                <div class='workerInfo'  id='worker<c:out value="${attachment.oldSilverpeasId}" />' style="visibility:visible"><fmt:message key="readOnly" /> <view:username zoom="false" userId="${attachment.editedBy}" /> <fmt:message key="at" /> <view:formatDate value="${attachment.reservation}" /></div>
              </c:when>
              <c:otherwise>
                <div class='workerInfo'  id='worker<c:out value="${attachment.oldSilverpeasId}" />' style="visibility:hidden"> </div>
              </c:otherwise>
            </c:choose>
          </c:if>	
					<c:if test="${spinfireViewerEnable && spinfire eq view:mimeType(attachment.filename)}">
            <div id="switchView" name="switchView" style="display: none">
              <a href="#" onClick="changeView3d('<c:out value="${attachment.id}" />')"><img name="iconeView<c:out value="${attachment.id}" />" valign="top" border="0" src="<c:url value="/util/icons/masque3D.gif" />"></a>
            </div>
            <div id="<c:out value="${attachment.id}" />" style="display: none">
						  <object classid="CLSID:A31CCCB0-46A8-11D3-A726-005004B35102" width="300" height="200" id="XV" >
                <param name="ModelName" value="<c:out value="${url}" escapeXml="false"/>">
                <param name="BorderWidth" value="1">
                <param name="ReferenceFrame" value="1">
                <param name="ViewportActiveBorder" value="FALSE">
                <param name="DisplayMessages" value="TRUE">
                <param name="DisplayInfo" value="TRUE">
                <param name="SpinX" value="0">
                <param name="SpinY" value="0">
                <param name="SpinZ" value="0">
                <param name="AnimateTransitions" value="0">
                <param name="ZoomFit" value="1">
						  </object>
            </div>
            <br/>
          </c:if>
          <c:if test="${isAttachmentPositionRight}"></li></c:if>
        </c:forEach>
      </ul>         
</c:if>
    <c:if test="${contextualMenuEnabled && dragAndDropEnable}">
      <view:settings var="maximumFileSize" settings="com.stratelia.webactiv.util.uploads.uploadSettings" key="MaximumFileSize" defaultValue="${10000000}" />
      <c:url var="dropUrl" value="/DragAndDrop/drop">
        <c:param name="UserId" value="${mainSessionController.userId}" />
        <c:param name="ComponentId" value="${componentId}" />
        <c:param name="PubId" value="${param.Id}" />
        <c:param name="IndexIt" value="${indexIt}" />
        <c:param name="Context" value="${param.Context}"/>
      </c:url>

	<div class="dragNdrop">
    <a href="javascript:showHideDragDrop('<%=URLManager.getServerURL(request)%><c:out value="${dropUrl}" />','<%=URLManager.getFullApplicationURL(request)%>/upload/explanationShort_<%=language%>.html','<fmt:message key="GML.applet.dnd.alt" />','<c:out value="${maximumFileSize}" />','<%=m_Context%>','<fmt:message key="GML.DragNDropExpand" />','<fmt:message key="GML.DragNDropCollapse" />')" id="dNdActionLabel"><fmt:message key="GML.DragNDropExpand" /></a>
    	<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding: 0px" align="top"> </div>
	</div>
</c:if>
    <c:if test="${contextualMenuEnabled && ! dragAndDropEnable}">
      <div class="dragNdrop"><br/><a href="javascript:AddAttachment();"><fmt:message key="GML.add" />...</a></div>
    </c:if>
    </div><!--/ATTACHMENTS -->
<div id="attachmentModalDialog" style="display: none"></div>
<c:if test="${spinfireViewerEnable}">
<script type="text/javascript">
  if (navigator.appName=='Microsoft Internet Explorer') {
    for (i=0; i<document.getElementsByName("switchView").length; i++) {
      document.getElementsByName("switchView")[i].style.display = '';
    }
  }
  function changeView3d(objectId) {
    if (document.getElementById(objectId).style.display == 'none') {
      document.getElementById(objectId).style.display = '';
      eval("iconeView"+objectId).src = '<c:url value="/util/icons/visible3D.gif" />';
    } else {
      document.getElementById(objectId).style.display = 'none';
      eval("iconeView"+objectId).src = '<c:url value="/util/icons/masque3D.gif" />';
    }
  }
</script>
</c:if>

<script type="text/javascript">
  String.prototype.format = function () {
    var args = arguments;
    return this.replace(/\{(\d+)\}/g, function (m, n) { return args[n]; });
  };

  
  // Create the tooltips only on document load
  $(document).ready(function() {
    // Use the each() method to gain access to each elements attributes
    $('a[rel]').each(function() {
      $(this).qtip(
      {
        content: {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text: '<img class="throbber" src="<c:url value="/util/icons/inProgress.gif" />" alt="Loading..." />',
          url: $(this).attr('rel'), // Use the rel attribute of each element for the url to load
          title: {
            text: '<fmt:message key="attachment.xmlForm.ToolTip"/> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
            button: '<fmt:message key="GML.close" />' // Show a close link in the title
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
    
    // function to transform insecable string into secable one
    $(".lineMain a").html(function() {
        var newLibelle = ""
        var maxLength = 38;
        var chainesInsecables = $(this).text().split(" ");
        for (i=0;i<chainesInsecables.length;i++) {
            var chainesSecables = " ";
            while(chainesInsecables[i].length>maxLength) {
                chainesSecables = chainesSecables+chainesInsecables[i].substring(0,maxLength)+'<br/>';
                chainesInsecables[i] = chainesInsecables[i].substring(maxLength);
            }
            chainesInsecables[i] = chainesSecables+chainesInsecables[i];
            newLibelle = newLibelle + chainesInsecables[i];
        }       
        $(this).html(newLibelle);
    });
  });

<c:if test="${contextualMenuEnabled}" >

  	var pageMustBeReloadingAfterSorting = false;
  
    function checkout(id, oldId, webdav, edit, download) {
      if (id.length > 0) {
        pageMustBeReloadingAfterSorting = true;
        $.get('<c:url value="/Attachment" />', {Id:id,FileLanguage:'<c:out value="${contentLanguage}" />',Action:'Checkout'},
        function(data) {
          if(data == 'ok') {           
            var oMenu = eval("oMenu"+oldId);
            oMenu.getItem(3).cfg.setProperty("disabled", false);
            oMenu.getItem(0).cfg.setProperty("disabled", true);
		        oMenu.getItem(1).cfg.setProperty("disabled", true);
            if (!webdav) {
              oMenu.getItem(2).cfg.setProperty("disabled", true);
            }
		        //disable delete
				  <c:choose>
            <c:when test="${useXMLForm}">oMenu.getItem(2,1).cfg.setProperty("disabled", true);</c:when>
            <c:otherwise>oMenu.getItem(1,1).cfg.setProperty("disabled", true);</c:otherwise>
          </c:choose>
				$('#worker'+oldId).html("<%=attResources.getString("readOnly")%> <%=m_MainSessionCtrl.getCurrentUserDetail().getDisplayedName()%> <%=attResources.getString("at")%> <%=DateUtil.getOutputDate(new Date(), language)%>");
          		$('#worker'+oldId).css({'visibility':'visible'});
              if (edit) {
                var url = "<%=URLManager.getFullApplicationURL(request)%>/attachment/jsp/launch.jsp?documentUrl="+eval("webDav".concat(oldId));
                window.open(url,'_self');
              } else if (download) {
                var url = $('#url'+oldId).attr('href');
                window.open(url);
              }
        	} else {
        		alert("<%=attResources.getString("attachment.dialog.checkout.nok")%>");
          		window.location.href=window.location.href;
        	}
        }, 'text');
        pageMustBeReloadingAfterSorting = true;
      }
    }

    function checkoutAndDownload(id, oldId, webdav) {
      checkout(id, oldId, webdav, false, true);
    }

    function checkoutAndEdit(id, oldId) {
      checkout(id, oldId, true, true, false);
    }

    function checkin(id, oldId, webdav, forceRelease) {
      if (id.length > 0) {
        var webdavUpdate = 'false';
        if (webdav)
        {
          if(confirm('<fmt:message key="confirm.checkin.message" />')) {
            webdavUpdate='true';
          }
        }
        if(forceRelease == 'true'){
          closeMessage();
        }
        $.get('<c:url value="/Attachment" />', {Id:id,FileLanguage:'<c:out value="${contentLanguage}" />',Action:'Checkin',update_attachment:webdavUpdate,force_release:forceRelease},
        function(data) {
          data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
          if (data == "locked") {
            displayWarning(id);
          }
          else {
            if (data == "ok") {
              menuCheckin(oldId);
            }
          }
        }, "text");
        pageMustBeReloadingAfterSorting = true;
      }
    }

    function menuCheckin(id) {
      var oMenu = eval("oMenu"+id);
      oMenu.getItem(3).cfg.setProperty("disabled", true);
      oMenu.getItem(0).cfg.setProperty("disabled", false);
      oMenu.getItem(1).cfg.setProperty("disabled", false);
      oMenu.getItem(2).cfg.setProperty("disabled", false);
      //enable delete
  <c:choose>
    <c:when test="${useXMLForm}">oMenu.getItem(2,1).cfg.setProperty("disabled", false);</c:when>
    <c:otherwise>oMenu.getItem(1,1).cfg.setProperty("disabled", false);</c:otherwise>
  </c:choose>    
      $('#worker'+id).html("");
      $('#worker'+id).css({'visibility':'hidden'});
    }
    
    function notifyAttachment(attachmentId) {
      alertUsersAttachment(attachmentId); //dans publication.jsp
    }

    function AddAttachment() {
  <%
       String winAddHeight = "240";
       if (I18NHelper.isI18N) {
         winAddHeight = "270";
       }
  %>
      SP_openWindow('<c:url value="/attachment/jsp/addAttFiles.jsp" />', "test", "600", "<%=winAddHeight%>","scrollbars=no, resizable, alwaysRaised");
    }

    function deleteAttachment(id) {     
      $( "#dialog-attachment-delete" ).data("id", id).dialog( "open" );
    }

    function removeAttachment(attachmentId) {
      var sLanguages = "";
      var boxItems = document.removeForm.languagesToDelete;
      if (boxItems != null){
        //at least one checkbox exists
        var nbBox = boxItems.length;
        if ( (nbBox == null) && (boxItems.checked) ) {
          sLanguages += boxItems.value+",";
        } else{
          for (i=0;i<boxItems.length ;i++ ){
            if (boxItems[i].checked){
              sLanguages += boxItems[i].value+",";
            }
          }
        }
      }	
	
  $.get('<c:url value="/Attachment" />', { id:attachmentId,Action:'Delete',languagesToDelete:sLanguages},
      function(data){
        data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
        if (data == "attachmentRemoved") {
          $('#attachment_'+attachmentId).remove();
        } else {
          if (data == "translationsRemoved") {
            reloadIncludingPage();
          }
        }
        closeMessage();
      }, 'text');
      pageMustBeReloadingAfterSorting = true;
    }

    function reloadIncludingPage() {
  <c:choose>
    <c:when test="${! view:isDefined(param.CallbackUrl)}">document.location.reload();</c:when>
    <c:otherwise>document.location.href = "<c:url value="${param.CallbackUrl}" />";</c:otherwise>
  </c:choose>
    }

  function updateAttachment(attachmentId, lang) {
    loadAttachment(attachmentId, lang);
    $( "#dialog-attachment-update" ).data('attachmentId', attachmentId).dialog( "open" );
  }

  <c:if test="${useXMLForm}">
    function EditXmlForm(id, lang) {
      var url = '<c:url value="/RformTemplate/jsp/Edit"><c:param name="ObjectId" value="${param.Id}"/><c:param name="IndexIt" value="${indexIt}" /><c:param name="ComponentId" value="${param.ComponentId}" /><c:param name="type" value="Attachment" /><c:param name="ObjectType" value="Attachment" /><c:param name="XMLFormName" value="${xmlForm}" /></c:url>&ReloadOpener=true&ObjectLanguage='+lang;
      SP_openWindow(url, "test", "600", "400","scrollbars=yes, resizable, alwaysRaised");
    }
  </c:if>

    function closeMessage() {
    	$("#attachmentModalDialog").dialog("close");
    }

    function displayWarning(attachmentId) {
    	var url = '<%=m_Context%>/attachment/jsp/warning_locked.jsp?id=' + attachmentId;
        $("#attachmentModalDialog").dialog("open").load(url);
    }  

    $(document).ready(function() {
      $("#fileLang").on("change", function (event) {Z
        $("#fileLang option:selected").each(function () {
          alert($(this).val());
          loadAttachment( $("#attachmentId").val(), $(this).val());
        });
      });
      
      $( '#update-attachment-form' ).iframePostForm ({
        json : true,
        post : function () {
        },
        complete : function (response) {
          reloadIncludingPage();
          $( this ).dialog( "close" );
        }
      });
      
      $( "#dialog-attachment-delete" ).dialog({
        autoOpen: false,
        title: '<fmt:message key="supprimerAttachment" />',
        height: 300,
        width: 350,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() {            
            deleteUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data("id");
            $.ajax({
              url: deleteUrl,
              type: "DELETE",
              cache: false,
              success: function(data) {
                 reloadIncludingPage();
                 $( this ).dialog( "close" );
              }
            });
          },
          '<fmt:message key="GML.cancel"/>': function() {
            $( this ).dialog( "close" );
          }
        },
        close: function() {          
        }
      });     
      
      $( "#dialog-attachment-update" ).dialog({
        autoOpen: false,
        height: 350,
        width: 600,
        modal: true,
        buttons: {
          '<fmt:message key="GML.ok"/>': function() { 
            var submitUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId');
            $('#update-attachment-form').attr('action', submitUrl);
            $('#update-attachment-form').submit();
          },
          '<fmt:message key="GML.delete"/>': function() {
            $.ajax({              
              url: '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + $(this).data('attachmentId')  + '/content/' + $("#fileLang").val(),
              type: "DELETE",
              contentType: "application/json",
              dataType: "json",
              cache: false,
              success: function(data) {
                 reloadIncludingPage();
                 $( this ).dialog( "close" );
              }
            }); 
            $( this ).dialog( "close" );
          },
          '<fmt:message key="GML.cancel"/>': function() {
            $( this ).dialog( "close" );
          }
        },
        close: function() {          
        }
      });      
      
      $("#attachmentList").sortable({opacity: 0.4, axis: 'y', cursor: 'move', placeholder: 'ui-state-highlight', forcePlaceholderSize: true});

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
    
    function sortAttachments(orderedList) {
      $.get('<c:url value="/Attachment" />', { orderedList:orderedList,Action:'Sort'},
      function(data){
        data = data.replace(/^\s+/g,'').replace(/\s+$/g,'');
        if (data == "error")
        {
          alert("Une erreur s'est produite !");
        }
      }, 'text');
      if (pageMustBeReloadingAfterSorting) {
	      reloadIncludingPage();
      }
    }

    function uploadCompleted(s) {
      reloadIncludingPage();
    }

    function ShareAttachment(id) {
      var url = '<c:url value="/RfileSharing/jsp/NewTicket"><c:param name="componentId" value="${param.ComponentId}" /><c:param name="type" value="Attachment" /></c:url>&objectId=' + id;
      SP_openWindow(url, "NewTicket", "700", "300","scrollbars=no, resizable, alwaysRaised");
    }
</c:if>
    
      function displayAttachment(attachment) {
        $('#fileName').text(attachment.fileName);
        $('#fileTitle').val(attachment.title);
        $('#fileDescription').val(attachment.description);
      }
      
      function clearAttachment() {
        $('#fileName').html('');
        $('#fileTitle').val('');
        $('#fileDescription').val('');
      }
      
      function loadAttachment(id, lang) {
        translationsUrl = '<c:url value="/services/documents/${sessionScope.Silverpeas_Attachment_ComponentId}/document/"/>' + id + '/translations';
        $.ajax({
          url: translationsUrl,
          type: "GET",
          contentType: "application/json",
          dataType: "json",
          cache: false,
          success: function(data) { 
            $('#attachmentId').val(id);
            clearAttachment();
            $.each(data, function(index, attachment) {
              if(attachment.lang == lang) {
                displayAttachment(attachment);
                return false;
              }
              return true;
            });
          }
        });
      }

</script>
        <div id="dialog-attachment-update" style="display:none">
            <form name="update-attachment-form" id="update-attachment-form" action="<c:url value="/attachment/jsp/updateFile.jsp" />" method="post" enctype="multipart/form-data" accept-charset="UTF-8" target="iframe-post-form">
              <label for="fileName"><fmt:message key="GML.file" /></label><br/>
              <span id="fileName"></span><br/>
              <input type="hidden" name="IdAttachment" id="attachmentId"/><br/>
              <label for="file_upload"><fmt:message key="fichierJoint"/></label><br/>
              <input type="file" name="file_upload" size="60" id="file_upload" multiple/><br/>
              <view:langSelect elementName="fileLang" elementId="fileLang" langCode="fr" /><br/>
              <label for="fileTitle"><fmt:message key="Title"/></label><br/>
              <input type="text" name="fileTitle" size="60" id="fileTitle" /><br/>
              <label for="fileDesc"><fmt:message key="GML.description" /></label><br/>
              <textarea name="fileDescription" cols="60" rows="3" id="fileDescription"></textarea><br/>
              <input type="submit" value="Submit" style="display:none" />
            </form>
        </div>
        <div id="dialog-attachment-delete" style="display:none">
          <span id="attachment-delete-warning-message"><fmt:message key="attachment.suppressionConfirmation" /></span>
        </div>
