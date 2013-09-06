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

/**
 * Silverpeas plugin build upon JQuery to check date.
 *
 * After this plugin installed, some helper methods are usable :
 * - isDateValid(...) : to verify that the given date (and optionally hour) is valid
 * - isDateFuture(...) : to verify that the given date (and optionally hour) is greater
 *                       or equal than today
 * - isPeriodValid(...) : to verify that the given date (and optionally hour) is greater
 *                        or equal than another one
 * - isPeriodEndingInFuture(...) : to verify that a period is valid and the end date is greater
 *                                 or equal than today
 *
 * Please notice that method date parameter can accept id of date HTML TAG instead of a date context
 * object. In this case, default values of a date context are used during check processes.
 *
 * If this plugin is installed via the taglib <view:includePlugin="datepicker" />, the language is
 * automatically set from the user preferences.
 *
 * Examples :
 * - One check > end date is greater or equals to begin date >
 *           var dateErrors = isPeriodValid({
 *             date : [value filled by user for begin date],
 *             label : "'Begin date'"
 *           }, {
 *             date : [value filled by user for end date],
 *             label : "'End date'"});
 *           $(dateErrors).each(function(index, error) {
 *             // handling here error.message (and error.code if necessary)
 *           });
 * - several checks > end date is greater or equals to begin date > the two dates are in the future
 *           var beginDate = {date : [value filled by user for begin date], label: "'Begin date'"};
 *           var endDate = {date : [value filled by user for end date], label: "'End date'"};
 *           var dateErrors = isDateFuture(beginDate).concat(isDateFuture(endDate))
 *                            .concat(isPeriodValid(beginDate, endDate));
 *           $(dateErrors).each(function(index, error) {
 *             // handling here error.message (and error.code if necessary)
 *           });
 *
 * ###############
 * # Definitions #
 * ###############
 *
 * - date context object : it is an object that contains all necessary values to verify a date.
 * Properties : {
 *    labelId : the id of the HTML tag that contains the label of the date
 *    dateId : the id of the HTML tag that contains the formatted date. If no labelId is passed, the
 *             value of the associated label TAG is searched
 *    labelHourId : the id of the HTML tag that contains the label of the hour
 *    hourId : the id of the HTML tag that contains the formatted hour of the date. If no
 *             labelHourId is passed, the value of the associated label TAG is searched
 *    label : the label of the date, empty by default. Ignored if a value exists for labelId
 *            parameter
 *    date : the formatted date, empty by default. Ignored if a value exists for dateId parameter
 *    labelHour : the label of the hour, empty by default. Ignored if a value exists for labelHourId
 *                parameter
 *    hour : the formatted hour for the date, empty by default. Ignored if a value exists for hourId
 *           parameter
 *    defaultDate : the formatted default date, empty by default. Ignored if isMandatory is true.
 *                  WARNING : this is a default date for the checker, source value is not modified
 *    defaultDateHour : the formatted default hour for the date if it exists, empty by default.
 *                      Ignored if isMandatoryHour is true.
 *                      WARNING : this is a default hour for the checker, source value is not
 *                      modified
 *    isMandatory : boolean value to indicate if the date is mandatory, false by default. If false
 *                  then an unexisting date is not an error
 *    isMandatoryHour : boolean value to indicate if the hour is mandatory, false by default. If
 *                      false then an unexisting hour is not an error
 *    canBeEqualToAnother : in case of comparison between two dates this boolean value to indicate
 *                          if the date can be equal to the other, true by default
 *  }
 *  ________________________________________________________________________________________________
 *
 * - array of error objects : in case of bad date(s), it is the array returned by each verify
 *                            methods that are included in this plugin.
 * Properties of an error object : {
 *    code : the error code, one of those defined by $.datechecker.CODE_ERROR,
 *    message : the functional message associated to the error aggregated with label of the bad
 *              date if exists
 *    dateContext : the date context object from which the error has been catched
 *  }
 */
