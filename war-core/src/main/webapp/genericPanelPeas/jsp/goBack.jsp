<%@ include file="check.jsp" %>

<HTML>
<HEAD>
<TITLE></TITLE>
<script language='Javascript'>
function QuitAndRefresh()
{
    var toPopup = '<%=((Boolean)request.getAttribute("ToPopup")).toString()%>';
		var hostUrl = '<%=(String) request.getAttribute("HostUrl")%>';
    if (toPopup=="false")
    {
       window.location.href=hostUrl;
    }
		else{
			window.opener.location.href=hostUrl;
			window.close();
		}
}
</script>
</HEAD>
<BODY onload="javascript:QuitAndRefresh()">
</BODY>
</HTML>
