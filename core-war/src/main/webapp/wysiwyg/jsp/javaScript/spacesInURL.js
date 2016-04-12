/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
function breakSpace(pstr)
{
	res="";
    for(i=0; i<pstr.length;i++)
	{
		c = pstr.charAt(i);
        switch(c) {
            case ' ':res+="%20";break;
            //case '>':res+="&gt;";break;
            //case '&':res+="&amp;";break;
            default:res+=c;
        }
    }
//	document.formtest.test.value = res;
	return res;
}

function unbreakSpace(pstr) {

	str = new String(pstr);
	res = "";
    if (pstr==null) return res;
	index = str.indexOf("%20");
	while (index != -1) {
        res += str.substring(0, index);
		res += " ";
		str = str.substring(index + 3, str.length);
		index = str.indexOf("%20");
    }
	res += str;
//	document.formtest.test.value = res;
    return res;
}