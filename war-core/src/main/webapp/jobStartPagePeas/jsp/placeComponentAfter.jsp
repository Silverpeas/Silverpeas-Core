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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%
    String m_ComponentName = (String) request.getAttribute("currentComponentName");
    ComponentInst[] brothers = (ComponentInst[]) request.getAttribute("brothers");
	SpaceInst selectedSpace = (SpaceInst) request.getAttribute("selectedSpace");
	SpaceInst currentSpace = (SpaceInst) request.getAttribute("currentSpace");
    SpaceInst[] spaces = (SpaceInst[]) request.getAttribute("spaces");
	boolean validLicense = ((Boolean) request.getAttribute("validLicense")).booleanValue();
	
	browseBar.setClickable(false);
	browseBar.setSpaceId(currentSpace.getId());
    browseBar.setComponentName(m_ComponentName);
    browseBar.setPath(resource.getString("JSPP.ComponentOrder"));

 	//Is a component with the same name already exist in the destination space ?
	boolean sameComponentName = false;
	if (!selectedSpace.getId().equals(currentSpace.getId()))
	{
	    for (int i = 0; i < brothers.length && !sameComponentName; i++)
	    {
	    	if (m_ComponentName.equals(brothers[i].getLabel()))
	    		sameComponentName = true;
		}
	}
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
    out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
image1 = new Image(1,1);
image1.src="<%=m_context%>/util/icons/colorPix/1px.gif";
image2 = new Image(83,20);
image2.src="<%=m_context%>/util/icons/attachment_to_upload.gif";

function B_ANNULER_ONCLICK() {
	window.close();
}
/*****************************************************************************/
function B_VALIDER_ONCLICK() {
	document.componentOrder.action = "EffectivePlaceComponent";
	<% if (validLicense) { %>
		//Ask confirmation only if space has changed
		if (document.componentOrder.DestinationSpace.options[document.componentOrder.DestinationSpace.selectedIndex].value != '<%=currentSpace.getId()%>')
		{
			if (confirm('<%=resource.getString("JSPP.MessageConfirmMovingComponentInSpace")%>'))
			{
				document.componentOrder.submit();
				document.img1.src = image2.src;
				if (navigator.appName == 'Microsoft Internet Explorer') {
					InProgress.style.visibility="";
				}
			}
		}
		else
		{
			document.componentOrder.submit();
		}
	<% } else { %>
		document.componentOrder.submit();
	<% } %>
}

function changeDestinationSpace() {
		document.componentOrder.action = "ChangeDestinationSpace";
	    document.componentOrder.submit();
}

</script>
</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5>
<FORM NAME="componentOrder"  METHOD="POST">
<%
    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr>
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
				<% if (validLicense)
				{ %>
					<!-- Spaces choice-->
					<tr align=center>
						<td class="intfdcolor4" valign="top" align=left>
							<span class="txtlibform"><%=resource.getString("JSPP.SpaceIn")%> :</span>
						</td>
							<td class="intfdcolor4" valign="top" align=left>
		                        <SELECT name="DestinationSpace" id="DestinationSpace" onChange="javascript:changeDestinationSpace();">
		                            <%
		                                for (int i = 0; i < spaces.length; i++)
		                                {
											String mode = "";
											String attribut = "";
											if (jobStartPageSC.isSpaceInMaintenance(spaces[i].getId().substring(2)) || spaces[i].getId().equals(currentSpace.getId()))
											{
												if (jobStartPageSC.isSpaceInMaintenance(spaces[i].getId().substring(2)))
													mode = "  (M)";
				                            	if (spaces[i].getId().equals(selectedSpace.getId()))
			                                		attribut = "selected";
		    	                        		out.println("<OPTION "+attribut+" value=\"" + spaces[i].getId() + "\">" + spaces[i].getName()+ mode + "</OPTION>");
		    								}
		
		                                }
		                            %>
		                        </SELECT>
							</td>
					</tr>
				<%  
				}
				else
				{
				%>
					<input type=hidden name="DestinationSpace" value="<%=currentSpace.getId()%>">
				<%
				}
				%>

				<!-- Component choice-->
				<tr align=center>
					<td class="intfdcolor4" valign="top" align=left>
						<span class="txtlibform"><%=resource.getString("JSPP.ComponentPlace")%> :</span>
					</td>
					<td width=50% class="intfdcolor4" valign="top" align=left>
                        <SELECT name="ComponentBefore" id="ComponentBefore">
                            <%
                                for (int i = 0; i < brothers.length; i++)
                                {
                                	//A component with the same name already exists in the destination space
                                	String color = "";
                                	if (m_ComponentName.equals(brothers[i].getLabel()))
                                		color = "style={color:#ff0000}";
                                    out.println("<OPTION "+color+" value=\"" + brothers[i].getId() + "\">" +  brothers[i].getLabel() + "</OPTION>");
                                }
                            %>
                            <OPTION value="-1" selected><%=resource.getString("JSPP.PlaceLast")%></OPTION>
                        </SELECT>
					</td>
				</tr>
				<% if (sameComponentName) { %>
					<tr>
						<td colspan=2 class="MessageReadHighPriority">
							<%=resource.getString("JSPP.ExistingComponentName")%>
						</td>
					</tr>
				<% } %>
				<% if (request.getAttribute("error") != null)
				{ %>
					<tr>
						<td colspan=2 class="MessageReadHighPriority">
							<%=request.getAttribute("error")%>
						</td>
					</tr>
				<% }
				if (validLicense) { %>
					<tr>
						<td colspan=2 class="txtlibform" align="center">
							<div id="InProgress" style="visibility:hidden">
								<%=resource.getString("JSPP.movingInProgress")%><br>
								<img name="img1" src>
							</div>
						</td>
					</tr>
				<% }
				else
					out.println("<tr><td colspan=2><br></td></tr>");
				 %>
			</table>
		</td>
	</tr>
</table>
<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  if (!sameComponentName)
			  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));

		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		  out.println(buttonPane.print());
%>
</center>
<%
		out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</FORM>
</BODY>
<script language="javascript">
    document.img1.src = image1.src;
</script>
</HTML>