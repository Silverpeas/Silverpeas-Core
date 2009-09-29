/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
