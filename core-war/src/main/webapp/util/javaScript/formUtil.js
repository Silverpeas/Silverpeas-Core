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
<!--
//
// Gestion des evenements et des liste de checkbox
//
//

var nav4 = window.Event ? true : false;
var gControlKeyDown, gShiftKeyDown, gAltKeyDown;


//-------------------------------------------------------------------------------------isAltKeyDown
function isAltKeyDown (e) {
  if (nav4) {
     isDown = e.modifiers & Event.ALT_MASK;
  } else {
     isDown = e.altKey;
  }
  return isDown;
}

//-------------------------------------------------------------------------------------isControlKeyDown
function isControlKeyDown (e) {
  if (nav4) {
     isDown = e.modifiers & Event.CONTROL_MASK;
  } else {
     isDown = e.ctrlKey;
  }
  return isDown;
}

//-------------------------------------------------------------------------------------isShiftKeyDown
function isShiftKeyDown (e) {
  if (nav4) {
     isDown = e.modifiers & Event.SHIFT_MASK;
  } else {
     isDown = e.shiftKey;
  }
  return isDown;
}

//-------------------------------------------------------------------------------------SwitchSelection
// Selection global des checkbox d'un formulaire
//
// Quand tout est selectionne : tout deselectionner
// Sinon : tout selectionner
//
// La touche control modifie le fonctionnement :
//    chaque item est inverse
//
// Parametres:
//   theForm : formulaire qui contient les input
//   itemName : cette methode ne traite que les input
//              dont le nom commence par itemName
//   e : event
//
// ex:
// <A HREF="javascript:void(0)"
//       onMouseDown="return SwitchSelection(quickInfoForm, 'quickinfoId', event)"
//       onClick="return false" >all</A>
//
function SwitchSelection (theForm, itemName, e) {
	max = theForm.elements.length;
	L = itemName.length;
	//status = "";
	if (isControlKeyDown (e)) {
		for (i = 0; i < max; i++) {
			elt = theForm.elements[i];
			//status = status + ", " + elt.name.substring (0,L);
			if ((elt.type == "checkbox")&& (elt.name.substring (0,L) == itemName))
				elt.checked = !elt.checked;
		}
	} else {
		allChecked = true
		for (i = 0; i < max; i++) {
			elt = theForm.elements[i];
			if ((elt.type == "checkbox")&& (elt.name.substring (0,L) == itemName))
				allChecked = allChecked && elt.checked;
		}
		//status = allChecked
		for (i = 0; i < max; i++) {
			elt = theForm.elements[i];
			if ((elt.type == "checkbox")&& (elt.name.substring (0,L) == itemName))
				elt.checked = !allChecked;
		}
	}
	return false;
}


//-->
