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

var editorName;
var storageFileWindow = window;
var galleryWindow = window;

function setEditorName(theEditorName) {
	editorName = theEditorName;
}

function getEditorName() {
	return editorName;
}

function getCKEditor() {
	var theEditorName = getEditorName();
	return CKEDITOR.instances[theEditorName];
}

function openStorageFileManager(editorName, context, nodeType) {
	setEditorName(editorName);
	var index = document.getElementById("storageFile").selectedIndex;
	var componentId = document.getElementById("storageFile").options[index].value;
	if (index != 0) {
		var url = context+"/kmelia/jsp/attachmentLinkManagement.jsp?key="+componentId+"&ntype="+nodeType;
		var windowName = "StorageFileWindow";
		var width = "750";
		var height = "580";
		var windowParams = "scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised";
		if (!storageFileWindow.closed && storageFileWindow.name==windowName) {
			storageFileWindow.close();
		}
		storageFileWindow = SP_openWindow(url, windowName, width, height, windowParams);
	}
}

function insertAttachmentLink(url, img, label){
	getCKEditor().insertHtml('<a href="'+url+'" target="_blank"><img src="'+img+'" width="20" border="0" align="top" alt=""/> '+label+'</a> ');
}

function choixImage(editorName) {
	setEditorName(editorName);
	var index = document.getElementById("images").selectedIndex;
	var str = document.getElementById("images").options[index].value;

	if (index != 0 && str != null) {
		getCKEditor().insertHtml('<img border="0" src="'+str+'" alt=""/>');
	}
}

function openGalleryFileManager(editorName, context, language) {
	setEditorName(editorName);
	var index = document.getElementById("galleryFile").selectedIndex;
	var componentId = document.getElementById("galleryFile").options[index].value;
	if (index != 0) {
		var url = context+"/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&amp;Language="+language;
		var windowName = "galleryWindow";
		var larg = "820";
		var haut = "600";
		var windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
		if (!galleryWindow.closed && galleryWindow.name=="galleryWindow") {
			galleryWindow.close();
		}
		galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
}

function choixImageInGallery(url) {
	getCKEditor().insertHtml('<img border="0" src="'+url+'" alt=""/>');
}

function chooseDynamicValuesdefault(editorName) {
	setEditorName(editorName);
	var index = document.getElementByName("dynamicValues").selectedIndex;
	var str = document.getElementByName("dynamicValues").options[index].value;
	if (index != 0 && str != null){
		getCKEditor().insertHtml('(%'+str+'%)');
	}
}
