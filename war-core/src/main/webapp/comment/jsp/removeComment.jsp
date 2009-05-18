<%@ include file="checkComment.jsp" %>
<%@ include file="graphicUtil.jsp.inc"%>

<%
    String comment_id 	= request.getParameter("comment_id");
	String component_id = request.getParameter("component_id");
    String action 		= request.getParameter("action");
    String url 			= request.getParameter("url");
    String pub_id 		= request.getParameter("pub_id");

    if ( "delete".equals(action) && comment_id != null && !"null".equals(comment_id) )
    {
        CommentPK pk = new CommentPK(comment_id);
		pk.setComponentName(component_id);
        CommentController.deleteComment(pk);
    }

    response.sendRedirect(URLManager.getApplicationURL()+ url + "?Action=ViewComment&PubId="+pub_id);
%>
