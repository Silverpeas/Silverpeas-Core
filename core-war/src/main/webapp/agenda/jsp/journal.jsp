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

<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ include file="checkAgenda.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  String journalId = request.getParameter("JournalId");
  boolean autoFocus = true;
  boolean readOnly = false;

  LocalizationBundle generalMessage =
			ResourceLocator.getGeneralLocalizationBundle(agenda.getLanguage());
	SettingBundle settings = agenda.getSettings();

  String action = request.getParameter("Action"); //Add || Update || View (vient de choisir une categorie)
													// || DiffusionListOK (vient de choisir des participants dans le UserPanel)
													// ReallyAdd || ReallyUpdate
													// CategoryOK (viens de choisir des cat�gories)

	if (!StringUtil.isDefined(action))
		action = "Update";

	String fromCategories 	= (String) request.getAttribute("FromCategories");
	String fromGuests		= request.getParameter("DiffusionListOK");
	if (action.equals("Add"))
	{
	  agenda.setCurrentCategories(new ArrayList());
	  agenda.setCurrentAttendees(new ArrayList());
	}
	if (action.equals("Update") && !StringUtil.isDefined(fromCategories) && !StringUtil.isDefined(fromGuests))
	{
	  Collection categories = agenda.getJournalCategories(journalId);
	  Collection guests = agenda.getJournalAttendees(journalId);
	  agenda.setCurrentCategories(categories);
	  agenda.setCurrentAttendees(guests);
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript">
var dayWin = window ;
var freeBusyWin = window ;
var diffusion = window ;
var category = window ;

// funtion to return to agenda

function gotoAgenda()
{
  if (freeBusyWin === "Free_Busy_Win" )
    freeBusyWin.close();
  if (dayWin === "calendrier_agenda" )
    dayWin.close();
  if ( diffusion === "diffusion" )
    diffusion.close();
  if ( category === "category" )
    category.close();
  window.location.replace("agenda.jsp");
}

// function for journal

function editDiffusionList()
{
	document.journalForm.Action.value = "EditDiffusionList";
	if (document.journalForm.CompleteDay.checked) {
		document.journalForm.WithoutHour.value = "true";
	}
	document.journalForm.submit();
}

function editCategory()
{
  document.journalForm.Action.value = "EditCategory";
  if (document.journalForm.CompleteDay.checked) {
    document.journalForm.WithoutHour.value = "true";
  }
  document.journalForm.submit();
}

function isCorrectForm() {
	var errorMsg = "";
	var errorNb = 0;
	var beginDate = document.journalForm.StartDate.value;
  var endDate = document.journalForm.EndDate.value;
  var yearBegin = extractYear(beginDate, '<%=agenda.getLanguage()%>');
  var monthBegin = extractMonth(beginDate, '<%=agenda.getLanguage()%>');
		var dayBegin = extractDay(beginDate, '<%=agenda.getLanguage()%>');

		var yearEnd = extractYear(endDate, '<%=agenda.getLanguage()%>');
		var monthEnd = extractMonth(endDate, '<%=agenda.getLanguage()%>');
		var dayEnd = extractDay(endDate, '<%=agenda.getLanguage()%>');

		var beginDateOK = false;
		var endDateOK = false;

		index = document.journalForm.StartHour.selectedIndex;
		var hourBegin = document.journalForm.StartHour.options[index].value;
		index = document.journalForm.StartMinute.selectedIndex;
		var minuteBegin = document.journalForm.StartMinute.options[index].value;
		index = document.journalForm.EndHour.selectedIndex;
		var hourEnd = document.journalForm.EndHour.options[index].value;
		index = document.journalForm.EndMinute.selectedIndex;
		var minuteEnd = document.journalForm.EndMinute.options[index].value;

		var hourMinuteBegin = hourBegin+minuteBegin;
		var hourMinuteEnd = hourEnd+minuteEnd;

		if (isWhitespace(document.journalForm.Name.value)) {
	           errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("nomNote")%>' <%=agenda.getString("MustContainsText")%>\n";
	           errorNb++;
	    }

	     if (isWhitespace(document.journalForm.StartDate.value)) {
			errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateDebutNote")%>' <%=agenda.getString("MustContainsText")%>\n";
			errorNb++;
						}

	     if (!isValidTextArea(document.journalForm.Description)) {
			errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("descriptionNote")%>' <%=agenda.getString("ContainsTooLargeText")+agenda.getString("NbMaxTextArea")+agenda.getString("Characters")%>\n";
			errorNb++;
						}

        if (! isWhitespace(beginDate)) {
		if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
			errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateDebutNote")%>' <%=agenda.getString("MustContainsCorrectDate")%>\n";
			errorNb++;
		}
		else beginDateOK = true;
        }

        if (! isWhitespace(endDate)) {
		if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
			errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateFinNote")%>' <%=agenda.getString("MustContainsCorrectDate")%>\n";
			errorNb++;
		}
		else endDateOK = true;
        }

        /* comme si on avait les dates identiques */
        else {

		if (! document.journalForm.CompleteDay.checked) {
			if( hourMinuteEnd < hourMinuteBegin) {
				errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("heureFinNote")%>' <%=agenda.getString("MustContainsPostDateToBeginDate")%>\n";
	                errorNb++;
			}
	        }
        }

        if (beginDateOK && endDateOK) {
			if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
				errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateFinNote")%>' <%=agenda.getString("MustContainsPostDateToBeginDate")%>\n";
                    errorNb++;
			}

			/* les 2 dates sont identiques */
			else if ((yearEnd == yearBegin) && (monthEnd == monthBegin) && (dayEnd == dayBegin)) {

				if (! document.journalForm.CompleteDay.checked) {
					if( hourMinuteEnd < hourMinuteBegin) {
						errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("heureFinNote")%>' <%=agenda.getString("MustContainsPostHourToBeginHour")%>\n";
				errorNb++;
					}
				}
			}
        }

     switch(errorNb)
     {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=agenda.getString("ThisFormContains")%> 1 <%=agenda.getString("Error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=agenda.getString("ThisFormContains")%> " + errorNb + " <%=agenda.getString("Errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     document.journalForm.EndMinute.disabled = false;
				document.journalForm.StartMinute.disabled = false;

     return result;

}

function reallyAdd()
{
	if (isCorrectForm()) {
		if (document.journalForm.CompleteDay.checked) {
			document.journalForm.WithoutHour.value = "true";
		}
		document.journalForm.Action.value = "ReallyAdd";
		document.journalForm.submit();
	}
}

function reallyUpdate()
{
    if (isCorrectForm()) {
		if (document.journalForm.CompleteDay.checked) {
			document.journalForm.WithoutHour.value = "true";
		}
		document.journalForm.Action.value = "ReallyUpdate";
		document.journalForm.submit();
    }

}

function deleteConfirm(name)
{
    if (window.confirm("<%=agenda.getString("confirmationSuppressionNote")%> '" + name + "' ?")){
          document.journalForm.Action.value = "ReallyRemove";
          if (document.journalForm.CompleteDay.checked) {
			document.journalForm.WithoutHour.value = "true";
		  }
          document.journalForm.submit();
    }
}

function viewFreeBusyTime(formIndex)
{
		if ( freeBusyWin == "Free_Busy_Win" )
			freeBusyWin.close();
    var date = document.journalForm.elements[formIndex].value;
    if ((date != null) && (date.length > 0 )) {
	    freeBusyWin = window.SP_openWindow('busyTime.jsp?Form=' + formIndex + '&amp;Date=' + date, 'Free_Busy_Win',800, 220,'alwaysRaised,scrollbars=yes,resizable');
    } else {
	    freeBusyWin = window.SP_openWindow('busyTime.jsp?Form=' + formIndex, 'Free_Busy_Win',800, 220,'alwaysRaised,scrollbars=yes,resizable');
    }
}

function hourModified()
{
  document.journalForm.CompleteDay.checked = false;

  if (document.journalForm.StartHour.value == '<%=settings.getString("endHour")%>')
				document.journalForm.StartMinute.disabled = true;
  else
				document.journalForm.StartMinute.disabled = false;

  if (document.journalForm.EndHour.value == '<%=settings.getString("endHour")%>')
				document.journalForm.EndMinute.disabled = true;
  else
				document.journalForm.EndMinute.disabled = false;
}

function setParticipationStatus(journalId, userId, status)
{
  document.journalForm.Action.value = "SetParticipationStatus";
  document.journalForm.JournalId.value = journalId;
  document.journalForm.UserId.value = userId;
  document.journalForm.Status.value = status;
  if (document.journalForm.CompleteDay.checked) {
		document.journalForm.WithoutHour.value = "true";
  }
  document.journalForm.submit();
}

function setBeginDateAndHour(date, hour, minutes)
{
	  document.getElementById("StartDate").value = date;
	  document.getElementById("StartHour").value = hour;
	  document.getElementById("StartMinute").value = minutes;
}

</script>
</head>

<%

  String hour = null;
  String eHour = null;

  /* Add || Update : Ajout ou Modification de l'evenement */
  if (action.equals("Add") || action.equals("Update")) {

    agenda.setCurrentJournalHeader(null);
    hour = request.getParameter("Hour"); //vient de la selection d'une heure dans l'agenda

    if (StringUtil.isDefined(hour)) {
      try {
        eHour = String.valueOf(new Integer(hour).intValue() + 1);
        if (hour.length() < 2) hour = "0"+ hour;
        if (eHour.length() < 2) eHour = "0"+ eHour;
        hour = hour + ":00";
        eHour = eHour + ":00";
      }
      catch (Exception e) {
        hour = null;
        eHour = null;
      }
    }

	else {
      try {
	hour = settings.getString("beginHour");

        eHour = String.valueOf(new Integer(hour).intValue() + 1);
        if (hour.length() < 2) hour = "0"+ hour;
        if (eHour.length() < 2) eHour = "0"+ eHour;
        hour = hour + ":00";
        eHour = eHour + ":00";
      }
      catch (Exception e) {
        hour = null;
        eHour = null;
      }
	}
  }

  JournalHeader journal = agenda.getCurrentJournalHeader();
  Collection attendees = agenda.getCurrentAttendees();
  Collection categories = agenda.getCurrentCategories();

  /* journal == null : premier acces a la page */
  if (journal == null) {
	/* Update et premier acces a la page */
    if (journalId != null) {
      if (journalId.length() > 0) {
        journal = agenda.getJournalHeader(journalId);
        agenda.selectDay(DateUtil.dateToString(journal.getStartDate(), agenda.getLanguage()));
      }
      else {
        journal = new JournalHeader("", agenda.getUserId());
        journal.setStartDate(agenda.getCurrentDay());
        if (hour != null) {
          journal.setStartHour(hour);
          journal.setEndHour(eHour);
        }
      }
    }

    /* Add et premier acces a la page */
    else {
      journal = new JournalHeader("", agenda.getUserId());
      journal.setStartDate(agenda.getCurrentDay());
      if (hour != null) {
        journal.setStartHour(hour);
        journal.setEndHour(eHour);
      }
    }
    agenda.setCurrentJournalHeader(journal);
  }
  /* journal != null */
  else {

	if ( (action.equals("View") || action.equals("EditDiffusionList")) || action.equals("EditCategory") )
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
	    String withoutHour = request.getParameter("WithoutHour"); //true ou false

	    journal.setName(name);
	    journal.setDescription(description);
	    journal.getClassification().setString(classification);
	    journal.getPriority().setValue(new Integer(priority).intValue());

	    try {
			Date start = resources.getDate(startDate);
		journal.setStartDate(start);
	    }
	    catch (Exception e) {
	      journal.setStartDate(null);
	    }

	    try {
			Date end = resources.getDate(endDate);
		journal.setEndDate(end);
	    }
	    catch (Exception e) {
	      journal.setEndDate(null);
	    }

	      if (withoutHour.equals("true")) {//journee complete
		startHour = null;
	        endHour =null;
	      }
	      else {
		startHour = startHour + ":" + startMinute;
	        endHour   = endHour   + ":" + endMinute;
	      }

	      journal.setStartHour(startHour);
	      journal.setEndHour(endHour);

	    agenda.setCurrentJournalHeader(journal);

	    if (action.equals("EditDiffusionList")) {
		//routage vers le UserPanel
		action = "View";
            autoFocus = false;
		%>

			<script type="text/javascript">
				SP_openWindow('diffusion.jsp','diffusion','750','550','scrollbars=yes, resizable, alwaysRaised');
			</Script>
			<%
		}

	    if (action.equals("EditCategory")) {
	      action = "View";
	      autoFocus = false;
	      %>
	      <script type="text/javascript">
	           window.SP_openWindow('category.jsp','category',500, 320,'alwaysRaised,scrollbars=yes,resizable')
	      </Script>
	      <%
	    }

	}

}//fin du else

  if (! agenda.getUserId().equals(journal.getDelegatorId()))
     readOnly = true;

  String toPrint = null;

  /* ReallyAdd || ReallyUpdate */
  if ((action.equals("ReallyAdd")) || (action.equals("ReallyUpdate"))) {
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

    try {
			Date date1;
			try {
				date1 = resources.getDate(startDate);
			} catch (java.text.ParseException e) {
				throw new AgendaUserException("dateDebutErreur");
			}

			Date date2;
			try {
			if (endDate.trim().length() == 0)
				date2 = date1;
			else
				date2 = resources.getDate(endDate);
		}
		catch (java.text.ParseException e) {
			throw new AgendaUserException("dateFinIncorrecte");
		}

	      if (withoutHour.equals("true")) {//journee complete
		startHour = null;
	        endHour =null;
	      }
	      else {
		startHour = startHour + ":" + startMinute;
	        endHour   = endHour   + ":" + endMinute;
	      }

	      String[] selectedUsers = new String[agenda.getCurrentAttendees().size()];
	      Iterator i = agenda.getCurrentAttendees().iterator();
	      int j = 0;
	      while (i.hasNext()) {
	        Attendee attendee = (Attendee) i.next();
	        selectedUsers[j] = attendee.getUserId();
	        j++;
	      }

	      String[] selectedCategories = new String[agenda.getCurrentCategories().size()];
	      i = agenda.getCurrentCategories().iterator();
	      j = 0;
	      while (i.hasNext()) {
	        Category category = (Category) i.next();
	        selectedCategories[j] = category.getId();
	        j++;
	      }

			if (journal.getId() == null) /* ReallyAdd */
			{
				journalId = agenda.addJournal(name, description, priority, classification, date1, startHour, date2, endHour);
			}
			else /* ReallyUpdate */
			{
				agenda.updateJournal(journalId, name, description, priority, classification, date1, startHour, date2, endHour);
			}
			agenda.setJournalAttendees(journalId, selectedUsers);
			agenda.setJournalCategories(journalId, selectedCategories);
			out.println("<body onLoad=gotoAgenda()>");
			out.println("</body>");
			out.println("</html>");
			return;
    }
    catch (AgendaUserException e) {
      toPrint = agenda.getString("saisieErreur")+ agenda.getString(e.getMessage());
    }

  }

  /* ReallyRemove */
  else if (action.equals("ReallyRemove")) {
    journalId = request.getParameter("JournalId");
    agenda.removeJournal(journalId);
    out.println("<body BGCOLOR=FFFFFF MARGINWIDTH=5 MARGINHEIGHT=5 LEFTMARGIN=5 TOPMARGIN=5 onLoad=gotoAgenda()>");
    out.println("</body>");
    out.println("</html>");
    return;
  }

  /* SetParticipationStatus */
  else if (action.equals("SetParticipationStatus")) {
    String userId = request.getParameter("UserId");
    String status = request.getParameter("Status");
    journalId = request.getParameter("JournalId");
    agenda.setJournalParticipationStatus(journalId, userId, status);
    attendees = agenda.getJournalAttendees(journalId);
    action = "View";
  }
