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

/**
 * Silverpeas plugin build upon JQuery to display a document preview.
 * It uses the JQuery UI framework.
 */
(function($) {

  $.preview = {
    webServiceContext: webContext + '/services',
    initialized: false,
    doInitialize: function() {
      if (!$.preview.initialized) {
        $.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/org/silverpeas/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        $.preview.initialized = true;
      }
    }
  };

  /**
   * The different preview methods handled by the plugin.
   */
  var methods = {
    /**
     * Does nothing
     */
    init: function(options) {
      // Nothing to do at all
    },
    /**
     * Handles the document preview.
     * It accepts one parameter that is an object with two mandatory attributes:
     * - componentInstanceId : the id of the current component instance,
     * - attachmentId : the id of the aimed attachment.
     */
    previewAttachment: function(options) {

      // Light checking
      if (!options.componentInstanceId || !options.attachmentId) {
        alert("Bad component instance id or attachment id");
        return false;
      }

      // Dialog
      return __openPreview($(this), options);
    }
  };

  /**
   * The preview Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the preview namespace in JQuery.
   */
  $.fn.preview = function(method) {

    if (!$().popup) {
      alert("Silverpeas Popup JQuery Plugin is required.");
      return false;
    }

    $.preview.doInitialize();
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.preview.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.preview');
    }
  };

  /**
   * Private function that handles the preview opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openPreview($this, options) {

    if (!$this.length)
      return $this;

    return $this.each(function() {
      var $_this = $(this);

      // Waiting animation
      $.popup.showWaiting();

      // Getting preview
      var url = $.preview.webServiceContext;
      url += "/preview/" + options.componentInstanceId;
      url += "/attachment/" + options.attachmentId;
      if (options.lang) {
        url += "?lang=" + options.lang;
      }
      $.ajax({
        url: url,
        type: 'GET',
        dataType: 'json',
        cache: false,
        success: function(data, status, jqXHR) {
          $.popup.hideWaiting();
          __openDialogPreview($_this, data);
        },
        error: function(jqXHR, textStatus, errorThrown) {
          $.popup.hideWaiting();
          alert(errorThrown);
        }
      });
    });
  }

  /** Technical attribute to avoid having several same functions triggered on keydown */
  var __previousOrNextPreview;

  /**
   * Private function that centralizes the dialog preview construction
   */
  function __openDialogPreview($this, preview) {

    if (__previousOrNextPreview) {
      $(document).unbind('keydown', __previousOrNextPreview);
    }

    // Initializing the resulting html container
    var $baseContainer = $("#documentPreview");
    if ($baseContainer.size() === 0) {
      $baseContainer = $("<div>")
              .attr('id', 'documentPreview')
              .css('display', 'block')
              .css('border', '0px')
              .css('padding', '0px')
              .css('margin', '0px auto')
              .css('text-align', 'center')
              .css('background-color', 'white');
      $baseContainer.insertAfter($this);
    }

    // Getting previous and next
    var previousIndex = -1;
    var nextIndex = -1;
    var allDocumentPreviews = $.makeArray($(document).find(".preview-file"));
    if (allDocumentPreviews && allDocumentPreviews.length > 1) {
      previousIndex = $.inArray($this.get(0), allDocumentPreviews) - 1;
      if (previousIndex < 0) {
        previousIndex = allDocumentPreviews.length - 1;
      }
      nextIndex = $.inArray($this.get(0), allDocumentPreviews) + 1;
      if (nextIndex >= allDocumentPreviews.length) {
        nextIndex = 0;
      }
    }

    // Function to navigate between images
    __previousOrNextPreview = function(e) {
      var keyCode = eval(e.keyCode);
      if (previousIndex >= 0 && 37 <= keyCode && keyCode <= 40) {
        e.preventDefault();
        var previousOrNextPreviewTarget = null;
        if (38 === keyCode) {
          // Up
          previousOrNextPreviewTarget = allDocumentPreviews[previousIndex];
        } else if (40 === keyCode) {
          // Down
          previousOrNextPreviewTarget = allDocumentPreviews[nextIndex];
        }
        $(previousOrNextPreviewTarget).click();
        return false;
      }
      return true;
    };
    $(document).bind('keydown', __previousOrNextPreview);

    // Popup settings
    var settings = {
      title: preview.originalFileName,
      width: preview.width,
      height: preview.height,
      callbackOnClose: function() {
        $(document).unbind('keydown', __previousOrNextPreview);
      }
    };

    // Preview content
    var $previewContent = $('<div>').css('display', 'block').css('margin', '0px').css('padding',
            '0px').css('text-align', 'center');
    $previewContent.append($('<img>').attr('src', preview.url)
            .attr('width', preview.width)
            .attr('height', preview.height));

    // Buttons
    var $previousButton;
    var $nextButton;
    if (previousIndex >= 0) {
      $previousButton = __buildButton($baseContainer,
              'previousPreview',
              allDocumentPreviews[previousIndex]);
      $nextButton = __buildButton($baseContainer,
              'nextPreview',
              allDocumentPreviews[nextIndex]);
      $previewContent.append($previousButton);
      $previewContent.append($nextButton);
    }

    // Popup
    $baseContainer.html($previewContent);
    $baseContainer.popup('preview', settings);

    // Final event/UI configurations (that need the dialog opened)
    if (previousIndex >= 0) {
      __configureButtonPosition('previousPreview', $baseContainer, $previousButton);
      __configureButtonPosition('nextPreview', $baseContainer, $nextButton);
    }
  }

  /**
   * Private function that centralizes an button construction
   */
  function __buildButton($baseContainer, type, nextDomObjectPreview) {

    // Initializing
    var $buttonContainer = $('<div>')
            .addClass('dialog-popup-button')
            .css('display', 'none')
            .css('position', 'absolute')
            .css('top', '0px')
            .css('left', '0px');

    // Help
    var titlePropertyKey;
    if (type === 'previousPreview') {
      titlePropertyKey = 'GML.preview.help.file.previous';
    } else {
      titlePropertyKey = 'GML.preview.help.file.next';
    }
    $buttonContainer.attr('title', $.i18n.prop(titlePropertyKey));

    // This first call permits to load required images for a button hover event
    __configureVisualButtonAspect(type, true);
    // This second call permits to load required images for a simple button
    var $button = __configureVisualButtonAspect(type, false);
    $buttonContainer.html($button);

    // Setting onclick result
    $buttonContainer.click(function() {
      $(nextDomObjectPreview).click();
    });

    // Setting onmouseover/onmouseout button event
    $buttonContainer.mouseenter(function() {
      __configureVisualButtonAspect(type, true, $button);
    });
    $buttonContainer.mouseleave(function() {
      __configureVisualButtonAspect(type, false, $button);
    });

    // Setting baseContainer event (print buttons)
    $baseContainer.mouseenter(function() {
      $buttonContainer.fadeIn(200);
    });
    $baseContainer.mouseleave(function() {
      $buttonContainer.fadeOut(400);
    });
    return $buttonContainer;
  }

  /**
   * Private function that centralizes the configuration of a button on visual side
   */
  function __configureVisualButtonAspect(type, isHover, $button) {

    // Choosing the right image
    if (type === 'previousPreview') {
      iconFileName = (!isHover) ? 'arrowUp.gif' : 'arrowUpModal.png';
    } else {
      iconFileName = (!isHover) ? 'arrowDown.gif' : 'arrowDownModal.png';
    }

    // Initializing the image if necessary
    if (!$button) {
      $button = $('<img>').addClass('dialog-popup-button-image');
    }

    // Setting the image source attribute
    $button.attr('src', webContext + '/util/icons/arrow/' + iconFileName);
    return $button;
  }

  /**
   * Private function that centralizes the position configuration of a button
   */
  function __configureButtonPosition(type, $target, $buttonContainer) {

    // Top
    var top = $(document).scrollTop();
    if (type !== 'previousPreview') {
      top += ($target.outerHeight(true) - $buttonContainer.outerHeight(true));
    }

    // Left
    var left = $(document).scrollLeft() + (($target.outerWidth(true) / 2) - ($buttonContainer.outerWidth(true) / 2));

    // Changing the position
    $buttonContainer.offset({top: top, left: left});
  }
})(jQuery);
