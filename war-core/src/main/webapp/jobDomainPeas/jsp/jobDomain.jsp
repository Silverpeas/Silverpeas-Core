<%@ include file="check.jsp" %>
<html>
<head>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>

  <frameset cols="200,*" border="0" framespacing="5" frameborder="NO"> 
    <frame src="domainNavigation" name="domainBar" marginwidth="0" marginheight="10" name="domainNavigation" frameborder="NO"  scrolling="AUTO">
    <frame src="about:blank" name="domainContent" marginwidth="10" marginheight="10" frameborder="NO" scrolling="AUTO">
  </frameset>
<noframes></noframes>

</html>