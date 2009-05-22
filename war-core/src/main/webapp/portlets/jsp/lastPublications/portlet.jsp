<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
    PortletPreferences pref = pReq.getPreferences();
    String error = pReq.getParameter(FormNames.ERROR_BAD_VALUE);
%>

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
Iterator publications = ((List) pReq.getAttribute("Publications")).iterator();

	while (publications.hasNext()) 
    {
        PublicationDetail 	pub 		= (PublicationDetail) publications.next();
		UserDetail 			pubUpdater 	= m_MainSessionCtrl.getOrganizationController().getUserDetail(pub.getUpdaterId());
		String 				url 		= m_sContext+URLManager.getURL("kmelia", null, pub.getPK().getInstanceId())+pub.getURL();
        out.println("<a href=\"javaScript:goTo('"+url+"','"+pub.getPK().getInstanceId()+"')\"><b>"+Encode.convertHTMLEntities(pub.getName(language))+"</b></a>");
        if (pubUpdater != null && pub.getUpdateDate() != null)
        	out.println("<br/>"+Encode.convertHTMLEntities(pubUpdater.getDisplayedName())+" - "+DateUtil.getOutputDate(pub.getUpdateDate(), language));
        else if (pubUpdater != null && pub.getUpdateDate() == null)
        	out.println("<br/>"+Encode.convertHTMLEntities(pubUpdater.getDisplayedName()));
        if (pubUpdater == null && pub.getUpdateDate() != null)
        	out.println("<br/>"+DateUtil.getOutputDate(pub.getUpdateDate(), language));
        if ("checked".equalsIgnoreCase(pref.getValue("displayDescription","")) && StringUtil.isDefined(pub.getDescription(language)))
        	out.println("<br/>"+Encode.javaStringToHtmlParagraphe(Encode.convertHTMLEntities(pub.getDescription(language))));
           
        if (publications.hasNext())
        	out.println("<br/><br/>");
    }
%>