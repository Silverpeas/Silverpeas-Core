function login()
{
	var loginField 	= document.authForm.Login.value;
	if (loginField.length != 0) 
	{
		document.authForm.action = getContext()+"/AuthenticationServlet";
		document.authForm.submit();
	}
}

function logout()
{
	document.authForm.action = getContext()+"/LogoutServlet";
	document.authForm.submit();
}

function checkSubmitToLogin(ev)
{
	var touche = ev.keyCode;
	if (touche == 13)
		login();
}