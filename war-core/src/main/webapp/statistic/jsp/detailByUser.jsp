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
		
	    StatisticBmHome statisticHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
	    StatisticBm statisticBm =  statisticHome.create();
	    
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
			    // affichage des contrôles de lecture pour l'utilisateur
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
		buttonPane.addButton((Button) gef.getFormButton(generalMessage.getString("GML.close"), "javascript:window.close()", false));
		out.println(buttonPane.print());
	%>
	</center>
	</body>
</html>
