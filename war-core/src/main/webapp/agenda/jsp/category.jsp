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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.agenda.view.*"%>
<%@ page import="com.stratelia.webactiv.calendar.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>

<%@ include file="checkAgenda.jsp.inc" %>

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
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(agenda.getLanguage());
%>

<HTML>
<HEAD>
<% out.println(graphicFactory.getLookStyleSheet()); %>
<SCRIPT LANGUAGE="JAVASCRIPT" SRC="<%=javaScriptSrc%>"></SCRIPT>

<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>
<Script language="JavaScript">
function reallyEditCategories()
{
      ValidateCategories();
      window.opener.document.journalForm.action = "ReallyEditCategories";
			var valueCategories = "";      
      for (var i=0; i<document.journalForm.selectedCategories.length; i++)
	      valueCategories = valueCategories + document.journalForm.selectedCategories[i].value + ",";
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

function ValidateCategories() {
    nbr = document.journalForm.selectedCategories.length;
    for (j=0;j < nbr;j++)
        document.journalForm.selectedCategories[j].selected = true;
}
</script>
</HEAD>

<%
  String action = null;
  ResourceLocator settings = agenda.getSettings();
  JournalHeader journal = agenda.getCurrentJournalHeader();
  Collection categories = agenda.getCurrentCategories();
  
%>
<BODY id="agenda">
<%
	Window window = graphicFactory.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(agenda.getString("editionCategories"));
	out.println(window.printBefore());
	Frame frame=graphicFactory.getFrame();
  out.println(frame.printBefore());

%>

<CENTER>
<table border="0" align="center" width="98%" cellspacing="2" cellpadding="5" class="intfdcolor">
<FORM NAME="journalForm" METHOD=POST >
        <tr valign="top">
          <td width="100%" align="center" class="intfdcolor4">
	       <input type="hidden" name="Action"> 
             <table width="100%" cellpadding="0" cellspacing="0" border="0"><tr>
                  <td class="intfdcolor4"> 
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%">
                      <tr> 
                        <td class="intfdcolor4" align="right"><span class="txtlibform"><%=agenda.getString("categoriesDispo")%> :</span></td>
                        <td class="intfdcolor4">&nbsp;</td>
                        <td class="intfdcolor4"><span class="txtlibform"><%=agenda.getString("categories")%> :</span></td>
                      </tr>
                      <tr> 
                        <td colspan="3" class="intfdcolor4"><img border="0" src="icons/1px.gif" width="1" height="15"></td>
                      </tr>
                      <tr> 
                        <td class="intfdcolor4" align="right" valign="top" width="50%"> 
                          <span class="selectNS">
                          <select name="availableCategories" multiple size="10">
                            
                          <%
                            Collection dispoCategories = agenda.getAllCategories();
                            Iterator iC = dispoCategories.iterator();
                            while (iC.hasNext()) {
                              Category category = (Category) iC.next();
                              if  (notIn(category.getId(), categories))
                                out.println("<option value=" + category.getId() + ">" + 
                                        category.getName() + "</option>");
                            }
                          %>
                          </select>
                          </span>
                        </td>
                        <td width="1" valign="top" align="center" class="intfdcolor4"> 
                          <table border="0" cellpadding="0" cellspacing="0" width="37">
                            <tr> 
                              <td class="intfdcolor" width="37"><a href="javascript:onClick=move_groups('+')"><image src="icons/bt_fleche-d.gif" width="37" height="24" border="0"></A><a href="javascript:onClick=move_groups('-')"><image src="icons/bt_fleche-g.gif" width="37" height="24" border="0"></A></td>
                            </tr>
                          </table>
                        </td>
                        <td class="intfdcolor4" valign="top" width="50%"> <span class="selectNS">
                          <select name="selectedCategories" multiple size="10">
                          <%
                          
                            if (categories != null) {
                              Iterator i = categories.iterator();
                              while (i.hasNext()) {
                                Category category = (Category) i.next();
                                out.println("<option value=" + category.getId() + ">" + category.getName() + "</option>");
                              }
                            }
                          
                          %>
                          </select>
                          </span> </td>
                      </tr>
                      <tr>
                        <td>&nbsp;</td>
                      </tr><tr> 
                        <td colspan="3" class="intfdcolor4"><img border="0" src="icons/1px.gif" width="1" height="4"></td>
                      </tr>
                    </table>
                  </td>
                </tr></table>
      </td>
    </tr>
</FORM>
  </table>
<%=separator%>		  
   <table width="100%" cellpadding=0 cellspacing=0 border=0>
      <tr>
        <td align="right">
      <%
      Button button = null;
      button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=reallyEditCategories()", false);
      out.print(button.print());
      %>
        </td>
				<td>&nbsp;</td>
        <td align="left">
      <%
      button = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=window.close()", false);
      out.print(button.print());
      %>
        </td>
       </tr>
    </table>
</CENTER>
<%
	out.println(frame.printMiddle());
    out.println(separator);
    out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>


