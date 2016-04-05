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

<%@page import="org.silverpeas.core.contribution.attachment.model.SimpleDocument"%>
<%@page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<script type="text/javascript">
function goTo(cUrl, componentId)
{
	jumpToComponent(componentId);
	location.href=cUrl;
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
</script>

<%
RenderRequest 	pReq 		= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator<SimpleDocument> attachments	= (Iterator<SimpleDocument>) pReq.getAttribute("Attachments");

LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);
	if (attachments != null && attachments.hasNext()) {
	// convertir la date du jour
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // convertir la date de demain
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE,1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        out.println("<ul>");

		// traitement des liens vers les fichiers joints
		while (attachments.hasNext()) {
			SimpleDocument att =  attachments.next();
			String url 	= m_sContext+URLUtil.getURL(null,null,att.getInstanceId())+"GoToFilesTab?Id="+att.getForeignId();
			String name = EncodeHelper.convertHTMLEntities(att.getTitle());
			if (StringUtil.isDefined(att.getFilename())) {
				name = EncodeHelper.convertHTMLEntities(att.getFilename());
			}

			out.println("<li><a href=\"javaScript:goTo('"+url+"','"+att.getInstanceId()+"')\">"+name+"</a>");

			if (att.getExpiry() != null) {
	            // convertir la date de l'evenement
	            Calendar atDate = Calendar.getInstance();
	            atDate.setTime(att.getExpiry());
	            atDate.set(Calendar.HOUR_OF_DAY, 0);
	            atDate.set(Calendar.MINUTE, 0);

	            // formatage de la date sous forme jj/mm/aaaa
	            String date = DateUtil.getInputDate(att.getExpiry(), language);
	            if (today.equals(atDate)) {
	              // evenement du jour
	              out.println(" (" + message.getString("today") + ")");
	            } else if (tomorrow.equals(atDate)) {
	              // evenement du lendemain
	              out.println(" (" + message.getString("tomorrow") + ")");
	            } else {
	              // recherche du libelle du jour
	              int day = atDate.get(Calendar.DAY_OF_WEEK);
	              String jour = "GML.jour" + day;
	              // recherche du libelle du mois
	              int month = atDate.get(Calendar.MONTH);
	              String mois = "GML.mois" + month;
	              out.println(" (" + generalMessage.getString(jour) + " " + atDate.get(Calendar.DATE)
	                  + " " + generalMessage.getString(mois) + " " + atDate.get(Calendar.YEAR) + ")");
	            }
		}

			out.println("</li>");
		}
		out.println("</ul>");
	 } else {
		out.println(generalMessage.getString("GML.noLockedFile"));
	 }
	 out.flush();
%>