(function($) {

  $.datechecker = {
    CODE_ERROR: {
      UNKNOWN: 'UNKNOWN',
      UNEXISTING: 'UNEXISTING',
      IS_NOT_VALID: 'IS_NOT_VALID',
      HOUR_IS_NOT_VALID: 'HOUR_IS_NOT_VALID',
      IS_NOT_AFTER_NOW: 'IS_NOT_AFTER_NOW',
      IS_NOT_AFTER_ANOTHER: 'IS_NOT_AFTER_ANOTHER'
    },
    settings: {
      language: 'en'
    },
    isDateValid: function() {
      var options = (arguments.length > 1) ? arguments[1] : null;
      return __isDateValid(__buildDateContext(arguments[0], options));
    },
    isDateAfterNow: function() {
      var options = (arguments.length > 1) ? arguments[1] : null;
      return __isDateAfterNow(__buildDateContext(arguments[0], options));
    },
    isDateAfterAnother: function(dateContext, anotherDateContext) {
      return __isDateAfterAnother(__buildDateContext(dateContext),
              __buildDateContext(anotherDateContext));
    }
  };

  /**
   * Centralized private method that verifies if a date is greater or equal than another one.
   * @param dateContext
   * @param anotherDateContext
   * @return {errors}
   * @private
   */
  function __isDateAfterAnother(dateContext, anotherDateContext) {
    var errors = __isDateValid(anotherDateContext).concat(__isDateValid(dateContext));
    if (!dateContext.isError && !anotherDateContext.isError && dateContext.parsedDate &&
            anotherDateContext.parsedDate) {
      if (__compareDateToAnother(dateContext.parsedDate, anotherDateContext.parsedDate) < 0 ||
              ((!dateContext.canBeEqualToAnother || !anotherDateContext.canBeEqualToAnother) &&
                      __compareDateToAnother(anotherDateContext.parsedDate, dateContext.parsedDate) === 0)) {
        errors.push(__buildError($.datechecker.CODE_ERROR.IS_NOT_AFTER_ANOTHER, dateContext,
                anotherDateContext));
      }
    }
    return errors;
  }

  /**
   * Centralized private method that verifies if a date is greater or equal than today.
   * @param dateContext
   * @return {errors}
   * @private
   */
  function __isDateAfterNow(dateContext) {
    var errors = __isDateAfterAnother(dateContext,
            {parsedDate: __getNow(!__isDefined(dateContext.hour))});
    if (errors.length > 0 &&
            $.datechecker.CODE_ERROR.IS_NOT_AFTER_ANOTHER == errors[errors.length - 1].code) {
      errors.pop();
      errors.push(__buildError($.datechecker.CODE_ERROR.IS_NOT_AFTER_NOW, dateContext));
    }
    return errors;
  }

  /**
   * Centralized private method that verifies if a date (and hour) is valid.
   * @param dateContext
   * @return {*}
   * @private
   */
  function __isDateValid(dateContext) {
    var errors = [];
    if (typeof dateContext.parsedDate === 'undefined') {

      // Existence
      if (dateContext.isMandatory && !__isDefined(dateContext.date)) {
        errors.push(__buildError($.datechecker.CODE_ERROR.UNEXISTING, dateContext));
      }

      if (dateContext.isMandatoryHour && !__isDefined(dateContext.hour)) {
        dateContext.hourError = true;
        errors.push(__buildError($.datechecker.CODE_ERROR.UNEXISTING, dateContext));
      }

      // Date format
      dateContext.hourOnly = false;
      if (__isDefined(dateContext.date)) {
        try {
          var dateFormat = $.datepicker.regional[$.datechecker.settings.language];
          if (dateFormat) {
            dateFormat = dateFormat.dateFormat;
          } else {
            dateFormat = $.datepicker.regional[''].dateFormat;
          }
          dateContext.parsedDate = $.datepicker.parseDate(dateFormat, dateContext.date, null);
          if (dateContext.date !=
                  $.datepicker.formatDate(dateFormat, dateContext.parsedDate, null)) {
            throw $.datechecker.CODE_ERROR.IS_NOT_VALID;
          }
        } catch (ex) {
          errors.push(__buildError($.datechecker.CODE_ERROR.IS_NOT_VALID, dateContext));
        }
      }

      // Hour
      if (__isDefined(dateContext.hour)) {
        if (typeof dateContext.parsedDate === 'undefined' || dateContext.parsedDate === null) {
          // Only hour will be verified
          dateContext.parsedDate = new Date('1970-01-01');
          dateContext.hourOnly = true;
        }
        // Hour is defined, first step : verify the value
        if (/^(([01][0-9])|(2[0-3]))[:]([0-5][0-9])$/.test(dateContext.hour)) {
          // Second step, set date with the valid hour
          var parsedHour = dateContext.hour.split(/:/);
          __resetHour(dateContext.parsedDate);
          dateContext.parsedDate.setHours(parsedHour[0]);
          dateContext.parsedDate.setMinutes(parsedHour[1]);
        } else {
          errors.push(__buildError($.datechecker.CODE_ERROR.HOUR_IS_NOT_VALID, dateContext));
        }
      }

      if (errors.length > 0) {
        dateContext.isError = true;
        if (dateContext.nbCheck > 1) {
          errors = [];
        }
      }
    }
    return errors;
  }

  /**
   * Centralized private method that verifies if a string value is defined.
   * @param value a string value
   * @return {boolean}
   * @private
   */
  function __isDefined(value) {
    return value && value.replace(/[ \t\n\r]{0,}/, "").length > 0;
  }

  /**
   * Centralized method to compare two dates together.
   * @param date
   * @param anotherDate
   * @return {int} -1, 0, or 1 as date is less than, equal to, or greater than anotherDate.
   * @private
   */
  function __compareDateToAnother(date, anotherDate) {
    if (date.getTime() < anotherDate.getTime()) {
      return -1;
    } else if (date.getTime() > anotherDate.getTime()) {
      return 1;
    }
    return 0;
  }

  /**
   * Centralized private method that initialized a date at today at 00:00.
   * @return {*}
   * @private
   */
  function __getNow(resetHour) {
    var date = new Date();
    if (resetHour) {
      __resetHour(date);
    }
    return date;
  }

  /**
   * Centralized private method that initialized a date at 00:00.
   * @param date
   * @return date
   * @private
   */
  function __resetHour(date) {
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);
    return date;
  }

  /**
   * Private method to build or aggregate a date object context.
   * @param dateOrDateContext
   * @return {*}
   * @private
   */
  function __buildDateContext(dateOrDateContext) {

    if (!$().datepicker) {
      alert("JQuery datepicker Plugin is required.");
      return false;
    }

    var options = (arguments.length > 1) ? arguments[1] : null;
    var dateContext = {
      labelId: '',
      dateId: '',
      labelHourId: '',
      hourId: '',
      label: '',
      date: '',
      labelHour: '',
      hour: '',
      defaultDate: '',
      defaultDateHour: '',
      isMandatory: false,
      isMandatoryHour: false,
      canBeEqualToAnother: true,
      nbCheck: 1,
      isError: false
    };
    if (dateOrDateContext) {
      if (typeof dateOrDateContext === 'string') {
        dateContext.dateId = dateOrDateContext;
      } else {
        $.extend(dateContext, dateOrDateContext);
        if (!dateOrDateContext.nbCheck) {
          dateOrDateContext.nbCheck = 1;
        } else {
          dateOrDateContext.nbCheck++;
          dateContext.nbCheck = dateOrDateContext.nbCheck;
        }
      }

      /*
       * Retrieving labels and values from given ids if necessary
       */
      var fromTagValues = {};
      __loadValuesFromHtmlTags(fromTagValues, {
        labelId: dateContext.labelId,
        id: dateContext.dateId,
        labelProp: 'label',
        valueProp: 'date'
      });
      __loadValuesFromHtmlTags(fromTagValues, {
        labelId: dateContext.labelHourId,
        id: dateContext.hourId,
        labelProp: 'labelHour',
        valueProp: 'hour'
      });
      $.extend(dateContext, fromTagValues);
    }

    // Options
    if (options && options !== null) {
      $.extend(dateContext, options);
    }

    // Default values if any
    if (!dateContext.isMandatory && dateContext.defaultDate && !__isDefined(dateContext.date)) {
      dateContext.date = dateContext.defaultDate;
    }
    if (!dateContext.isMandatoryHour && dateContext.defaultDateHour &&
            !__isDefined(dateContext.hour) && __isDefined(dateContext.date)) {
      dateContext.hour = dateContext.defaultDateHour;
    }

    // Returning initialized date context
    return dateContext;
  }

  /**
   * Private method that centralizes the retrieving of label and value from HTML tags.
   * @param result
   * @param params
   * @return {*}
   * @private
   */
  function __loadValuesFromHtmlTags(result, params) {

    // Label
    var label = __isDefined(params.labelId) ? $('#' + params.labelId).html() : '';
    if (!__isDefined(label) && __isDefined(params.id)) {
      label = $("label[for='" + params.id + "']").html();
      if (__isDefined(label)) {
        label = "'" + label + "'";
      }
    }
    if (__isDefined(label)) {
      result[params.labelProp] = label;
    }

    // Value
    var value = __isDefined(params.id) ? $('#' + params.id).val() : '';
    if (__isDefined(value)) {
      result[params.valueProp] = value;
    }

    // Completed result
    return result;
  }

  /**
   * Private method to build an error object.
   * @param code
   * @param dateContext
   * @return {*}
   * @private
   */
  function __buildError(code, dateContext) {
    var anotherDateContext = (arguments.length > 2) ? arguments[2] : null;
    var error = {};
    error.dateContext = dateContext;
    error.code = code;
    error.message = '';
    if (dateContext.label) {
      error.message += dateContext.label + ' ';
    }
    switch (code) {
      case $.datechecker.CODE_ERROR.UNEXISTING:
        if (dateContext.hourError && dateContext.labelHour) {
          error.message = dateContext.labelHour + ' ';
        }
        error.message += __getFromBundleKey('GML.MustBeFilled');
        break;
      case $.datechecker.CODE_ERROR.IS_NOT_VALID:
        error.message += __getFromBundleKey('GML.MustContainsCorrectDate');
        break;
      case $.datechecker.CODE_ERROR.HOUR_IS_NOT_VALID:
        if (dateContext.labelHour) {
          error.message = dateContext.labelHour + ' ';
        }
        error.message += __getFromBundleKey('GML.MustContainsCorrectHour');
        break;
      case $.datechecker.CODE_ERROR.IS_NOT_AFTER_NOW:
        error.message += __getFromBundleKey(
                (dateContext.canBeEqualToAnother) ? 'GML.MustContainsPostOrEqualDate' :
                'GML.MustContainsPostDate');
        break;
      case $.datechecker.CODE_ERROR.IS_NOT_AFTER_ANOTHER:
        error.message += __getFromBundleKey(
                (dateContext.canBeEqualToAnother) ? 'GML.MustContainsPostOrEqualDateTo' :
                'GML.MustContainsPostDateTo');
        if (anotherDateContext.label) {
          error.message += ' ' + anotherDateContext.label;
        }
        break;
      default :
        error.code = $.datechecker.CODE_ERROR.UNKNOWN;
        error.message += $.datechecker.CODE_ERROR.UNKNOWN;
    }
    return error;
  }

  // Initialization indicator.
  var __i18nInitialized = false;

  /**
   * Private method that handles i18n.
   * @param key
   * @return message
   * @private
   */
  function __getFromBundleKey(key) {
    if (webContext) {
      if (!__i18nInitialized) {
        $.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/org/silverpeas/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        __i18nInitialized = true;
      }
      return $.i18n.prop(key);
    }
    return key;
  }

})(jQuery);

