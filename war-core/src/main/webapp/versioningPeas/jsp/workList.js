function rtrim(texte){
	while (texte.substring(0,1) == ' '){
		texte = texte.substring(1, texte.length);
	}

	return texte;
}

function ltrim(texte){
	while (texte.substring(texte.length-1,texte.length) == ' ') {
		texte = texte.substring(0, texte.length-1);
	}

	return texte;
}

function trim(texte){
	var len = texte.length;
	if (len == 0){
		texte = "";
	}
	else {
		texte = rtrim(texte);
		texte = ltrim(texte);
	}
	return texte;
}

function toggleVV()
{
    var vv = document.workForm.VV;
    if ( vv.value == "" )
    {
        vv.value = "on";
    }
    else
    {
        vv.value = "";
    }
}

function checkForm()
{
    var style_type = document.workForm.style_type.value;
    if ( style_type == "0" )
    {
        var users = document.workForm.lines.value;
        if ( users == 0 )
        {
            alert("Select at least one user first");

            return false;
        }
        return true;
    }
    else if ( style_type == "1" )
    {
        if ( document.workForm.VV.value != "" )
        {
            return true;
        }
        else
        {
            alert("Select validator");
            return false;
        }
    }
    else if ( style_type == "2" )
    {
        var writers = false;
        var users = document.workForm.lines.value;
        for ( i=0; i<users; i++ )
        {
            var oW = eval("document.workForm.chw"+i);
            if ( oW.checked == true )
            {
                writers = true;
                break;
            }
        }

        if ( writers == true && document.workForm.VV.value != "" )
        {
            return true;
        }
        else
        {
            if ( writers == false )
            {
                if ( document.workForm.VV.value == "" )
                {
                    alert("Last user must be validator and you should select writer(s)");
                }
                else
                {
                    alert("You should select writer(s)");
                }
            }
            else
            {
                alert("Last user must be validator");
            }
            return false;
        }
    }
    return true;
}

function moveUp(index) {
    document.workForm.up.value = index;
    document.workForm.action = "ChangeOrder";
    document.workForm.submit();
}

function moveDown(index) {
    document.workForm.down.value = index;
    document.workForm.action = "ChangeOrder";
    document.workForm.submit();
}

function addUser(index) {
    document.workForm.add.value = index;
    document.workForm.action = "ChangeOrder";
    document.workForm.submit();
}

function selectAllValidators(fChecked)
{
    var users = document.workForm.lines.value;
    var vv = document.workForm.VV;

    for (i=0; i<users; i++)
    {
        var oV = eval("document.workForm.chv"+i);
        if ( oV.disabled == false )
            oV.checked = fChecked;
    }

    vv.value = "";
    if (fChecked)
        vv.value = "1";
}
