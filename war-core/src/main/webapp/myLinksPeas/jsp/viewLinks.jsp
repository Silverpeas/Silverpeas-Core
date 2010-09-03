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

<%@ include file="check.jsp"%>
<%
  Collection links = (Collection) request.getAttribute("Links");
  String url = (String) request.getAttribute("UrlReturn");
  String instanceId = (String) request.getAttribute("InstanceId");
%>
<%@page import="com.silverpeas.util.StringUtil"%>

<html>

<head>
<view:looknfeel />

<script type="text/javascript"
	src="<%=m_context%>/util/javaScript/animation.js"></script>

<script language="javascript">

var linkWindow = window;

function addLink() 
{
	urlWindows = "NewLink";
	windowName = "linkWindow";
	larg = "700";
	haut = "300";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!linkWindow.closed && linkWindow.name== "linkWindow")
        linkWindow.close();
    linkWindow = SP_openWindow(urlWindows, windowName, larg, haut, windowParams);
}

function editLink(id) 
{
    urlWindows = "EditLink?LinkId="+id;
    windowName = "linkWindow";
	larg = "700";
	haut = "300";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!linkWindow.closed && linkWindow.name== "linkWindow")
        linkWindow.close();
    linkWindow = SP_openWindow(urlWindows, windowName, larg, haut, windowParams);
}


function deleteSelectLinksConfirm() 
{
	/*var boxItems = document.readForm.linkCheck;
    var selectItems = "";
    if (boxItems != null)
    {
    	// au moins une checkbox exist
        var nbBox = boxItems.length;
        if ( (nbBox == null) && (boxItems.checked == true) )
        {
        	selectItems += boxItems.value;
        } 
        else
        {
            for (i=0;i<boxItems.length ;i++ )
            {
        		if (boxItems[i].checked == true)
        		{
                	selectItems += boxItems[i].value+",";
                }
        	}
        	selectItems = selectItems.substring(0,selectItems.length-1);
		}
	}*/
	//if ( (selectItems.length > 0) && (areYouSure()) ) 
	if (areYouSure())
	{
    	document.readForm.mode.value = 'delete';
    	document.readForm.submit();
  	}
}

function areYouSure()
{
    return confirm("<%=resource.getString("myLinks.deleteSelection")%>");
}

</script>

</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

<form name="readForm" action="DeleteLinks" method="POST">
  <input type="hidden" name="mode"> 
<%
   String bBar = resource.getString("myLinks.links");
   if (instanceId != null)
     bBar = resource.getString("myLinks.linksByComponent");
   browseBar.setComponentName(bBar);

   operationPane.addOperation(resource.getIcon("myLinks.addLink"), resource.getString("myLinks.addLink"), "javaScript:addLink()");

   operationPane.addOperation(resource.getIcon("myLinks.deleteLinks"), resource
       .getString("myLinks.deleteLinks"), "javaScript:deleteSelectLinksConfirm()");

   ButtonPane buttonPane = gef.getButtonPane();
   Button returnButton =
       (Button) gef.getFormButton(resource.getString("myLinks.retour"), url, false);

   out.println(window.printBefore());
   out.println(frame.printBefore());

   ArrayPane arrayPane = gef.getArrayPane("linkList", "ViewLinks", request, session);
   arrayPane.addArrayColumn(resource.getString("GML.nom"));
   arrayPane.addArrayColumn(resource.getString("GML.description"));
   ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("myLinks.operation"));
   columnOp.setSortable(false);

   // Fill ArrayPane with links
   if ((links != null) && (links.size() != 0)) {
     Iterator it = (Iterator) links.iterator();
     while (it.hasNext()) {
       ArrayLine line = arrayPane.addArrayLine();
       LinkDetail link = (LinkDetail) it.next();
       int linkId = link.getLinkId();
       String lien = link.getUrl();
       String name = link.getName();
       String desc = link.getDescription();

       if (!StringUtil.isDefined(name)) {
         name = lien;
       }
       if (!StringUtil.isDefined(desc)) {
         desc = "";
       }
       // Add context before link if needed
       if (lien.indexOf("://") == -1) {
         lien = m_context + lien;
       }
       ArrayCellLink monLien = line.addArrayCellLink(name, lien);
       if (link.isPopup()) {
         monLien.setTarget("_blank");
       }

       line.addArrayCellText(desc);

       IconPane iconPane = gef.getIconPane();
       Icon updateIcon = iconPane.addIcon();
       updateIcon.setProperties(resource.getIcon("myLinks.update"), resource
           .getString("myLinks.updateLink"), "javaScript:onClick=editLink('" + linkId + "')");
       line.addArrayCellText(updateIcon.print() +
           "&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"linkCheck\" value=\"" +
           link.getLinkId() + "\">");
     }
   }

   out.println(arrayPane.print());
   if (instanceId != null) {
     buttonPane.addButton(returnButton);
     out.println("<BR><center>" + buttonPane.print() + "</center><BR>");
   }
   out.println(frame.printAfter());
   out.println(window.printAfter());
 %>
</form>

<form name="linkForm" action="" Method="POST">
  <input type="hidden" name="LinkId"> 
  <input type="hidden" name="Name">
  <input type="hidden" name="Description"> 
  <input type="hidden" name="Url"> 
  <input type="hidden" name="Visible"> 
  <input type="hidden" name="Popup">
</form>

</body>
</html>