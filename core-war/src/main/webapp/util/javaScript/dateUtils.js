/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
function makeArray(n) {
//*** BUG: If I put this line in, I get two error messages:
//(1) Window.length can't be set by assignment
//(2) daysInMonth has no property indexed by 4
//If I leave it out, the code works fine.
//   this.length = n;
   for (var i = 1; i <= n; i++) {
      this[i] = 0
   }
   return this
}

var daysInMonth = new Array(13);
daysInMonth[1] = 31;
daysInMonth[2] = 29;   // must programmatically check this
daysInMonth[3] = 31;
daysInMonth[4] = 30;
daysInMonth[5] = 31;
daysInMonth[6] = 30;
daysInMonth[7] = 31;
daysInMonth[8] = 31;
daysInMonth[9] = 30;
daysInMonth[10] = 31;
daysInMonth[11] = 30;
daysInMonth[12] = 31;

function getDateSeparator(lang) {
	if (lang == "de") {
		return ".";
	}
	return "/";
}

// Global variable defaultEmptyOK defines default return value
// for many functions when they are passed the empty string.
// By default, they will return defaultEmptyOK.
//
// defaultEmptyOK is false, which means that by default,
// these functions will do "strict" validation.  Function
// isInteger, for example, will only return true if it is
// passed a string containing an integer; if it is passed
// the empty string, it will return false.
//
// You can change this default behavior globally (for all
// functions which use defaultEmptyOK) by changing the value
// of defaultEmptyOK.
//
// Most of these functions have an optional argument emptyOK
// which allows you to override the default behavior for
// the duration of a function call.
//
// This functionality is useful because it is possible to
// say "if the user puts anything in this field, it must
// be an integer (or a phone number, or a string, etc.),
// but it's OK to leave the field empty too."
// This is the case for fields which are optional but which
// must have a certain kind of content if filled in.
var defaultEmptyOK = false

function extractYear(date, language) {
  var d = date.split(getDateSeparator(language));
    return d[2];
}

function extractMonth(date, language) {
  var d = date.split(getDateSeparator(language));
    if (language == 'en')
        return d[0];
    else
        return d[1];
}

function extractDay(date, language) {
  var d = date.split(getDateSeparator(language));
    if (language == 'en')
        return d[1];
    else
        return d[0];
}

function extractHour(hour, language) {
  var d = hour.split(":");
    return d[0];
}

function extractMinute(hour, language) {
  var d = hour.split(":");
    return d[1];
}

function isDateOK(date, language) {
	var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
	if (language == "de") {
		re = /(\d\d\.\d\d\.\d\d\d\d)/i;
	}
	var year = extractYear(date, language);
    var month = extractMonth(date, language);
    var day = extractDay(date, language);
	return (date.replace(re, "OK") == "OK") && isCorrectDate(year, month, day);
}

function isDate1AfterDate2(date1, date2, language) {
	var year1 = extractYear(date1, language);
    var month1 = extractMonth(date1, language);
    var day1 = extractDay(date1, language);
    var year2 = extractYear(date2, language);
    var month2 = extractMonth(date2, language);
    var day2 = extractDay(date2, language);

    return isD1AfterD2(year1, month1, day1, year2, month2, day2);
}

function isCorrectDate(y, m, d) {
	var Day_char = d;
	var Month_char = m;
	var Year_char = y;

	if (Month_char != null) {
		if (Month_char.charAt(0) == '0') {
      var Month_char2 = Month_char.charAt(1);
			Month_char = Month_char2;
		}
	}
	else return false;

	if (Day_char != null) {
		if (Day_char.charAt(0) == '0') {
      var Day_char2 = Day_char.charAt(1);
			Day_char = Day_char2;
		}
	}
	else return false;

	// isDate returns true if string arguments year, month, and day
	// form a valid date.
	if (Year_char != null) {
		if (Year_char.length != 4)
			return false;
		else
			return(isDate(Year_char, Month_char, Day_char));
	}
	else return false;

}
// isDate returns true if string arguments year, month, and day
// form a valid date.
function isCorrectHour(h, m) {
	var Min_char = m;
	var Hour_char = h;

	if (Hour_char != null) {
		if (Hour_char.length > 2)
			return false;
	}
	else return false;

	if (Min_char != null) {
		if (Min_char.length > 2)
			return false;
		else
			return(isHour(Hour_char,false) && isMinute(Min_char,false));
	}
	else return false;
}

// isYear (STRING s [, BOOLEAN emptyOK])
//
// isYear returns true if string s is a valid
// Year number.  Must be 2 or 4 digits only.
//
// For Year 2000 compliance, you are advised
// to use 4-digit year numbers everywhere.
//
// For B.C. compliance, write your own function. ;->
function isYear(y, canBeEmpty) {
  if (isEmpty(y)) {
    return !!canBeEmpty;
  }
  if (!isNonnegativeInteger(y, canBeEmpty)) {
    return false;
  }
  return ((y.length == 2) || (y.length == 4));
}

