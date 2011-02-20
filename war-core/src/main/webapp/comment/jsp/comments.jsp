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
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-comment.js"></script>
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/styleSheets/jquery/jquery-comment.css" />

<%
  //initialisation des variables
  String id = request.getParameter("id");
  String user_id = request.getParameter("userid");
  String url = request.getParameter("url");
  String component_id = request.getParameter("component_id");
  String profile = request.getParameter("profile");
  String indexIt = request.getParameter("IndexIt");

  if (indexIt == null || "null".equals(indexIt) || indexIt.length() == 0) {
    indexIt = "1";
  }

  boolean isUserGuest = "G".equals(m_MainSessionCtrl.getCurrentUserDetail().getAccessLevel());
  boolean adminAllowedToUpdate = resources.getSetting("AdminAllowedToUpdate", true);
  CommentService commentService = CommentServiceFactory.getFactory().getCommentService();
  List comments = commentService.getAllCommentsOnPublication(new CommentPK(id, component_id));

  Board board = gef.getBoard();
  out.println(board.printBefore());
  //displayComments(messages, id, component_id, user_id, profile, hLineSrc, modif_icon, delete_icon, adminAllowedToUpdate, language, out);
  String requiredField = resources.getString("GML.requiredField");
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<div id="commentaires" class="commentaires">

</div><!-- End commentaires-->
<%
  out.println(board.printAfter());
  ButtonPane buttonPane = gef.getButtonPane();
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
    $('#commentaires').comment({
      'uri': "<%=m_context%>/services/comments/<%=component_id%>/<%=id%>",
      'update': {
        'activated': function( comment ) {
          if (("admin" === '<%= profile%>' && <%=adminAllowedToUpdate%>) || (comment.author.id === '<%= user_id%>'))
          return true;
          else
            return false;
        },
        'icon': '<%=modif_icon%>',
        'altText': '<%=resources.getString("GML.update")%>'
      },
      'deletion': {
        'activated': function( comment ) {
          if ((comment.author.id === '<%= user_id%>') || ("admin" === '<%= profile%>'))
          return true;
          else
            return false;
        },
        'confirmation': '<%=messages.getString("comment.suppressionConfirmation")%>',
        'icon': '<%=delete_icon%>',
        'altText': '<%=resources.getString("GML.delete")%>'
      },
      'updateBox': {
        'title': '<%=messages.getString("comment.comment")%>: '
      },
      'editionBox': {
        'title': '<%=resources.getString("comment.add")%>: ',
        'ok': '<%= resources.getString("GML.validate")%>',
        'location': 'before'
      },
      'validate': function(text) {
        if (text == null || text.length == 0) {
          alert("<%=messages.getString("comment.pleaseFill_single")%>");
        } else if (!isValidTextArea(text)) {
          alert("<%=messages.getString("comment.champsTropLong")%>");
        } else {
          return true;
        }
        return false;
      },
      'mandatory': '<%=mandatory_field%>',
      'mandatoryText': '<%=resources.getString("GML.requiredField")%>'
    })

  $('#commentaires').comment('edition', function() {
    return {author: {id: '<%= user_id %>' }, componentId: '<%= component_id %>',
      resourceId: '<%= id %>'}
  });
  $('#commentaires').comment('list');

</script>
