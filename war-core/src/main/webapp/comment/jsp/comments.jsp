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
<%@ include file="checkComment.jsp" %>
<%@ include file="graphicUtil.jsp.inc"%>
<%!
    void displayComments( ResourceLocator messages, String id, String component_id, String user_id, String profile, String hLineSrc, String modif_icon, String delete_icon, boolean adminAllowedToUpdate, String language, JspWriter out) throws IOException, CreateException, SQLException, NamingException, RemoteException, FinderException {
          out.println("<TABLE border=\"0\" cellPadding=\"0\" cellSpacing=\"3\" align=\"center\" width=\"100%\"><tr><td>");
          out.println("<TR><TD colspan=\"6\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+
                        "\" width=\"100%\" height=\"1\"></TD></TR>");
          out.println("<TR><TD align=\"left\" width=\"15%\"><b>"+messages.getString("author")+
                        "</b></TD><TD align=\"left\" width=\"56%\"><b>"+messages.getString("c_comment")+
                        "</b></TD><TD align=\"left\" width=\"15%\"><b>"+messages.getString("created")+
                        "</b></TD><TD align=\"left\" width=\"10%\"><b>"+messages.getString("modified")+
                        "</b></TD><TD align=\"center\" width=\"2%\"><b></b></TD><TD align=\"center\" width=\"2%\"><b></b></TD></TR>");
          out.println("<TR><TD colspan=\"6\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");

        CommentService commentService = CommentServiceFactory.getFactory().getCommentService();
        List comments = commentService.getAllCommentsOnPublication(new CommentPK(id, component_id));
        Comment comment;
        String comment_id;
        String owner_id;
        boolean is_admin = "admin".equals(profile);
        String creation_date;
        String modification_date;

        for ( int i=0; i< comments.size(); i++ )
        {
            comment = (Comment)comments.get(i);
            comment_id = comment.getCommentPK().getId();

            out.println("<tr>");
            out.println("<td align=\"left\" valign=\"top\">"+comment.getOwner()+"</TD>");
            out.println("<td align=\"left\" valign=\"top\">"+Encode.javaStringToHtmlParagraphe(comment.getMessage())+"</td>");
            creation_date = "";
            modification_date = "";
            try
            {
                creation_date = DateUtil.getOutputDate(comment.getCreationDate(), language);
                modification_date = DateUtil.getOutputDate(comment.getModificationDate(), language);
            }
            catch ( Exception ex)
            {
            }
            out.println("<td align=\"left\" valign=\"top\">"+creation_date+"</td>");
            out.println("<td align=\"left\" valign=\"top\">"+modification_date+"</td>");
            owner_id = String.valueOf(comment.getOwnerId());
            if (owner_id.equals(user_id))
            {
                out.println("<td align=\"center\" valign=\"top\"><A href=\"javascript:updateComment("+comment_id+")\"><IMG SRC=\""+modif_icon+"\" border=\"0\" alt=\""+messages.getString("modify")+"\" title=\""+messages.getString("modify")+"\"></A></td>");
                out.println("<td align=\"center\" valign=\"top\"><A href=\"javascript:removeComment("+comment_id+")\"><IMG SRC=\""+delete_icon+"\" border=\"0\" alt=\""+messages.getString("delete")+"\" title=\""+messages.getString("delete")+"\"></A></td>");
            }
            else if (is_admin)
            {
            	if (adminAllowedToUpdate)
            	{
            		out.println("<td align=\"center\" valign=\"top\"><A href=\"javascript:updateComment("+comment_id+")\"><IMG SRC=\""+modif_icon+"\" border=\"0\" alt=\""+messages.getString("modify")+"\" title=\""+messages.getString("modify")+"\"></A></td>");
            	}
                out.println("<td align=\"center\" valign=\"top\"><A href=\"javascript:removeComment("+comment_id+")\"><IMG SRC=\""+delete_icon+"\" border=\"0\" alt=\""+messages.getString("delete")+"\" title=\""+messages.getString("delete")+"\"></A></td>");
            }
            else
            {
                out.println("<td align=\"center\" valign=\"top\"></td>");
                out.println("<td align=\"center\" valign=\"top\"></td>");
            }
            out.println("</tr>");
        }
          out.println("<TR><TD colspan=\"6\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></TD></TR>");
          out.println("</td></tr></TABLE>");
    }

%>
<%
    //initialisation des variables
    String id 			= request.getParameter("id");
    String user_id 		= request.getParameter("userid");
    String url 			= request.getParameter("url");
    String component_id = request.getParameter("component_id");
    String profile 		= request.getParameter("profile");
    String indexIt		= request.getParameter("IndexIt");

    if (indexIt == null || "null".equals(indexIt) || indexIt.length()==0)
  		indexIt = "1";

    boolean isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());
    ResourceLocator commentSettings = new ResourceLocator("com.stratelia.webactiv.util.comment.Comment","");
	boolean adminAllowedToUpdate = commentSettings.getBoolean("AdminAllowedToUpdate", true);

    Board board = gef.getBoard();
    out.println(board.printBefore());
    displayComments(messages, id, component_id, user_id, profile, hLineSrc, modif_icon, delete_icon, adminAllowedToUpdate, language, out);
    out.println(board.printAfter());
    ButtonPane buttonPane = gef.getButtonPane();
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<% if (!isUserGuest) { %>
<script language="Javascript">
function addComment()
{
    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?foreign_id=<%=id%>&component_id=<%=component_id%>&userid=<%=user_id%>&IndexIt=<%=indexIt%>", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
}
function updateComment(id)
{
    SP_openWindow("<%=m_context%>/comment/jsp/newComment.jsp?id="+id+"&IndexIt=<%=indexIt%>", "blank", "600", "250","scrollbars=no, resizable, alwaysRaised");
}
function removeComment(id)
{
    if (window.confirm("<%=messages.getString("suppressionConfirmation")%>  <%=messages.getString("comment")%> ?"))
    {
        document.removeForm.comment_id.value=id;
        document.removeForm.submit();
    }
}
function commentCallBack()
{
	location.reload();
}
</script>
<br> <div align="center">
<%
    buttonPane.addButton((Button) gef.getFormButton(messages.getString("add"), "javascript:addComment()", false));
    out.println(buttonPane.print());
 %>
</div>
<form name="removeForm" action="<%=m_context%>/comment/jsp/removeComment.jsp?action=delete" method="POST">
<input type="hidden" name="comment_id">
<input type="hidden" name="component_id" value="<%=component_id%>">
<input type="hidden" name="url" value="<%=url%>">
<input type="hidden" name="pub_id" value="<%=id%>">
</form>
<% } %>
