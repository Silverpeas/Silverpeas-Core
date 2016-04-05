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

<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%!
	String getErrorMessage(int error, MultiSilverpeasBundle resource)
	{
		return resource.getString("importExportPeas.ImportError"+error);
	}

	String getStatusMessage(int status, MultiSilverpeasBundle resource)
	{
		return resource.getString("importExportPeas.ImportStatus"+status);
	}
%>
<%
	ImportReport importReport = (ImportReport) request.getAttribute("importReport");
%>
<html>
<head>
<view:looknfeel/>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%

browseBar.setComponentName(resource.getString("importExportPeas.Importation"), "Main");

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

if (importReport != null)
{
	out.println("<b>"+resource.getString("importExportPeas.StatGlobal")+"</b><br><br>");

	out.println(resource.getString("importExportPeas.ImportDuration")+" : "+importReport.getDuration()+"<br>");
	out.println(resource.getString("importExportPeas.NbFilesImported")+" : "+importReport.getNbFilesProcessed()+"<br>");
	out.println(resource.getString("importExportPeas.NbFilesNotFound")+" : "+importReport.getNbFilesNotImported()+"<br>");
	out.println(resource.getString("importExportPeas.TotalFileUploadedSize")+" : " + FileRepositoryManager.formatFileSize(importReport.getTotalImportedFileSize()) + "<br>");

	out.println("<br><b>"+resource.getString("importExportPeas.StatComponent")+"</b><br>");

	List listcpnt = importReport.getListComponentReport();
	if (listcpnt != null) {
		Iterator 		itListcpnt 		= listcpnt.iterator();
		ComponentReport componentRpt 	= null;
		while(itListcpnt.hasNext())
		{
			componentRpt = (ComponentReport)itListcpnt.next();

			out.println("<br>"+resource.getString("importExportPeas.Composant")+" <b><a href=\""+URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentRpt.getComponentId(), true)+"\">" + componentRpt.getComponentName() + "</a></b> ("+componentRpt.getComponentId() + ")<br>");
			out.println(resource.getString("importExportPeas.NbPubCreated")+" : "+componentRpt.getNbPublicationsCreated() + "<br>");
			out.println(resource.getString("importExportPeas.NbPubUpdated")+" : "+componentRpt.getNbPublicationsUpdated() + "<br>");
			out.println(resource.getString("importExportPeas.NbTopicCreated")+" : "+componentRpt.getNbTopicsCreated() + "<br>");
			out.println(resource.getString("importExportPeas.TotalFileUploadedSize")+" : " + FileRepositoryManager.formatFileSize(componentRpt.getTotalImportedFileSize()) + "<br>");

			//Affichage des rapports unitaires
			List unitReports = componentRpt.getListUnitReports();
			if (unitReports != null) {
				Iterator 	itUnitReports 	= unitReports.iterator();
				UnitReport 	unitReport 		= null;
				while (itUnitReports.hasNext())
				{
					unitReport = (UnitReport)itUnitReports.next();
					if (unitReport.getError() != -1)
					{
						out.println("<font color=\"red\">" + EncodeHelper.javaStringToHtmlString(unitReport
                  .getLabel() + " : " + unitReport.getItemName() + ", " + resource.getString(
                  "GML.error") + " : " + getErrorMessage(unitReport.getError(), resource)) + ", "
                  + resource.getString("importExportPeas.Status") + " : " + getStatusMessage(
                  unitReport.getStatus(), resource) + "</font><br>");
					}
				}
			}

			//Affichage des rapports massifs
			List massiveReports = componentRpt.getListMassiveReports();
			if (massiveReports != null) {
				Iterator 		itMassiveReports 	= massiveReports.iterator();
				MassiveReport 	massiveReport		= null;
				while (itMassiveReports.hasNext())
				{
					massiveReport = (MassiveReport) itMassiveReports.next();

					out.println(resource.getString("importExportPeas.Repository")+" <b>"+massiveReport.getRepositoryPath()+"</b><br>");
					if (massiveReport.getError() != -1)
					{
						out.println("<font color=\"red\">"+resource.getString("GML.error")+" : "+getErrorMessage(massiveReport.getError(), resource)+"</font><br>");
					}
					out.println(resource.getString("importExportPeas.NbPubCreated")+" : "+massiveReport.getNbPublicationsCreated()+"<br>");
					out.println(resource.getString("importExportPeas.NbPubUpdated")+" : "+massiveReport.getNbPublicationsUpdated()+"<br>");
					out.println(resource.getString("importExportPeas.NbTopicCreated")+" : "+massiveReport.getNbTopicsCreated()+"<br>");

					unitReports = massiveReport.getListUnitReports();
					if (unitReports != null) {
						Iterator 	itUnitReports 	= unitReports.iterator();
						UnitReport 	unitReport 		= null;
						while (itUnitReports.hasNext())
						{
							unitReport = (UnitReport)itUnitReports.next();
							if (unitReport.getError() != -1)
							{
								out.println("<font color=\"red\">"+ Encode.javaStringToHtmlString(
                    unitReport.getLabel() + " : " + unitReport.getItemName() + ", " +
                        resource.getString("GML.error") + " : " +
                        getErrorMessage(unitReport.getError(), resource)) + ", "+resource.getString("importExportPeas.Status")+" : " +getStatusMessage(unitReport.getStatus(), resource)+"</font><br>");
							}
						}
					}
				}
			}
		}
	}
}
out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>