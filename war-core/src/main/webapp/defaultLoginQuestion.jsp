<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

<%@ include file="headLog.jsp"%>

<html>
	<head>
		<title><%=generalMultilang.getString("GML.popupTitle")%></title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<link rel="stylesheet" href="<%=styleSheet%>">
		<style type="text/css">
		<!--
			body {  background-attachment: fixed; background-image: url(admin/jsp/icons/rayure.gif); background-repeat: repeat-x}
		-->
		</style>
		
	   <script type="text/javascript">
       function checkForm() {
           var form = document.forms["questionForm"];
           if (form.elements["answer"].value.length == 0) {
               alert("Veuillez saisir la réponse à la question");
               return false;
           } else {
               return true;
           }
       }
	   </script>
	</head>

<%
    UserDetail userDetail = (UserDetail)request.getAttribute("userDetail");
%>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
	  <tr> 
	    <td valign="top" width="45%"> 
		    <form name="questionForm" action="<%=m_context%>/CredentialsServlet/ValidateAnswer" method="post">
	   
		    <table width="380" border="0" cellspacing="0" cellpadding="0" class="intfdcolor">
				<tr> 
		           <td valign="middle" align="left" colspan="2" style="padding-left: 10px;">
		           		<span class="txtpetitblanc">
		           		<br>
						Veuillez r&eacute;pondre &agrave; la question personnelle ci-dessous<br><br>
    					<%=userDetail.getLoginQuestion()%>
    					</span>
    					<br><br>
					</td>
				</tr>	   
	          <tr> 
	            <td width="80" align="right" valign="middle"><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">R&eacute;ponse&nbsp;:</span></td>
	            <td valign="middle" style="padding-bottom: 10px;"> 
					<input type="password" name="answer" id="answer" size="40"/>
	            </td>
	          </tr>
				<tr bgcolor="#FFFFFF"> 
				  <td class="intfdcolor51" align="center">&nbsp;</td>
				  <td class="intfdcolor51" align="center"> 
	                <input type=image src="<%=m_context%>/util/icons/login_fl.gif" border="0" name="image" onclick="return checkForm()">
				  </td>
				</tr>
			</table>
	       <input type="hidden" name="Login" value="<%=userDetail.getLogin()%>"/>
	       <input type="hidden" name="DomainId" value="<%=userDetail.getDomainId()%>"/>
         </form>
        </td>
     </tr>
  </table>
        
</body>
</html>