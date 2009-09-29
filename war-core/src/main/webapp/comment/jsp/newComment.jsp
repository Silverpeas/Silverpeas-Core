<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="com.stratelia.silverpeas.comment.model.Comment,
                 java.util.Date"%>
<%@ include file="checkComment.jsp" %>
<%@ include file="graphicUtil.jsp.inc"%>
<%
// declaration of labels !!!

String id 			= request.getParameter("id");
String foreign_id 	= request.getParameter("foreign_id");
String message 		= request.getParameter("message");
String action 		= request.getParameter("action");
String component_id = request.getParameter("component_id");
String user_id 		= request.getParameter("userid");
String indexIt		= request.getParameter("IndexIt");

if ( "save".equals(action) )
{
	boolean bIndexIt = true;
	if ("0".equals(indexIt))
		bIndexIt = false;

    String current_date = DateUtil.date2SQLDate(new Date());

    if ( id != null && !"null".equals(id) && !"".equals(id))
    {
        CommentPK pk = new CommentPK(id);
        Comment comment = CommentController.getComment(pk);
        comment.setMessage(message);
        comment.setModificationDate(current_date);
        CommentController.updateComment(comment, bIndexIt);
    }
    else if ( foreign_id != null )
    {
        CommentPK foreign_pk = new CommentPK(foreign_id);
        CommentPK pk = new CommentPK("X");
        pk.setComponentName(component_id);
        Comment comment = new Comment(pk, foreign_pk, Integer.parseInt(user_id), "X", message, current_date, current_date);
        CommentController.createComment(comment, bIndexIt);
    }
%>
<html>
<title></title>
<%
    out.println(gef.getLookStyleSheet());
%>
<head>
<script language="Javascript">
window.opener.commentCallBack();
window.close();
</script>
</head>
<body>
</body>
</html>
<%
}
else
{
    if ( id != null )
    {
        CommentPK pk = new CommentPK(id);
        Comment comment = CommentController.getComment(pk);
        message = comment.getMessage();
    }
    else
    {
        message = "";
    }
%>

<html>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<%
out.println(gef.getLookStyleSheet());
%>
<head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="Javascript">
function addComment()
{
    if ( document.addForm.message.value == "" )
    {
        alert("<%=messages.getString("pleaseFill_single")%>");
        return;
    }
    if (!isValidTextArea(document.addForm.message)) 
    {
     	alert("<%=messages.getString("champsTropLong")%>");	
	return;
    }  	
    document.addForm.submit();
}

</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
Board board = gef.getBoard();
        out.println(window.printBefore());
        out.println(frame.printBefore());
%>
<center>
<%
	out.println(board.printBefore());
%>
<form name="addForm" action="newComment.jsp?action=save" method="POST">
<input type="hidden" name="id" value="<%=id%>">
<input type="hidden" name="foreign_id" value="<%=foreign_id%>">
<input type="hidden" name="userid" value="<%=user_id%>">
<input type="hidden" name="component_id" value="<%=component_id%>">
<input type="hidden" name="IndexIt" value="<%=indexIt%>">
<span class="txtlibform"><%=messages.getString("comments")%>: </span><BR>
<div align="left">
<TEXTAREA ROWS="6" COLS="100" name="message"><%=message%></TEXTAREA>&nbsp;<IMG border="0" width="5" height="5" src="<%=mandatory_field%>">
</div>
<br>(<IMG border="0" width="5" height="5" src="<%=mandatory_field%>"> : <%=messages.getString("required")%>)
</form>
<%
	out.println(board.printAfter());
%>
<br>
<%
    ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(messages.getString("ok"),"javascript:addComment()", false));
	buttonPane.addButton((Button) gef.getFormButton(messages.getString("cancel"), "javascript:window.close()", false));
	out.println(buttonPane.print());
%>
</center>
<%
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>
<%
}
%>