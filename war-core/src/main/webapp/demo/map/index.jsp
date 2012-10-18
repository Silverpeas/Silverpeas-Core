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


<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Collection"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.publication.model.PublicationPK"%>
<%@page import="com.stratelia.webactiv.util.publication.control.PublicationBm"%>
<%@page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@page import="java.util.List"%>
<%@page import="com.stratelia.webactiv.util.JNDINames"%>
<%@page import="com.stratelia.webactiv.util.publication.control.PublicationBmHome"%>
<%@page import="com.stratelia.webactiv.util.EJBUtilitaire"%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Demo cartographie</title>
<link REL="SHORTCUT ICON" HREF="util/icons/favicon.ico">
<view:looknfeel />
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript" src="/silverpeas/demo/map/js/StyledMarker.js"></script>
<script type="text/javascript" src="/silverpeas/util/javaScript/silverpeas-profile.js"></script>
<script type="text/javascript" src="/silverpeas/demo/map/js/silverpeas-map.js"></script>

<style type="text/css">
#container {
  position: relative;
  width: 100%;
  margin: auto;
}

#container #map {
  width: 100%;
  height: 100%;
  margin: auto;
}

#container #filters {
  position: absolute;
}

.mapping-filters {
  text-align: center;
  margin: 5px;
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #666666;
  background-color: #efefef;
  opacity: 0.8;
  font-weight: bold;
}

.info-window {
  margin: 0 0 0 0;
  padding: 0 0 0 0;
  display: table;
  border-width: 1px;
  border-color: black;
}

.info-window div {
  vertical-align:top;
}

.info-window .bloc {
  display: table;
}

.info-window .row {
  display: table-row;
}

.info-window .cell {
  display: table-cell;
}

.info-window .horizontal-separator-margin {
  height: 5px;
}

.info-window .horizontal-separator-margin {
  background-color: #efefef;
  height: 1px;
}

.info-window .vertical-separator-margin {
  width: 5px;
}

.info-window .vertical-separator {
  background-color: gray;
  width: 2px;
}

.info-window .title {
  font-weight: bold;
  text-align: center;
  border-width: 1px;
  border-color: black;
  text-decoration: underline;
  font-style: italic;
  font-variant: small-caps;
}

.info-window .label {
  text-align: right;
  padding-right: 15px;
  font-weight: bold;
  width: 150px;
  white-space: nowrap;
  border-width: 1px;
  border-color: black;
}

.info-window .img {
  max-width: 150px;
  max-height: 200px;
}

.info-window .data {
  font-weight: normal;
  white-space: nowrap;
  margin-left: 5px;
}

.info-window .data-multiline {
  font-weight: normal;
  margin-left: 5px;
}

.info-window .vertical-centered {
  vertical-align: middle;
}

.info-window .horizontal-centered {
  text-align: center;
}
</style>

<script type="text/javascript">
<%
final PublicationBm publicationBm =
  EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)
      .create();
final Collection<PublicationDetail> publications =
  publicationBm.getAllPublications(new PublicationPK("useless", request
      .getParameter("application")));
%>
  $(document)
      .ready(
          function() {
            $('#map')
                .mapping(
                    {
                      spInfoPoints : [<%
String separator = "";
for (PublicationDetail pub : publications) {
  if (StringUtil.isDefined(separator)) {
    out.println(separator);
  }
  final Map<String, String> form = pub.getFormValues(null);
  if (form == null || form.isEmpty()) {
    continue;
  }
  String centerPhoto = EncodeHelper.javaStringToJsString(pub.getFieldValue("centerPhoto"));
  if (StringUtil.isDefined(centerPhoto)) {
    centerPhoto = URLManager.getApplicationURL() + centerPhoto;
  }
  String centerAccess = EncodeHelper.javaStringToJsString(pub.getFieldValue("access"));
  if (StringUtil.isDefined(centerAccess)) {
    centerAccess = URLManager.getApplicationURL() + centerAccess;
  }
%>
  new spInfoPoint(
    "<%=EncodeHelper.javaStringToJsString(form.get("group")).replaceAll("[\r\n]", "")%>",
    "<%=EncodeHelper.javaStringToJsString(form.get("type")).replaceAll("[\r\n]", "")%>",
    "<%=EncodeHelper.javaStringToJsString(pub.getName())%>",
    "<%=centerPhoto%>",
    <%=pub.getFieldValue("centerLatitude")%>,
    <%=pub.getFieldValue("centerLongitude")%>,
    "<%=pub.getFieldValue("centerPhone")%>",
    "<%=pub.getFieldValue("centerFax")%>",
    new spAddress(
        "<%=EncodeHelper.javaStringToJsString(pub.getFieldValue("centerAddress1"))%>",
        "<%=EncodeHelper.javaStringToJsString(pub.getFieldValue("centerAddress2"))%>",
        "<%=new String(EncodeHelper.javaStringToJsString(pub.getFieldValue("centerZipCode")
        + " "
        + EncodeHelper.javaStringToJsString(pub.getFieldValue("centerCity")))).trim()%>"
    ),
    "<%=EncodeHelper.javaStringToJsString(centerAccess)%>",
    "<%=EncodeHelper.javaStringToJsString(pub.getFieldValue("complement"))%>",
    [new spChief(<%=pub.getFieldValue("chief")%>), new spAssistant(<%=pub.getFieldValue("assistant")%>)])
<%
  separator = ",";
}
%>
    ]});
  });
</script>
</head>
<body>
  <div id="container">
    <div id="map">
      <p>Veuillez patienter pendant le chargement de la carte...</p>
    </div>
  </div>
</body>
</html>
