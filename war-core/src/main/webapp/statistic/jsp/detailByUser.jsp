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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.io.IOException,javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException,
                 java.rmi.RemoteException,javax.ejb.FinderException,java.util.Date"%>
<%@ include file="checkStatistic.jsp" %>

<html>
	<title><%=generalMessage.getString("GML.popupTitle")%></title>
	<%
	    out.println(gef.getLookStyleSheet());
	%>
	<head></head>
	<body>
	<%
	    //initialisation des variables
		String objectId		= request.getParameter("id");
		String componentId 	= request.getParameter("componentId");
	    String userId 		= request.getParameter("userId");
	    String userName		= request.getParameter("userName");
	    String objectType	= request.getParameter("objectType");		
	    StatisticBm statisticBm =  EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);	    
	    ForeignPK foreignPK = new ForeignPK(objectId, componentId);	    
	    Collection readingState = statisticBm.getHistoryByObjectAndUser(foreignPK, 1, objectType, userId);
	   
	    // affichage du nom de l'utilisateur
	    %>
	    <table>
	    	<tr>
	    		<td class="txtlibform" nowrap><%=generalMessage.getString("GML.user")%> :</td>
	    		<td nowrap><%=userName%></td>
		    </tr>
		    <tr>
		    	<td>
		    	<%
			    // affichage des controles de lecture pour l'utilisateur
			    ArrayPane arrayPane = gef.getArrayPane("detailByUser.jsp", "detailByUser.jsp?id="+objectId+"&componentId="+componentId+"&objectType="+objectType+"&userId="+userId+"&userName="+userName, request, session);
			
			    arrayPane.addArrayColumn(messages.getString("statistic.detail"));
			    
			    Iterator it = readingState.iterator();
			    while (it.hasNext())
			    {
			    	ArrayLine ligne = arrayPane.addArrayLine();
			    	
			    	HistoryObjectDetail historyObject = (HistoryObjectDetail) it.next();
			    	
			    	ArrayCellText cell1 = null;
			    	Date haveRead = historyObject.getDate();
			    	String readingDate = DateUtil.getOutputDateAndHour(haveRead, language);
			        cell1 = ligne.addArrayCellText(readingDate);
			        if (haveRead != null)
			        	cell1.setCompareOn(haveRead);
			     }
			    %>
			    </td>
	       </tr>
	    </table>
	    <%	
	    out.println(arrayPane.print());      
	
	%>
	<br>
	<center>
	<%
	  ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(generalMessage.getString("GML.close"), "javascript:window.close()", false));
		out.println(buttonPane.print());
	%>
	</center>
	</body>
</html>
