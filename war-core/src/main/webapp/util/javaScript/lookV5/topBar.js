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
var navVisible = true;
function resizeFrame()
{		
	 if (navVisible)
	 {
		 if (displayPDCFrame()) {
			 parent.setframevalue("rows", "10,100%,*,*,*,*");
		 } else {
			 parent.setframevalue("rows", "10,100%,*,*,*");
		 }
		 
		 document.body.scroll = "no"; 
		 document.getElementById('space2Expand').height='10';
		 document.images['expandReduce'].src="icons/silverpeasV5/extendTopBar.gif";
	 }
	 else
	 {
		 document.body.scroll = "no";
		 document.getElementById('space2Expand').height='0';
		 document.images['expandReduce'].src="icons/silverpeasV5/reductTopBar.gif";
		 
		 if (displayPDCFrame()) {
			 parent.setframevalue("rows", "115,100%,26,*,*,*");
		 } else {
			 parent.setframevalue("rows", "115,100%,26,*,*");
		 }
	 }
	 document.images['expandReduce'].blur();
	 navVisible = !navVisible;
}

function goToItem(spaceId, subSpaceId, componentId, url, itemId, reloadPage)
{
	top.bottomFrame.SpacesBar.location = getDomainsBarPage()+"?privateDomain="+spaceId+"&privateSubDomain="+subSpaceId+"&component_id="+componentId+"&FromTopBar=1";
    top.bottomFrame.MyMain.location = url;
    
    if (reloadPage)
    {
    	location.href = getTopBarPage()+"?ComponentId="+componentId+"&SpaceId="+spaceId;
    }
    else
    {
		//unactivate all items    
	    var tr = document.getElementById('item'+itemId).parentNode;
	    if (tr.hasChildNodes())
	    {
	    	var children = tr.childNodes;
	  		for (var i = 0; i < children.length; i++) 
	  		{
	   			var child = children[i];
	   			if (child.id != null && (child.id.substring(0,4) == "item"))
	   			{
	   				child.className = "";
	   			}
	  		}
	    }
	    
	    //activate item
	    document.getElementById('item'+itemId).className = "activeShortcut";
    }
}