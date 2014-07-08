/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

// Author: Kelvin Tan (kelvint at apache.org)
// JavaScript Lucene Query Validator

// The original code has been changed in order to make it works with Silverpeas application
// Main changes are i18n message support and silverpeas noty plugin instead of javascript alert
// Special characters are + - && || ! ( ) { } [ ] ^ " ~ * ? : \
// Special words are (case-sensitive) AND NOT OR

// Makes wildcard queries case-insensitive if true.
// Refer to http://www.mail-archive.com/lucene-user@jakarta.apache.org/msg00646.html

$.i18n.properties({
  name: 'generalMultilang',
  path: webContext + '/services/bundles/org/silverpeas/multilang/',
  language: getUserLanguage(),
  mode: 'map'
});

var wildcardCaseInsensitive = true;

// Mutator method for wildcardCaseInsensitive.
// @param Should wildcard queries be case-insensitive?
function setWildcardCaseInsensitive(bool)
{
  wildcardCaseInsensitive = bool;
}

// Should the user be prompted with an alert box if validation fails?
var alertUser = true;

function setAlertUser(bool)
{
  alertUser = bool;
}

// validates a lucene query.
// @param Form field that contains the query
function doCheckLuceneQuery(queryField)
{
  return doCheckLuceneQueryValue(queryField.value)
}

// validates a lucene query.
// @param query string
function doCheckLuceneQueryValue(query)
{
  if(query != null && query.length > 0)
  {
    query = removeEscapes(query);

    // check for allowed characters
    if(!checkAllowedCharacters(query)) return false;

    // check * is used properly
    if(!checkAsterisk(query)) return false;

    // check for && usage
    if(!checkAmpersands(query)) return false;

    // check ^ is used properly
    if(!checkCaret(query)) return false;

    // check ~ is used properly
    if(!checkSquiggle(query)) return false;

    // check ! is used properly
    if(!checkExclamationMark(query)) return false;

    // check question marks are used properly
    if(!checkQuestionMark(query)) return false;

    // check parentheses are used properly
    if(!checkParentheses(query)) return false;

    // check '+' and '-' are used properly
    if(!checkPlusMinus(query)) return false;

    // check AND, OR and NOT are used properly
    if(!checkANDORNOT(query)) return false;

    // check that quote marks are closed
    if(!checkQuotes(query)) return false;

    // check ':' is used properly
    if(!checkColon(query)) return false;

    if(wildcardCaseInsensitive)
    {
      if(query.indexOf("*") != -1)
      {
        var i = query.indexOf(':');
        if(i == -1)
        {
          query.value = query.toLowerCase();
        }
        else // found a wildcard field search
        {
          query.value = query.substring(0, i) + query.substring(i).toLowerCase();
        }
      }
    }
    return true;
  }
}

// remove the escape character and the character immediately following it
function removeEscapes(query)
{
  return query.replace(/\\./g, "");
}

function checkAllowedCharacters(query)
{
  matches = query.match(/[^a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@#\/$%'= ]/);
  if(matches != null && matches.length > 0)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.allowed.characters'));
    }
    return false;
  }
  return true;
}

function checkAsterisk(query)
{
  matches = query.match(/^[\*]*$|[\s]\*|^\*[^\s]/);
  if(matches != null)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.asterisk'));
    }
    return false;
  }
  return true;
}

function checkAmpersands(query)
{
  matches = query.match(/[&]{2}/);
  if(matches != null && matches.length > 0)
  {
    matches = query.match(/^([a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@#\/$%'=]+( && )?[a-zA-Z0-9_+\-:.()\"*?|!{}\[\]\^~\\@#\/$%'=]+[ ]*)+$/); // note missing & in pattern
    if(matches == null)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.ampersands'));
      }
      return false;
    }
  }
  return true;
}

function checkCaret(query)
{
  matches = query.match(/[^\\]\^([^\s]*[^0-9.]+)|[^\\]\^$/);
  if(matches != null)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.caret'));
    }
    return false;
  }
  return true;
}

