<%@ include file="check.jsp" %>

<HTML>
<HEAD>
<TITLE></TITLE>
<script language='Javascript'>
function goToTarget()
{
    var fullURL = '<%=(String)request.getAttribute("fullURL")%>';
    window.location.href=fullURL;
}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" onload="javascript:goToTarget()">
</BODY>
</HTML>
