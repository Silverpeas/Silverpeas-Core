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
	    /*
			Password Validator 0.1
			(c) 2007 Steven Levithan <stevenlevithan.com>
			MIT License
		*/
		function validatePassword (pw, options) {
			// default options (allows any password)
			var o = {
				combined: 0,
				lower:    0,
				upper:    0,
				alpha:    0, /* lower + upper */
				numeric:  0,
				special:  0,
				length:   [0, Infinity],
				custom:   [ /* regexes and/or functions */ ],
				badWords: [],
				badSequenceLength: 0,
				noQwertySequences: false,
				noSequential:      false
			};
		
			for (var property in options)
				o[property] = options[property];
		
			var	re = {
					lower:   /[a-z]/g,
					upper:   /[A-Z]/g,
					alpha:   /[A-Z]/gi,
					numeric: /[0-9]/g,
					special: /[\W_]/g
				},
				rule, i;
		
			// enforce min/max length
			if (pw.length < o.length[0] || pw.length > o.length[1])
				return false;
	
			var combinedFound = 0;
			
			// enforce lower/upper/alpha/numeric/special rules
			for (rule in re) {
				if ((pw.match(re[rule]) || []).length > 0) {
					combinedFound++;
				}
				if ((pw.match(re[rule]) || []).length < o[rule])
					return false;
			}
	
			// check nb combined found
			if ( combinedFound < o[combined] ) 
				return false;
			
			// enforce word ban (case insensitive)
			for (i = 0; i < o.badWords.length; i++) {
				if (pw.toLowerCase().indexOf(o.badWords[i].toLowerCase()) > -1)
					return false;
			}
		
			// enforce the no sequential, identical characters rule
			if (o.noSequential && /([\S\s])\1/.test(pw))
				return false;
		
			// enforce alphanumeric/qwerty sequence ban rules
			if (o.badSequenceLength) {
				var	lower   = "abcdefghijklmnopqrstuvwxyz",
					upper   = lower.toUpperCase(),
					numbers = "0123456789",
					qwerty  = "qwertyuiopasdfghjklzxcvbnm",
					start   = o.badSequenceLength - 1,
					seq     = "_" + pw.slice(0, start);
				for (i = start; i < pw.length; i++) {
					seq = seq.slice(1) + pw.charAt(i);
					if (
						lower.indexOf(seq)   > -1 ||
						upper.indexOf(seq)   > -1 ||
						numbers.indexOf(seq) > -1 ||
						(o.noQwertySequences && qwerty.indexOf(seq) > -1)
					) {
						return false;
					}
				}
			}
		
			// enforce custom regex/function rules
			for (i = 0; i < o.custom.length; i++) {
				rule = o.custom[i];
				if (rule instanceof RegExp) {
					if (!rule.test(pw))
						return false;
				} else if (rule instanceof Function) {
					if (!rule(pw))
						return false;
				}
			}
		
			// great success!
			return true;
		}
	
		function checkPassword() {
			var newPassword = document.forms['changePwdForm'].password.value;
			var passed = validatePassword(document.forms['changePwdForm'].password.value, {
				length:   [8, Infinity],
				combined: 3
			});
	    	if (newPassword != document.forms['changePwdForm'].confirmPassword.value) {
	    		alert("Le mot de passe et sa confirmation ne sont pas identiques");
		    	return false;
	    	}
	    	else if (passed == false) {
		    	alert("Votre mot de passe doit comporter au moins huit caractères et être composé d'une combinaison de trois types de caractères (à choisir entre minuscules, majuscules, chiffres et signes spéciaux). ");
		    	return false;
	    	}
	    	else {
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
		   
				<form name="changePwdForm" action="<%=m_context%>/CredentialsServlet/ChangePassword" method="POST">
			    <table width="340" border="0" cellspacing="0" cellpadding="0" class="intfdcolor">
					<tr> 
			           <td valign="middle" align="left" colspan="2" style="padding-left: 10px; padding-bottom: 10px;"><br>
							<span class="txtpetitblanc">
							     R&eacute;initialisation de votre mot de passe<br>
							</span>
						</td>
					</tr>	   

		          <tr> 
		            <td width="150" align="right" valign="middle"><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Nouveau mot de passe&nbsp;:</span></td>
		            <td valign="middle"> 
						<input type="password" name="password" id="password" size="30"/>
					</td>
				  </tr>							

		          <tr> 
		            <td align="right" valign="middle"><span class="txtpetitblanc"><img src="admin/jsp/icons/1px.gif" width="1" height="25" align="middle">Confirmation&nbsp;:</span></td>
		            <td valign="middle"> 
						<input type="password" name="confirmPassword" id="confirmPassword" size="30"/>
						<br>&nbsp;
					</td>
				  </tr>	

					<tr bgcolor="#FFFFFF"> 
						<td class="intfdcolor51" align="center">&nbsp;</td>
						<td class="intfdcolor51" align="center"> 
			                <input type=image src="<%=m_context%>/util/icons/login_fl.gif" border="0" name="image" onclick="return checkPassword()">
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