%>
<%
if (autoFocus && !readOnly)
{
%>
<body id="agenda" onload="document.forms[0].Name.focus();">
<%
}
else
{
%>
<body id="agenda">
<%
}
%>

<form name="journalForm" action="journal.jsp" method="post">
      <input type="hidden" name="Action"/>
      <input type="hidden" name="UserId"/>
      <input type="hidden" name="Status"/>
      <input type="hidden" name="selectedCategories"/>
      <input type="hidden" name="JournalId" <%if (journal.getId() != null) out.println("value=\""+journal.getId()+"\"");%>/>

    <!-- Barre de titre du composant -->

<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"), "agenda.jsp");
	browseBar.setPath(agenda.getString("editerNote"));

		if (agenda.getUserId().equals(journal.getDelegatorId()))
		{
			OperationPane operationPane = window.getOperationPane();
			String formIndex = "7";
			if (journal.getDelegatorId().equals(agenda.getUserId()))
				formIndex = "6";
			if (journal.getId() != null) {
				operationPane.addOperation(agendaDelSrc,
					agenda.getString("supprimerNote"),
					"javascript:onClick=deleteConfirm('"+ Encode.javaStringToHtmlString(Encode.javaStringToJsString(journal.getName()))+"')"
				);
				operationPane.addLine();
			}
			operationPane.addOperation(agendaChronoSrc,agenda.getString("trouverCreneau"),"javascript:onClick=viewFreeBusyTime('"+formIndex+"')");
            operationPane.addLine();
			operationPane.addOperation(agendaAssignmentSrc,agenda.getString("editionListeDiffusion"),"javascript:onClick=editDiffusionList()");
			operationPane.addLine();
			operationPane.addOperation(agendaCategorySrc,agenda.getString("editionCategories"),"javascript:onClick=editCategory()");

	  }

	out.println(window.printBefore());

    Frame frame=graphicFactory.getFrame();
    out.println(frame.printBefore());
    out.println(board.printBefore());

