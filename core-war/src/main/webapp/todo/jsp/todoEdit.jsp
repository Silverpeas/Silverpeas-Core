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

<%@page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.calendar.model.Attendee"%>
<%@ page import="org.silverpeas.core.calendar.model.Classification" %>
<%@ page import="org.silverpeas.core.calendar.model.Priority" %>
<%@ page import="org.silverpeas.core.calendar.model.ToDoHeader"%>
<%@ page import="org.silverpeas.web.todo.control.TodoUserException" %>
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.core.util.EncodeHelper" %>

<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>

<%@ include file="checkTodo.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%

  String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
  String action = request.getParameter("Action");
  LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(todo.getLanguage());
  SettingBundle settings = todo.getSettings();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel withCheckFormScript="true"/>
  <view:includePlugin name="datepicker"/>
<script type="text/javascript">

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

  if (isWhitespace(document.todoEditForm.Name.value)) {
    errorMsg += "  - '<%=todo.getString("nomToDo")%>' <%=todo.getString("MustContainsText")%>\n";
    errorNb++;
  }

  if (!isValidTextArea(document.todoEditForm.Description)) {
    errorMsg +=
        "  - '<%=todo.getString("descriptionToDo")%>' <%=todo.getString("ContainsTooLargeText")+todo.getString("NbMaxTextArea")+todo.getString("Characters")%>\n";
    errorNb++;
  }

  var dateErrors = isPeriodValid('StartDate', 'EndDate');
  $(dateErrors).each(function(index, error) {
    errorMsg += "  - " + error.message + "\n";
    errorNb++;
  });

  var result;
  switch (errorNb) {
    case 0 :
      result = true;
      break;
    case 1 :
      errorMsg =
          "<%=todo.getString("ThisFormContains")%> 1 <%=todo.getString("Error")%> : \n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
    default :
      errorMsg = "<%=todo.getString("ThisFormContains")%> " + errorNb +
          " <%=todo.getString("Errors")%> :\n" + errorMsg;
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
</script>

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
  Collection<Attendee> attendees = todo.getCurrentAttendees();

  /* todo == null : premier acces a la page */
  if (todoHeader == null) {
    String toDoId = request.getParameter("ToDoId");

    if (toDoId != null) {
      if (toDoId.length() == 0) {
        toDoId = null;
      }
    }

    /* Update et premier acces a la page */
    if (toDoId != null) {
      todoHeader = todo.getToDoHeader(toDoId);
      attendees = todo.getToDoAttendees(toDoId);
    }

    /* Add et premier acces a la page */
    else {
      todoHeader = new ToDoHeader("", todo.getUserId());
      attendees = new ArrayList<Attendee>();
      // the current organizer
       attendees.add(new Attendee(todo.getUserId()));
    }

    todo.setCurrentToDoHeader(todoHeader);
    todo.setCurrentAttendees(attendees);
  }

  /* todo != null */
  else {

        if (action.equals("View") || action.equals("EditDiffusionList")) {

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

                 <script language="javascript" type="text/javascript">
                         SP_openWindow('diffusion.jsp','diffusion','750','550','scrollbars=yes, resizable, alwaysRaised');
                 </script>
                 <%
                }
        }
  } //fin else  %>
  </head>

  <%
  /* ReallyAdd || ReallyUpdate */
  if (action.equals("ReallyAdd") || action.equals("ReallyUpdate")) {
    String name = request.getParameter("Name");
    String description = request.getParameter("Description");
    String priority = request.getParameter("Priority");
    String classification = request.getParameter("Classification");
    String startDate = request.getParameter("StartDate");
    if (startDate == null) {
        startDate = "";
    }
    String startHour = request.getParameter("StartHour");
    String startMinute = request.getParameter("StartMinute");
    String endDate = request.getParameter("EndDate");
    if (endDate == null) {
        endDate = "";
    }
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
                Iterator<Attendee> i = todo.getCurrentAttendees().iterator();
                int j = 0;
                while (i.hasNext()) {
                        Attendee attendee = i.next();
                        selectedUsers[j] = attendee.getUserId();
                        j++;
                }

                /* ReallyAdd */
                // now diffusion list can be empty
                if (todoHeader.getId() == null) {
                        String id = todo.addToDo(name, description, priority, classification, date1, startHour, date2, endHour, percent);
                        todo.setToDoAttendees(id, selectedUsers);
                        out.println("<body onload=gotoToDo()>");
                        out.println("</body>");
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
                        out.println("<body onload=gotoToDo()>");
                        out.println("</body>");
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
    out.println("<body onload=gotoToDo()>");
    out.println("</body>");
    out.println("</html>");
    return;
  }

%>
<body onload="document.todoEditForm.Name.focus();">
<form name="todoEditForm" action="todoEdit.jsp" method="post">

<%
        Window window = graphicFactory.getWindow();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setComponentName(todo.getString("todo"),"todo.jsp");
        browseBar.setPath(todo.getString("editionTodo"));

        OperationPane operationPane = window.getOperationPane();

        if (todoHeader.getId() != null && todoHeader.getDelegatorId().equals(todo.getUserId()) && toPrint == null) {

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
    out.println("<br/>");
    out.println("<br/>");
                out.println("<div align=\"center\">");
                Button button = graphicFactory.getFormButton(todo.getString("retour"), "javascript:onClick=history.back()", false);
    out.print(button.print());
                out.println("</div>");
  }

  /* Add || Update || View || DiffusionListOK */
  else if (action.equals("Update") || action.equals("Add") || action.equals("View") || action.equals("DiffusionListOK")) {
%>
<center>
<view:board>
                <table border="0" cellspacing="0" cellpadding="5">
                        <tr>
                                <td class="txtlibform"><%=todo.getString("organisateurToDo")%></td>
                                        <td class="txtnav">
                                                <%
                                                        if (todoHeader.getDelegatorId() != null) {
                                                                UserDetail user = todo.getUserDetail(todoHeader.getDelegatorId());
                                                                if (user != null)
                                                                        out.print(user.getDisplayedName());
                                                                else
                                                                        out.println(todo.getString("utilisateurInconnu"));
                                                        }
                                                %>
                                </td>
                        </tr>
                        <tr>
                                <td class="txtlibform"><%=todo.getString("nomToDo")%></td>
                                <td>
                                        <input type="text" name="Name" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" <%
                                                if (todoHeader.getName() != null)
                                                        out.print("value=\""+EncodeHelper.javaStringToHtmlString(todoHeader.getName())+"\" ");
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>/>&nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5"/>
                                </td>
                        </tr>
                        <tr>
                                <td class="txtlibform"><%=todo.getString("percentCompletedToDo")%></td>
                                <td>
                                        <select name="PercentCompleted">
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
                                                                out.println("<option selected=\"selected\" value=\""+v+"\">" + s);
                                                        else
                                                                out.println("<option value=\""+v+"\">" + s);
                                                }
                                        %>
                                        </select>
                                </td>
                        </tr>
                        <tr>
                                <td class="txtlibform"><%=todo.getString("descriptionToDo")%></td>
                                <td><textarea name="Description" rows="6" cols="49" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId()))
                                                        out.print("disabled ");
                                                %>><%if (todoHeader.getDescription() != null) out.print(EncodeHelper.javaStringToHtmlString(todoHeader.getDescription()));%></textarea></td>
                        </tr>
                        <tr>

                                <td class="txtlibform"><label for="StartDate" class="txtlibform"><%=todo.getString("dateDebutToDo")%></label></td>
                                <td>
                                        <input type="text" name="StartDate" id="StartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId())) {
													out.print("disabled ");
												} else {
													out.print("class=\"dateToPick\" ");
												}
                                                if (todoHeader != null)
                                                        if (todoHeader.getStartDate() != null)
                                                                out.println("value=\""+resources.getInputDate(todoHeader.getStartDate())+"\"");%>/>

                                        <%if (todo.getUserId().equals(todoHeader.getDelegatorId())) { %>
                                                <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
                                        <%}%>
                                        </td>
                                </tr>
                                <tr>
                                <td class="txtlibform"><label for="EndDate" class="txtlibform"><%=todo.getString("dateFinToDo")%></label></td>
                                <td>
                                                <input type="text" name="EndDate" id="EndDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                                                if (! todo.getUserId().equals(todoHeader.getDelegatorId())) {
													out.print("disabled ");
												} else {
													out.print("class=\"dateToPick\" ");
												}
                                                if (todoHeader != null) {
                                                        if (todoHeader.getEndDay() != null) {
                                                                if (todoHeader.getStartDay() == null)
                                                                        out.print("value=\""+resources.getInputDate(todoHeader.getEndDate())+"\"");
                                                                else if (! todoHeader.getEndDay().equals(todoHeader.getStartDay()))
                                                                        out.print("value=\""+resources.getInputDate(todoHeader.getEndDate())+"\"");
                                                        }
                                                }
                                                %>/>
                                                <%if (todo.getUserId().equals(todoHeader.getDelegatorId())) { %>
                                                        <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
                                                <%}%>
                                </td>
                        </tr>
                        <tr>
                                <td class="txtlibform"><%=todo.getString("classification")%></td>
                                <td>
                                        <select name="Classification" <%
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
                                                                out.println("<option selected=\"selected\" value=\"" + classifications[i] +"\">" + todo.getString(classifications[i])+"</option>");
                                                        else
                                                                out.println("<option value=\"" + classifications[i] +"\">" + todo.getString(classifications[i])+"</option>");
                                                }
                                        %>
                                        </select>
                                </td>
                         </tr>
                         <tr>
                                <td class="txtlibform"><%=todo.getString("priorite")%></td>
                                <td>
                                        <select name="Priority" <%
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
                                                                out.println("<option selected=\"selected\" value=\"" + priorities[i] +"\">" + todo.getString("priorite" + priorities[i])+"</option>");
                                                        else
                                                                out.println("<option value=\"" + priorities[i] +"\">" + todo.getString("priorite" +priorities[i])+"</option>");
                                                }
                                        %>
                                        </select>
                                </td>
                        </tr>
                        <tr>
                                <td class="txtlibform"><%=todo.getString("listeDiffusion")%></td>
                                        <td>
                                                <table width="100%" cellspacing="1" border="0" cellpadding="1"><!--tablliste-->
                                                <%
                                                        if (attendees != null) {
                                                                if (attendees.size() == 0) {
                                                                        out.print("<tr><td>");
                                                                        out.print(todo.getString("listeDiffusionVide"));
                                                                        out.println("</td></tr>");
                                                                }
                                                                Iterator<Attendee> i = attendees.iterator();
                                                                while (i.hasNext()) {
                                                                        out.println("<tr><td>");
                                                                        Attendee attendee = i.next();
                                                                        UserDetail user = todo.getUserDetail(attendee.getUserId());
                                                                        if (user != null)
                                                                                out.print("&nbsp;" + user.getDisplayedName() + " ");
                                                                        else
                                                                                out.println(todo.getString("utilisateurInconnu"));
                                                                        out.println("</td>");
                                                                        out.println("</tr>");
                                                                }
                                                        }
                                                %>
                                                </table><!--endTablListe-->
                                </td>
                                        </tr>
                                <tr>
                                        <td colspan="2" nowrap="nowrap"><span class="txt"><img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5"/> : <%=todo.getString("mandatoryFields")%></span>
                                        </td>
                        </tr>

                </table>
</view:board>
</center>
<%
out.println(frame.printMiddle());
%>
                <input type="hidden" name="Action"/>
                <br/>
                <center>
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
				</center>

<%
        } //fin du if
        else out.println("Erreur : Action inconnu = '"+ action+"'");
%>

<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>

</form>
</body>
</html>