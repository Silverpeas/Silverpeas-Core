/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
 * The PdC plugin built on JQuery.
 * It provides a way to render the axis of a PdC according to the function invoked.
 */
(function($) {

  var pluginSettings = {
    context: webContext,
    workspace: null,
    component: null,
    withSecondaryAxis: false
  };

  var methods = {
    /**
     * Renders a widget with the axis of the PdC that are used in the classification of the contents
     * in Silverpeas. For each axis, only the values actually used in a classification are rendered
     * with the axis and for each of them the count of contents having this value in their classification.
     * The user can select some of the values with the widget.
     */
    used: function(parameters) {
      return this.each(function() {
        var $this = $(this);
        init($this, parameters);
        var settings = $(this).data('settings');
        loadPdC(settings.pdcUri, function(loadedPdC) {
          settings.selectorParameters.axis = loadedPdC.axis;
          $this.pdcAxisValuesSelector(settings.selectorParameters);
        }, function(pdc, error) {
          alert(error.message);
        });
      });
    },
    /**
     * Function to get the axis' values selected by the user through the widget above.
     * @returns {Array} an array of axis' values. A value is an object of type:
     * {
     *   id: the unique identifier of the value, that is its path from the root axis' value,
     *   axisId: the unique identifier of the axis the value belongs to,
     *   treeId: the unique identifier of the tree the axis is related to,
     *   meaning: the text of the value,
     *   synonyms: the possible synonyms of the value's text.
     * }.
     * The array supports the function 'flatten()' that flattens the values into a single string
     * with, for each of them, the following format: 'axisId:valueId'; the values are comma-separated
     * in the string.
     */
    selectedValues: function() {
      return $(this).data('values');
    }
  };

  /**
   * The JQuery plugin pdc.
   * @param {string} method the method name to invoke with the plugin pdc.
   * @param {type} options the options to pass to the plugin invocation.
   * @returns {@exp;methods@pro;init@call;apply|unresolved}
   */
  $.fn.pdc = function(method, options) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.pdc');
    }
  };

  function init($this, parameters) {
    var settings = $.extend(true, {}, pluginSettings);
    if (parameters) {
      $.extend(true, settings, parameters);
    }
    settings.pdcUri = uriOfUsedPdc(settings);
    $.i18n.properties({
      name: 'pdcBundle',
      path: webContext + '/services/bundles/org/silverpeas/pdcPeas/multilang/',
      language: '$$', /* take the user langage from its session */
      mode: 'map'
    });
    settings.selectorParameters = {
      title: "",
      positionError: "",
      mandatoryAxisText: "",
      mandatoryAxisError: "",
      mandatoryAxisLegend: "",
      invariantAxisLegend: "",
      anotherValueLegend: "",
      labelOk: $.i18n.prop("GML.ok"),
      labelCancel: $.i18n.prop("GML.cancel"),
      multiValuation: false,
      dialogBox: false,
      classifiedContentCount: true,
      axisTypeDisplay: true,
      rootValueDisplay: true,
      primaryAxisIcon: webContext + '/pdcPeas/jsp/icons/primary.gif',
      secondaryAxisIcon: webContext + '/pdcPeas/jsp/icons/secondary.gif',
      axis: [],
      values: settings.values,
      onValueChange: function(axis, value) {
        var updatedAxis, settings = $this.data('settings'), values = $this.data('values');
        if (value === null)
          values.removeByAxis(axis);
        else
          values.put(value);
        var flattenedValues = values.flatten();
        var url = settings.pdcUri;
        if (values.length > 0)
          url += (url.lastIndexOf('?') > 0 ? '&' : '?') + 'values=' + flattenedValues;
        loadPdC(url, function(loadedPdC) {
          updatedAxis = loadedPdC.axis;
        }, function(pdc, error) {
          alert(error.message);
        }, true);

        return updatedAxis;
      },
      onValuesSelected: null
    };
    var values = [];
    values.put = function(aValue) {
      /**
       * Put the specified value into the array. If a value for the same axis already exists, then
       * the specified value replaces it, otherwise it is just pushed at the end of the array.
       */
      var found = false;
      for (var i = 0; i < values.length && !found; i++) {
        if (values[i].axisId === aValue.axisId) {
          values[i] = aValue;
          found = true;
        }
      }
      if (!found)
        this.push(aValue);
    };
    values.removeByAxis = function(axis) {
      /**
       * Removes the value for the specified axis.
       */
      for (var i = 0; i < values.length; i++) {
        if (values[i].axisId === axis.id) {
          values.splice(i, 1);
          break;
        }
      }
    };
    values.flatten = function() {
      /**
       * Flatten the values into a string in which each value is comma-separated and in the form
       * axisId:valueId.
       */
      var v = '';
      if (this.length > 0) {
        var v = this[0].axisId + ':' + this[0].id;
        for (var i = 1; i < this.length; i++) {
          v += ',' + this[1].axisId + ':' + this[1].id;
        }
      }
      return v;
    };
    $this.data('values', values);
    $this.data('settings', settings);
  }

})(jQuery);