// isMonth (STRING s [, BOOLEAN emptyOK])
//
// isMonth returns true if string s is a valid
// month number between 1 and 12.
function isMonth (m, canBeEmpty) {
  return isIntegerInRange(m, 1, 12, canBeEmpty);
}


// isDay (STRING s [, BOOLEAN emptyOK])
//
// isDay returns true if string s is a valid
// day number between 1 and 31.
function isDay (s, canBeEmpty) {
  return isIntegerInRange(s, 1, 31, canBeEmpty);
}


// isHour (STRING s [, BOOLEAN emptyOK])
//
// isHour returns true if string s is a valid
// hour number between 0 and 23.
function isHour(h, canBeEmpty) {
  return isIntegerInRange(h, 0, 23, canBeEmpty);
}


// isMinute (STRING s [, BOOLEAN emptyOK])
//
// isDay returns true if string s is a valid
// day number between 0 and 59.
function isMinute(m, canBeEmpty) {
  return isIntegerInRange(m, 0, 59, canBeEmpty);
}


// daysInFebruary (INTEGER year)
//
// Given integer argument year,
// returns number of days in February of that year.
function daysInFebruary (year)
{   // February has 29 days in any year evenly divisible by four,
    // EXCEPT for centurial years which are not also divisible by 400.
    return (  ((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0) ) ) ? 29 : 28 );
}

// isDate (STRING year, STRING month, STRING day)
//
// isDate returns true if string arguments year, month, and day
// form a valid date.
function isDate (year, month, day)
{
	//window.alert(year);
	//window.alert(month);
	//window.alert(day);

	// catch invalid years (not 2- or 4-digit) and invalid months and days.
    if (!(isYear(year, false) && isMonth(month, false) && isDay(day, false))) return false;
    // Explicitly change type to integer to make code work in both
    // JavaScript 1.1 and JavaScript 1.2.
    var intYear = parseInt(year);
    var intMonth = parseInt(month);
    var intDay = parseInt(day);

    // catch invalid days, except for February
    if (intDay > daysInMonth[intMonth]) return false;

  return (intMonth == 2) && (intDay > daysInFebruary(intYear)) ? false : true;

}

// Check whether string s is empty.
function isEmpty(s) {
	return ((s == null) || (s.length == 0))
}


// isIntegerInRange (STRING s, INTEGER a, INTEGER b [, BOOLEAN emptyOK])
//
// isIntegerInRange returns true if string s is an integer
// within the range of integer arguments a and b, inclusive.
function isIntegerInRange(value, a, b, canBeEmpty) {
  if (isEmpty(value)) {
    return !!canBeEmpty;
  }

  // Catch non-integer strings to avoid creating a NaN below,
  // which isn't available on JavaScript 1.0 for Windows.
  if (!isInteger(value, false)) {
    return false;
  }

  var num = parseInt(value);
  return ((num >= a) && (num <= b));
}

// isInteger (STRING s [, BOOLEAN emptyOK])
//
// Returns true if all characters in string s are numbers.
//
// Accepts non-signed integers only. Does not accept floating
// point, exponential notation, etc.
function isInteger (value, canBeEmpty)
{
  if (isEmpty(value)) {
    return !!canBeEmpty;
  }

  // Search through string's characters one by one
  // until we find a non-numeric character.
  // When we do, return false; if we don't, return true.
  for (var i = 0; i < value.length; i++) {
    // Check that current character is number.
    var c = value.charAt(i);
    if (!isDigit(c)) {
      return false;
    }
  }

  // All characters are numbers.
  return true;
}

// isSignedInteger (STRING s [, BOOLEAN emptyOK])
//
// Returns true if all characters are numbers;
// first character is allowed to be + or - as well.
//
// Does not accept floating point, exponential notation, etc.
//
// We don't use parseInt because that would accept a string
// with trailing non-numeric characters.
//
function isSignedInteger(value, canBeEmpty) {
  if (isEmpty(value)) {
    return !!canBeEmpty;
  } else {
    var startPos = 0;

    // skip leading + or -
    if ((value.charAt(0) == "-") || (value.charAt(0) == "+")) {
      startPos = 1;
    }
    return (isInteger(value.substring(startPos, value.length), canBeEmpty))
  }
}


// isNonnegativeInteger (STRING s [, BOOLEAN emptyOK])
//
// Returns true if string s is an integer >= 0.
//
function isNonnegativeInteger(value, canBeEmpty) {
  var _canBeEmpty = !!canBeEmpty;
  return (isSignedInteger(value, _canBeEmpty) &&
      ((isEmpty(value) && _canBeEmpty) || (parseInt(value) >= 0)));
}


