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
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
<%@ page import="com.stratelia.webactiv.calendar.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.homepage.HomePageUtil"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.webactiv.todo.control.TodoUserException"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.util.EncodeHelper" %>

<%@ include file="checkTodo.jsp" %>

<%

  String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
  String action;
  ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(todo.getLanguage());
  ResourceLocator settings = todo.getSettings();

  action = request.getParameter("Action"); 
%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
%>
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>

<TITLE>_________________/ Silverpeas - Corporate portal organizer \_________________/</TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<Script language="JavaScript">

function gotoToDo()
{
  if (window.dayWin != null)
    window.dayWin.close();
  if (window.diffusion != null)
    window.diffusion.close();
  window.location.replace("todo.jsp");
}

function isCorrectForm() {
        var errorMsg = "";
        var errorNb = 0;
        var beginDate = document.todoEditForm.StartDate.value;
        var endDate = document.todoEditForm.EndDate.value;
        var yearBegin = extractYear(beginDate, '<%=todo.getLanguage()%>');
        var monthBegin = extractMonth(beginDate, '<%=todo.getLanguage()%>');
                var dayBegin = extractDay(beginDate, '<%=todo.getLanguage()%>');
                
                var yearEnd = extractYear(endDate, '<%=todo.getLanguage()%>'); 
                var monthEnd = extractMonth(endDate, '<%=todo.getLanguage()%>');
                var dayEnd = extractDay(endDate, '<%=todo.getLanguage()%>'); 
                
                var beginDateOK = false;
                var endDateOK = false;
                
                if (isWhitespace(document.todoEditForm.Name.value)) {
                   errorMsg+="  - '<%=todo.getString("nomToDo")%>' <%=todo.getString("MustContainsText")%>\n";
                   errorNb++; 
            }

                if (!isValidTextArea(document.todoEditForm.Description)) {
                        errorMsg+="  - '<%=todo.getString("descriptionToDo")%>' <%=todo.getString("ContainsTooLargeText")+todo.getString("NbMaxTextArea")+todo.getString("Characters")%>\n";
                        errorNb++;              
                }           
       
        if (! isWhitespace(beginDate)) {
                if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
                        errorMsg+="  - '<%=todo.getString("dateDebutToDo")%>' <%=todo.getString("MustContainsCorrectDate")%>\n";
                        errorNb++;
                }
                else beginDateOK = true;
        }
              
        if (! isWhitespace(endDate)) {
                if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                        errorMsg+="  - '<%=todo.getString("dateFinToDo")%>' <%=todo.getString("MustContainsCorrectDate")%>\n";
                        errorNb++;
                }
                else endDateOK = true;
        }
        
        if (beginDateOK && endDateOK) {
                        if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
                                errorMsg+="  - '<%=todo.getString("dateFinToDo")%>' <%=todo.getString("MustContainsPostDateToBeginDate")%>\n";
                    errorNb++;  
                        }
        }    
              
        
     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=todo.getString("ThisFormContains")%> 1 <%=todo.getString("Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=todo.getString("ThisFormContains")%> " + errorNb + " <%=todo.getString("Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;     
     
}


function reallyAdd()
{
        if (isCorrectForm()) {
        document.todoEditForm.Action.value = "ReallyAdd";
        document.todoEditForm.submit();
        }
}

function reallyUpdate()
{
	document.todoEditForm.Name.disabled = false;
    document.todoEditForm.PercentCompleted.disabled = false;
    document.todoEditForm.Description.disabled = false;
    document.todoEditForm.StartDate.disabled = false;
    document.todoEditForm.EndDate.disabled = false;
    document.todoEditForm.Classification.disabled = false;
    document.todoEditForm.Priority.disabled = false;     
    if (isCorrectForm()) {
     document.todoEditForm.Action.value = "ReallyUpdate";
     document.todoEditForm.submit();
    }
}

function deleteConfirm(name)
{
    if (window.confirm("<%=todo.getString("todoDeleteConfirm")%> '" + name + "' ?")){
          document.todoEditForm.Action.value = "ReallyRemove";
          document.todoEditForm.submit();
    }
}

function test(){
        document.todoEditForm.Action.value = "EditDiffusionList";
        document.todoEditForm.Name.disabled = false;
        document.todoEditForm.PercentCompleted.disabled = false;
        document.todoEditForm.Description.disabled = false;
        document.todoEditForm.StartDate.disabled = false;
        document.todoEditForm.EndDate.disabled = false;
        document.todoEditForm.Classification.disabled = false;
        document.todoEditForm.Priority.disabled = false;        
        document.todoEditForm.submit(); 
}

