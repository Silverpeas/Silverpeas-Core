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

function loadApplet(divId, webcontext, sessionId, syncUrl, altMessage)
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
      objectDND.setAttribute("width", "1");
      objectDND.setAttribute("height", "1");
      objectDND.setAttribute("type", "application/x-java-applet;version=1.7");
    }
    addParam(objectDND, "archive", "outlook.jar,commons-io.jar,commons-lang3.jar,jackson-mapper-lgpl.jar,jackson-core-lgpl.jar,jacob.jar");
    addParam(objectDND, "code", "org.silverpeas.calendar.outlook.ImportEventsApplet");
    addParam(objectDND, "name", "Silverpeas Outlook Import Applet");
    addParam(objectDND, "codebase", webcontext + "/outlook");
  }
  else
  {
    /* mac and linux */
    objectDND = document.createElement("applet");
    objectDND.setAttribute("id", "applet"+divId);
    objectDND.setAttribute("width", "1");
    objectDND.setAttribute("height", "1");
    objectDND.setAttribute("codebase", webcontext + "/outlook");
    objectDND.setAttribute("archive", "outlook.jar,commons-io.jar,commons-lang3.jar,jackson-mapper-lgpl.jar,jackson-core-lgpl.jar,jacob.jar");
    objectDND.setAttribute("code", "org.silverpeas.calendar.outlook.ImportEventsApplet");
    objectDND.setAttribute("name", "Silverpeas Drag And Drop");
    objectDND.setAttribute("SESSIONID", "0");
    objectDND.setAttribute("SERVLETURL", "0");
    objectDND.setAttribute("alt", altMessage);
  }
  addParam(objectDND, "SESSIONID", sessionId);
  addParam(objectDND, "SERVLETURL", syncUrl);

  if (win) {
	  try {
		  var alternatetext = document.createTextNode(altMessage);
		  objectDND.appendChild(alternatetext);
	  } catch (e) {
      if (typeof console !== 'undefined') {
        console.log(e);
      }
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