// Returns true if character c is a digit (0 .. 9).
function isDigit (c) {
	return ((c >= "0") && (c <= "9"))
}

//Check if date d1 is after date d2
function isD1AfterD2(year1, month1, day1, year2, month2, day2) {
	//the dates d1 and d2 must be correct
	//use isCorrectDate to verify it

	//jj, mm, aa of the first date
  var Day1 = day1;
  var Month1 = month1;
  var Year1 = year1;
	//Integer Convertion
	var iDay1 = atoi(Day1);
	var iMonth1 = atoi(Month1);
	var iYear1 = atoi(Year1);

//	window.alert("Day1 = "+iDay1+"\nMonth1 = "+iMonth1+"\nYear1 = "+iYear1);

	//jj, mm, aa of the current date
  var Day2 = day2;
  var Month2 = month2;
  var Year2 = year2;
	//Integer Convertion
	var iDay2 = atoi(Day2);
	var iMonth2 = atoi(Month2);
	var iYear2 = atoi(Year2);

//	window.alert("Day2 = "+iDay2+"\nMonth2 = "+iMonth2+"\nYear2 = "+iYear2);

	if (iYear2 == iYear1) {
		if (iMonth2 == iMonth1) {
			if (iDay2 <= iDay1)
				ok = true;
			else
				ok = false;
		} else {
			if (iMonth2 <= iMonth1)
				ok = true;
			else
				ok = false;
		}
	} else {
		if (iYear2 < iYear1)
			ok = true;
		else
			ok = false;
	}
	return(ok);
}


//Check if date d1 is after date d2
function isD1AfterD2Hour(year1, month1, day1, hour1, minute1, year2, month2, day2, hour2, minute2) {
	//the dates d1 and d2 must be correct
	//use isCorrectDate to verify it

	//jj, mm, aa of the first date
  var Day1 = day1;
  var Month1 = month1;
  var Year1 = year1;
  var Hour1 = hour1;
  var Minute1 = Minute1
	//Integer Convertion
	var iHour1 = atoi(Hour1);
	var iMinute1 = atoi(Minute1);
	var iDay1 = atoi(Day1);
	var iMonth1 = atoi(Month1);
	var iYear1 = atoi(Year1);

//	window.alert("Day1 = "+iDay1+"\nMonth1 = "+iMonth1+"\nYear1 = "+iYear1);

	//jj, mm, aa of the current date

  var Day2 = day2;
  var Month2 = month2;
  var Year2 = year2;
  var Hour2 = hour2
  var Minute2 = minute2
	//Integer Convertion
	var iHour2 = atoi(Hour2);
	var iMinute2 = atoi(Minute2);
	var iDay2 = atoi(Day2);
	var iMonth2 = atoi(Month2);
	var iYear2 = atoi(Year2);
//	window.alert("Day2 = "+iDay2+"\nMonth2 = "+iMonth2+"\nYear2 = "+iYear2);

	if (iYear2 == iYear1) {
		if (iMonth2 == iMonth1) {
			if (iDay2 == iDay1) {
				if (iHour2 == iHour1) {
					if (iMinute2 <= iMinute1)
						ok = true;
					else
						ok = false;
				}
				else {
					if (iHour2 < iHour1)
						ok = true;
					else
						ok = false;
				}
			}
			else {
				if (iDay2 < iDay1)
					ok = true;
				else
					ok = false;
			}
		} else {
			if (iMonth2 <= iMonth1)
				ok = true;
			else
				ok = false;
		}
	} else {
		if (iYear2 < iYear1)
			ok = true;
		else
			ok = false;
	}
	return(ok);
}

function isFuture(date, language) {
	var year = extractYear(date, language);
    var month = extractMonth(date, language);
    var day = extractDay(date, language);

    return isFutureDate(year, month, day);
}

function isFutureDate(year, month, day) {
	var todayDate = new Date();
	var yearToday = "" + todayDate.getFullYear();
    var monthToday = todayDate.getMonth() + 1;
	if (monthToday < 10)
		monthToday = "0" + monthToday;
    var dayToday = "" + todayDate.getDate();

	return isD1AfterD2(year, month, day, yearToday, monthToday, dayToday);
}

function atoi(str) {
	var nb = 0;
  for (var i = 0; i < str.length; i++) {
		nb = nb*10 + string2int(str.charAt(i));
	}
	return(nb);
}

function string2int(str) {
  var nb;
	switch(str) {
		case '0' : nb = 0;	break;
		case '1' : nb = 1;	break;
		case '2' : nb = 2;	break;
		case '3' : nb = 3;	break;
		case '4' : nb = 4;	break;
		case '5' : nb = 5;	break;
		case '6' : nb = 6;	break;
		case '7' : nb = 7;	break;
		case '8' : nb = 8;	break;
		case '9' : nb = 9;	break;
	}
	return(nb);
}