<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
String okLabel = messages.getString("ok");
String nokLabel = messages.getString("cancel");
String commentsLabel = messages.getString("comments");//"Comments";
String pleaseFill = messages.getString("pleaseFill");
String mandatoryField = m_context+"/util/icons/mandatoryField.gif";
String action = request.getParameter("action");
%>

<html>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<head>

<script language="Javascript">

function submitComment(action)
{
    var cm = document.all.comment.value;
    if ( cm == null || cm == "" )
    {
        alert("<%=pleaseFill%>");
    }
    else
    {
		    if ( action == "validate" || action == "refuse" )
		    {
		        window.opener.document.forms[0].comment.value = cm;
		        window.opener.document.forms[0].elements['action'].value=action;
		        window.opener.document.forms[0].submit();
						window.close();
		    }
		}
}
</script>
</head>
<body onLoad="document.all.comment.focus();">
<%
Board board = gef.getBoard();
out.println(window.printBefore());
%>
<form>
<P>
<table>
        <tr>
                <td class="txtlibform" valign="top">
                        <%=commentsLabel%> :
                </td>
                <td align=left valign="baseline">
                        <textarea id="comment" rows="5" cols="50" ></textarea>  &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
                </td>
        </tr>
</table>
<%

        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(gef.getFormButton(okLabel, "javascript:submitComment('"+action+"');", false));
        buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close();", false));

%>
        <center>
<%
        out.println(buttonPane.print());
%>
        </center>
</form>

<%
        out.println(board.printAfter());
        out.println(window.printAfter());

%>

</body>
</html>