<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetail"%>
<%@ page import="com.stratelia.silverpeas.versioning.model.Document"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

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
Iterator 		attachments	= (Iterator) pReq.getAttribute("Attachments");
Iterator 		documents 	= (Iterator) pReq.getAttribute("Documents");

ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);
boolean full = false;
      
	if ((attachments != null && attachments.hasNext()) || (documents != null && documents.hasNext()))
	{
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
		                
		// traitement des liens vers les fichiers joints
 		if (attachments != null) 
		{
			while (attachments.hasNext()) 
			{
				AttachmentDetail att = (AttachmentDetail) attachments.next();
				String url 	= m_sContext+URLManager.getURL(null,null,att.getPK().getInstanceId())+"GoToFilesTab?Id="+att.getForeignKey().getId();
				String name = Encode.convertHTMLEntities(att.getTitle());
				if (StringUtil.isDefined(att.getLogicalName(language)))
					name = Encode.convertHTMLEntities(att.getLogicalName(language));
			
				out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+att.getPK().getInstanceId()+"')\">"+name+"</a>");
				
				if (att.getExpiryDate() != null)
				{
					// convertir la date de l'évènement
                    Calendar atDate = Calendar.getInstance();
                    atDate.setTime(att.getExpiryDate());
                    atDate.set(Calendar.HOUR_OF_DAY, 0);
                    atDate.set(Calendar.MINUTE, 0);
                    
                    // formatage de la date sous forme jj/mm/aaaa 
                    String date = DateUtil.getInputDate(att.getExpiryDate(), language);
                    if (today.equals(atDate))
                    {
                    	// évènement du jour
                   		out.println(" (" + message.getString("today") + ")");
                    }
                    else if (tomorrow.equals(atDate))
                    {
                    	// évènement du lendemain
                      	out.println(" (" + message.getString("tomorrow") + ")");    
                    }
                    else          
                    {
                      	// recherche du libellé du jour
                    	int day = atDate.get(Calendar.DAY_OF_WEEK);
                     	String jour = "GML.jour" + day;
						// recherche du libellé du mois
                    	int month = atDate.get(Calendar.MONTH);
                      	String mois = "GML.mois" + month;
               			out.println(" (" + generalMessage.getString(jour)+ " " + atDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + atDate.get(Calendar.YEAR) + ")");
                    }
                    out.println("<br/>");
				}
				else
				{
					// affichage sans la date
                 	out.println("<br/>");
				}
			}
		}
		// traitement des liens vers les fichiers joints versionnés
		if (documents != null && documents.hasNext()) 
		{
			while (documents.hasNext()) 
			{
				Document doc = (Document) documents.next();
				String url 	= m_sContext+URLManager.getURL(null,null,doc.getPk().getInstanceId())+"GoToFilesTab?Id="+doc.getForeignKey().getId();
				String name = doc.getName(); 					
				
				out.println("&#149; <a href=\"javaScript:goTo('"+url+"','"+doc.getPk().getInstanceId()+"')\">"+name+"</a>");
				
				if (doc.getExpiryDate() != null)
				{
					// convertir la date de l'évènement
                       Calendar veDate = Calendar.getInstance();
                       veDate.setTime(doc.getExpiryDate());
                       veDate.set(Calendar.HOUR_OF_DAY, 0);
                       veDate.set(Calendar.MINUTE, 0);

                       // formatage de la date sous forme jj/mm/aaaa 
                       String date = DateUtil.getInputDate(doc.getExpiryDate(), language);
                       if (today.equals(veDate))
                       {
                       		// évènement du jour
                      		out.println(" (" + message.getString("today") + ")");
                       }
                       else if (tomorrow.equals(veDate))
                       {
                       		// évènement du lendemain
                      		out.println(" (" + message.getString("tomorrow") + ")");
                        }
                       else          
                       {
                         	// recherche du libellé du jour
                       		int day = veDate.get(Calendar.DAY_OF_WEEK);
                        	String jour = "GML.jour" + day;
							// recherche du libellé du mois
                       		int month = veDate.get(Calendar.MONTH);
                         	String mois = "GML.mois" + month;
                  			out.println(" ("+ generalMessage.getString(jour)+ " " + veDate.get(Calendar.DATE) +" " + generalMessage.getString(mois) + " " + veDate.get(Calendar.YEAR) + ")");
                       }
                       out.println("<br/>");
				}
				else
				{
					out.println("<br/>");
				}
			}
		}
	 }
	 else
	 {
	 	out.println("Aucun fichier réservé !");
	 }
	 out.flush();
%>