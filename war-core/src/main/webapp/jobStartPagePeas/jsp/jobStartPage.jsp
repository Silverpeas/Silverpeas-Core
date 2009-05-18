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
    <frame src="jobStartPageNav" marginwidth="0" marginheight="10" name="startPageNavigation" frameborder="0" scrolling="AUTO">
    <frame src="welcome" name="startPageContent" marginwidth="10" marginheight="10" frameborder="0" scrolling="AUTO">
  </frameset>
<noframes></noframes>


</html>