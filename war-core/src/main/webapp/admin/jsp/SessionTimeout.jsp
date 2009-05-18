<html>
<HEAD>
<Script language="JavaScript">

function gotoLogin()
{
	var errorCode = 3;
    window.top.location.replace("../../Login.jsp?ErrorCode="+errorCode);
}

</script>
</HEAD>

<body onLoad="gotoLogin();">
  <BR>
  <BR>
  <BR>
  <BR>
  La session est terminée.<BR>
  Veuillez vous reconnecter.
</body>
</html>