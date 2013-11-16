/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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

// An anonymous function to wrap around the menu to avoid conflict
(function($) {

  $.spCore = {
    initialized : false,
    doInitialize : function() {
      if (!$.spCore.initialized) {
        $.spCore.initialized = true;
      }
    },
    definitions : {

      /*
       * Entities
       */
      SPACE_TYPE : 'space',
      COMPONENT_TYPE : 'component',
      TOOL_TYPE : 'tool',

      /*
       * Menus
       */
      MENU : {
        FAVORITES : {
          DISABLED : "DISABLE",
          BOOKMARKS : "BOOKMARKS",
          ALL : "ALL"
        }
      }
    },

    /**
     * Centralizes synchronous ajax request for json response
     *
     * @returns
     */
    getJSonData : function(url) {
      return __performAjaxRequest({url: url, type: 'GET', dataType : 'json'});
    },

    /**
     * Centralizes synchronous ajax request for json response
     *
     * @returns
     */
    putJSonData : function(url, data) {
      return __performAjaxRequest({
        url : url,
        type : 'PUT',
        dataType : 'json',
        data : $.toJSON(data),
        contentType : "application/json",
      });
      return result;
    },

    /**
     * Centralizes asynchronous ajax request for html response
     *
     * @returns
     */
    loadHtml : function(url, targetOrTargetId) {
      var $target;
      if (typeof targetOrTargetId == "string") {
        // The parameter is a target id
        $target = $('#' + targetOrTargetId);
      } else {
        $target = targetOrTargetId;
      }
      $target.html($('<iframe>').attr('width', '100%').attr('height', '600px')
          .attr('src', url));
    },

    /**
     * Debug function
     *
     * @param object
     * @returns
     */
    printParams : function(object) {
      var $result = $('<div>').attr('style',
          'display: table; padding: 5px; margin: 5px;');
      var object = object;
      if (object instanceof MenuItem) {
        object = object.getData();
      }
      if ($.isPlainObject(object)) {
        var content = $.param(object).replace(/%3A/g, ":").replace(/%2F/g, "/")
            .replace(/%C3%A9/g, "é").replace(/%C3%A8/g, "è").replace(/%C3%89/g,
                'E').replace(/%C3%88/g, 'E').replace(/%C3%AA/g, 'ê').replace(
                /%C3%A0/g, 'à').replace(/\+/g, " ").replace(/%23/g, "#")
            .replace(/%C3%A2/g, "â");
        var splittedContent = content.split("&");
        for ( var i = 0; i < splittedContent.length; i++) {
          var $line = $('<div>').attr('style', 'display: table-row;')
          var splittedLine = splittedContent[i].split("=");
          for ( var j = 0; j < splittedLine.length; j++) {
            $line.append($('<div>').attr('style',
                'display: table-cell; padding: 2px;').html(splittedLine[j]));
          }
          $result.append($line);
        }
      }
      return $result;
    }
  }

  /**
   * The core UI Silverpeas plugin based on JQuery.
   */
  $.fn.spCore = function() {
    $.spCore.doInitialize();
  };

  /**
   * Private function that performs an ajax request. By default, the request is
   * synchroned, that is to say the javascript running is waiting for the return of ajax request
   * request.
   */
  function __performAjaxRequest(settings) {
    var result = {};

    // Default options.
    // url, type, dataType are missing.
    var options = {
      cache : false,
      async : false,
      success : function(data, status, jqXHR) {
        result = data;
        if (options.onSuccessful) {
          options.onSuccessful(data);
        }
      },
      error : function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    };

    // Adding settings
    options = $.extend(options, settings);

    // Ajax request
    $.ajax(options);
    return result;
  }
})(jQuery);

/**
 * Object to manage common Silverpeas UI usage
 */
function LinkedItem(previous) {
  var self = this;
  var _previous = previous;
  if (_previous != null) {
    _previous.setNext(this);
  }
  var _next = null;
  this.setPrevious = function(node) {
    _previous = node;
  };
  this.getPrevious = function() {
    return _previous;
  };
  this.setNext = function(node) {
    _next = node;
  };
  this.getNext = function() {
    return _next;
  };
  this.getFirst = function() {
    var result = self;
    while (result.getPrevious() != null) {
      result = result.getPrevious();
    }
    return result;
  };
  this.getLast = function() {
    var result = self;
    while (result.getNext() != null) {
      result = result.getNext();
    }
    return result;
  };
  this.add = function(node) {
    node.setNext(_next);
    node.setPrevious(self);
    _next = node;
  };
  this.remove = function() {
    if (_previous != null) {
      _previous.setNext(self.getNext());
      if (self.getNext() != null) {
        self.getNext().setPrevious(_previous);
      }
      return _previous.getFirst();
    } else {
      _next.setPrevious(null);
      return _next.getFirst();
    }
  };
}
