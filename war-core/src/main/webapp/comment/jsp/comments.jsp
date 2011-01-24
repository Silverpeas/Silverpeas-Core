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

<%@ page import="java.util.List,
                 java.io.IOException,
                 javax.ejb.CreateException,
                 java.sql.SQLException,
                 javax.naming.NamingException,
                 java.rmi.RemoteException,
                 javax.ejb.FinderException,
                 com.silverpeas.comment.model.Comment,
                 java.util.Date"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ include file="checkComment.jsp" %>
<%@ include file="graphicUtil.jsp.inc"%>

<%
    //initialisation des variables
    String id 			= request.getParameter("id");
    String user_id 		= request.getParameter("userid");
    String url 			= request.getParameter("url");
    String component_id = request.getParameter("component_id");
    String profile 		= request.getParameter("profile");
    String indexIt		= request.getParameter("IndexIt");

    if (indexIt == null || "null".equals(indexIt) || indexIt.length()==0) {
  		indexIt = "1";
    }

    boolean isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());
	boolean adminAllowedToUpdate = resources.getSetting("AdminAllowedToUpdate", true);
	CommentService commentService = CommentServiceFactory.getFactory().getCommentService();
  	List comments = commentService.getAllCommentsOnPublication(new CommentPK(id, component_id));

    Board board = gef.getBoard();
    out.println(board.printBefore());
    //displayComments(messages, id, component_id, user_id, profile, hLineSrc, modif_icon, delete_icon, adminAllowedToUpdate, language, out);
    
%>
	
<div class="commentaires">
		<% if (!isUserGuest) { %> 
			<form name="commentForm" action="<%=m_context %>/comment/jsp/newComment.jsp" method="post">	
				<p class="txtlibform"><%=resources.getString("comment.add")%> :</p>
				<textarea rows="4" cols="100" name="message"></textarea>
				<input type="hidden" name="foreign_id" value="<%=id%>"/>
				<input type="hidden" name="id" value=""/>
				<input type="hidden" name="action" value="save"/>
				<input type="hidden" name="userid" value="<%=user_id %>"/>
				<input type="hidden" name="IndexIt" value="<%=indexIt %>"/>
				<input type="hidden" name="component_id" value="<%=component_id %>"/>
				<input type="hidden" name="url" value="<%=url%>">
			</form>
	
			<%
			ButtonPane buttonPaneComment = gef.getButtonPane();
			Button validateComment 	= (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=addComment();", false);
			buttonPaneComment.addButton(validateComment);
			out.println("<center>"+buttonPaneComment.print()+"</center><br/>");
			%>
		<% } %>

	<% if (comments != null) { 
			Iterator itCom = (Iterator) comments.iterator();
			if (itCom.hasNext()) { %>
				<hr />
			<% } 
			while (itCom.hasNext()) {
				Comment unComment = (Comment) itCom.next();
				String commentDate = DateUtil.getOutputDate(unComment.getCreationDate(), language);
				String ownerId = Integer.toString(unComment.getOwnerId());
				%>
					<div class="oneComment">
						<div>
							<div class="avatar">
								<img src="<%=m_context%><%=unComment.getOwnerDetail().getAvatar() %>"/>
							</div>
							<p class="author">
								<%=unComment.getOwnerDetail().getDisplayedName()%>
								<span class="date"> - <%=commentDate%></span>
							</p>
							<% if (ownerId.equals(user_id)) { %>
								<div class="action">
									<a href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=id%>)"><img src="<%=modif_icon %>" alt="<%=resources.getString("GML.update")%>" title="<%=resources.getString("GML.update")%>" align="absmiddle"/></a>
									<a href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><img src="<%=delete_icon %>" alt="<%=resources.getString("GML.delete")%>" title="<%=resources.getString("GML.delete")%>" align="absmiddle"/></a>
								</div>
							<% } else if ("admin".equals(profile)) { %>
								<div class="action">
									<% if (adminAllowedToUpdate) { %>
										<a href="javascript:updateComment(<%=unComment.getCommentPK().getId()%>,<%=id%>)"><img src="modif_icon" alt="<%=resources.getString("GML.update")%>" title="<%=resources.getString("GML.update")%>" align="absmiddle"/></a>
									<% } %>
									<a href="javascript:removeComment(<%=unComment.getCommentPK().getId()%>)"><img src="<%=delete_icon %>" alt="<%=resources.getString("GML.delete")%>" title="<%=resources.getString("GML.delete")%>" align="absmiddle"/></a>
								</div>
							<% } %>
							<p class="message"><%=EncodeHelper.javaStringToHtmlParagraphe(unComment.getMessage())%></p>
						</div>
					</div>
				<%
			}
		}
		%>
	
	</div><!-- End commentaires-->
<%    
    out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<% if (!isUserGuest) { %>
<script type="text/javascript">
function addComment()
{
    document.commentForm.submit();
}
function updateComment(id)
{
    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&IndexIt=<%=indexIt%>", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
}
function removeComment(id)
{
    if (window.confirm("<%=messages.getString("comment.suppressionConfirmation")%>")) {
        document.removeForm.comment_id.value=id;
        document.removeForm.submit();
    }
}
function commentCallBack()
{
	location.reload();
}
</script>
<form name="removeForm" action="<%=m_context%>/comment/jsp/removeComment.jsp?action=delete" method="POST">
<input type="hidden" name="comment_id">
<input type="hidden" name="component_id" value="<%=component_id%>">
<input type="hidden" name="url" value="<%=url%>">
<input type="hidden" name="pub_id" value="<%=id%>">
</form>
<% } %>