</SCRIPT>
</HEAD>

<%

  if (action == null) {
    action = "View";
  }
  String toPrint = null;


  if (action.equals("Add") || action.equals("Update")) {
    todo.setCurrentToDoHeader(null);
    todo.setCurrentAttendees(null);
  }
  
  ToDoHeader todoHeader = todo.getCurrentToDoHeader();
  Collection attendees = todo.getCurrentAttendees();

  /* todo == null : premier acces a la page */
  if (todoHeader == null) {
    String toDoId = request.getParameter("ToDoId");
    
    if (toDoId != null)
      if (toDoId.length() == 0)
        toDoId = null;
    
    /* Update et premier acces a la page */
    if (toDoId != null) {
      todoHeader = todo.getToDoHeader(toDoId);
      attendees = todo.getToDoAttendees(toDoId);
    } 
    
    /* Add et premier acces a la page */
    else {
      todoHeader = new ToDoHeader("", todo.getUserId());
      attendees = new ArrayList();
      // the current organizer
       attendees.add(new Attendee(todo.getUserId()));
    }
    
    todo.setCurrentToDoHeader(todoHeader);
    todo.setCurrentAttendees(attendees);
  }   
  
  /* todo != null */  
  else {
  
        if ( (action.equals("View")) || (action.equals("EditDiffusionList")) )
        {
            
            //sauvegarde des valeurs saisies
            String name = request.getParameter("Name");
            String description = request.getParameter("Description");
            String priority = request.getParameter("Priority");
            String classification = request.getParameter("Classification");
            String startDate = request.getParameter("StartDate");
            String startHour = request.getParameter("StartHour");
            String startMinute = request.getParameter("StartMinute");
            String endDate = request.getParameter("EndDate");
            String endHour = request.getParameter("EndHour");
            String endMinute = request.getParameter("EndMinute");
            String withoutHour = request.getParameter("WithoutHour");
            String percent = request.getParameter("PercentCompleted");
        
            todoHeader.setName(name);
            todoHeader.setDescription(description);
            todoHeader.getClassification().setString(classification);
            
            try {
              todoHeader.setPercentCompleted(new Integer(percent).intValue());
            } 
            catch (Exception e) {
                        throw new TodoUserException("pourcentErreur");
            }
            
            try {
                Date start = DateUtil.stringToDate(startDate, todo.getLanguage());
                todoHeader.setStartDate(start);
            }
            catch (Exception e) {
              todoHeader.setStartDate(null);
            }
            
            
            try {
                Date end = DateUtil.stringToDate(endDate, todo.getLanguage());
                todoHeader.setEndDate(end);
            }
            catch (Exception e) {
              todoHeader.setEndDate(null);
            }
        
            todo.setCurrentToDoHeader(todoHeader);
            
            
            if (action.equals("EditDiffusionList")) {
                //routage vers le UserPanel
                action = "View";
                %>
           
                 <Script language="JavaScript">
                         SP_openWindow('diffusion.jsp','diffusion','750','550','scrollbars=yes, resizable, alwaysRaised');
                 </Script>
                 <%  
                } 
        }
  } //fin else  
  
  /* ReallyAdd || ReallyUpdate */ 
  if ((action.equals("ReallyAdd")) || (action.equals("ReallyUpdate"))) {
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    String priority = request.getParameter("Priority");
    String classification = request.getParameter("Classification");
    String startDate = request.getParameter("StartDate");
    if (startDate == null) 
        startDate = "";
    String startHour = request.getParameter("StartHour");
    String startMinute = request.getParameter("StartMinute");
    String endDate = request.getParameter("EndDate");
    if (endDate == null) 
        endDate = "";
    String endHour = request.getParameter("EndHour");
    String endMinute = request.getParameter("EndMinute");
    String withoutHour = request.getParameter("WithoutHour");
    String percent = request.getParameter("PercentCompleted");
    

    try {
                Date date1 = null;
                if (name == null)
                        throw new Exception("nameErreur");
                else if (name.length() == 0)
                        throw new Exception("nameErreur");
                        
                try {
                        if (startDate.trim().length() > 0) 
                                date1 = DateUtil.stringToDate(startDate, todo.getLanguage());
                } 
                catch (java.text.ParseException e) {
                        throw new Exception("dateDebutErreur");
                }
                
                Date date2 = null;

                try {
                  if (endDate.trim().length() == 0) 
                        date2 = date1;
                  else
                                date2 = DateUtil.stringToDate(endDate, todo.getLanguage());
                } 
                catch (java.text.ParseException e) {
                        throw new Exception("dateFinIncorrecte");
                }
                                
                startHour = null;
                endHour =null;
                
                String[] selectedUsers = new String[todo.getCurrentAttendees().size()];
                Iterator i = todo.getCurrentAttendees().iterator();
                int j = 0;
                while (i.hasNext()) {
                        Attendee attendee = (Attendee) i.next();
                        selectedUsers[j] = attendee.getUserId();
                        j++;
                }

                /* ReallyAdd */
                // now diffusion list can be empty
                if (todoHeader.getId() == null) {
                        String id = todo.addToDo(name, description, priority, classification, date1, startHour, date2, endHour, percent);
                        todo.setToDoAttendees(id, selectedUsers);
                        out.println("<BODY onLoad=gotoToDo()>");
                        out.println("</BODY>");
                        out.println("</html>");
                        return;
                }
                
                /* ReallyUpdate */
                else {
                        if (todo.getUserId().equals(todoHeader.getDelegatorId())) {
                                todo.updateToDo(todoHeader.getId(), name, description, priority, classification, date1, startHour, date2, endHour, percent);
                                todo.setToDoAttendees(todoHeader.getId(), selectedUsers);
                        }
                        else {
                                todo.setToDoPercentCompleted(todoHeader.getId(), percent);
                                todo.setToDoAttendees(todoHeader.getId(), selectedUsers);
                        }
                        out.println("<BODY onLoad=gotoToDo()>");
                        out.println("</BODY>");
                        out.println("</html>");
                        return;
                }
  
    }
    catch (Exception e) {
            toPrint =  todo.getString("saisieErreur")+ todo.getString(e.getMessage());
    }
  } 
  
  /* ReallyRemove */
  else if (action.equals("ReallyRemove")) {
    todo.removeToDo(todoHeader.getId());
    out.println("<BODY onLoad=gotoToDo()>");
    out.println("</BODY>");
    out.println("</html>");
    return;
  }  
    