// ###########
// # HELPERS #
// ###########

/**
 * This method verifies that the given date (and optionally hour) is valid. In common use please
 * passing a date object context as parameter to the method like the following example :
 *     var errors = isDateValid({
 *       label : 'Begin date',
 *       date : '01/01/2012'
 *     });
 *     $(errors).each(function(index, error) {
 *       // handling here error.message (and error.code if necessary)
 *     }
 * If date is mandatory and hour exists, please add the instruction like following :
 *     var errors = isDateValid({
 *       label : 'Begin date',
 *       date : '01/01/2012',
 *       labelHour : 'Begin date',
 *       hour : '01/01/2012',
 *       isMandatory : true
 *     });
 *     ...
 * @return {Array} array of error objects (empty array if no error).
 */
function isDateValid() {
  return $.datechecker.isDateValid.apply(this, arguments);
}

/**
 * This method verifies that the given date (and optionally hour) is greater or equal than today.
 * Notice that if the given date doesn't exist and is not mandatory then no error is returned.
 * In common use please passing a date object context as parameter to the method like the following
 * example :
 *     var errors = isDateFuture({
 *       label : 'Begin date',
 *       date : '01/01/2012'
 *     });
 *     ...
 * If date has to by strictly greater than today, please add the instruction like following :
 *     var errors = isDateFuture({
 *       label : 'Begin date',
 *       date : '01/01/2012',
 *       canBeEqualToAnother : false
 *     });
 *     ...
 * @return {Array} array of error objects (empty array if no error).
 */