function checkSquiggle(query)
{
  matches = query.match(/[^\\]~[^\s]*[^0-9\s]+/);
  if(matches != null)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.squiggle'));
    }
    return false;
  }
  return true;
}

function checkExclamationMark(query)
{
  // foo! is not a query, but !foo is. hmmmm...
  // NB: doesn't handle term1 ! term2 ! term3 or term1 !term2
  matches = query.match(/^[^!]*$|^([a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@#\/$%'=]+( ! )?[a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@#\/$%'=]+[ ]*)+$/);
  if(matches == null || matches.length == 0)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.exclamation.mark'));
    }
    return false;
  }


  return true;
}

function checkQuestionMark(query)
{
  matches = query.match(/^(\?)|([^a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@#\/$%'=]\?+)/);
  if(matches != null && matches.length > 0)
  {
      if(alertUser) {
        notyWarning(getString('GML.search.error.question.mark'));
      }
    return false;
  }
  return true;
}

function checkParentheses(query)
{
  var hasLeft = false;
  var hasRight = false;
  matchLeft = query.match(/[(]/g);
  if(matchLeft != null) hasLeft = true
  matchRight = query.match(/[)]/g);
  if(matchRight != null) hasRight = true;

  if(hasLeft || hasRight)
  {
    if(hasLeft && !hasRight || hasRight && !hasLeft)
    {
        if(alertUser) {
          notyWarning(getString('GML.search.error.parentheses.closed'));
        }
        return false;
    }
    else
    {
      var number = matchLeft.length + matchRight.length;
      if((number % 2) > 0 || matchLeft.length != matchRight.length)
      {
        if(alertUser) {
          notyWarning(getString('GML.search.error.parentheses.closed'));
        }
        return false;
      }
    }
    matches = query.match(/\(\)/);
    if(matches != null)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.parentheses.content'));
      }
      return false;
    }
  }
  return true;
}

function checkPlusMinus(query)
{
  matches = query.match(/^[^\n+\-]*$|^([+-]?[a-zA-Z0-9_:.()\"*?&|!{}\[\]\^~\\@#\/$%'=]+[ ]?)+$/);
  if(matches == null || matches.length == 0)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.plus.minus'));
    }
    return false;
  }
  return true;
}

function checkANDORNOT(query)
{
  matches = query.match(/AND|OR|NOT/);
  if(matches != null && matches.length > 0)
  {
    matches = query.match(/^([a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@\/#$%'=]+\s*((AND )|(OR )|(AND NOT )|(NOT ))?[a-zA-Z0-9_+\-:.()\"*?&|!{}\[\]\^~\\@\/#$%'=]+[ ]*)+$/);
    if(matches == null || matches.length == 0)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.and.or.not'));
      }
      return false;
    }

    // its difficult to distinguish AND/OR/... from the usual [a-zA-Z] because they're...words!
    matches = query.match(/^((AND )|(OR )|(AND NOT )|(NOT ))|((AND)|(OR)|(AND NOT )|(NOT))[ ]*$/)
    if(matches != null && matches.length > 0)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.and.or.not'));
      }
      return false;
    }
  }
  return true;
}

function checkQuotes(query)
{
  matches = query.match(/\"/g);
  if(matches != null && matches.length > 0)
  {
    var number = matches.length;
    if((number % 2) > 0)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.quotes.closed'));
      }
      return false;
    }
    matches = query.match(/""/);
    if(matches != null)
    {
      if(alertUser) {
        notyWarning(getString('GML.search.error.quotes.content'));
      }
      return false;
    }
  }
  return true;
}

function checkColon(query)
{
  matches = query.match(/[^\\\s]:[\s]|[^\\\s]:$|[\s][^\\]?:|^[^\\\s]?:/);
  if(matches != null)
  {
    if(alertUser) {
      notyWarning(getString('GML.search.error.colon'));
    }
    return false;
  }
  return true;
}