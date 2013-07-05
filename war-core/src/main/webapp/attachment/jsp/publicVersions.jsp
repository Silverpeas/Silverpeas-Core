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

<%@page import="com.silverpeas.util.FileUtil" %>
<%@page import="org.apache.commons.lang3.CharEncoding" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%@ page import="
				 com.silverpeas.util.StringUtil,
                 com.stratelia.silverpeas.peasCore.ComponentContext,
                 com.stratelia.silverpeas.peasCore.MainSessionController,
                 com.stratelia.silverpeas.peasCore.URLManager,
                 com.stratelia.silverpeas.util.ResourcesWrapper,
                 com.stratelia.webactiv.util.FileRepositoryManager,
                 com.stratelia.webactiv.util.FileServerUtils,
                 com.stratelia.webactiv.util.ResourceLocator,
                 com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane" %>
<%@page import="org.silverpeas.attachment.model.SimpleDocument" %>
<%@page import="org.silverpeas.attachment.web.VersioningSessionController" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.List" %>

<%
  GraphicElementFactory gef =
      (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  ResourcesWrapper resources = (ResourcesWrapper) request.getAttribute("resources");
  ResourceLocator attachmentSettings =
      new ResourceLocator("org.silverpeas.util.attachment.Attachment", "");
  MainSessionController mainSessionCtrl = (MainSessionController) session
      .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  VersioningSessionController versioningSC =
      (VersioningSessionController) request.getAttribute(URLManager.CMP_VERSIONINGPEAS);%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp" %>

<view:setBundle basename="org.silverpeas.versioningPeas.multilang.versioning"/>
<view:setBundle basename="org.silverpeas.util.attachment.multilang.attachment" var="attachmentBundle" />
<fmt:setLocale value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<%
  ResourceLocator messages =
      new ResourceLocator("org.silverpeas.versioningPeas.multilang.versioning",
          mainSessionCtrl.getFavoriteLanguage());

  SimpleDocument document = (SimpleDocument) request.getAttribute("Document");
  List<SimpleDocument> vVersions = (List<SimpleDocument>) request.getAttribute("Versions");
  boolean fromAlias = StringUtil.getBooleanValue((String) request.getAttribute("Alias"));

  String componentId = document.getPk().getInstanceId();
  String id = document.getPk().getId();
  boolean spinfireViewerEnable = attachmentSettings.getBoolean("SpinfireViewerEnable", false);
  String contentLanguage = (String) request.getAttribute("ContentLanguage");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="popupTitle"/></title>
  <view:looknfeel/>
  <view:includePlugin name="qtip"/>
</head>
<body>
<view:window>
  <view:browseBar extraInformations="${requestScope.Document.title}" clickable="false"/>

  <%
    ArrayPane arrayPane = gef.getArrayPane("List",
        "ViewAllVersions?DocId=" + id + "&Alias=null&ComponentId=" + componentId, request,
        session);// declare an array

// header of the array
    ArrayColumn arrayColumn_version = arrayPane.addArrayColumn(messages.getString("version"));
    arrayColumn_version.setSortable(false);
    ArrayColumn arrayColumn_mimeType =
        arrayPane.addArrayColumn(messages.getString("GML.attachments"));
    arrayColumn_mimeType.setSortable(false);
    ArrayColumn arrayColumn_titre = arrayPane.addArrayColumn(messages.getString("GML.title"));
    arrayColumn_mimeType.setSortable(false);
    ArrayColumn arrayColumn_infos = arrayPane.addArrayColumn(messages.getString("description"));
    arrayColumn_mimeType.setSortable(false);
    ArrayColumn arrayColumn_creatorLabel = arrayPane.addArrayColumn(messages.getString("creator"));
    arrayColumn_creatorLabel.setSortable(false);
    ArrayColumn arrayColumn_date = arrayPane.addArrayColumn(messages.getString("date"));
    arrayColumn_date.setSortable(false);
    ArrayColumn arrayColumn_status = arrayPane.addArrayColumn(messages.getString("comments"));
    arrayColumn_status.setSortable(false);

    ArrayLine arrayLine = null; // declare line object of the array

    for (SimpleDocument publicVersion : vVersions) {
      arrayLine = arrayPane.addArrayLine(); // set a new line
      String url =
          FileServerUtils.getUrl(publicVersion.getInstanceId(), publicVersion.getFilename()) +
              "&DocumentId=" + publicVersion.getId() + "&VersionId=" + publicVersion.getId();
      if (fromAlias) {
        url = publicVersion.getAliasURL();
      }

      String spinFire = "";
      if (FileUtil.isSpinfireDocument(publicVersion.getFilename()) && spinfireViewerEnable) {
        spinFire = "<br><div id=\"switchView\" name=\"switchView\" style=\"display: none\">";
        spinFire += "<a title=\"Viewer SpinFire 3D\"href=\"#\" onClick=\"changeView3d(" +
            publicVersion.getPk().getId() +
            ")\"><img name= \"iconeView\" border=0 src=\"/util/icons/masque.gif\"></a>";
        spinFire += "</div>";
        spinFire += "<div id=\"" + publicVersion.getPk().getId() + "\" style=\"display: none\">";
        spinFire += "<OBJECT classid=\"CLSID:A31CCCB0-46A8-11D3-A726-005004B35102\"";
        spinFire += "width=\"300\" height=\"200\" id=\"XV\">";
        spinFire += "<PARAM NAME=\"ModelName\" VALUE=\"" + url + "\">";
        spinFire += "</OBJECT>";
        spinFire += "</div>";
      }
      String permalink =
          " <a href=\"" + URLManager.getSimpleURL(URLManager.URL_VERSION, publicVersion.getId()) +
              "\"><img src=\"" + URLManager.getApplicationURL() +
              "/util/icons/link.gif\" border=\"0\" valign=\"absmiddle\" alt=\"" +
              messages.getString("versioning.CopyLink") + "\" title=\"" +
              messages.getString("versioning.CopyLink") + "\" target=\"_blank\"></a> ";
      arrayLine.addArrayCellText(
          "<a href=\"" + url + "\" target=\"_blank\">" + publicVersion.getMajorVersion() + "." +
              publicVersion.getMinorVersion() + "</a>" + permalink + spinFire);
      arrayLine.addArrayCellText("<a href=\"" + url + "\" target=\"_blank\"><img src=\"" +
          FileRepositoryManager.getFileIcon(publicVersion.getFilename()) +
          "\" border=\"0\" title=\"" + publicVersion.getFilename() + "\"/> " +
          publicVersion.getFilename() + "</a>");

      if (StringUtil.isDefined(publicVersion.getTitle())) {
        arrayLine.addArrayCellText(publicVersion.getTitle());
      } else {
        arrayLine.addArrayCellText("");
      }
      if (StringUtil.isDefined(publicVersion.getDescription())) {
        arrayLine.addArrayCellText(publicVersion.getDescription());
      } else {
        arrayLine.addArrayCellText("");
      }
      if (StringUtil.isDefined(publicVersion.getCreatedBy())) {
        arrayLine.addArrayCellText(
            versioningSC.getUserNameByID(Integer.parseInt(publicVersion.getCreatedBy())));
      } else {
        arrayLine.addArrayCellText("????");
      }
      ArrayCellText cell =
          arrayLine.addArrayCellText(resources.getOutputDateAndHour(publicVersion.getUpdated()));
      cell.setNoWrap(true);

      String xtraData = "";
      if (StringUtil.isDefined(publicVersion.getXmlFormId())) {
        String xmlURL = URLManager.getApplicationURL() + "/RformTemplate/jsp/View?ObjectId=" +
            publicVersion.getId() + "&ComponentId=" + componentId +
            "&ObjectType=Attachment&XMLFormName=" +
            URLEncoder.encode(publicVersion.getXmlFormId(), CharEncoding.UTF_8) +
            "&ObjectLanguage=" + contentLanguage;
        xtraData = "<a rel=\"" + xmlURL + "\" href=\"#\" title=\"" + document.getFilename() + " " +
            publicVersion.getMajorVersion() + "." + publicVersion.getMinorVersion() +
            "\"><img src=\"" + URLManager.getApplicationURL() +
            "/util/icons/info.gif\" border=\"0\"></a> ";
      }
      if (StringUtil.isDefined(publicVersion.getComment())) {
        arrayLine.addArrayCellText(xtraData + publicVersion.getComment());
      } else {
        arrayLine.addArrayCellText("");
      }
    }

    out.println(arrayPane.print());%>
</view:window>
</body>
</html>
<% if (spinfireViewerEnable) { %>
<script type="text/javascript">
  if (navigator.appName == 'Microsoft Internet Explorer') {
    for (i = 0; document.getElementsByName("switchView")[i].style.display == 'none'; i++)
      document.getElementsByName("switchView")[i].style.display = '';
  }
  function changeView3d(objectId) {
    if (document.getElementById(objectId).style.display == 'none') {
      document.getElementById(objectId).style.display = '';
      iconeView.src = '/util/icons/visible.gif';
    } else {
      document.getElementById(objectId).style.display = 'none';
      iconeView.src = '/util/icons/masque.gif';
    }
  }
</script>
<% } %>
<script type="text/javascript">
  // Create the tooltips only on document load
  $(document).ready(function() {
    // Use the each() method to gain access to each elements attributes
    $('a[rel]').each(function() {
      $(this).qtip({
        content : {
          // Set the text to an image HTML string with the correct src URL to the loading image you want to use
          text : '<img class="throbber" src="<c:url value="/util/icons/inProgress.gif" />" alt="Loading..." />',
          ajax: {
            url : $(this).attr('rel') // Use the rel attribute of each element for the url to load
          },
          title : {
            text : '<fmt:message key="attachment.xmlForm.ToolTip" bundle="${attachmentBundle}"/> \"' + $(this).attr('title') + "\"", // Give the tooltip a title using each elements text
            button : '<fmt:message key="GML.close" />' // Show a close link in the title
          }
        },
        position : {
          adjust : {
            method : "flip flip"
          },
          at : "left center",
          my : "right center",
          viewport : $(window) // Keep the tooltip on-screen at all times
        },
        show : {
          solo : true,
          event : "click"
        },
        hide : {
          event : "unfocus"
        },
        style : {
          tip : true, // Apply a speech bubble tip to the tooltip at the designated tooltip corner
          width : 570,
          classes : "qtip-shadow qtip-light"
        }
      });
    });
  });
</script>