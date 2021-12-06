/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

/**
 * Silverpeas plugin build upon JQuery to display a modal dialog box.
 * It uses the JQuery UI framework.
 */
(function($, $window) {
  if ($.popup) {
    return;
  }

  var popupDebug = false;

  var FS_MANAGER = new function() {
    this.isWindowCompatible = function() {
      return top.spLayout && !top.spLayout.isWindowTop($window);
    };
    this.getLayoutManager = function() {
      var _topWindow = top.spLayout.getWindowTopFrom($window).window;
      return _topWindow.spAdminLayout ? _topWindow.spAdminLayout : spLayout;
    };
    this.top$ = function() {
      return top.spLayout.getWindowTopFrom($window).jQuery;
    };
    this.topDocument = function() {
      return top.spLayout.getWindowTopFrom($window).document;
    }
  };

  var __displayFullscreenModalBackground = FS_MANAGER.isWindowCompatible();

  // Little hack to prevent some unexpected errors when escape key is
  // pressed during an ajax request
  const __preventEscape = function(e) {
    if (e.keyCode === 27) {
      e.preventDefault();
    }
  };

  $.popup = {
    /**
     * Shows a waiting information. Usually used while the popup is rendering or when the treatment
     * fired by the popup is being processed.
     */
    showWaiting: function() {
      var $waiting = $("#spWaiting");
      if ($waiting.length === 0) {
        $waiting = $("<div>").attr('id', 'spWaiting').attr('style',
                'display: none; border: 0; padding: 0; text-align: center; overflow: hidden;');
        $(document.body).append($waiting);
        $waiting.popup("waiting");
        $waiting.dialog("widget").keydown(__preventEscape);
      }
      $waiting.dialog("open");
    },
    /**
     * Hides the waiting information. Usually used once the popup is fully rendered or once the
     * treatment fired by the popin is done.
     */
    hideWaiting: function() {
      var $waiting = $("#spWaiting");
      if ($waiting.length > 0) {
        $waiting.dialog("close");
        $waiting.dialog("destroy");
        $waiting.remove();
      }
    },
    /**
     * Shows a confirmation popup with the specified message and the popup parameters.
     * @param message the message to display in the popup.
     * @param params the parameters to parametrize the popup window.
     */
    confirm: function(message, params) {
      var options = params;
      var $confirm = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callback: params
        }
      }
      $confirm.popup('confirmation', options);
    },
    /**
     * Shows a validate popup with the specified message and the popup parameters.
     * @param message the message to display in the popup.
     * @param params the parameters to parametrize the popup window.
     */
    validate: function(message, params) {
      let options = params;
      const $confirm = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callback: params
        }
      }
      $confirm.popup('validation', options);
    },
    /**
     * Shows an info popup with the specified message and the popup parameters.
     * @param message the info message to display in the popup.
     * @param params the parameters to parametrize the popup window.
     */
    info: function(message, params) {
      var options = params;
      var $info = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callbackOnClose: params
        }
      }
      $info.popup('information', options);
    },
    /**
     * Shows an error popup with the specified message and the popup parameters.
     * @param message the error message to display in the popup.
     * @param params the parameters to parametrize the popup window.
     */
    error: function(message, params) {
      var options = params;
      message = message.replaceAll('\n', '<br/>');
      var $error = $('<div>').append($('<p>').append(message));
      if (typeof params === 'function') {
        options = {
          callback: params
        }
      }
      $error.popup('error', options);
    },
    /**
     * Loads the popup from the specified URL.
     * @param {string} url the url at which the popup's content is loaded.
     * @param {object} [context] the HTTP context to use when requesting the popup's content: it is
     * an object with as attributes:
     * <ul>
     * <li> method: the HTTP method to use. By default, if not set, 'GET'. Either 'GET' or 'POST'
     * <li> params: the HTTP parameters to pass with the HTTP request.
     * </ul>
     * @return (*) a wrapper of the loaded popup.
     */
    load: function(url, context) {
      var loadRequest = sp.ajaxRequest(url);
      if (context) {
        if (context.method === 'POST') {
          loadRequest = loadRequest.byPostMethod();
        }
        if (context.params) {
          loadRequest = loadRequest.withParams(context.params);
        }
      }
      var loadPromise = loadRequest.send();
      return {
        /**
         * Shows the loaded popup by using the specified rendering type and popup parameters.
         * @param type the popup rendering type.
         * @param params the parameters to parametrize the popup window.
         * @return a promise within which the popup is being rendered. Once the popup is rendered,
         * any treatment declared in the then() function is then invoked.
         */
        show: function(type, params) {
          jQuery.popup.showWaiting();
          return new Promise(function(resolve, reject) {
            var options = params;
            loadPromise.then(function(request) {
              var data = request.responseText;
              var $popup = $('#popupHelperContainer');
              if ($popup.length !== 0) {
                $popup.remove();
              }
              $popup = $('<div>', {id: 'popupHelperContainer', 'style' : 'display:none'});
              $popup.appendTo(document.body);
              $popup.append(data);
              if (typeof params === 'function') {
                options = {
                  callback: params
                }
              }
              var onClose = options.callbackOnClose;
              options.callbackOnClose = function(arg) {
                if (typeof onClose === 'function') {
                  onClose.call(arg);
                }
                $popup.remove();
              };
              $popup.popup(type, options);
              resolve(data);
              if (sp.promise.isOne(options.openPromise)) {
                options.openPromise.then(jQuery.popup.hideWaiting);
              } else {
                jQuery.popup.hideWaiting();
              }
            }, function(request) {
              notyError(request.responseText);
              reject();
              jQuery.popup.hideWaiting();
            });
          });
        },
        close : function() {
          jQuery('#popupHelperContainer').popup('close');
        }
      }
    }
  };

  var __getLabel = function(key) {
    return sp.i18n.get(key);
  };

  /**
   * The different methods on messages handled by the plugin.
   */
  var methods = {
    /**
     * Close the current popup
     */
    close : function() {
      $(this).dialog('close');
    },
    /**
     * The modal free dialog : configure as you want your popup.
     */
    free: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      settings = $.extend(__buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }), settings);

      updateSettingsForIE7(settings, options);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal basic dialog. (scroll is deactivated)
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    basic: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        disabledParentScroll: true,
        width: 'auto'
      }));

      updateSettingsForIE7(settings, options);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal information dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing.
     */
    information: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = __getLabel('GML.information.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: __getLabel('GML.ok'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal error dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing.
     */
    error: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = __getLabel('GML.error.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: __getLabel('GML.ok'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal help dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing.
     */
    help: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = __getLabel('GML.help.dialog.title');
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextNo: __getLabel('GML.ok'),
        isMaxWidth: true,
        dialogClass: 'help-modal-message'
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal validation dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing.
     */
    validation: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      settings = $.extend(__buildInternalSettings({
        buttonTextYes: __getLabel('GML.validate'),
        buttonTextNo: __getLabel('GML.cancel'),
        isMaxWidth: true
      }), settings);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal confirmation dialog.
     * A warning icon is automatically inserted into the title bar.
     * It accepts one parameter that is an object with following attributes:
     * - title : the title of the dialog box (if it is empty a default title is used),
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing.
     */
    confirmation: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      if (!settings.title) {
        settings.title = __getLabel('GML.confirmation.dialog.title');
      }
      var $title = $('<div>').attr('style', 'display: table;');
      var $titleRow = $('<div>').attr('style', 'display: table-row;');
      $title.append($titleRow);
      var $icon = $('<span>').addClass('ui-icon ui-icon-alert');
      $icon.attr('style', 'float:left; margin:0 7px 0 0;');
      var $titleText = $('<div>').attr('style', 'display: table-cell;vertical-align: bottom;');
      $titleText.html(settings.title);
      $titleRow.append($('<div>').attr('style',
              'display: table-cell;vertical-align: middle;').append($icon));
      $titleRow.append($titleText);
      settings.title = $('<div>').append($title).html();

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonTextYes: __getLabel('GML.yes'),
        buttonTextNo: __getLabel('GML.no'),
        isMaxWidth: true
      }));

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal preview dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    preview: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      settings.title = __getLabel('GML.preview.dialog.title');
      if (options.title && options.title.length > 0) {
        settings.title =
                settings.title + " " + __getLabel('GML.preview.dialog.title.of') + " " + options.title;
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }));

      updateSettingsForIE7(settings, options);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal view dialog.
     * It accepts one parameter that is an object with following attributes:
     * - title : the document title of the dialog box,
     * - callback : the callback to invoke when the user clicks on the yes button. The callback must
     * returns a boolean indicating that all is ok and the dialog box can be closed or a promise
     * which resolve action close the dialog box,
     * - callbackOnClose : the callback on dialog box closing,
     * - width : width of content. Mandatory for IE7 browser and ignored in other cases,
     * - height : height of content. Mandatory for IE7 browser and ignored in other cases.
     */
    view: function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);
      settings.title = __getLabel('GML.view.dialog.title');
      if (options.title && options.title.length > 0) {
        settings.title =
                settings.title + " " + __getLabel('GML.view.dialog.title.of') + " " + options.title;
      }

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        buttonDisplayed: false,
        width: 'auto'
      }));

      updateSettingsForIE7(settings, options);

      // Dialog
      return __openPopup($(this), settings);
    },
    /**
     * The modal waiting dialog.
     */
    waiting: function() {
      var $container = $(this);

      // Common settings
      var settings = __extendCommonSettings({});

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        displayTitle: false,
        closeOnEscape: false,
        buttonDisplayed: false,
        width: "32px",
        height: 39
      }));

      // Waiting animation
      var imageUrl = popupViewGeneratorIconPath + '/inProgress.gif';
      $container.html($('<img>').attr('src', imageUrl).attr('width', '32').attr('height', '32'));

      // Dialog
      return __openPopup($container, settings);
    }
  };

  /**
   * The modal dialog box Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the popup namespace in JQuery in which methods on messages are provided.
   */
  $.fn.popup = function(method) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.validation.apply(this, arguments);
    } else {
      return $.error('Method ' + method + ' does not exist on jQuery.popup');
    }
  };

  /**
   * Private function that centralizes extension of common settings
   */
  function __extendCommonSettings(options) {
    var settings = {
      openPromise: undefined,
      title: '',
      callback: null,
      alternativeCallback: null,
      callbackOnClose: null
    };
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
      displayTitle: true,
      closeOnEscape: true,
      disabledParentScroll: false,
      buttonDisplayed: true,
      buttonTextYes: '',
      buttonTextNo: '',
      isMaxWidth: false,
      minWidth: undefined,
      maxWidth: undefined,
      width: 570,
      height: 'auto',
      dialogClass: ''
    };
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that checks if the browser is an IE7 one
   */
  function __isIE7() {
    return (navigator.appVersion.indexOf("MSIE 7.") !== -1);
  }

  function updateSettingsForIE7(settings, options) {
    if (__isIE7()) {
      // Width & Height
      if (options.width) {
        settings.width = options.width;
      }
      if (options.height) {
        settings.height = Number(options.height) + 27;
      }
    }
  }

  /**
   * Private function that centralizes a dialog creation and its opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openPopup($this, options) {
    if (!$this.length) {
      return $this;
    }

    return $this.each(function() {
      var $_this = $(this);
      if (!options.title) {
        options.title = $_this.attr('title');
      }

      $_this.dialog({
        closeOnEscape: options.closeOnEscape,
        title: options.title,
        autoOpen: false,
        modal: true,
        resizable: false,
        height: options.height,
        dialogClass: options.dialogClass
      });

      // Removing the title if requested
      if (!options.displayTitle) {
        $_this.dialog('widget').find(".ui-dialog-titlebar").hide();
      }

      // Buttons
      if (options.buttonDisplayed) {
        var buttons = [];
        if (options.buttonTextYes) {
          buttons.push({
            text: options.buttonTextYes,
            click: function() {
              var whenIsOk;
              if (options.callback) {
                // A callback must be processed before closing the dialog
                var result = options.callback.call($_this);
                if (sp.promise.isOne(result)) {
                  // The result of the callback is a promise, the dialog is closed after resolve
                  whenIsOk = result;
                } else if (typeof result === 'undefined' || result) {
                  // No result or true result, the dialog can be closed
                  whenIsOk = sp.promise.resolveDirectlyWith();
                } else {
                  // Explicit false result, the dialog stays open
                  whenIsOk = sp.promise.rejectDirectlyWith();
                }
              } else {
                // No callback, closing in any case
                whenIsOk = sp.promise.resolveDirectlyWith();
              }
              // Closing the dialog if ok
              whenIsOk.then(function() {
                $_this.dialog("close");
              })['catch'](function() {
                sp.log.debug('dialog not closed as validation failed');
              });
            }
          });
        }
        if (options.buttonTextNo) {
          buttons.push({
            text: options.buttonTextNo,
            click: function() {
              if (options.alternativeCallback) {
                options.alternativeCallback.call($_this);
              }
              $_this.dialog("close");
            }
          });
        }
        if (buttons.length > 0) {
          $_this.dialog("option", "buttons", buttons);
        }
      }

      // Callback on close
      $_this.dialog("option", "close", function(event, ui) {
        if (options.callbackOnClose) {
          options.callbackOnClose.call(this);
        }
      });

      // Scroll
      if (options.disabledParentScroll) {
        $_this.dialog("option", "open", function(event, ui) {
          $("html,body").css("overflow", "hidden");
        });
        $_this.dialog("option", "beforeClose", function(event, ui) {
          $("html,body").css("overflow", "auto");
        });
      }

      // Width
      var width = "" + options.width;
      var maxWidth = (width !== 'auto') ? width.replace(/px/, '') + 'px' : width;
      if (options.isMaxWidth) {
        $_this.dialog("option", "width", "auto");
      } else {
        $_this.dialog("option", "width", width);
      }
      var __openPopup = function() {
        // Dialog opening
        var $dialog = $_this.dialog('open');

        // Since JQuery upgrade (jquery-1.10.1.min.js), HTML code in title is escaped.
        // The below code surrounds this problem.
        // For new version of jquery, please verify if it can be removed.
        $dialog.data("uiDialog")._title = function(title) {
          title.html(this.options.title);
        };
        $dialog.dialog('option', 'title', options.title);

        // This below code handles the width of the dialog after it has been displayed.
        if (options.isMaxWidth) {
          // If max width is required, resizing and repositioning after the dialog open
          $_this.dialog("widget").css('max-width', maxWidth);
          $_this.dialog({position : $_this.dialog('option', 'position')});
        }

        // Min Width
        if (options.minWidth) {
          var _minWidth = "" + options.minWidth;
          _minWidth = (_minWidth !== 'auto') ? _minWidth.replace(/px/, '') + 'px' : _minWidth;
          $_this.dialog("widget").css('min-width', _minWidth);
        }

        // Max Width
        if (options.maxWidth) {
          var _maxWidth = "" + options.maxWidth;
          _maxWidth = (_maxWidth !== 'auto') ? _maxWidth.replace(/px/, '') + 'px' : _maxWidth;
          $_this.dialog("widget").css('max-width', _maxWidth);
        }
      };
      if (sp.promise.isOne(options.openPromise)) {
        options.openPromise.then(__openPopup);
      } else {
        __openPopup();
      }
    });
  }

  var spFullscreenModalBackgroundContext = new function() {
    var __debug = function(message) {
      __logDebug("FSContext -", message)
    };
    var removeContainers = function($containers) {
      if ($containers.length > 0) {
        $containers.each(function(index, $container){
          $container.spUIManager.destroy();
        });
        __debug($containers.length + " dialogs removed");
      }
    };
    this.clear = function() {
      __debug("clearing");
      removeContainers(this.getContainers());
      FS_MANAGER.getLayoutManager().getBody().getContent().setOnBackground();
    };
    this.removeLast = function() {
      __debug("removing last");
      var $containers = this.getContainers();
      removeContainers($containers.filter(':last'));
      if ($containers.length === 1) {
        FS_MANAGER.getLayoutManager().getBody().getContent().setOnBackground();
      }
    };
    this.getContainers = function() {
      return FS_MANAGER.top$()(".spFullscreenModalBackground", FS_MANAGER.topDocument());
    };
  };

  /**
   * Private function that centralizes a fullscreen modal background.
   * Be careful, options have to be well initialized before this function call
   */
  function __openFullscreenModalBackground($dialogInstance) {
    var nbCurrentContainers = spFullscreenModalBackgroundContext.getContainers().length;
    if (nbCurrentContainers === 0) {
      FS_MANAGER.getLayoutManager().getBody().getContent().setOnForeground();
    }
    var $container = FS_MANAGER.top$()("<div>")
        .attr('class', 'spFullscreenModalBackground ui-widget-overlay ui-front')
        .attr('style', 'display: none; border: 0; padding: 0; overflow: hidden;');
    FS_MANAGER.top$()(FS_MANAGER.topDocument().body).append($container);
    var $containerElement = $container[0];
    $containerElement.spUIManager = new function() {
      this.isOpen = function() {
        return $container.css('display') !== 'none';
      };
      this.open = function() {
        return $container.show();
      };
      this.close = function() {
        return $container.hide();
      };
      this.destroy = function() {
        return $container.remove();
      };
    };

    // Little hack to prevent some unexpected errors when escape key is
    // pressed during an ajax request
    $containerElement.addEventListener('keydown', __preventEscape);

    // Handling HTML forms in order to close the dialog on submit action.
    // As jQuery Handles only jQuery triggering, jQuery method and the standard one must be
    // managed.
    var $forms = $('form', $dialogInstance);
    var dialogInstanceElement = $dialogInstance[0];
    var forms = dialogInstanceElement.querySelectorAll("form");
    var __lastRegisteredHandler = dialogInstanceElement.__lastRegisteredHandler;
    if (__lastRegisteredHandler) {
      $forms.unbind('submit', __lastRegisteredHandler);
      [].slice.call(forms, 0).forEach(function(form) {
        form.removeEventListener('submit', __lastRegisteredHandler);
      });
    }
    dialogInstanceElement.__lastRegisteredHandler = function() {
      try {
        if ($containerElement.spUIManager.isOpen()) {
          $containerElement.spUIManager.close();
        }
      } catch (e) {
        sp.log.debug(e);
      }
      return true;
    };
    $forms.bind('submit', dialogInstanceElement.__lastRegisteredHandler);
    [].slice.call(forms, 0).forEach(function(form) {
      form.addEventListener('submit', dialogInstanceElement.__lastRegisteredHandler);
    });

    // Displaying the dialog.
    $containerElement.spUIManager.open();
  }

  function __closeFullscreenModalBackground() {
    spFullscreenModalBackgroundContext.removeLast();
  }

  function __adjustPosition(dialogOptions) {
    var position = dialogOptions.position;
    if (position.my === "center" && position.at === "center" && position.of === window) {
      var _layoutManager = FS_MANAGER.getLayoutManager();
      var headerHeightOffset = _layoutManager.getHeader().getContainer().offsetHeight / 2;
      var isContentFullWidth = !(FS_MANAGER.top$()(_layoutManager.getBody().getContent().getContainer()).position().left);
      if (isContentFullWidth) {
        var navigationHeightOffset = _layoutManager.getBody().getNavigation().getContainer().offsetHeight / 2;
        position.at = "center center-" + (headerHeightOffset + navigationHeightOffset);
      } else {
        var navigationWidthOffset = _layoutManager.getBody().getNavigation().getContainer().offsetWidth / 2;
        position.at = "center-" + navigationWidthOffset + " center-" + headerHeightOffset;
      }
    }
    return position;
  }

  $.widget("ui.dialog", $.ui.dialog, {
    open : function() {
      if (__displayFullscreenModalBackground && !this._isOpen) {
        __adjustPosition(this.options);
        __openFullscreenModalBackground(this.element);
      }
      return this._super();
    },
    close : function() {
      if (__displayFullscreenModalBackground && this._isOpen) {
        __closeFullscreenModalBackground();
      }
      return this._super();
    },
    destroy : function() {
      if (__displayFullscreenModalBackground && this._isOpen) {
        __closeFullscreenModalBackground();
      }
      return this._super();
    }
  });

  if (__displayFullscreenModalBackground) {
    __logDebug("cleaning popup from iframe, window " + window.name);
    spFullscreenModalBackgroundContext.clear();
  }

  /**
   * Logs debug messages.
   * @private
   */
  function __logDebug() {
    if (popupDebug) {
      var mainDebugStatus = sp.log.debugActivated;
      sp.log.debugActivated = true;
      var messages = [];
      Array.prototype.push.apply(messages, arguments);
      messages.splice(0, 0, "Popup -");
      sp.log.debug.apply(this, messages);
      sp.log.debugActivated = mainDebugStatus;
    }
  }
})(jQuery, window);
