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
	           }
	           else if (form.elements["answerConfirmed"].value.length == 0) {
	               alert("Veuillez confirmer la réponse à la question");
	               return false;
	           }
	           else if (form.elements["answer"].value != form.elements["answerConfirmed"].value) {
	               alert("La réponse et sa confirmation sont différents, veuillez les ressaisir");
	               return false;
	           }
	
	            else {
	               form.action = "<%=m_context%>/CredentialsServlet/ValidateQuestion";
	               form.submit();
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
			<form name="questionForm" action="<%=m_context%>/CredentialsServlet/ValidateQuestion" method="post">

		    <table class="intfdcolor" width="600" border="0" cellpadding="0" cellspacing="0">
	          <tbody>
				<tr> 
		           <td valign="middle" colspan="2"><br>
						<center>
						     Vous n'avez pas encore choisi de question personnelle.<br>
						        Cette question vous permettra de réitinialiser votre mot de passe en cas d'oubli.<br>
						</center><br>
					</td>
				</tr>	   
	          <tr> 
	            <td width="150" align="right" valign="middle"><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Question personnelle&nbsp;:</span></td>
	            <td valign="middle"> 
                   <select name="question">
                   <%
						int questionsCount = Integer.parseInt(general.getString("loginQuestion.count"));
						String question;
						for (int i = 1; i <= questionsCount; i++)
						{
						    question = general.getString("loginQuestion." + i);
						%>
                       <option value="<%=question%>"><%=question%></option><%
						}
					%>
                   </select>
	            </td>
	          </tr>
	          <tr> 
	            <td align="right" valign="top" nowrap><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">R&eacute;ponse &agrave; la question choisie :&nbsp;</span></td>
	            <td valign="top"> 
					<input type="password" name="answer" id="answer" size="40"/>
	            </td>
	          </tr>
	          <tr> 
	            <td align="right" valign="top" nowrap><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Confirmation :&nbsp;</span><br>&nbsp;</br></td>
	            <td valign="top"> 
					<input type="password" name="answerConfirmed" id="answerConfirmed" size="40"/>
	            </td>
	          </tr>

				<tr bgcolor="#FFFFFF"> 
				  <td class="intfdcolor51" align="center">&nbsp;</td>
				  <td class="intfdcolor51" align="center"> 
	                <input type=image src="<%=m_context%>/util/icons/login_fl.gif" border="0" name="image" onclick="return checkForm()">
				  </td>
				</tr>
			</table>
            </form>
        </td>
     </tr>
  </table>
        
</body>
</html>