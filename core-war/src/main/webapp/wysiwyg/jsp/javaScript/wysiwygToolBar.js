/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

let editorName;
let storageFileWindow = window;
let galleryWindow = window;

function setEditorName(theEditorName) {
	editorName = theEditorName;
}

function getEditorName() {
	return editorName;
}

function getCKEditor() {
	const theEditorName = getEditorName();
	return CKEDITOR.instances[theEditorName];
}

function openStorageFileManager(editorName, componentId) {
  setEditorName(editorName);
  if (componentId) {
		const url = webContext + "/kmelia/jsp/attachmentLinkManagement.jsp?key=" + componentId
				+ "&ntype=COMPONENT";
		const windowName = "StorageFileWindow";
		const windowParams = "scrollbars=1,directories=0,menubar=0,toolbar=0, alwaysRaised";
		if (!storageFileWindow.closed && storageFileWindow.name === windowName) {
      storageFileWindow.close();
    }
    storageFileWindow = SP_openWindow(url, windowName, "750", "580", windowParams);
  }
}

function insertAttachmentLink(url, img, label){
	getCKEditor().insertHtml('<a href="'+url+'" target="_blank"><img src="'+img+'" width="20"' +
			' style="border: 0; vertical-align: top" alt="' + label + '"/> '+label+'</a> ');
}

function choixImage(editorName) {
	setEditorName(editorName);
	const index = document.getElementById("images").selectedIndex;
	const str = document.getElementById("images").options[index].value;

	if (index !== 0 && str !== null) {
		getCKEditor().insertHtml('<img src="'+str+'" alt=""/>');
	}
}

function openGalleryFileManager(editorName, componentId) {
	setEditorName(editorName);
	if (!componentId) {
		const index = document.getElementById("galleryFile").selectedIndex;
		componentId = document.getElementById("galleryFile").options[index].value;
  }
	if (componentId) {
		const url = webContext + "/gallery/jsp/wysiwygBrowser.jsp?ComponentId=" + componentId + "&Language=" + getUserLanguage();
		const windowName = "galleryWindow";
		const larg = "820";
		const haut = "600";
		const windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
		if (!galleryWindow.closed && galleryWindow.name === "galleryWindow") {
			galleryWindow.close();
		}
		galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
}

function choixImageInGallery(url) {
	getCKEditor().insertHtml('<img src="'+url+'" alt=""/>');
}