%>

<BODY onLoad="document.todoEditForm.Name.focus();">
<FORM NAME="todoEditForm" ACTION="todoEdit.jsp" METHOD=POST >

<%
        Window window = graphicFactory.getWindow();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setComponentName(todo.getString("todo"),"todo.jsp");


        browseBar.setPath(todo.getString("editionTodo"));

        OperationPane operationPane = window.getOperationPane();

        if ( (todoHeader.getId() != null) && 
           (todoHeader.getDelegatorId().equals(todo.getUserId())) &&
           (toPrint == null) ) {
           
            // en cas de modification
                operationPane.addOperation(m_context + "/util/icons/task_del.gif",
                        todo.getString("supprimerTodo"),
                        "javascript:onClick=deleteConfirm('"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(todoHeader.getName()))+"')"
                );
        operationPane.addOperation(m_context + "/util/icons/task_assignment.gif",
                        todo.getString("modifierTodo"),
                        "javascript:test();"
                );
                
        } else {
        
            // en creation
                operationPane.addOperation(m_context + "/util/icons/task_assignment.gif",
                        todo.getString("affecterTodo"),
                        "javascript:test();"
                );
        
        }

        out.println(window.printBefore());
        Frame frame = graphicFactory.getFrame();
        out.println(frame.printBefore());

  if (toPrint != null) {
    out.println(toPrint);
    out.println("<BR>");
    out.println("<BR>");
                out.println("<div align=\"center\">");
                Button button = graphicFactory.getFormButton(todo.getString("retour"), "javascript:onClick=history.back()", false);
    out.print(button.print());
                out.println("</div>");
  }

  /* Add || Update || View || DiffusionListOK */  
  else if ((action.equals("Update")) || (action.equals("Add")) || (action.equals("View")) || (action.equals("DiffusionListOK"))) {      
%>      
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr> 
        <td nowrap>
                <table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
                        <tr align=center> 

                                <td  class="intfdcolor4" valign="baseline" align=left> <span class="txtlibform"><%=todo.getString("organisateurToDo")%> :</span></td>
                                        <td  class="intfdcolor4" align=left valign="baseline"><span class="txtnav">
                                                <%
                                                        if (todoHeader.getDelegatorId() != null) {
                                                                UserDetail user = todo.getUserDetail(todoHeader.getDelegatorId());
                                                                if (user != null)
                                                                        out.print(user.getLastName() + " " + user.getFirstName());
                                                                else
                                                                        out.println(todo.getString("utilisateurInconnu"));
                                                        }
                                                %>
                                        </span>
                                </td>
                        </tr>
                        <tr align=center>
                        
                                <td class="intfdcolor4" valign="baseline" align=left><span class="txtlibform"><%=todo.getString("nomToDo")%> :</span></td>
                                <td class="intfdcolor4" align=left valign="baseline">
                                        <input type="text" name="Name" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" <%
                                                if (todoHeader.getName() != null) 
                                                        out.print("VALUE=\""+EncodeHelper.javaStringToHtmlString(todoHeader.getName())+"\" ");
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>>&nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5">
                                </td>
                        </tr>
                        <tr align=center>

                                <td class="intfdcolor4" valign="baseline" align=left><span class="txtlibform"><%=todo.getString("percentCompletedToDo")%> :</span></td>
                                <td class="intfdcolor4" align=left valign="baseline">
                                        <SELECT name="PercentCompleted">
                                        <%
                                                for (int i = -1; i <= 10; i++) {
                                                        String s;
                                                        int v;
                                                        
                                                        v = i*10;
                                                        if (v < 0)
                                                                s = todo.getString("percentUndefined");
                                                        else
                                                                s = String.valueOf(v);

                                                        boolean selected = false;
                                                        if (todoHeader != null) 
                                                                if ((todoHeader.getPercentCompleted() >= v) && (todoHeader.getPercentCompleted() < v + 10))
                                                                                selected = true;
                                                        if (v < 0) v = ToDoHeader.PERCENT_UNDEFINED;
                                                        if (selected)
                                                                out.println("<OPTION SELECTED value=\""+v+"\">" + s);
                                                        else
                                                                out.println("<OPTION value=\""+v+"\">" + s);
                                                }
                                        %>
                                        </SELECT>
                                </td>
                        </tr>
                        <tr align=center> 

                                <td class="intfdcolor4"  valign="top" align=left><span class="txtlibform"><%=todo.getString("descriptionToDo")%> :</span>&nbsp;</td>
                                <td class="intfdcolor4"  align=left valign="baseline"><font size=1><textarea name="Description" wrap="VIRTUAL" rows="6" cols="49" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>><%if (todoHeader.getDescription() != null) out.print(EncodeHelper.javaStringToHtmlString(todoHeader.getDescription()));%></textarea></font>
                                </td>
                        </tr>
                        <tr align=center>

                                <td class="intfdcolor4" nowrap valign="baseline" align=left><span class="txtlibform"><%=todo.getString("dateDebutToDo")%> :</span>&nbsp;
                                        </td>
                                <td class="intfdcolor4" nowrap valign="baseline" align=left>
                                        <input type="text" name="StartDate" id="StartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId())) {
													out.print("disabled ");
												} else {
													out.print("class=\"dateToPick\" ");
												}
                                                if (todoHeader != null) 
                                                        if (todoHeader.getStartDate() != null)
                                                                out.println("VALUE=\""+resources.getInputDate(todoHeader.getStartDate())+"\"");%>>

                                        <%if (todo.getUserId().equals(todoHeader.getDelegatorId())) { %>
                                                <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
                                        <%}%>
                                        </td>
                                </tr>
                                <tr align=center>
                                <td nowrap class="intfdcolor4" valign="baseline" align=left> 
                                        <span class="txtnote"><span class="txtlibform"><%=todo.getString("dateFinToDo")%> :</span>&nbsp;
                                                </span>
                                </td>
                                <td class="intfdcolor4" nowrap valign="baseline" align=left>
                                                <input type="text" name="EndDate" id="EndDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId())) {
													out.print("disabled ");
												} else {
													out.print("class=\"dateToPick\" ");
												}
                                                if (todoHeader != null) {
                                                        if (todoHeader.getEndDay() != null) {
                                                                if (todoHeader.getStartDay() == null)
                                                                        out.print("VALUE=\""+resources.getInputDate(todoHeader.getEndDate())+"\"");
                                                                else if (! todoHeader.getEndDay().equals(todoHeader.getStartDay()))
                                                                        out.print("VALUE=\""+resources.getInputDate(todoHeader.getEndDate())+"\"");
                                                        }
                                                }
                                                %>>
                                                <%if (todo.getUserId().equals(todoHeader.getDelegatorId())) { %>
                                                        <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></p>
                                                <%}%>
                                </td>
                        </tr>
                        <tr align=center> 

                                <td class="intfdcolor4" valign="baseline" align=left><span class="txtlibform"><%=todo.getString("classification")%> :</span>&nbsp;
                                </td>
                                <td class="intfdcolor4" nowrap valign="baseline" align=left>
                                        <SELECT name="Classification" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>>
                                        <%
                                                String[] classifications = Classification.getAllClassificationsWithoutConfidential();
                                                for (int i = 0; i < classifications.length; i++) {
                                                        boolean selected =false;
                                                        if (todoHeader != null)
                                                                if (todoHeader.getClassification().getString().equals(classifications[i]))
                                                                        selected = true;
                                                        if (selected)
                                                                out.println("<OPTION SELECTED VALUE=\"" + classifications[i] +"\">" + todo.getString(classifications[i]));
                                                        else
                                                                out.println("<OPTION VALUE=\"" + classifications[i] +"\">" + todo.getString(classifications[i]));
                                                }
                                        %>
                                        </SELECT>
                                </td>
                                <tr align=center>
                                <td class="intfdcolor4" valign="baseline" align=left><span class="txtlibform"><%=todo.getString("priorite")%> :</span>&nbsp;
                                </td>
                                <td class="intfdcolor4" nowrap valign="baseline" align=left>
                                        <SELECT name="Priority" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>>
                                        <%
                                                int[] priorities = Priority.getAllPriorities();
                                                for (int i = 0; i < priorities.length; i++) {
                                                        boolean selected =false;
                                                        if (todoHeader != null)
                                                                if (todoHeader.getPriority().getValue() == priorities[i])
                                                                        selected = true;
                                                        if (selected)
                                                                out.println("<OPTION SELECTED VALUE=\"" + priorities[i] +"\">" + todo.getString("priorite" + priorities[i]));
                                                        else
                                                                out.println("<OPTION VALUE=\"" + priorities[i] +"\">" + todo.getString("priorite" +priorities[i]));
                                                }
                                        %>
                                        </SELECT>
                                </td>
                        </tr>
                        
                        <tr align=center>

                                <td valign="top" class="intfdcolor4" nowrap align=left>
                                <% if (todo.getUserId().equals(todoHeader.getDelegatorId())) { %>

                                <% }%>
                                         <span class="txtlibform"><%=todo.getString("listeDiffusion")%> 
                                        :</span> 
                                        &nbsp;
                                        </td>
                                        <td valign="baseline" class="intfdcolor4" nowrap align=left>
                                        <span class="selectNS">
                                                <TABLE width="100%" cellspacing="1" border="0" cellpadding="1"><!--tablliste-->
                                                <%
                                                        if (attendees != null) {
                                                                if (attendees.size() == 0) {
                                                                        out.print("<TR><TD>");
                                                                        out.print(todo.getString("listeDiffusionVide"));
                                                                        out.println("</TD></TR>");
                                                                }
                                                                Iterator i = attendees.iterator();
                                                                while (i.hasNext()) {
                                                                        out.println("<TR><TD>");
                                                                        Attendee attendee = (Attendee) i.next();
                                                                        UserDetail user = todo.getUserDetail(attendee.getUserId());
                                                                        if (user != null)
                                                                                out.print("&nbsp;" + user.getLastName() + " " + user.getFirstName() + " "); 
                                                                        else
                                                                                out.println(todo.getString("utilisateurInconnu"));
                                                                        out.println("</TD>");
                                                                        out.println("</TR>");
                                                                }
                                                        }
                                                %>
                                                </table><!--endTablListe-->
                                        </span>
                                        
                                </td>
                                        </tr>
                                <tr align=center nowrap>
                                 
                                        <td class="intfdcolor4" valign="top" align=left colspan=2 nowrap><span class="txt">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5"> : <%=todo.getString("mandatoryFields")%>) </span> 
                                        </td>
                        </tr>
                                
                </table>
        </td>
</tr>

</table></center>
<%
out.println(frame.printMiddle());
%>
                <input type="hidden" name="Action">
                <br>
                <center><table width="100%" cellpadding=2 cellspacing=0 border=0>
                        <tr align=center>
                                <td>
                                        <%
                                        ButtonPane pane = graphicFactory.getButtonPane();
                                        Button button = null;
                                        if (todoHeader.getId() != null) 
                                                button = graphicFactory.getFormButton(todo.getString("mettreAJour"), "javascript:onClick=reallyUpdate()", false);
                                        else
                                                button = graphicFactory.getFormButton(todo.getString("ajouter"), "javascript:onClick=reallyAdd()", false);

                                        pane.addButton(button);
                                        
                                        button = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=gotoToDo()", false);

                                        pane.addButton(button);                                                                 
                                        out.println(pane.print());
                                        %>
                                </td>
                        </tr>
                </table></center>
                                        
<%
        } //fin du if
        else out.println("Erreur : Action inconnu = '"+ action+"'");
%>

<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>

</FORM>

 <FORM NAME="searchForm" ACTION="todo.jsp" METHOD=POST >
  <input type="hidden" name="query">
</FORM>

</BODY>

</HTML>


