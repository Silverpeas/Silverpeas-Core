// noinspection JSUnusedGlobalSymbols

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
//
// Management of the events and of the checkbox lists
//
//

const nav4 = !!window.Event;


//-------------------------------------------------------------------------------------isAltKeyDown
function isAltKeyDown (e) {
	let isDown;
  if (nav4) {
     isDown = e.modifiers & Event.ALT_MASK;
  } else {
     isDown = e.altKey;
  }
  return isDown;
}

//-------------------------------------------------------------------------------------isControlKeyDown
function isControlKeyDown (e) {
  let isDown;
  if (nav4) {
     isDown = e.modifiers & Event.CONTROL_MASK;
  } else {
     isDown = e.ctrlKey;
  }
  return isDown;
}

//-------------------------------------------------------------------------------------isShiftKeyDown
function isShiftKeyDown (e) {
  let isDown;
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
	if (isControlKeyDown (e)) {
		inverseIndividualCheckedState(theForm, itemName);
	} else {
		inverseGlobalCheckedState(theForm, itemName);
	}
	return false;
}

function inverseIndividualCheckedState(theForm, itemName) {
	const itemNameLength = itemName.length;
	theForm.elements.forEach(function(elt) {
		if ((elt.type === "checkbox") && (elt.name.substring(0, itemNameLength) === itemName)) {
			elt.checked = !elt.checked;
		}
	});
}

function inverseGlobalCheckedState(theForm, itemName) {
	const itemNameLength = itemName.length;
	let allChecked = true
	theForm.elements.forEach(function(elt) {
		if ((elt.type === "checkbox") && (elt.name.substring(0, itemNameLength) === itemName)) {
			allChecked = allChecked && elt.checked;
		}
	});
	theForm.elements.forEach(function(elt) {
		if ((elt.type === "checkbox") && (elt.name.substring(0, itemNameLength) === itemName)) {
			elt.checked = !allChecked;
		}
	});
}
