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
<%@ include file="checkTodo.jsp" %>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>

<%
  String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
  SettingBundle settings = todo.getSettings();

  String action = (String) request.getParameter("Action");
  if (action == null) {
    action = "View";
  }
  else if (action.equals("SetPercent")) {
    String todoId = request.getParameter("ToDoId");
    String percent = request.getParameter("Percent");
    todo.setToDoPercentCompleted(todoId, percent);
    action = "View";
  }
  else if (action.equals("CloseToDo")) {
    String todoId = request.getParameter("ToDoId");
    todo.closeToDo(todoId);
    action = "View";
  }
  else if (action.equals("ReopenToDo")) {
    String todoId = request.getParameter("ToDoId");
    todo.reopenToDo(todoId);
    action = "View";
  }
  else if (action.equals("ViewParticipantTodo")) {
    todo.setViewType(ToDoSessionController.PARTICIPANT_TODO_VIEW);
    action = "View";
  }
  else if (action.equals("ViewOrganizedTodo")) {
    todo.setViewType(ToDoSessionController.ORGANIZER_TODO_VIEW);
    action = "View";
  }
  else if (action.equals("ViewClosedTodo")) {
    todo.setViewType(ToDoSessionController.CLOSED_TODO_VIEW);
    action = "View";
  }
%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<title></title>
<script type="text/javascript">

function viewToDo(todoId) {
  document.todoEditForm.ToDoId.value = todoId;
  document.todoEditForm.Action.value = "Update";
  document.todoEditForm.submit();
}

function setPercent(todoId, percent) {
  document.todoForm.ToDoId.value = todoId;
  document.todoForm.Percent.value = percent;
  document.todoForm.Action.value = "SetPercent";
  document.todoForm.submit();
}

function closeToDo(todoId) {
  document.todoForm.ToDoId.value = todoId;
  document.todoForm.Action.value = "CloseToDo";
  document.todoForm.submit();
}

function reopenToDo(todoId) {
  document.todoForm.ToDoId.value = todoId;
  document.todoForm.Action.value = "ReopenToDo";
  document.todoForm.submit();
}

function addToDo() {
  document.todoEditForm.Action.value = "Add";
  document.todoEditForm.ToDoId.value = "";
  document.todoEditForm.submit();
}

function viewParticipantTodo() {
  document.todoForm.Action.value = "ViewParticipantTodo";
  document.todoForm.submit();
}

function viewOrganizedTodo() {
  document.todoForm.Action.value = "ViewOrganizedTodo";
  document.todoForm.submit();
}

function viewClosedTodo() {
  document.todoForm.Action.value = "ViewClosedTodo";
  document.todoForm.submit();
}

function percentCompletedSet(name, value)
{
  num = -1;
  for (i=0 ; i < document.images.length ; i++) {
    if (document.images[i].name == name + '1')
      num = i;
  }
  if (num != -1) {
    k = 0;
    num--;
    if (value != '0') {
      do {
        num++;
        k++;
        document.images[num].src = "icons/on.gif";
      } while ((document.images[num].name != name + value) && (k < 10))
    }
    while (k < 10) {
      num++;
      k++;
      document.images[num].src = "icons/off.gif";
    }
  }
}

function goTo(baseURL, Id, Type, componentId) {

	jumpToComponent(componentId);

	location.href=baseURL+"searchResult.jsp?Type="+Type+"&Id="+Id;
}

function jumpToComponent(componentId) {

	//Reload DomainsBar
	parent.SpacesBar.document.privateDomainsForm.component_id.value=componentId;
	parent.SpacesBar.document.privateDomainsForm.privateDomain.value="";
	parent.SpacesBar.document.privateDomainsForm.privateSubDomain.value="";
	parent.SpacesBar.document.privateDomainsForm.submit();

	//Reload Topbar
	parent.SpacesBar.reloadTopBar(true);

}

function areYouSure(){
    return confirm("<%=todo.getString("deleteSelectedTodoConfirm")%>");
}

function deleteSelectedToDo() {
	  var boxItems = document.todoCheckForm.todoCheck;
    var selectItems = "";
    if (boxItems != null){
	    // au moins une checkbox existe
	    var nbBox = boxItems.length;
	    if ( (nbBox == null) && (boxItems.checked == true) ){
	            selectItems += boxItems.value;
	    } else{
	      for (i=0;i<boxItems.length ;i++ ){
		  if (boxItems[i].checked == true){
	         selectItems += boxItems[i].value+",";
	        }
	      }
	      selectItems = selectItems.substring(0,selectItems.length-1);
	    }
    }
    if ( (selectItems.length > 0) && (areYouSure()) ) {
	    document.todoCheckForm.action = "DeleteTodo";
	document.todoCheckForm.submit();
    }
}

