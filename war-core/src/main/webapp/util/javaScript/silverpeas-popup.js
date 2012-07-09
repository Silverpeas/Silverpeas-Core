/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * Silverpeas plugin build upon JQuery to display a modal dialog box.
 * It uses the JQuery UI framework.
 */
(function( $ ){

  $.popup = {
    initialized: false,
    doInitialize : function() {
      if (! $.popup.initialized) {
        $.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/com/stratelia/webactiv/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        $.popup.initialized = true;
      }
    }
  }

  /**
   * The different methods on messages handled by the plugin.
   */
  var methods = {

    /**
     * The modal validation dialog.
     * It accepts one parameter that is an object with two attributes:
     * - the title of the dialog box,
     * - the callback to invoke when the user clicks on the validation button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed.
     */
    validation : function( options ) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextYes : $.i18n.prop('GML.validate'),
        buttonTextNo : $.i18n.prop('GML.cancel')
      }));

      // Dialog
      return __openPopup($(this), settings);
    },

    /**
     * The modal confirmation dialog.
     * A warning icon is automatically inserted into the title bar.
     * It accepts one parameter that is an object with two attributes:
     * - the title of the dialog box (if it is empty a default title is used),
     * - the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed.
     */
    confirmation : function( options ) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title || settings.title == null || settings.title.length == 0) {
        settings.title = $.i18n.prop('GML.confirmation.dialog.title');
      }
      var $title = $('<div>').attr('style', 'display: table;');
      var $titleRow = $('<div>').attr('style', 'display: table-row;');
      $title.append($titleRow);
      var $icon = $('<span>').addClass('ui-icon ui-icon-alert');
      $icon.attr('style', 'float:left; margin:0 7px 0 0;');
      var $titleText = $('<div>').attr('style','display: table-cell;vertical-align: bottom;');
      $titleText.html(settings.title);
      $titleRow.append($('<div>').attr('style', 'display: table-cell;vertical-align: middle;')
                       .append($icon));
      $titleRow.append($titleText);
      settings.title = $('<div>').append($title).html();

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextYes : $.i18n.prop('GML.yes'),
        buttonTextNo : $.i18n.prop('GML.no'),
        isMaxWidth : true
      }));

      // Dialog
      return __openPopup($(this), settings);
    }
  };

  /**
   * The modal dialog box Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the popup namespace in JQuery in which methods on messages are provided.
   */
  $.fn.popup = function( method ) {
    $.popup.doInitialize();
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.validation.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.popup' );
    }
  };

  /**
   * Private function that centralizes extension of common settings
   */
  function __extendCommonSettings(options) {
    var settings = {
      title : '',
      callback : null
    }
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes extension of internal settings
   */
  function __buildInternalSettings(options) {
    var settings = {
      buttonTextYes : '',
      buttonTextNo : '',
      isMaxWidth : false
    }
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes a dialog creation and its opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openPopup($this, options) {

    if (!$this.length)
      return $this;

    return $this.each(function() {
      $(this).dialog({
        title : options.title,
        autoOpen : false,
        modal : true,
        resizable : false,
        buttons : [ {
          text : options.buttonTextYes,
          click : function() {
            var isok = true;
            if (options.callback) {
              isok = options.callback();
            }
            if (isok)
              $(this).dialog("close");
          }
        }, {
          text : options.buttonTextNo,
          click : function() {
            $(this).dialog("close");
          }
        } ],
        height : 'auto'
      });
      var widthOption = "width";
      if (options.isMaxWidth) {
        widthOption = "maxWidth";
      }
      $(this).dialog("option", widthOption, 570);
      $(this).dialog('open');
    })
  }

})( jQuery );

