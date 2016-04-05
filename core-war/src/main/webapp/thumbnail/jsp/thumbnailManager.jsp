<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@page import="org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail"%>
<%@page import="org.silverpeas.core.util.file.FileServerUtils"%>
<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@ include file="thumbnailHeader.jsp"%>

<%
	String result		    = (String)request.getAttribute("resultThumbnail");
    String action		    = (String)request.getAttribute("action");
	String componentId		= request.getParameter("ComponentId");
	String objectId			= request.getParameter("ObjectId");
	String objectType		= request.getParameter("ObjectType");
	String backUrl		    = request.getParameter("BackUrl");
	String thumbnailHeight  = request.getParameter("ThumbnailHeight");
    String thumbnailWidth   = request.getParameter("ThumbnailWidth");
    if (!StringUtil.isDefined(thumbnailHeight) && StringUtil.isDefined(thumbnailWidth)) {
      thumbnailHeight = Long.toString(Math.round(Integer.parseInt(thumbnailWidth) * 0.75));
    }

	ThumbnailSessionController thumbnailScc = (ThumbnailSessionController) request.getAttribute("thumbnail");
	ThumbnailDetail currentThumbnail = (ThumbnailDetail) request.getAttribute("thumbnaildetail");
	boolean isCreateMode = currentThumbnail == null;

	boolean isAddMode = true;
	boolean isUpdateFileMode = "update".equals(action);
	if(isUpdateFileMode){
		isAddMode = false;
		isCreateMode = true;
	}

	// case update
	String vignette_url = null;
	if(!isCreateMode){
		vignette_url = FileServerUtils.getUrl(currentThumbnail.getInstanceId(), "vignette",
        currentThumbnail.getOriginalFileName(),currentThumbnail.getMimeType(), "images");
	}

	boolean error = false;
	if(result != null && !"ok".equals(result)){
		error = true;
	}

	// force dummy area on first crop
	if (currentThumbnail.getXStart() == 0 && currentThumbnail.getYStart() == 0 &&
	    currentThumbnail.getXLength() == 0 && currentThumbnail.getYLength() == 0) {
	  currentThumbnail.setXLength(100);
	  currentThumbnail.setYLength(100);
	}
%>

<view:includePlugin name="tkn"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.Jcrop.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/jquery.Jcrop.min.css">

<style>
.jcrop-holder {
	float: left;
}
.container {
	height:auto;
}

#visuVignette {
	height:100%;
	background-color:#FFF;
	border:1px solid #CCCCCC;
	width:230px;
	float:right;
	/*margin:0px 50px;*/
	padding:0px 0px 20px 0px;
	text-align:center;
}

.txtlibform {
	margin-top: 10px;
	margin-bottom: 5px;
}

#visuVignette .txtlibform {
	color:#909090;
	text-align:center;
	padding-right:0;
}

#visuVignette #preview {
	border:2px solid #CCC;
	margin-top:118px;
	float:none;
	width:<%=thumbnailWidth%>px;
	height:<%=thumbnailHeight%>px;
	overflow:hidden;
}
</style>

<script type="text/javascript">

function save(){
	var path = document.thumbnailForm.OriginalFile.value;
    if(path != null && path.length > 0){
	saveUpdate();
	} else {
	  alert('<%=resource.getString("thumbnail.nofile")%>');
	}
}

function saveUpdate(){
	document.thumbnailForm.submit();
}

function cancelWindow(){
	// into no have return false
	closeThumbnailDialog();
}
<%if(isCreateMode){%>
function initThumbnailManager(){
	document.thumbnailForm.originalFile.focus();
}
<%}else{%>
function initThumbnailManager(){
		jQuery('#cropbox').Jcrop({
			onChange: showPreview,
			onSelect: showPreview,
			aspectRatio: <%=thumbnailWidth%>/<%=thumbnailHeight%>,
			boxWidth: 800,
			boxHeight: 330,
			setSelect : [<%=currentThumbnail.getXStart()%>,<%=currentThumbnail.getYStart()%>,<%=currentThumbnail.getXStart() + currentThumbnail.getXLength()%>,<%=currentThumbnail.getYStart() + currentThumbnail.getYLength()%>]
		});
}
<%}%>

