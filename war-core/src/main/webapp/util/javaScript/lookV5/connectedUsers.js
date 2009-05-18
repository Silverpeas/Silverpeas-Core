function setConnectedUsers(nb)
{
	//alert("setConnectedUsers = "+nb);
	try
	{
		var label = getConnectedUsersLabel(nb);
		
		if (nb <= 0)
		{
			document.getElementById("connectedUsers").style.visibility = "hidden";
		}
		else
		{
			document.getElementById("connectedUsers").style.visibility = "visible";
			if (nb > 1)
				document.getElementById("connectedUsers").innerHTML = nb + label + " | ";
			else 
				document.getElementById("connectedUsers").innerHTML = nb + label + " | ";
		}
	}
	catch (e)
	{
	}
}

function openConnectedUsers()
{
    chemin = getContext()+"/RcommunicationUser/jsp/Main";
    SP_openWindow(chemin,"users_pop",400,400,"scrollbars=yes,resizable=yes");
}