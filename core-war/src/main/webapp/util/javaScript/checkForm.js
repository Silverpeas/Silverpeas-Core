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
// whitespace characters
var whitespace = " \t\n\r";


// Check whether string s is empty.
function isEmpty(s) {
	return ((s === null) || (s === undefined) || (s.length === 0));
}

// Returns true if string s is empty or
// whitespace characters only.
function isWhitespace (s) {

	var i;

    // Is s empty?
    if (isEmpty(s)) return true;

    // Search through string's characters one by one
    // until we find a non-whitespace character.
    // When we do, return false; if we don't, return true.
    for (i = 0; i < s.length; i++) {
        // Check that current character isn't whitespace.
        var c = s.charAt(i);
        if (whitespace.indexOf(c) < 0) return false;
    }

    // All characters are whitespace.
    return true;
}

// Removes all characters which appear in string bag from string s.
function stripCharsInBag (s, bag) {

	var i;
    var returnString = "";

    // Search through string's characters one by one.
    // If character is not in bag, append to returnString.
    for (i = 0; i < s.length; i++) {
        // Check that current character isn't whitespace.
        var c = s.charAt(i);
        if (bag.indexOf(c) < 0) returnString += c;
    }

    return returnString;
}



// Removes all characters which do NOT appear in string bag
// from string s.
function stripCharsNotInBag (s, bag) {
	var i;
    var returnString = "";

    // Search through string's characters one by one.
    // If character is in bag, append to returnString.

    for (i = 0; i < s.length; i++)
    {
        // Check that current character isn't whitespace.
        var c = s.charAt(i);
        if (bag.indexOf(c) >= 0) returnString += c;
    }

    return returnString;
}



// Removes all whitespace characters from s.
// Global variable whitespace (see above)
// defines which characters are considered whitespace.

function stripWhitespace (s) {
	return stripCharsInBag (s, whitespace);
}




// WORKAROUND FUNCTION FOR NAVIGATOR 2.0.2 COMPATIBILITY.
//
// The below function *should* be unnecessary.  In general,
// avoid using it.  Use the standard method indexOf instead.
//
// However, because of an apparent bug in indexOf on
// Navigator 2.0.2, the below loop does not work as the
// body of stripInitialWhitespace:
//
// while ((i < s.length) && (whitespace.indexOf(s.charAt(i)) != -1))
//   i++;
//
// ... so we provide this workaround function charInString
// instead.
//
// charInString (CHARACTER c, STRING s)
//
// Returns true if single character c (actually a string)
// is contained within string s.
function charInString (c, s) {
	for (i = 0; i < s.length; i++) {
		if (s.charAt(i) === c) return true;
    }
    return false;
}



// Removes initial (leading) whitespace characters from s.
// Global variable whitespace (see above)
// defines which characters are considered whitespace.
function stripInitialWhitespace (s) {
	var i = 0;
    while ((i < s.length) && charInString (s.charAt(i), whitespace))
       i++;

    return s.substring (i, s.length);
}

//return true if length of s is < textFieldLength
function isValidText(input, textFieldLength) {
	var s = input.value;
	if (typeof s !== 'undefined') {
		return s.length <= Number(textFieldLength);
	}
	return true;
}

//return true if length of s is < textFieldLength
function isValidTextField(input) {
	var textFieldLength = 1000;
	return isValidText(input, textFieldLength);
}

//return true if length of s is < textAreaLength
function isValidTextArea(input) {
	var textAreaLength = 2000;
	var s = input.value;
    if (s === null || s === undefined) {
      s = input;
    }
//	input.select();
	return (s.length <= textAreaLength);
}

//return true if length of s is < textMaxiLength
function isValidTextMaxi(input) {
	var textMaxiLength = 4000;
	var s = input.value;
	if (typeof s !== 'undefined') {
		return (s.length <= textMaxiLength);
	}
	return true;
}

// Notify user that required field theField is empty.
// String s describes expected contents of theField.value.
// Put focus in theField and return false.

function warnEmpty (theField, s)
{   theField.focus();
    alert(s);
    return false;
}

// checkString (TEXTFIELD theField, STRING s, [, BOOLEAN emptyOK==false])
//
// Check that string theField.value is not all whitespace.
//
// For explanation of optional argument emptyOK,
// see comments of function isInteger.

function checkString (theField, s)
{   // Next line is needed on NN3 to avoid "undefined is not a number" error
    // in equality comparison below.
    if (isWhitespace(theField.value))
       return warnEmpty (theField, s);
    else return true;
}

//return true if the field is numeric
function isNumericField(field)
{
    validChars = "0123456789.,";
    for( var i=0; i<field.length; i++ )
		if (validChars.indexOf(field.charAt(i)) < 0)
			return false;
	return true;
}

function formatNumericField(field)
{
	var regExp = /,/gi;
	return field.replace(regExp,'.');
}

//return true if the field is an integer
function isInteger(field)
{
    validChars = "0123456789";
    for( var i=0; i<field.length; i++ )
	if (validChars.indexOf(field.charAt(i)) < 0)
		return false;
    return true;
}

function checkHour(hour)
{
	if (isWhitespace(hour))
		return true;
	else
	{
		e = new RegExp("(([01][0-9])|(2[0-3]))[:]([0-5][0-9])");
		return e.test(hour);
	}
}

function checkemail(email) {
	var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	if (email === '' || !re.test(email)) {
	    return false;
	}
	return true;
}