// Our simple event handler, called from onChange and onSelect
// event handlers, as per the Jcrop invocation above
function showPreview(coords)
{
	var cropbox = document.getElementById('cropbox');
	if (parseInt(coords.w) > 0)
	{
		// visual calcul
		var rx = <%=thumbnailWidth%> / coords.w;
		var ry = <%=thumbnailHeight%> / coords.h;
		var thumbwidth = Math.round(rx * cropbox.width);
		var thumbheight = Math.round(ry * cropbox.height);
		var xStart = Math.round(rx * coords.x);
		var yStart = Math.round(ry * coords.y);

		$('#preview').css({
			width: thumbwidth + 'px',
			height: thumbheight + 'px',
			marginLeft: '-' + xStart + 'px',
			marginTop: '-' + yStart + 'px'
		});

		// save for form
		document.thumbnailForm.XStart.value = Math.round(coords.x);
		document.thumbnailForm.YStart.value = Math.round(coords.y);
		document.thumbnailForm.XLength.value = Math.round(coords.w);
		document.thumbnailForm.YLength.value = Math.round(coords.h);
	}
};

</script>
<center>
<form name="thumbnailForm" method="post" action="<%=m_context%>/Thumbnail/jsp/thumbnailManager.jsp" <%if(isCreateMode){%>enctype="multipart/form-data"<%}%>>
<input type="hidden" name="ComponentId" value="<%=componentId%>"/>
<input type="hidden" name="ObjectId" value="<%=objectId%>"/>
<input type="hidden" name="ObjectType" value="<%=objectType%>"/>
<input type="hidden" name="BackUrl" value="<%=backUrl%>"/>
<%if(thumbnailHeight != null){%>
<input type="hidden" name="ThumbnailHeight" value="<%=thumbnailHeight%>"/>
<%}
if(thumbnailWidth != null){%>
<input type="hidden" name="ThumbnailWidth" value="<%=thumbnailWidth%>"/>
<%}%>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
			<% 	if(error) { %>
				<tr align="center">
					<td class="txtlibform"><%=resource.getString("thumbnail." + result)%></td>
				</tr>
			<% } else if(isCreateMode) { %>
				<tr align="center">
					<td class="txtlibform"><%=resource.getString("thumbnail.path")%></td>
				<td>
				<%if(isUpdateFileMode){%>
					<input type="hidden" name="Action" value="SaveUpdateFile">
				<%}else{%>
					<input type="hidden" name="Action" value="Save">
				<%}%>
						<input type="file" name="OriginalFile" size="60"/>
					</td>
				</tr>
			<% } else { %>
				<tr><td>
						<p class="txtlibform"><%=resource.getString("thumbnail.picture")%></p>
					<div class="container">
						<input type="hidden" name="Action" value="Crop"/>
							<input type="hidden" name="XStart"/>
							<input type="hidden" name="YStart"/>
							<input type="hidden" name="XLength"/>
							<input type="hidden" name="YLength"/>
						<img src="<%=vignette_url%>" id="cropbox" />
						<div id="visuVignette">
						<p class="txtlibform"><%=resource.getString("thumbnail.preview")%></p>
						<div style="width:<%=thumbnailWidth%>px;height:<%=thumbnailHeight%>px;overflow:hidden;margin-left:auto;margin-right:auto;border-width:2px;border-style:solid;border-color:#CCC;">
							<img src="<%=vignette_url%>" id="preview" alt="Vignette" />
						</div>
						</div>
					</div>
				</td>
				</tr>
			<% } %>
			</table>
</form>
<br/>
<%
if(!error){
	ButtonPane buttonPane = gef.getButtonPane();
	if(isCreateMode){
	    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:save();", false));
	}else{
	    buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:saveUpdate();", false));
	}
    buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancelWindow();", false));
    out.println(buttonPane.print());
}
%>
</center>
<%
if(!error){
// on lance l init vu qu on n a pas de onLoad()
%>
<script type="text/javascript">
	setTimeout("initThumbnailManager()", 500);
</script>
<%
}
%>