</script>
</head>
<body>
<form name="todoCheckForm" action="todo.jsp" method="post">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(todo.getString("todo"));

	OperationPane operationPane = window.getOperationPane();

	operationPane.addOperationOfCreation(m_context + "/util/icons/create-action/add-task.png", todo.getString("ajouterTache"), "javascript:onClick=addToDo()");
	operationPane.addOperation(m_context + "/util/icons/delete.gif", todo.getString("deleteSelectedTodo"), "javascript:onClick=deleteSelectedToDo()");

	out.println(window.printBefore());
%>
<view:areaOfOperationOfCreation/>
<%

	TabbedPane tabbedPane = graphicFactory.getTabbedPane();
    tabbedPane.addTab(todo.getString("mesTaches"), "javascript:onClick=viewParticipantTodo()", (todo.getViewType() == ToDoSessionController.PARTICIPANT_TODO_VIEW) );
    tabbedPane.addTab(todo.getString("suiviAffectation"), "javascript:onClick=viewOrganizedTodo()", (todo.getViewType() == ToDoSessionController.ORGANIZER_TODO_VIEW));
    tabbedPane.addTab(todo.getString("historique"), "javascript:onClick=viewClosedTodo()", (todo.getViewType() == ToDoSessionController.CLOSED_TODO_VIEW));
    out.println(tabbedPane.print());

	Frame frame = graphicFactory.getFrame();

	out.println(frame.printBefore());

    ArrayPane arrayPane = graphicFactory.getArrayPane("todoList", pageContext);
    arrayPane.addArrayColumn(todo.getString("nomToDo"));
    arrayPane.addArrayColumn(todo.getString("priorite"));
    if (todo.getViewType() == ToDoSessionController.ORGANIZER_TODO_VIEW)
		arrayPane.addArrayColumn(todo.getString("listeDiffusionCourt"));
    else
        arrayPane.addArrayColumn(todo.getString("organisateurToDo"));
    arrayPane.addArrayColumn(todo.getString("dueDateToDo"));
    arrayPane.addArrayColumn(todo.getString("percentCompletedToDo"));

	ArrayColumn column = arrayPane.addArrayColumn(todo.getString("actions"));
	column.setSortable(false);
	ArrayColumn columnOp = arrayPane.addArrayColumn(todo.getString("GML.operation"));
	columnOp.setSortable(false);

	Collection		todos		= todo.getToDos();
    Iterator		i			= todos.iterator();
    Date			today		= new Date();
    int				j			= 0;
	ToDoHeader		todoHeader	= null;
	ArrayLine		arrayLine	= null;
	Date			todoEndDate = null;
	ComponentInstLight 	componentI 	= null;
	String 			spaceId 	= null;
	String 			spaceLabel 	= "";
	String 			componentLabel = "";
    while (i.hasNext()) {
		todoHeader 	= (ToDoHeader) i.next();
		todoEndDate = todoHeader.getEndDate();
        arrayLine 	= arrayPane.addArrayLine();
        if (todoHeader.getPercentCompleted() < 100)
			if (todoHeader.getCompletedDay() == null) {
				if (todoEndDate != null) {
					if (today.after(todoEndDate)) {
						arrayLine.setStyleSheet("ArrayCellHot");
					}
				}
			}

			if (todoHeader.getExternalId() == null) {
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(todoHeader.getName()), "javascript:onClick=viewToDo('"+todoHeader.getId()+"')");
			} else {
				componentI 		= todo.getComponentInst(todoHeader.getComponentId());
				componentLabel 	= todoHeader.getComponentId();
				if (componentI != null) {
					spaceId 		= componentI.getDomainFatherId();
					SpaceInstLight space = todo.getSpaceInst(spaceId);
					if (space != null)
						spaceLabel 		= space.getName();
					componentLabel 	= componentI.getLabel();
				}
				//Trick for workflow
				String externalId = todoHeader.getExternalId().replace('#', '_');
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(spaceLabel+" > "+componentLabel+" > "+todoHeader.getName()), "javascript:onClick=goTo('" + m_context + URLUtil.getURL(null, spaceId, todoHeader.getComponentId()) + "','"+externalId+"','TodoDetail','"+todoHeader.getComponentId()+"')");
			}

            ArrayCellText cellText = arrayLine.addArrayCellText(todo.getString("priorite"+todoHeader.getPriority().getValue()));
            cellText.setCompareOn(todoHeader.getPriority());
            StringBuffer text = new StringBuffer();
            Collection attendees = todo.getToDoAttendees(todoHeader.getId());
            Iterator att;
            if (todo.getViewType() == ToDoSessionController.ORGANIZER_TODO_VIEW)
            {
              att = attendees.iterator();
              int countAttendees = 0;
              while (att.hasNext()) {
                if (countAttendees > 0)
                  text.append("<br/>");
                if (countAttendees > 2) {
                  text.append("...");
                  break;
				}
                Attendee attendee = (Attendee) att.next();
                UserDetail user = todo.getUserDetail(attendee.getUserId());
                if (user != null)
                  text.append(user.getLastName()).append(" ").append(user.getFirstName());
                else
                  text.append(todo.getString("utilisateurInconnu"));
                countAttendees++;
              }
            } else {
              if (todoHeader.getDelegatorId() != null) {
                UserDetail user = todo.getUserDetail(todoHeader.getDelegatorId());
                if (user != null)
                  text.append(user.getLastName()).append(" ").append(user.getFirstName());
                else
                  text.append(todo.getString("utilisateurInconnu"));
              }
            }
            arrayLine.addArrayCellText(text.toString());
            if (todoEndDate != null) {
              cellText = arrayLine.addArrayCellText(resources.getOutputDate(todoEndDate));
              cellText.setCompareOn(todoEndDate);
            } else {
              arrayLine.addArrayEmptyCell();
            }
            if (todoHeader.getPercentCompleted() == ToDoHeader.PERCENT_UNDEFINED) {
              cellText = arrayLine.addArrayCellText(todo.getString("percentUndefined"));
              cellText.setCompareOn(new Integer(-1));
            } else {
              cellText = arrayLine.addArrayCellText(todoHeader.getPercentCompleted()+ "%");
              cellText.setCompareOn(new Integer(todoHeader.getPercentCompleted()));
            }

			if (todoHeader.getExternalId() == null) {
					text = new StringBuffer();
					if (todo.getViewType() == ToDoSessionController.PARTICIPANT_TODO_VIEW) {
						for (int k=1; k<=10;k++) {
							text.append("<a href=\"javascript:onclick=setPercent('").append(todoHeader.getId()).append("','");
							text.append(String.valueOf(k*10)).append("')\" onmouseout=\"percentCompletedSet('percentCompleted");
							text.append(String.valueOf(j)).append("_','").append(todoHeader.getPercentCompleted()/10);
							text.append("')\" onmouseover=\"percentCompletedSet('percentCompleted").append(String.valueOf(j)).append("_', '");
							text.append(String.valueOf(k)).append("')\"><img width=\"5\" height=\"5\" name=\"percentCompleted");
							text.append(String.valueOf(j)).append("_").append(String.valueOf(k)).append("\" border=\"0\" src=\"icons/");
								if (k*10 > todoHeader.getPercentCompleted())
									text.append("off.gif");
								else
									text.append("on.gif");
								text.append("\" alt=\""+String.valueOf(k*10)+"%\" title=\""+String.valueOf(k*10)+"%\"/></a>");
						}
					} else {
						if (todo.getViewType() == ToDoSessionController.ORGANIZER_TODO_VIEW) {
							text.append("<a href=\"javascript:onclick=closeToDo('").append(todoHeader.getId()).append("')\">").append("<img width=\"15\" height=\"15\" border=\"0\" src=\"icons/unlock.gif\" alt=\""+(todo.getString("cadenas_ouvert"))+"\" title=\""+(todo.getString("cadenas_ouvert"))+"\"/>" ).append("</a>");
						}
						else if (todoHeader.getDelegatorId().equals(todo.getUserId()))
						{
							text.append("<a href=\"javascript:onclick=reopenToDo('").append(todoHeader.getId()).append("')\">").append("<img width=\"15\" height=\"15\" border=\"0\" src=\"icons/lock.gif\" alt=\""+(todo.getString("cadenas_clos"))+"\" title=\""+(todo.getString("cadenas_clos"))+"\"/>" ).append("</a>");
						}
						else text.append("&nbsp;");
					}
					j++;

					arrayLine.addArrayCellText(text.toString());
					if (todoHeader.getDelegatorId().equals(todo.getUserId())) {//je peux supprimer les taches manuelles dont je suis l'organisateur (le créateur)
					  arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"todoCheck\" value=\""+todoHeader.getId()+"\"/>");
					} else {
					 arrayLine.addArrayCellText("");
					}
			} else {
			  arrayLine.addArrayCellText("");
			  att = attendees.iterator();
			  Attendee firstAttendee = (Attendee) att.next();
			  if (attendees.size() == 1 && todoHeader.getDelegatorId().equals(firstAttendee.getUserId())) {//je peux supprimer les taches automatiques dont je suis l'initiateur (le créateur) et le responsable
			     arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"todoCheck\" value=\""+todoHeader.getId()+"\"/>");
			  } else {
			    arrayLine.addArrayCellText("");
			  }
			}
        }
				out.println(arrayPane.print());
			out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</form>

<form name="todoEditForm" action="todoEdit.jsp" method="post">
  <input type="hidden" name="ToDoId"/>
  <input type="hidden" name="Action"/>
</form>

<form name="todoForm" action="todo.jsp" method="post">
  <input type="hidden" name="ToDoId"/>
  <input type="hidden" name="Percent"/>
  <input type="hidden" name="Action"/>
</form>

<form name="searchForm" action="agenda.jsp" method="post">
  <input type="hidden" name="query"/>
</form>

</body>
</html>
