<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.silverpeas.socialNetwork.invitation.model.InvitationUser"%>
<%@page import="com.silverpeas.socialNetwork.myProfil.servlets.MyProfileRoutes"%>

<%
	List invitations = null;
	String receivedCssClass = "";
	String sentCssClass = "";
	boolean outbox = view.equals(MyProfileRoutes.MySentInvitations.toString());
	if (outbox) {
		invitations = (List) request.getAttribute("Outbox");
		sentCssClass = "class=\"active\"";
	} else {
	  	invitations = (List) request.getAttribute("Inbox");
	  	receivedCssClass = "class=\"active\"";
	}
	ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");
%>
    

<div class="sousNavBulle">
	<p>Mes invitations : <a <%=receivedCssClass %> href="<%=MyProfileRoutes.MyInvitations.toString() %>">Re&ccedil;ues</a> <a <%=sentCssClass %> href="<%=MyProfileRoutes.MySentInvitations.toString() %>">Envoy&eacute;es</a></p>
</div>

<div id="RecieveInvitation">

	<% for (int i=0; i<invitations.size(); i++) {
	  	InvitationUser invitation = (InvitationUser) invitations.get(i);
	  	String senderId = invitation.getUserDetail().getId();
	%>
		<div class="a_invitation">
                 <div class="profilPhoto"><a href="<%=URLManager.getApplicationURL() %>/Rprofil/jsp/Main?userId=<%=senderId%>"><img class="defaultAvatar" alt="" src="<%=URLManager.getApplicationURL()+invitation.getUserDetail().getAvatar() %>" /></a></div>
                 <div class="action">
                 		<% if (outbox) { %>
                 			<a onclick="OpenPopup(<%=senderId %>,'Hugonnet Emmanuel')" class="link notification" href="#">Annuler l'invitation</a>
                    	<% } else { %>
							<a onclick="OpenPopupInvitaion(<%=senderId %>,'Hugonnet Emmanuel');" class="link invitation" href="#">Accepter l'invitation</a>
                    		<a onclick="OpenPopup(<%=senderId %>,'Hugonnet Emmanuel')" class="link notification" href="#">Ignorer l'invitation</a>
                    	<% } %>
                    	<a onclick="OpenPopup(<%=senderId %>,'Hugonnet Emmanuel')" class="link notification" href="#">Envoyer un message</a>
				</div>
				<div class="txt">
                	<p>
                    	<a class="name" href="<%=URLManager.getApplicationURL() %>/Rprofil/jsp/Main?userId=<%=senderId%>"> <%=invitation.getUserDetail().getDisplayedName() %> </a>
                    </p>
                    <p>
                    	Date de l'invitation : <%= resource.getOutputDateAndHour(invitation.getInvitation().getInvitationDate())%>
                    </p>
                    <p class="message">
                    <%=invitation.getInvitation().getMessage() %>
             		</p>
				</div>
      </div>
      <% } %>
      
</div> 