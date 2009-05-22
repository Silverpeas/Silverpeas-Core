<%@ page import="com.silverpeas.myLinks.model.LinkDetail"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		links 	= (Iterator) pReq.getAttribute("Links");

if (!links.hasNext()) {
	out.println(message.getString("NoFavorites"));
}
else 
{
	//affichage des liens favoris de l'utilisateur
	LinkDetail link = null;
	while (links.hasNext()) 
	{
		link = (LinkDetail) links.next();
		if (link.isVisible())
		{
			// afficher que les liens que l'utilisateur a topé "visible en page d'accueil"
			String lien = link.getUrl();
			String name = Encode.convertHTMLEntities(link.getName());
			if (!StringUtil.isDefined(name))
				name = lien;
			
			// ajouter le context devant le lien si nécéssaire
			if (lien.indexOf("://") == -1)
				lien = URLManager.getApplicationURL() + lien;

			String target = "_self";
			if (link.isPopup())
				target = "_blank";
			%>
			&#149; <a href="<%=lien%>" target="<%=target%>"><%=name%></a><br/>
			<%
		}
	}
}
out.flush();
%>