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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
// whitespace characters
const whitespace = " \t\n\r";

/**
 * Check whether string s is empty.
 * @param {string} s
 */
function isEmpty(s) {
	return ((s === null) || (s === undefined) || (s.length === 0));
}
/**
 * Returns true if string s is empty or
 * whitespace characters only.
 * @param {string} s
 */
function isWhitespace (s) {
    // Is s empty?
    if (isEmpty(s)) return true;

    // Search through string's characters one by one
    // until we find a non-whitespace character.
    // When we do, return false; if we don't, return true.
    for (let i = 0; i < s.length; i++) {
        // Check that current character isn't whitespace.
        const c = s.charAt(i);
        if (whitespace.indexOf(c) < 0) return false;
    }

    // All characters are whitespace.
    return true;
}

/**
 * Removes all characters which appear in string bag from string s.
 * @param {string} s
 * @param {string} bag
 * @returns {string}
 */
function stripCharsInBag (s, bag) {

    let returnString = "";

    // Search through string's characters one by one.
    // If character is not in bag, append to returnString.
    for (let i = 0; i < s.length; i++) {
        // Check that current character isn't whitespace.
        const c = s.charAt(i);
        if (bag.indexOf(c) < 0) returnString += c;
    }

    return returnString;
}



/**
 * Removes all characters which do NOT appear in string bag from string s
 * @param {string} s
 * @param {string} bag
 * @returns {string}
 */
function stripCharsNotInBag (s, bag) {
    let returnString = "";

    // Search through string's characters one by one.
    // If character is in bag, append to returnString.

    for (let i = 0; i < s.length; i++)
    {
        // Check that current character isn't whitespace.
        const c = s.charAt(i);
        if (bag.indexOf(c) >= 0) returnString += c;
    }

    return returnString;
}



/**
 *
 * Removes all whitespace characters from s.
 * Global variable whitespace (see above)
 * defines which characters are considered whitespace.
 * @param {string} s
 * @returns {string}
 */
function stripWhitespace (s) {
	return stripCharsInBag (s, whitespace);
}






/**
 * Removes initial (leading) whitespace characters from s.
 * Global variable whitespace (see above)
 * defines which characters are considered whitespace.
 * @param {string} s
 * @returns {string}
 */
function stripInitialWhitespace (s) {
    let i = 0;
    while ((i < s.length) && whitespace.includes(s.charAt(i)))
       i++;

    return s.substring (i, s.length);
}


/**
 * Return true if length of s is < textFieldLength
 * @param {string|HTMLInputElement} input
 * @param {number} textFieldLength
 * @returns {boolean}
 */
function isValidText(input, textFieldLength) {
    const s = typeof input === 'string' ? input : input.value;
    if (typeof s !== 'undefined') {
		return s.length <= Number(textFieldLength);
	}
	return true;
}

/**
 * Return true if length of s is < at an expected fixed length (1000 characters)
 * @param {HTMLInputElement} input
 * @returns {boolean}
 */
function isValidTextField(input) {
    const textFieldLength = 1000;
    return isValidText(input, textFieldLength);
}

/**
 * Return true if length of s is < at an expected fixed length (2000 characters)
 * @param {HTMLInputElement} input
 * @returns {boolean}
 */
function isValidTextArea(input) {
    const textAreaLength = 2000;
    let s = input.value;
    if (s === null || s === undefined) {
      s = input;
    }
	return (s.length <= textAreaLength);
}

/**
 * Return true if length of s is < at an expected fixed length (4000 characters)
 * @param {HTMLInputElement} input
 * @returns {boolean}
 */
function isValidTextMaxi(input) {
    const textMaxiLength = 4000;
    const s = typeof input === 'string' ? input : input.value;
    if (typeof s !== 'undefined') {
		return (s.length <= textMaxiLength);
	}
	return true;
}


/**
 * Notify user that required field theField is empty.
 * String s describes expected contents of theField.value.
 * Put focus in theField and return false.
 * @param {HTMLInputElement} theField
 * @param {string} s
 * @returns {boolean}
 */
function warnEmpty (theField, s)
{   theField.focus();
    notyWarning(s);
    return false;
}

/**
 * Check that string theField.value is not all whitespace.
 * For explanation of optional argument emptyOK,
 * see comments of function isInteger.
 * @param {HTMLInputElement} theField
 * @param {string} s
 * @returns {boolean}
 */
function checkString (theField, s)
{   // Next line is needed on NN3 to avoid "undefined is not a number" error
    // in equality comparison below.
    if (isWhitespace(theField.value))
       return warnEmpty (theField, s);
    else return true;
}

/**
 * Return true if the field is numeric
 * @param {string} field a field text
 * @returns {boolean}
 */
function isNumericField(field)
{
    const validChars = "0123456789.,";
    for(let i=0; i<field.length; i++ ) {
      if (validChars.indexOf(field.charAt(i)) < 0) return false;
    }
	return true;
}

function formatNumericField(field)
{
    const regExp = /,/gi;
    return field.replace(regExp,'.');
}

/**
 * Return true if the field is an integer
 * @param {string} field
 * @returns {boolean}
 */
function isInteger(field)
{
    const validChars = "0123456789";
    for(let i=0; i<field.length; i++ ) {
      if (validChars.indexOf(field.charAt(i)) < 0) return false;
    }
    return true;
}

function checkHour(hour)
{
	if (isWhitespace(hour))
		return true;
	else
	{
        const e = /(([01]\d)|(2[0-3])):([0-5]\d)/;
        return e.test(hour);
	}
}

function checkemail(email) {
    const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return !(email === '' || !re.test(email));
}

/**
 * Verifies some transversal rules about file upload.
 * @param {File} mediaFile the instance of the file input into the DOM.
 * @returns {Promise<*>} which is rejected on upload problem detected, resolved otherwise.
 */
window.verifyFileUpload = function(file){
    const data = new FormData();
    data.append('fullPath', file.fullPath);
    data.append('name', file.name);
    data.append('size', file.size);
    return silverpeasAjax({
        method: 'POST',
        url: (webContext + '/services/fileUpload/verify'),
        data: data
    });
}

/**
 * Verifies the given file input against some transversal rules about file upload.
 * If the input is a multifile one, then each file is verified.
 * @param {HTMLInputElement} $fileInput the instance of the input file field into the DOM.
 * @returns {Promise<*>} which is rejected if at least one file specified by the input generates an
 * upload problem, resolved otherwise.
 */
window.verifyFileUploadOfInput = function($fileInput){
    const files = [];
    Array.prototype.push.apply(files, $fileInput.files);
    return sp.promise.whenAllResolved(files.map(function(file) {
        return verifyFileUpload(file);
    }));
}