function isDateFuture() {
  return $.datechecker.isDateAfterNow.apply(this, arguments);
}

/**
 * This method verifies that the given date (and optionally hour) is greater or equal than another
 * one. Notice that if one of the two dates doesn't exist and is not mandatory then no error is
 * returned.
 * Example :
 *     var errors = isPeriodValid({
 *       label : 'Begin date',
 *       date : '01/01/2012'
 *     },{
 *       label : 'End date',
 *       date : '12/31/2012'
 *     });
 *     ...
 * If date has to be strictly greater than the other, please add the instruction like following :
 *     var errors = isPeriodValid({
 *       label : 'Begin date',
 *       date : '01/01/2012'
 *     },{
 *       label : 'End date',
 *       date : '12/31/2012',
 *       canBeEqualToAnother : false
 *     });
 *     ...
 * @param beginDateContext the other date
 * @param endDateContext the date to compare with another one
 * @return {Array} array of error objects (empty array if no error).
 */
function isPeriodValid(beginDateContext, endDateContext) {
  return $.datechecker.isDateAfterAnother(endDateContext, beginDateContext);
}

/**
 * This method verifies that the given period (defined by beginDate and endDate) is valid and that
 * the endDate is greater or equal than now.
 * Notice that if one of the two dates doesn't exist and is not mandatory then no error is
 * returned.
 * Example :
 *     var errors = isPeriodEndingInFuture('beginDateHtmlTagId', 'endDateHtmlTagId');
 *     ...
 * Other example :
 *     var errors = isPeriodEndingInFuture({
 *       label : 'Begin date',
 *       date : '01/01/2012'
 *     },{
 *       label : 'End date',
 *       date : '12/31/2012'
 *     });
 *     ...
 * @param beginDateContext HTML TAG id or date context object
 * @param endDateContext HTML TAG id or date context object
 * @return {Array}
 */
function isPeriodEndingInFuture(beginDateContext, endDateContext) {
  return isPeriodValid(beginDateContext, endDateContext).concat(isDateFuture(endDateContext));
}