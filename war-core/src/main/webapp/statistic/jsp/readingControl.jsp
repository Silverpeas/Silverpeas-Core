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

<%@ page import="java.io.IOException,javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException,
                 java.rmi.RemoteException,javax.ejb.FinderException,java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ include file="checkStatistic.jsp" %>

<%
    //initialisation des variables
    String id 			= request.getParameter("id");
    String userId 		= request.getParameter("userId");
    String url 			= request.getParameter("url");
    String componentId 	= request.getParameter("componentId");
    String objectType	= request.getParameter("objectType");
    List 	userIds 	= (List) request.getAttribute("UserIds");
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
    Collection readingState = statisticBm.getHistoryByObject(foreignPK, 1, objectType, userIds);
    
    // affichage des contr�les de lecture
    ArrayPane arrayPane = gef.getArrayPane("readingControl", "ReadingControl", request, session);

    arrayPane.addArrayColumn(generalMessage.getString("GML.user"));
    arrayPane.addArrayColumn(messages.getString("statistic.lastAccess"));
    arrayPane.addArrayColumn(messages.getString("statistic.nbAccess"));
    ArrayColumn columnDetail = arrayPane.addArrayColumn(messages.getString("statistic.detail"));
    columnDetail.setSortable(false);
    
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
        
        if (haveRead != null)
        {
        String historyUserId = historyByUser.getUser().getId();
        IconPane iconPane = gef.getIconPane();
		Icon detailIcon = iconPane.addIcon();
		
		detailIcon.setProperties(m_context + "/util/icons/info.gif", messages.getString("statistic.detail"), "javascript:editDetail('"+historyUserId+"','"+actorName+"')");

   		ligne.addArrayCellIconPane(iconPane);
        }
     }
    	
    out.println(arrayPane.print());  
%>