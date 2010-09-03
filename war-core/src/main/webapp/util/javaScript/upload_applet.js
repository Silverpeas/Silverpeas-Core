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
function hideApplet(divId)
{
  try
  {
    var appletObj = document.getElementById("applet"+divId);
    appletObj.setAttribute("height", "1");
  } catch (e) {
  }
}

function showApplet(divId)
{
  try
  {
    var appletObj = document.getElementById("applet"+divId);
    appletObj.setAttribute("height", "70");
  } catch (e) {
  }
}

function loadApplet(divId, targetURL, message, max_upload, webcontext, altMessage)
{
  var divDND = document.getElementById(divId);

  var _info = navigator.userAgent;
  var ie = (_info.indexOf("MSIE") > 0);
  var win = (_info.indexOf("Win") > 0);

  var objectDND;
  if (win)
  {
    objectDND = document.createElement("object");

    if (!ie)
    {
      objectDND.setAttribute("id", "applet"+divId);
      objectDND.setAttribute("width", "100%");
      objectDND.setAttribute("height", "70");
      objectDND.setAttribute("type", "application/x-java-applet;version=1.6");
    }
    addParam(objectDND, "archive", "wjhk.jupload.jar,jakarta-commons-net.jar");
    addParam(objectDND, "code", "wjhk.jupload2.JUploadApplet");
    addParam(objectDND, "name", "Silverpeas Drag And Drop");
    addParam(objectDND, "codebase", webcontext + "/upload");
  }
  else
  {
    /* mac and linux */
    objectDND = document.createElement("applet");
    objectDND.setAttribute("id", "applet"+divId);
    objectDND.setAttribute("width", "100%");
    objectDND.setAttribute("height", "70");
    objectDND.setAttribute("codebase", webcontext + "/upload");
    objectDND.setAttribute("archive", "wjhk.jupload.jar,jakarta-commons-net.jar");
    objectDND.setAttribute("code", "wjhk.jupload2.JUploadApplet");
    objectDND.setAttribute("name", "Silverpeas Drag And Drop");
    objectDND.setAttribute("hspace", "0");
    objectDND.setAttribute("vspace", "0");
    objectDND.setAttribute("MAYSCRIPT", "yes");
    objectDND.setAttribute("align", "middle");
    objectDND.setAttribute("alt", altMessage);
  }

  addParam(objectDND, "postURL", targetURL);
  addParam(objectDND, "message", message);
  addParam(objectDND, "uploadPolicy", "SilverpeasUploadPolicy");
  addParam(objectDND, "showLogWindow", "false");
  addParam(objectDND, "showStatusBar", "true");
  addParam(objectDND, "afterUploadURL", "javascript:uploadCompleted('%message%')");
  addParam(objectDND, "afterUploadURL", "javascript:uploadCompleted('%message%')");
  addParam(objectDND, "ftpCreateDirectoryStructure", "true");
  addParam(objectDND, "maxFileSize", max_upload);
  addParam(objectDND, "bgcolor_r", "245");
  addParam(objectDND, "bgcolor_g", "245");
  addParam(objectDND, "bgcolor_b", "245");
  
  if (win) {
	  try {
		  var alternatetext = document.createTextNode(altMessage);
		  objectDND.appendChild(alternatetext);	
	  } catch (e) {
	  }
  }
  
  divDND.appendChild(objectDND);

  if (ie)
  {
    objectDND.setAttribute("id", "applet"+divId);
    objectDND.setAttribute("width", "100%");
    objectDND.setAttribute("height", "70");
    objectDND.setAttribute("classid", "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93");
  }
}

function addParam(object, paramName, paramValue)
{
  var param = document.createElement("param");
  param.setAttribute("name", paramName);
  param.setAttribute("value", paramValue);

  object.appendChild(param);
}