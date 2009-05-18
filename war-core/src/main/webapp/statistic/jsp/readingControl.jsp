<%@ page import="java.io.IOException,javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException,
                 java.rmi.RemoteException,javax.ejb.FinderException,java.util.Date"%>
<%@ include file="checkStatistic.jsp" %>

<%
    //initialisation des variables
    String id 			= request.getParameter("id");
    String userId 		= request.getParameter("userId");
    String url 			= request.getParameter("url");
    String componentId 	= request.getParameter("componentId");
    String objectType	= request.getParameter("objectType");
    %>
    <script language="javascript">
    function editDetail(userId, actorName)
    {
        SP_openWindow("<%=m_context%>/statistic/jsp/detailByUser.jsp?id=<%=id%>&userId="+userId+"&userName="+actorName+"&componentId=<%=componentId%>&objectType=<%=objectType%>", "blank", "280", "330","scrollbars=no, resizable, alwaysRaised");
    }
    </script>
    <%    
    StatisticBmHome statisticHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
    StatisticBm statisticBm =  statisticHome.create();
    
    ForeignPK foreignPK = new ForeignPK(id, componentId);
    Collection readingState = statisticBm.getHistoryByObject(foreignPK, 1, objectType);
    
    // affichage des contrôles de lecture
    ArrayPane arrayPane = gef.getArrayPane("readingControl", "ReadingControl", request, session);

    arrayPane.addArrayColumn(generalMessage.getString("GML.user"));
    arrayPane.addArrayColumn(messages.getString("statistic.lastAccess"));
    arrayPane.addArrayColumn(messages.getString("statistic.nbAccess"));
    arrayPane.addArrayColumn(messages.getString("statistic.detail"));
    
    Iterator it = readingState.iterator();
    while (it.hasNext())
    {
    	ArrayLine ligne = arrayPane.addArrayLine();
    	
    	HistoryByUser historyByUser = (HistoryByUser) it.next();
    	String actorName = historyByUser.getUser().getLastName() + " " + historyByUser.getUser().getFirstName();
    	ligne.addArrayCellText(actorName);
    	ArrayCellText cell1 = null;
    	Date haveRead = historyByUser.getLastAccess();
    	String readingDate = "";
        if (haveRead == null) 
        	readingDate = "&nbsp;";
        else 
            readingDate = resource.getOutputDateAndHour(haveRead);
        cell1 = ligne.addArrayCellText(readingDate);
        if (haveRead != null)
        	cell1.setCompareOn(haveRead);
        int nbAccess = historyByUser.getNbAccess();
        ligne.addArrayCellText(nbAccess);
        
        String historyUserId = historyByUser.getUser().getId();
        IconPane iconPane = gef.getIconPane();
		Icon detailIcon = iconPane.addIcon();
		
		detailIcon.setProperties(m_context + "/util/icons/info.gif", messages.getString("statistic.detail"), "javascript:editDetail('"+historyUserId+"','"+actorName+"')");

   		ligne.addArrayCellIconPane(iconPane);
     }
    	
    out.println(arrayPane.print());  
%>