%>
<table width="98%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td valign="top" width="100%">

<%
  if (toPrint != null) {
    out.println(toPrint);
    out.println("<BR/>");
    out.println("<BR/>");
		Button button = graphicFactory.getFormButton(agenda.getString("retour"), "javascript:onClick=history.back()", false);
    out.print(button.print());
  }

  /* Add || Update || View || DiffusionListOK */
  else if (action.equals("Update") || action.equals("Add") || action.equals("View")
      || action.equals("DiffusionListOK") || action.equals("CategoryOK")) {
    %>

      <table width="100%" cellpadding="3" border="0">
          <tr>
            <td class="txtlibform"><%= agenda.getString("organisateurNote") %> :</td>
			<td width="100%">
              <%
                if (journal.getDelegatorId() != null) {
                  UserDetail user = agenda.getUserDetail(journal.getDelegatorId());
                  if (user != null)
			%>
                    <span class="txtnav"><view:username userId="<%=user.getId()%>"/></span>
              <% } else { %>
                    <span class="txtnav"><%= agenda.getString("utilisateurInconnu") %></span>
              <% } %>
            </td>
          </tr>
		  <tr>
            <td class="txtlibform"><%= agenda.getString("nomNote") %> :</td>
			<td><input type="text" name="Name" size="62" maxlength="<%=DBUtil.getTextFieldLength()%>" <% if (journal.getName() != null) out.println("value=\""+Encode.javaStringToHtmlString(journal.getName())+"\"");%> <% if (readOnly) out.print("disabled");%>/>&nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align="top" alt=""/></td>
          </tr>
		  <tr>
            <td class="txtlibform"><%=agenda.getString("descriptionNote")%> :</td>
	        <td><textarea name="Description" rows="6" cols="60"  <% if (readOnly) out.print("disabled=\"disabled\"");%>><%if (journal.getDescription() != null) out.println(Encode.javaStringToHtmlString(journal.getDescription()));%></textarea></td>
          </tr>
		  <tr>
		    <!-- affichage de la date de d�but de note -->
            <td class="txtlibform"><%= agenda.getString("dateDebutNote") %> :</td>
			<td><input type="text" class="dateToPick" name="StartDate" id="StartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                if (journal != null)
                  if (journal.getStartDate() != null)
                    out.println("value=\""+resources.getInputDate(journal.getStartDate())+"\"");%> <% if (readOnly) out.print("disabled=\"disabled\"");%>/>&nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align="bottom" alt=""/> <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
			</td>
		  </tr>
		  <tr>
			<td class="txtlibform"><%=agenda.getString("heureDebutNote")%> :</td>
			<td><select name="StartHour" id="StartHour" onchange="javascript:hourModified();"  <% if (readOnly) out.print("disabled=\"disabled\"");%>>
              <%
			boolean readOnlyMinute = false;
                int beginHour = new Integer(settings.getString("beginHour")).intValue();
                int endHour = new Integer(settings.getString("endHour")).intValue();
                for (int i = beginHour; i <= endHour; i++) {
                  String s = String.valueOf(i);
                  boolean selected = false;
                  if (s.length() == 1) s = "0"+s;
                  if (journal != null)
                  {
                    if (journal.getStartHour() != null)
                    {
                      if (journal.getStartHour().startsWith(s))
                      {
                        selected = true;
						                  if (i==endHour)
												readOnlyMinute = true;
						                }
						              }
						            }
                  if (selected)
                    out.println("<option value='"+s+"' selected=\"selected\">" + s+"</option>");
                  else
                    out.println("<option value='"+s+"'>" + s+"</option>");
                }
              %>
              </select>
              :
              <select name="StartMinute" id="StartMinute" onchange="javascript:hourModified();"  <% if (readOnly || readOnlyMinute) out.print("disabled=\"disabled\"");%>>
              <%
                for (int i = 0; i < 4; i++) {
                  String s = String.valueOf(i * 15);
                  boolean selected = false;
                  if (s.length() == 1) s = "0"+s;
                  if (journal != null)
                    if (journal.getStartHour() != null)
                      if (journal.getStartHour().endsWith(s))
                        selected = true;
                  if (selected)
                    out.println("<option value='"+s+"' selected=\"selected\">" + s+"</option>");
                  else
                    out.println("<option value='"+s+"'>" + s+"</option>");
                }
              %>
              </select></td>
		  </tr>
		  <tr>
			<td class="txtlibform"><%=agenda.getString("dateFinNote")%> :</td>
			<td><input type="text" class="dateToPick" name="EndDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>" <%
                if (journal != null) {
                  if (journal.getEndDay() != null) {
                    if (journal.getStartDay() == null)
                      out.println("value=\""+resources.getInputDate(journal.getEndDate())+"\"");
                    else if (! journal.getEndDay().equals(journal.getStartDay()))
                      out.println("value=\""+resources.getInputDate(journal.getEndDate())+"\"");
                  }
                } %> <% if (readOnly) out.print("disabled=\"disabled\"");%>/> <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></td>
		  </tr>
		  <tr>
			<td class="txtlibform"><%=agenda.getString("heureFinNote")%> :</td>
			<td><select name="EndHour" onchange="javascript:hourModified();" <% if (readOnly) out.print("disabled=\"disabled\"");%>>
              <%
                for (int i = beginHour; i <= endHour; i++) {
                  String s = String.valueOf(i);
                  boolean selected = false;
                  if (s.length() == 1) s = "0"+s;
                  if (journal != null)
                  {
                    if (journal.getEndHour() != null)
                    {
                      if (journal.getEndHour().startsWith(s))
                      {
                        selected = true;
                        if (i==endHour)
										readOnlyMinute = true;
						}
				}
                  }
                  if (selected)
                    out.println("<option value='"+s+"' selected=\"selected\">" + s+"</option>");
                  else
                    out.println("<option value='"+s+"'>" + s+"</option>");
                }
              %>
              </select>
              :
              <select name="EndMinute" onchange="javascript:hourModified();" <% if (readOnly || readOnlyMinute) out.print("disabled=\"disabled\"");%>>
              <%
                for (int i = 0; i < 4; i++) {
                  String s = String.valueOf(i * 15);
                  boolean selected = false;
                  if (s.length() == 1) s = "0"+s;
                  if (journal != null)
                    if (journal.getEndHour() != null)
                      if (journal.getEndHour().endsWith(s))
                        selected = true;
                  if (selected)
                    out.println("<option value='"+s+"' selected=\"selected\">" + s+"</option>");
                  else
                    out.println("<option value='"+s+"'>" + s+"</option>");
                }
              %>
              </select></td>
		  </tr>
		  <tr>
			<td class="txtlibform"><%=agenda.getString("sansHoraireSpecifique")%> :</td>
			<td><input type="checkbox" name="CompleteDay"
              <%
                if (journal.getStartHour() == null && journal.getEndHour() == null)
			out.println("checked=\"checked\"");
		if (readOnly)
			out.print("disabled=\"disabled\"");%>/>
			</td>
		  </tr>
	      <tr>
			<td class="txtlibform"><%=agenda.getString("classification")%> :</td>
			<td><select name="Classification" <% if (readOnly) out.print("disabled=\"disabled\"");%>>
              <%
                String[] classifications = Classification.getAllClassificationsWithoutConfidential();
                for (int i = 0; i < classifications.length; i++) {
                  boolean selected =false;
                  if (journal != null)
                    if (journal.getClassification().getString().equals(classifications[i]))
                      selected = true;
                  if (selected)
                    out.println("<option selected=\"selected\" value=\"" + classifications[i] +"\">" + agenda.getString(classifications[i])+"</option>");
                  else
                    out.println("<option value=\"" + classifications[i] +"\">" + agenda.getString(classifications[i])+"</option>");
                }
              %>
              </select></td>
		  </tr>
		  <tr>
			<td class="txtlibform"><%=agenda.getString("priorite")%> :</td>
			<td><select name="Priority" <% if (readOnly) out.print("disabled=\"disabled\"");%>>
              <%
                int[] priorities = Priority.getAllPriorities();
                for (int i = 0; i < priorities.length; i++) {
                  boolean selected =false;
                  if (journal != null)
                    if (journal.getPriority().getValue() == priorities[i])
                      selected = true;
                  if (selected)
                    out.println("<option selected=\"selected\" value=\"" + priorities[i] +"\">" + agenda.getString("priorite" + priorities[i])+"</option>");
                  else
                    out.println("<option value=\"" + priorities[i] +"\">" + agenda.getString("priorite" +priorities[i])+"</option>");
                }
              %>
              </select></td>
		  </tr>
          <tr>
			<td class="txtlibform" nowrap="nowrap">
              <%=agenda.getString("listeDiffusion")%> :
            </td>
			<td>
			<%
			if (attendees != null)
			{
		if (attendees.size() == 0)
		{
	                out.print(agenda.getString("listeDiffusionVide"));
	            }
				else
				{
					out.println("<table width=\"100%\" cellspacing=\"1\" border=\"0\" cellpadding=\"1\">");

                    Iterator i = attendees.iterator();
                    while (i.hasNext())
                    {
			out.println("<tr><td nowrap=\"nowrap\">");
                        Attendee attendee = (Attendee) i.next();
                        UserDetail user = agenda.getUserDetail(attendee.getUserId());
                        out.print("<img src=\"icons/pixel_CCCCCC.gif\" width=\"5\" height=\"5\" alt=\"\"/>");
                        if (user != null)
				out.print("&nbsp;"+user.getDisplayedName()+"&nbsp;");
                        else
                           out.println(agenda.getString("utilisateurInconnu"));
                        out.println("</td><td width=\"100%\">");
                        if (attendee.getUserId().equals(agenda.getUserId()) && journal.getId() != null)
                        {
                           out.println("<select name=\"ParticipationStatus\" onchange=\"javascript:setParticipationStatus('"+
                              journal.getId() + "', '" + attendee.getUserId()+"', document.journalForm.ParticipationStatus.value);\">");
                           String[] all = ParticipationStatus.getJournalParticipationStatus();
                           for (int iP = 0; iP < all.length; iP++)
                           {
				 out.print("<option value=\""+all[iP]+"\"");
                             if (all[iP].equals(attendee.getParticipationStatus().getString())) {
                               out.print(" selected=\"selected\"");
                             }
                             out.print(">" + agenda.getString(all[iP]));
                             out.println("</option>");
                           }
                           out.println("</select>");
                         }
                         else
                         {
				out.print(agenda.getString(attendee.getParticipationStatus().getString()));
                         }
                         out.println("</td></tr>");
                    }
                    out.println("</table>");
		}
			}
			%>
            </td>
			</tr>
			<tr>
            <td class="txtlibform"><%=agenda.getString("listeCategories")%> :</td>
			<td>
                <%
                    if (categories != null) {
                      if (categories.size() == 0) {
				out.print(agenda.getString("listeCategoriesVide"));
                      }
                      else
                      {
	                      Iterator i = categories.iterator();
	                      while (i.hasNext()) {
	                        Category category = (Category) i.next();
							out.print(category.getName() + "<br/>");
	                      }
                      }
                    }
                %>
			 </td>
			 </tr>
		<tr>
		  <td colspan="2" nowrap="nowrap">
		<span class="txtlnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" alt="<%=generalMessage.getString("GML.requiredField")%>"/>&nbsp;:&nbsp;<%=generalMessage.getString("GML.requiredField")%>) <img src="icons/1px.gif" width="20" height="1" alt=""/></span>
          </td>
        </tr>
        </table>
<%
  }

  else
	out.println("Erreur : Action inconnu = '"+ action+"'");
%>

  </td>
  </tr>
 </table>
<%
		out.println(board.printAfter());

		if (toPrint == null) {
			ButtonPane buttonPane = graphicFactory.getButtonPane();
		if (agenda.getUserId().equals(journal.getDelegatorId()) ) {
				Button button = null;
				if (journal.getId() != null)
					button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=reallyUpdate()", false);
				else
					button = graphicFactory.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=reallyAdd()", false);

				Button buttonCancel = graphicFactory.getFormButton(generalMessage.getString("GML.cancel"), "javascript:onClick=gotoAgenda()", false);

				buttonPane.addButton(button);
				buttonPane.addButton(buttonCancel);
		} else {
			Button button = graphicFactory.getFormButton(agenda.getString("retour"), "javascript:onClick=gotoAgenda()", false);
			buttonPane.addButton(button);
		}
		out.print("<br/><center>"+buttonPane.print()+"</center>");
		}

		out.println(frame.printAfter());
	    out.println(window.printAfter());
%>
	<input type="hidden" name="WithoutHour" value="false"/>
</form>

</body>
</html>