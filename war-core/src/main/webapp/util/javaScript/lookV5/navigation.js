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
var currentSpaceId 		= "-1";
var currentSpaceLevel 	= 0;
var currentSpacePath 	= "";
var currentRootSpaceId	= "-1";
var currentComponentId	= "";
var currentAxisId		= "-1";
var currentValuePath	= "-1";
var displayMySpace		= "off";
var displayComponentIcons = false;

var currentLook			= "none";
var currentWallpaper	= "0";

var notContextualPDCDisplayed = false;
var notContextualPDCLoaded = false;

function openMySpace()
{
  	if (displayMySpace == "off")
  	{
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','SpaceId=spacePerso');
  		displayMySpace = "on";
  		
  		try
  		{
  			parent.MyMain.location.href=getContext()+getPersoHomepage();
  		}
  		catch (e)
  		{
  			parent.MyMain.location.href=getContext()+getHomepage();
  		}
  	}
  	else
  	{
  		var spaceContent = document.getElementById("contentSpace"+"spacePerso");
      	var space = spaceContent.parentNode;
      	space.removeChild(spaceContent);
      	
  		displayMySpace = "off";
  	}
}
  
function openSpace(spaceId, spaceLevel, spaceLook, spaceWallpaper)
{
  	//alert("spaceId = "+spaceId+", currentSpaceId = "+currentSpaceId);
  	//alert("spaceLevel = "+spaceLevel+", currentSpaceLevel = "+currentSpaceLevel);
  	//alert("currentSpacePath = "+currentSpacePath);
  	
  	//alert ("currentLook = "+currentLook+", spaceLook = "+spaceLook);
  	
  	if (spaceLook != currentLook)
  	{
  		top.location = getContext()+"/admin/jsp/MainFrameSilverpeasV5.jsp?RedirectToSpaceId="+spaceId;
  	}
  	
  	if (spaceWallpaper != currentWallpaper)
  	{
  		top.location = getContext()+"/admin/jsp/MainFrameSilverpeasV5.jsp?RedirectToSpaceId="+spaceId;
  	}
  	
  	closeCurrentComponent();
  	
  	if (currentSpaceId == spaceId)
  	{
  		closeSpace(spaceId, currentSpaceLevel, true);
  		
  		//Envoi de la requête pour afficher le contenu de l'espace
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','GetPDC='+displayPDC(),'SpaceId='+spaceId);
  	}
  	else
  	{
  		var closePDC = (spaceLevel == 0);
  		if (currentSpaceId != "-1" && spaceLevel == currentSpaceLevel)
  		{
  			closeSpace(currentSpaceId, currentSpaceLevel, closePDC);
  			
  			if (spaceLevel == 0)
  				hideTransverseSpace();
  		}
  		else
  		{
	   		if (spaceLevel == 0 && currentSpacePath.length > 0)
	   		{
	   			closeSpace(currentSpacePath.substring(0, currentSpacePath.indexOf("/")), 0, closePDC);
	   			hideTransverseSpace();
	   			currentSpacePath = spaceId;
	   		}
  		}
  		if (spaceLevel == currentSpaceLevel)
  			currentSpacePath = currentSpacePath.substring(0, currentSpacePath.lastIndexOf("/")+1)+spaceId;
  		if (spaceLevel > currentSpaceLevel)
  			currentSpacePath += "/"+spaceId;
  		
  		//alert("new currentSpacePath = "+currentSpacePath);
  		
  		try
  		{
	  		//Message temporaire de chargement
	  		var imgSpace = document.getElementById("img"+spaceId);
	  		imgSpace.setAttribute("src", "icons/silverpeasV5/loading.gif");
	  		imgSpace.setAttribute("width", "16");
	  		imgSpace.setAttribute("height", "22");
	  		imgSpace.setAttribute("align", "absmiddle");
  		} catch (e)
  		{
  		}
  		
  		//Envoi de la requête pour afficher le contenu de l'espace
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','GetPDC='+displayPDC(),'SpaceId='+spaceId);
  	}
  	
  	currentSpaceId = spaceId;
	currentSpaceLevel = spaceLevel;
  	 	
  	parent.MyMain.location.href=getContext()+getHomepage()+"?SpaceId="+spaceId;
  	
  	refreshPDCFrame();
  	refreshTopFrame();
  }

  function refreshPDCFrame()
  {
	  displayPDCFrame(currentSpaceId, currentComponentId);
  }
  
  function refreshTopFrame()
  {
	  try
	  {
		  top.topFrame.location.href=getContext()+"/admin/jsp/"+getTopBarPage();
	  }
	  catch (e)
	  {
		  //frame named 'pdcFrame' does not exist
	  }
  }
  
  function displayPDCFrame(spaceId, componentId)
  {
	  try
	  {
		  top.pdcFrame.location.href=getFooterPage()+"spaces="+spaceId+"&componentSearch="+componentId;
	  }
	  catch (e)
	  {
		  //frame named 'pdcFrame' does not exist
	  }
  }
  
  function hideTransverseSpace()
  {
	  try
	  {
		  document.getElementById("spaceTransverse").innerHTML = "";
		  document.getElementById("spaceTransverse").style.display = "none";
		  document.getElementById("basSpaceTransverse").style.display = "none";
	  }
	  catch (e)
	  {
		  //one of this elements are not present
	  }
  }
  
  function showTransverseSpace()
  {
	  try
	  {
		  document.getElementById("spaceTransverse").style.display = "";
		  document.getElementById("basSpaceTransverse").style.display = "";
	  }
	  catch (e)
	  {
		  //one of this elements are not present
	  }
  }
  
  function closeSpace(spaceId, spaceLevel, closePDC)
  {
  	//alert("closeSpace, id = "+spaceId);
  	//alert("closeSpace, spaceLevel = "+spaceLevel);
  	
  	if (spaceLevel == 0)
  	{
	  	var spaceHeader = document.getElementById(spaceId);
		spaceHeader.setAttribute("class", "spaceLevel1");
	  	spaceHeader.setAttribute("className", "spaceLevel1");
	}
  	 
  	try
  	{
  		var spaceContent = document.getElementById("contentSpace"+spaceId);
  		var space = spaceContent.parentNode;
  		space.removeChild(spaceContent);
  	}
  	catch (e)
  	{
  		//closed space have no content
  	}
  	
  	if (closePDC)
  	{
	   	removePDC();
  	}
  	
  	currentSpaceId = "-1";
  }
  
  function openComponent(componentId, componentLevel, componentURL)
  {
  	document.getElementById("img"+componentId).src = "icons/silverpeasV5/activComponent.gif";
  	document.getElementById("img"+componentId).width = "20";
  	document.getElementById("img"+componentId).height = "8";
  	
  	//Ajout DLE:
  	var componentActiv = document.getElementById(componentId);
		componentActiv.setAttribute("class", "browseComponentActiv");
	  componentActiv.setAttribute("className", "browseComponentActiv");
		//FiN DLE
		
  	if (componentId != currentComponentId)
	  	closeCurrentComponent();
  	
  	currentAxisId = "-1";
  	currentValuePath = "-1";
  	
  	currentComponentId = componentId;
  	
  	if (componentURL.substring(0,11).toLowerCase() != "javascript:")
  		parent.MyMain.location.href=getContext()+componentURL;
  	else
  		eval(componentURL);
  	  	  	
  	//Envoi de la requête pour afficher le plan de classement du composant
	ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','GetPDC='+displayPDC(),'ComponentId='+currentComponentId);
	
	refreshPDCFrame();
  	refreshTopFrame();
  }
  
  function closeCurrentComponent()
  {
  	if (currentComponentId != "")
  	{
	   	document.getElementById("img"+currentComponentId).src = "icons/1px.gif";
	   	document.getElementById("img"+currentComponentId).width = "1";
	   	document.getElementById("img"+currentComponentId).height = "1";

  	//Ajout DLE:
  	var componentActiv = document.getElementById(currentComponentId);
		componentActiv.setAttribute("class", "browseComponent");
	  componentActiv.setAttribute("className", "browseComponent");
		//FiN DLE
	   		    	
	   	currentComponentId = "";
  	}
  }
  
  function pdcAxisExpand(axisId)
  {
  	if (currentAxisId != "-1")
  		pdcAxisCollapse(currentAxisId);
  	
  	currentAxisId = axisId;
  	currentValuePath = "/0/";
  	
  	var img = document.getElementById("imgAxis"+axisId);
  	img.setAttribute("src", "icons/silverpeasV5/loading.gif");
  	img.setAttribute("width", "16");
  	img.setAttribute("height", "22");
  	img.setAttribute("align", "absmiddle");
  	document.getElementById("jsAxis"+axisId).setAttribute("href", "javaScript:pdcAxisCollapse('"+axisId+"')");
  	
  	//Envoi de la requête pour afficher le contenu de l'axe
  	if (isPDCContextual())
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','SpaceId='+currentSpaceId,'ComponentId='+currentComponentId,'AxisId='+axisId,'ValuePath='+currentValuePath);
  	else
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','AxisId='+axisId,'ValuePath='+currentValuePath);
  }
  
  function pdcAxisCollapse(axisId)
  {
  	currentAxisId = "-1";
  	currentValuePath = "-1";
  	
  	document.getElementById("imgAxis"+axisId).setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
  	document.getElementById("jsAxis"+axisId).setAttribute("href", "javaScript:pdcAxisExpand('"+axisId+"')");
  	
	var value = document.getElementById("axisContent"+axisId).firstChild;
	while (value != null)
	{
		document.getElementById("axisContent"+axisId).removeChild(value);
		value = document.getElementById("axisContent"+axisId).firstChild;
	}
  }
  
  function pdcAxisSearch(axisId)
  {
  	currentValuePath="/0/";
  	var query = getContext()+"/RpdcSearch/jsp/showaxishfromhomepage?AxisId="+axisId+"&ValueId="+currentValuePath;
  	if (isPDCContextual())
  		query += "&component_id="+currentComponentId+"&space_id="+currentSpaceId;
  	
  	parent.MyMain.location.href=query;
  }
  
  function pdcValueSearch(valuePath)
  {
	  var query = getContext()+"/RpdcSearch/jsp/showaxishfromhomepage?AxisId="+currentAxisId+"&ValueId="+valuePath;
	  if (isPDCContextual())
		  query += "&component_id="+currentComponentId+"&space_id="+currentSpaceId;
	  
	  parent.MyMain.location.href=query;
  }
  
  function pdcValueExpand(valuePath)
  {
  	currentValuePath = valuePath;
  	
  	var img = document.getElementById("imgValue"+valuePath);
  	img.setAttribute("src", "icons/silverpeasV5/loading.gif");
  	img.setAttribute("width", "16");
  	img.setAttribute("height", "22");
  	img.setAttribute("align", "absmiddle");
  	document.getElementById("jsValue"+valuePath).setAttribute("href", "javaScript:pdcValueCollapse('"+valuePath+"')");
  	
  	//Envoi de la requête pour afficher le contenu de la valeur de l'axe
  	if (isPDCContextual())
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','SpaceId='+currentSpaceId,'ComponentId='+currentComponentId,'AxisId='+currentAxisId,'ValuePath='+valuePath);
  	else
  		ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','AxisId='+currentAxisId,'ValuePath='+valuePath);
  }
  
  function pdcValueCollapse(valuePath)
  {
  	document.getElementById("imgValue"+valuePath).setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
  	document.getElementById("jsValue"+valuePath).setAttribute("href", "javaScript:pdcValueExpand('"+valuePath+"')");
  	
	var value = document.getElementById("valueContent"+valuePath).firstChild;
	while (value != null)
	{
		document.getElementById("valueContent"+valuePath).removeChild(value);
		value = document.getElementById("valueContent"+valuePath).firstChild;
	}
  }
  
  function displayPDCNotContextual()
  {
  	if (notContextualPDCDisplayed)
  	{
  		notContextualPDCDisplayed = false;
  		//emptyPDC();
  		document.getElementById("pdc").style.visibility = "hidden";
  	} 
  	else
  	{
		notContextualPDCDisplayed = true;
		document.getElementById("pdc").style.visibility = "visible";
		if (notContextualPDCLoaded == false)
		{
			notContextualPDCLoaded = true;
			ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=0','Pdc=1');
		}
  	}
  	return;
  }
  
  function emptyPDC()
  {
  	try
	{
		var pdc = document.getElementById("pdc");
		pdc.innerHTML = "";
  	}
	catch (e)
	{
	}
  }
  
  function isPDCContextual()
  {
	  var contextualPDC = true;
	  try {
		  contextualPDC = displayContextualPDC();
	  } catch(e) {}
	  return contextualPDC;
  }
  
  function removePDC()
  {
  	if (isPDCContextual())
  	{
  		try
	   	{
	   		var pdc = document.getElementById("pdc");
	   		space = pdc.parentNode;
	       	space.removeChild(pdc);
	   	}
	   	catch (e)
	   	{
	   		//closed space have no pdc
	   	}
	}
  }
  
  var spaceUpdater;

  Event.observe(window, 'load', function(){ 
  
  	currentLook			= getLook();
  	currentWallpaper 	= getWallpaper();
  	
  	hideTransverseSpace();
  	
  	spaceUpdater = new SpaceUpdater();
  	ajaxEngine.registerRequest( 'getSpaceInfo', getContext()+'/RAjaxSilverpeasV5/dummy' );
  	ajaxEngine.registerAjaxObject( 'spaceUpdater', spaceUpdater );
  	 	  	
  	ajaxEngine.sendRequest('getSpaceInfo','ResponseId=spaceUpdater','Init=1','GetPDC='+displayPDC(),'SpaceId='+getSpaceIdToInit(),'ComponentId='+getComponentIdToInit());
  	
  	displayPDCFrame(getSpaceIdToInit(), getComponentIdToInit());
  })

  var SpaceUpdater = Class.create();

  SpaceUpdater.prototype = {
     initialize: function() {
        this.useHighlighting    = true;
        this.lastPersonSelected = null;
     },
     ajaxUpdate: function(ajaxResponse) {
  	  // alert("in callBack");
  	   var nbElements = ajaxResponse.childNodes.length;
  	   if (ajaxResponse.childNodes[0].tagName == "spacePerso")
  	   {
  		   this.displayMySpace(ajaxResponse.childNodes[0]);
  	   }
  	   else
  	   {
	  	   if (currentSpaceId == "-1")
	  	   {
	  		   if (ajaxResponse.childNodes[0].tagName == "item")
			   {
	  			   //it's a transversal space
	  			   this.displaySpaceTransverse(ajaxResponse.childNodes[0], "true");
	  			   
	  			   //display others spaces
	  			   this.displayTree(ajaxResponse.childNodes[1]);
				   this.displayAxis(ajaxResponse.childNodes[2]);
			   }
	  		   else
	  		   {
		  		   if (ajaxResponse.childNodes[0].tagName == "spaces")
		  		   {
		  			   this.displayTree(ajaxResponse.childNodes[0]);
		  			   this.displayAxis(ajaxResponse.childNodes[1]);
		  		   }
		  		   else
		  		   {
		  			   if (ajaxResponse.childNodes[0].tagName == "pdc")
		  			   {
		  				 if (currentAxisId != "-1")
			   				   this.displayValue(ajaxResponse.childNodes[0]);
			   			   else
			   				   this.displayAxis(ajaxResponse.childNodes[0]);
		  			   }
		  		   }
	  		   }
	  	   } 
	  	   else
	  	   {
	  	   		if (currentSpaceId != -1)
	  	   		{
	  	   			//hide inProgress image
		  	   		var imgSpace = document.getElementById("img"+currentSpaceId);
		  	   		try
		  	   		{
			   			imgSpace.setAttribute("src", "icons/1px.gif");
			   			imgSpace.setAttribute("width", "0");
			   			imgSpace.setAttribute("height", "0");
		  	   		}
		  	   		catch (e)
		  	   		{
		  	   		}
		   		}
	  		   if (nbElements == 2)
	  		   {
	  			   var child = ajaxResponse.childNodes[0];
	  			   var type = child.getAttribute("type");
	  			   if (type=="spaceTransverse")
	  			   {
	  				   this.displaySpaceTransverse(ajaxResponse.childNodes[0]);
	  			   }
	  			   else
	  			   {
	  				   this.displaySpace(ajaxResponse.childNodes[0], "false");
	  			   }
	  			   this.displayAxis(ajaxResponse.childNodes[1]);
	  		   }
	  		   else
	  		   {
	  			   if (ajaxResponse.childNodes[0].tagName == "pdc")
	  			   {
	  			   	   if (currentAxisId != "-1")
		   				   this.displayValue(ajaxResponse.childNodes[0]);
		   			   else
		   				   this.displayAxis(ajaxResponse.childNodes[0]);
	  			   }
	  		   }
	  	   }
  	   }
     },
     displayTree: function(tree) {
  	   document.getElementById("spaces").innerHTML = "";
  	   var nbSpaces = tree.childNodes.length;
  	   //alert("nb spaces = "+nbSpaces);
  	   for (i=0; i<nbSpaces; i++)
  	   {
  		   var space = tree.childNodes[i];
  		   
  		   //create new entry
  		   var spaceId 		= space.getAttribute("id");
  		   var open			= space.getAttribute("open");
  		   var look			= space.getAttribute("look");
  		   var wallpaper	= space.getAttribute("wallpaper");
  		   
  		   var newSpaceURL = document.createElement("a");
  		   newSpaceURL.setAttribute("href", "javaScript:openSpace('"+spaceId+"', 0, '"+look+"', '"+wallpaper+"')");
  		   newSpaceURL.setAttribute("onfocus", "this.blur()");
		   newSpaceURL.setAttribute("class", "spaceURL");
  		   newSpaceURL.setAttribute("className", "spaceURL");
	   
	   	   var newSpaceLabel = document.createTextNode(space.getAttribute("name"));
  		   newSpaceURL.appendChild(newSpaceLabel);
  		   
  		   var imgSpace = document.createElement("img");
  		   imgSpace.setAttribute("id", "img"+spaceId);
  		   imgSpace.setAttribute("src", "icons/1px.gif");
  		   imgSpace.setAttribute("align", "absmiddle");
  		   imgSpace.setAttribute("border", "0");
  		   imgSpace.setAttribute("width", "0");
  		   imgSpace.setAttribute("height", "0");
  		   
  		   var newSpace = document.createElement("div");
  		   newSpace.setAttribute("id", spaceId);
	   	   newSpace.setAttribute("class", "spaceLevel1");
  		   newSpace.setAttribute("className", "spaceLevel1");
  		   newSpace.appendChild(imgSpace);
  		   newSpace.appendChild(newSpaceURL);

  		   //add new entry to list
  		   document.getElementById("spaces").appendChild(newSpace);
  		   
  		   if (open == "true")
  		   {
  		   	  currentRootSpaceId = spaceId;
  		      this.displaySpace(space, "true");
  		   }
  	   }
     },
     displaySpaceTransverse: function(space) {
		   document.getElementById("spaceTransverse").innerHTML = "";
		   showTransverseSpace();
		   
		   //create new entry
		   var spaceId 		= space.getAttribute("id");
		   var open			= "true";
		   var look			= space.getAttribute("look");
		   var wallpaper	= space.getAttribute("wallpaper");
		   
		   var newSpaceURL = document.createElement("a");
		   newSpaceURL.setAttribute("href", "javaScript:openSpace('"+spaceId+"', 0, '"+look+"', '"+wallpaper+"')");
		   newSpaceURL.setAttribute("onfocus", "this.blur()");
		   newSpaceURL.setAttribute("class", "spaceURL");
		   newSpaceURL.setAttribute("className", "spaceURL");
	   
	   	   	   var newSpaceLabel = document.createTextNode(space.getAttribute("name"));
		   newSpaceURL.appendChild(newSpaceLabel);
		   
		   var imgSpace = document.createElement("img");
		   imgSpace.setAttribute("id", "img"+spaceId);
		   imgSpace.setAttribute("src", "icons/1px.gif");
		   imgSpace.setAttribute("align", "absmiddle");
		   imgSpace.setAttribute("border", "0");
		   imgSpace.setAttribute("width", "0");
		   imgSpace.setAttribute("height", "0");
		   
		   var newSpace = document.createElement("div");
		   newSpace.setAttribute("id", spaceId);
	   	   newSpace.setAttribute("class", "spaceLevel1");
		   newSpace.setAttribute("className", "spaceLevel1");
		   newSpace.appendChild(imgSpace);
		   newSpace.appendChild(newSpaceURL);
	
		   //add new entry to list
		   document.getElementById("spaceTransverse").appendChild(newSpace);
		   
		   if (open == "true")
		   {
		   	  currentRootSpaceId = spaceId;
		      this.displaySpace(space, "true");
		   }
	   },
    displaySpace: function(spaceContent, init) {
  	   
  	   currentSpaceId 		= spaceContent.getAttribute("id");
	   currentSpaceLevel 	= parseInt(spaceContent.getAttribute("level"));
	   
	   //alert("currentSpaceLevel = "+currentSpaceLevel);
	   
	   var spaceHeader = document.getElementById(currentSpaceId);
	   if (currentSpaceLevel == 0)
	   {
	   		currentRootSpaceId = currentSpaceId;
	   		spaceHeader.setAttribute("class", "spaceLevel1On");
  	   		spaceHeader.setAttribute("className", "spaceLevel1On");
		   /*if (init == "false")
		   {
		   		spaceHeader.setAttribute("class", "spaceLevel1On");
	  	   		spaceHeader.setAttribute("className", "spaceLevel1On");
	  	   }
	  	   else
	  	   {
	  	   		spaceHeader.setAttribute("class", "spaceLevel1");
	  	   		spaceHeader.setAttribute("className", "spaceLevel1");
	  	   }*/
	   }
	   
	   if (init == "true")
	   {
	   	   if (currentSpacePath == "")
	   		   currentSpacePath = currentSpaceId;
	   	   else
	   		   currentSpacePath += "/"+currentSpaceId;
	   }
 	   
  	   var nbItems = spaceContent.childNodes.length;
  	   //alert("displaySpace, nbItems = "+nbItems);
  	   
  	   try
  	   {
  		   var spaceContentDiv = document.getElementById("contentSpace"+currentSpaceId);
  		   document.getElementById(currentSpaceId).removeChild(spaceContentDiv);
  	   } catch (e)
  	   {
  		   //the div does not exist
  	   }
  	   
  	   var spaceContentDiv = document.createElement("div");
  	   spaceContentDiv.setAttribute("id", "contentSpace"+currentSpaceId);
   	   spaceContentDiv.setAttribute("class", "contentSpace");
   	   spaceContentDiv.setAttribute("className", "contentSpace");
  		      	   
  	   document.getElementById(currentSpaceId).appendChild(spaceContentDiv);
  	   
  	   /*if (currentSpaceLevel == 0)
  	   {
  		   if (document.getElementById("pdc") == null)
  		   {
   		   var pdcDiv = document.createElement("div");
       	   pdcDiv.setAttribute("id", "pdc");
   		   document.getElementById(currentSpaceId).appendChild(pdcDiv);
  		   }
  	   }*/
  	   
  	   var item = spaceContent.firstChild;
  	   
  	   //alert("nb spaces = "+nbSpaces);
  	   while (item != null)
  	   {
  		   var itemId		= item.getAttribute("id");
  		   var itemLevel	= item.getAttribute("level");
  		   var itemKind		= item.getAttribute("kind");
  		   var itemType		= item.getAttribute("type");
  		   var itemOpen		= item.getAttribute("open");
  		   var itemURL		= item.getAttribute("url");
  		   
  		   //create new entry
  		   var newEntry = document.createElement("div");
  		   newEntry.setAttribute("id", itemId);
	   		if (itemType == "component") {
  			   newEntry.setAttribute("class", "browseComponent");
  			   newEntry.setAttribute("className", "browseComponent");			  
	   		} else {
  			   newEntry.setAttribute("class", "browseSpace");
  			   newEntry.setAttribute("className", "browseSpace");
  		   }

  		   var newEntryURL = document.createElement("a");
  		   newEntryURL.setAttribute("onfocus", "this.blur()");
  		   
  		   var newEntryIcon = document.createElement("img");
  		   newEntryIcon.setAttribute("align", "absmiddle");
  		   
  		   var newEntryIconSel  = document.createElement("img");
  		   
  		   if (itemType == "component") {    			   
      		   newEntryIconSel.setAttribute("id", "img"+itemId);
  			   if (itemOpen == "true")
  			   {
						newEntry.setAttribute("class", "browseComponentActiv");
  			   newEntry.setAttribute("className", "browseComponentActiv");			  
  				   newEntryIconSel.setAttribute("src", "icons/silverpeasV5/activComponent.gif");
  				   }
  			   else
  				   newEntryIconSel.setAttribute("src", "icons/1px.gif");
  			   newEntryURL.setAttribute("href", "javaScript:openComponent('"+itemId+"',"+itemLevel+",'"+itemURL+"')");
  			   
  			   if (displayComponentsIcons())
  			   {
  				   newEntryIcon.setAttribute("src", getContext()+"/util/icons/component/"+itemKind+"Small.gif");
  				   newEntryIcon.setAttribute("class", "browseIconComponent");
  				   newEntryIcon.setAttribute("className", "browseIconComponent");  				   
  			   }
  			   else
  			   {
  				   newEntryIcon.setAttribute("src", "icons/1px.gif");
  				}
  		   } else {
	  		   var look			= item.getAttribute("look");
	  		   var wallpaper	= item.getAttribute("wallpaper");
	  		   
  			   newEntryIcon.setAttribute("id", "img"+itemId);
  			   newEntryIcon.setAttribute("src", "icons/1px.gif");
  			   newEntryURL.setAttribute("href", "javaScript:openSpace('"+itemId+"',"+itemLevel+",'"+look+"', '"+wallpaper+"')");
		   	   /*newEntryURL.setAttribute("class", "browseSpace");
  			   newEntryURL.setAttribute("className", "browseSpace");*/
  		   }
  		   var newEntryLabel = document.createTextNode(item.getAttribute("name"));
  		   newEntryURL.appendChild(newEntryLabel);
  		  
  		   newEntry.appendChild(newEntryIcon);		   
  		   newEntry.appendChild(newEntryURL);
  		   
  		   if (itemType == "component")
  			   newEntry.appendChild(newEntryIconSel);

  		   //add new entry to list
  		   spaceContentDiv.appendChild(newEntry);
  		   
  		   if (itemOpen == "true")
  		   {
	   		   if(itemType == "space")
	   			   this.displaySpace(item, "true");
	   		   if(itemType == "component")
	   		   {
	   			   currentSpaceLevel 	= parseInt(itemLevel)-1;
	   			   currentComponentId	= itemId;
	   		   }
  		   }
  		   
  		   item = item.nextSibling;
  	   }
     },
     displayMySpace: function(spaceContent) {
  	   
  	   var spacePersoId = spaceContent.getAttribute("id");
 		   
  	   var nbItems = spaceContent.childNodes.length;
  	   //alert("displaySpace, nbItems = "+nbItems);
  	   
  	   try
  	   {
  		   var spaceContentDiv = document.getElementById("contentSpace"+spacePersoId);
  		   document.getElementById(spacePersoId).removeChild(spaceContentDiv);
  	   } catch (e)
  	   {
  		   //the div does not exist
  	   }
  	   
  	   var spaceContentDiv = document.createElement("div");
  	   spaceContentDiv.setAttribute("id", "contentSpace"+spacePersoId);
	   spaceContentDiv.setAttribute("class", "contentSpace");
	   spaceContentDiv.setAttribute("className", "contentSpace");
  	      	   
  	   document.getElementById(spacePersoId).appendChild(spaceContentDiv);
  	   
  	   var item = spaceContent.firstChild;   	   
  	   while (item != null)
  	   {
  		   var itemId		= item.getAttribute("id");
  		   var itemLevel	= item.getAttribute("level");
  		   var itemKind		= item.getAttribute("kind");
  		   var itemType		= item.getAttribute("type");
  		   var itemOpen		= item.getAttribute("open");
  		   var itemURL		= item.getAttribute("url");
  		   
  		   //create new entry
  		   var newEntry = document.createElement("div");
  		   newEntry.setAttribute("id", itemId);
		   newEntry.setAttribute("class", "browseComponent");
		   newEntry.setAttribute("className", "browseComponent");			  
	   
  		   var newEntryURL 		= document.createElement("a");
  		   newEntryURL.setAttribute("onfocus", "this.blur()");
  		   var newEntryIcon 	= document.createElement("img");
  		   var newEntryIconSel  = document.createElement("img");
  		   
  		   newEntryIconSel.setAttribute("id", "img"+itemId);
		   newEntryIconSel.setAttribute("src", "icons/1px.gif");
		   /*newEntryURL.setAttribute("class", "browseComponent");
		   newEntryURL.setAttribute("className", "browseComponent");*/
		   newEntryURL.setAttribute("href", "javaScript:openComponent('"+itemId+"',"+itemLevel+",'"+itemURL+"')");
	   
		   /*if (displayComponentsIcons())
			   newEntryIcon.setAttribute("src", getContext()+"/util/icons/component/"+itemKind+"Small.gif");
		   else*/
			   newEntryIcon.setAttribute("src", "icons/1px.gif");
			   
  		   var newEntryLabel = document.createTextNode(item.getAttribute("name"));
  		   newEntryURL.appendChild(newEntryLabel);
  		      		   
 		   newEntry.appendChild(newEntryIcon);
  		   newEntry.appendChild(newEntryURL);
  		   
  		   newEntry.appendChild(newEntryIconSel);

  		   //add new entry to list
  		   spaceContentDiv.appendChild(newEntry);
  		   
  		   item = item.nextSibling;
  	   }
     },
     displayAxis: function(axisList) {
     	removePDC();
  	   
  	   var nbAxis = axisList.childNodes.length;
  	   
  	   if (nbAxis > 0)
  	   {
  		   //if (currentSpaceLevel == 0)
      	   //{
      		   if (isPDCContextual() && document.getElementById("pdc") == null)
      		   {
	      		   	var pdcText = document.createTextNode(getPDCLabel());
	      		   	
      		   		var pdcLabel = document.createElement("div");
      		   		pdcLabel.setAttribute("id", "pdcLabel");
      		   		pdcLabel.appendChild(pdcText);
      		   		
  	    		   	var pdcDiv = document.createElement("div");
  	        	   	pdcDiv.setAttribute("id", "pdc");
  	        	   	pdcDiv.appendChild(pdcLabel);
  	        	   	
  	    		   	document.getElementById(currentRootSpaceId).appendChild(pdcDiv);
      		   }
      	   //}
  	   }
  	   
  	   var axis = axisList.firstChild;
  	   //alert("nb spaces = "+nbSpaces);
  	   while (axis != null)
  	   {
  		   //create new entry
  		   var axisId 	= axis.getAttribute("id");
  		   var nbObj	= axis.getAttribute("nbObjects");
  		   
  		   var axisURL = document.createElement("a");
  		   axisURL.setAttribute("href", "javaScript:pdcAxisSearch('"+axisId+"')");
  		   axisURL.setAttribute("onfocus", "this.blur()");
	   	   axisURL.setAttribute("class", "browseAxis");
  		   axisURL.setAttribute("className", "browseAxis");
  		   
  		   var iconURL = document.createElement("a");
  		   iconURL.setAttribute("id", "jsAxis"+axisId);
  		   iconURL.setAttribute("href", "javaScript:pdcAxisExpand('"+axisId+"')");
  		   iconURL.setAttribute("onfocus", "this.blur()");
  		   
  		   var icon = document.createElement("img");
  		   icon.setAttribute("id", "imgAxis"+axisId);
  		   icon.setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
  		   icon.setAttribute("align", "absmiddle");
  		   icon.setAttribute("border", "0");
  		   icon.setAttribute("width", "15");
  		   icon.setAttribute("height", "15");
  		   icon.setAttribute("onfocus", "this.blur()");
  		   
  		   iconURL.appendChild(icon);
  		   
  		   var axisLabel = document.createTextNode(axis.getAttribute("name")+" ("+nbObj+")");
  		   axisURL.appendChild(axisLabel);
  		   
  		   //var axisClass = document.createElement("span");
  		   //axisClass.setAttribute("class", "browseAxis");
  		   //axisClass.setAttribute("className", "browseAxis");
  		   //axisClass.appendChild(axisURL);
  		   
  		   var newAxis = document.createElement("div");
  		   newAxis.setAttribute("id", "axis"+axisId);
  		   newAxis.appendChild(iconURL);
  		   //newAxis.appendChild(axisClass);
  		   newAxis.appendChild(axisURL);

  		   var newAxisContent = document.createElement("div");
  		   newAxisContent.setAttribute("id", "axisContent"+axisId);
  		   
  		   //add new entry to list
  		   document.getElementById("pdc").appendChild(newAxis);
  		   document.getElementById("pdc").appendChild(newAxisContent);
  		   
  		   axis = axis.nextSibling;
  	   }
     },
     displayValue: function(values) {
  	   var nbValue = values.childNodes.length;
  	   var value = values.firstChild;
  	   //alert("nb spaces = "+nbSpaces);
  	   
  	   var valueIds = currentValuePath.split("/");
   		//alert(valueIds);
  	   
  	   while (value != null)
  	   {
  		   //create new entry
  		   var valuePath 	= value.getAttribute("id");
  		   var nbObj		= value.getAttribute("nbObjects");
  		   var valueLevel	= parseInt(value.getAttribute("level"));
  		   
  		   var valueURL = document.createElement("a");
  		   valueURL.setAttribute("href", "javaScript:pdcValueSearch('"+valuePath+"')");
  		   valueURL.setAttribute("onfocus", "this.blur()");
	   	   valueURL.setAttribute("class", "browseValue");
  		   valueURL.setAttribute("className", "browseValue");
  		   
  		   var iconURL = document.createElement("a");
  		   iconURL.setAttribute("id", "jsValue"+valuePath);
  		   iconURL.setAttribute("href", "javaScript:pdcValueExpand('"+valuePath+"')");
  		   iconURL.setAttribute("onfocus", "this.blur()");

  		   var icon = document.createElement("img");
  		   icon.setAttribute("id", "imgValue"+valuePath);
  		   icon.setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
  		   icon.setAttribute("align", "absmiddle");
  		   icon.setAttribute("border", "0");
  		   icon.setAttribute("width", "15");
  		   icon.setAttribute("height", "15");
  		   
  		   iconURL.appendChild(icon);
  		   
  		   var valueLabel = document.createTextNode(value.getAttribute("name")+" ("+nbObj+")");
  		   valueURL.appendChild(valueLabel);
  		   
  		   //var valueClass = document.createElement("span");
  		   //valueClass.setAttribute("class", "browseValue");
  		   //valueClass.setAttribute("className", "browseValue");
  		   //valueClass.appendChild(valueURL);
  		   
  		   var newValue = document.createElement("div");
  		   newValue.setAttribute("id", "value"+valuePath);
  		   
  		   value = value.nextSibling;
  		       		   
  		   var iconT = document.createElement("img");
  		   if (value == null)
  			   iconT.setAttribute("src", getContext()+"/util/icons/minusTreeL.gif");
  		   else
  			   iconT.setAttribute("src", getContext()+"/util/icons/minusTreeT.gif");
  		   iconT.setAttribute("align", "absmiddle");
  		   iconT.setAttribute("border", "0");
  		   iconT.setAttribute("width", "15");
  		   iconT.setAttribute("height", "15");
  		   
  		   //var motherValuePath = valuePath.substring(0, valuePath.lastIndexOf("/")+1);
  		   //alert(motherValuePath);
  		   if (valueLevel > 1)
  		   {
	   		   var ancetre = "/0/";
	   		   for (v=0; v<valueIds.length; v++)
	   		   {
	   			   if (valueIds[v] != "" && valueIds[v] != "0")
	   			   {
	   				   //alert("valueIds[v] = "+valueIds[v]);
	   				   
	    			   ancetre += valueIds[v]+"/";
	    			   
	    			   //alert("ancetre = "+ancetre);
	    			   
	    			   var iconIndent = document.createElement("img");
	        		   //iconIndent.setAttribute("src", "<%=m_sContext%>/util/icons/minusTreeI.gif");
	        		   iconIndent.setAttribute("align", "absmiddle");
	        		   iconIndent.setAttribute("border", "0");
	        		   iconIndent.setAttribute("width", "15");
	        		   iconIndent.setAttribute("height", "15");
	    			
	    			   if (document.getElementById("value"+ancetre) != null && document.getElementById("value"+ancetre).nextSibling != null)
	    			   {
	        			   iconIndent.setAttribute("src", getContext()+"/util/icons/minusTreeI.gif");
	        			   iconIndent.setAttribute("width", "15");
		        		   iconIndent.setAttribute("height", "15");
	    			   }
	    			   else
	    			   {
	        			   iconIndent.setAttribute("src", "icons/1px.gif");
	        			   iconIndent.setAttribute("width", "15");
		        		   iconIndent.setAttribute("height", "15");
	    			   }
	    			   
	    			   newValue.appendChild(iconIndent);
	   			   }
	   		   }
  		   }
  		   
  		   newValue.appendChild(iconT);
  		   newValue.appendChild(iconURL);
  		   //newValue.appendChild(valueClass);
  		   newValue.appendChild(valueURL);

  		   var newValueContent = document.createElement("div");
  		   newValueContent.setAttribute("id", "valueContent"+valuePath);
  		   
  		   newValue.appendChild(newValueContent);
  		   
  		   //add new entry to list
  		   if (currentValuePath != "-1" && currentValuePath != "/0/")
  			   document.getElementById("valueContent"+currentValuePath).appendChild(newValue);
  		   else
  			   document.getElementById("axisContent"+currentAxisId).appendChild(newValue);
  	   }
  	   
  	   var img = null;
  	   if (currentValuePath == "/0/")
  	   {
  		   img = document.getElementById("imgAxis"+currentAxisId);
  		   img.setAttribute("src", "icons/silverpeasV5/pdcPeas_minimize.gif");
  	   } 
  	   else
  	   {
  		   img = document.getElementById("imgValue"+currentValuePath);
  		   img.setAttribute("src", "icons/silverpeasV5/pdcPeas_minimize.gif");
  	   }
  	   img.setAttribute("width", "15");
  	   img.setAttribute("height", "15");
     }
  };