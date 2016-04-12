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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.util.LocalizationBundle"%>
<%@ page import="org.silverpeas.core.calendar.model.JournalHeader" %>
<%@ page import="org.silverpeas.core.calendar.model.Category" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>

<%@ include file="checkAgenda.jsp" %>

<%!

boolean notIn(String id, Collection categories) {
  if (categories == null) return true;
  Iterator i = categories.iterator();
  while (i.hasNext()) {
    Category category = (Category) i.next();
    if (category.getId().equals(id)) return false;
  }
  return true;
}

%>
<%
LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(agenda.getLanguage());
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
<script type="text/javascript">
function reallyEditCategories()
{
	validateCategories();
	window.opener.document.journalForm.action = "ReallyEditCategories";
	var valueCategories = "";
	for (var i=0; i<document.journalForm.selectedCategories.length; i++) {
		valueCategories = valueCategories + document.journalForm.selectedCategories[i].value + ",";
	}
	window.opener.document.journalForm.selectedCategories.value = valueCategories;
	window.opener.document.journalForm.submit();
	window.close();
}

function move_groups(btn) {
   var z = 0;                       //used to index indexArray
   var indexArray = new Array();    //used to keep track of values in multiple selection case

   if( btn == "+" )     //check which button
   {
      var listObj = document.journalForm.availableCategories;
      var targetObj = document.journalForm.selectedCategories;
   }
   else
   {
      var listObj = document.journalForm.selectedCategories;
      var targetObj = document.journalForm.availableCategories;
   }

   for( var i = 0; i < listObj.length; i++ )   //loop through list to find selected items
   {
      if(listObj.options[i].selected)          //only do something if item is selected
      {
         var selectedItem = listObj.options[i].text;
         var selectedItem2 = listObj.options[i].value;
         targetObj.options[targetObj.length] = new Option( selectedItem, selectedItem2 );   //create new items in target select box
         indexArray[z] = i;             //keep track of indices of selected items
         z++;                           //indexArray only gets a value if the item is selected and the 'if' statement is entered
      }
   }

   for( var i = indexArray.length - 1; i >= 0; i-- )   //cycle backwards through items and clear all selected items
   {                                                //must cycle backwards so the loop does not miss any items when list size changes...
      listObj.options[indexArray[i]] = null;        //...and index of selected item changes
   }                                                //ex. when loop begins, items 1 and 2 are selected, if 1 is deleted first...
}

function moveall_groups(select_actors, actors)
{
    var listObj = document.journalForm.selectedCategories;
    var targetObj = document.journalForm.availableCategories;

   for( var i = 0; i < listObj.length; i++ )        //loop through list
   {
      var selectedItem = listObj.options[i].text;
      var selectedItem2 = listObj.options[i].value;
      targetObj.options[targetObj.length] = new Option( selectedItem, selectedItem2 );
   }

   for( var i = listObj.length - 1; i >= 0; i-- )   //loop backwards through list clearing every item
   {
      listObj.options[i] = null;
   }
}

function validateCategories() {
    nbr = document.journalForm.selectedCategories.length;
    for (j=0;j < nbr;j++)
        document.journalForm.selectedCategories[j].selected = true;
}
</script>
</head>

<%
  String action = null;
  SettingBundle settings = agenda.getSettings();
  JournalHeader journal = agenda.getCurrentJournalHeader();
  Collection categories = agenda.getCurrentCategories();

%>
<body id="agenda">
<%
	Window window = graphicFactory.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(agenda.getString("editionCategories"));
	out.println(window.printBefore());
	Frame frame=graphicFactory.getFrame();
	out.println(frame.printBefore());
	out.println(board.printBefore());
%>

<center>
<form name="journalForm" method="post" action="">
<input type="hidden" name="Action"/>
             <table width="100%" cellpadding="0" cellspacing="0" border="0"><tr>
                  <td>
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%">
                      <tr>
                        <td align="right"><span class="txtlibform"><%=agenda.getString("categoriesDispo")%> :</span></td>
                        <td>&nbsp;</td>
                        <td><span class="txtlibform"><%=agenda.getString("categories")%> :</span></td>
                      </tr>
                      <tr>
                        <td colspan="3"><img border="0" src="icons/1px.gif" width="1" height="15" alt=""/></td>
                      </tr>
                      <tr>
                        <td align="right" valign="top" width="50%">
                          <span class="selectNS">
                          <select name="availableCategories" multiple="multiple" size="10">
                          <%
                            Collection dispoCategories = agenda.getAllCategories();
                            Iterator iC = dispoCategories.iterator();
                            while (iC.hasNext()) {
                              Category category = (Category) iC.next();
                              if  (notIn(category.getId(), categories))
                                out.println("<option value=\"" + category.getId() + "\">" +
                                        category.getName() + "</option>");
                            }
                          %>
                          </select>
                          </span>
                        </td>
                        <td width="1" valign="top" align="center">
                          <table border="0" cellpadding="0" cellspacing="0" width="37">
                            <tr>
                              <td class="intfdcolor" width="37"><a href="javascript:onClick=move_groups('+')"><img src="icons/bt_fleche-d.gif" width="37" height="24" border="0" alt=""/></a><br/><a href="javascript:onClick=move_groups('-')"><img src="icons/bt_fleche-g.gif" width="37" height="24" border="0" alt=""/></a></td>
                            </tr>
                          </table>
                        </td>
                        <td valign="top" width="50%"> <span class="selectNS">
                          <select name="selectedCategories" multiple="multiple" size="10">
                          <%
                            if (categories != null) {
                              Iterator i = categories.iterator();
                              while (i.hasNext()) {
                                Category category = (Category) i.next();
                                out.println("<option value=\"" + category.getId() + "\">" + category.getName() + "</option>");
                              }
                            }

                          %>
                          </select>
                          </span> </td>
                      </tr>
                      <tr>
                        <td>&nbsp;</td>
                      </tr><tr>
                        <td colspan="3"><img border="0" src="icons/1px.gif" width="1" height="4" alt=""/></td>
                      </tr>
                    </table>
      </td>
    </tr>
  </table>
  </form>
<%=separator%>
</center>
<%
	ButtonPane buttonPane = graphicFactory.getButtonPane();
	Button button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=reallyEditCategories()", false);
	buttonPane.addButton(button);
	button = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=window.close()", false);
	buttonPane.addButton(button);
	out.println("<center>"+buttonPane.print()+"</center>");
	out.println(board.printAfter());
    out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</body>
</html>