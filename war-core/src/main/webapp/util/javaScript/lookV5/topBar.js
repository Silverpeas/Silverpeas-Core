var navVisible = true;
function resizeFrame()
{
	if (displayPDCFrame())
	 	parent.resizeFrame('0,10,*,0,0,0');
	else
		parent.resizeFrame('0,10,*,0,0');
	
	 if (navVisible)
	 {
		 document.body.scroll = "no"; 
		 document.getElementById('space2Expand').height='10';
		 document.images['expandReduce'].src="icons/silverpeasV5/extendTopBar.gif";
	 }
	 else
	 {
		 document.body.scroll = "no";
		 document.getElementById('space2Expand').height='0';
		 document.images['expandReduce'].src="icons/silverpeasV5/reductTopBar.gif";
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