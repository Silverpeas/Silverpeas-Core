/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

/**
 * Silverpeas plugin build upon JQuery to display a document preview.
 * It uses the JQuery UI framework.
 */
(function() {

  const $W = window;
  const $TW = top.window;
  const docSrc = $W.document;
  const $Src = $W.jQuery;
  const docUI = $TW.document;
  const $UI = $TW.jQuery;

  $Src.preview = {
    webServiceContext: webContext + '/services'
  };

  /**
   * The different preview methods handled by the plugin.
   */
  const methods = {
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
        console.error("Bad component instance id or attachment id");
        return false;
      }

      // Dialog
      return __openPreview($Src(this), options);
    }
  };

  /**
   * The preview Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the preview namespace in JQuery.
   */
  $Src.fn.preview = function(method) {

    if (!$UI.popup) {
      console.error("Silverpeas Popup JQuery Plugin is required.");
      return false;
    }

    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.preview.apply(this, arguments);
    } else {
      $Src.error('Method ' + method + ' does not exist on jQuery.preview');
    }
  };

  /**
   * Private function that handles the preview opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openPreview($this, options) {
    if (!$this.length) {
      return $this;
    }
    const service = new ViewService();
    const dialog = new PreviewDialog();
    return $this.each(function() {
      const $_this = $Src(this);
      // Waiting animation
      $UI.popup.showWaiting();
      service.getDocumentPreview(options.attachmentId, options.componentInstanceId, options.lang).then(function(preview) {
        $UI.popup.hideWaiting();
        dialog.showWith($_this[0], preview);
      }, function(e) {
        $UI.popup.hideWaiting();
        console.error(e);
      });
    });
  }

  const PreviewDialog = function() {
    let __$ref;
    let __preview;
    let $container = docUI.querySelector("#documentPreview");
    function __closePreviousPopup() {
      const __spPreviewPopup = $container.__spPreviewPopup;
      if (__spPreviewPopup) {
        __spPreviewPopup.popup('destroy');
      }
    }
    if ($container) {
      __closePreviousPopup();
      $container.remove();
    }
    $container = docUI.createElement('div');
    $container.setAttribute('id', 'documentPreview');
    docUI.body.appendChild($container);
    this.showWith = function($ref, preview) {
      __$ref = $ref;
      __preview = preview;
      this.refresh();
    }
    this.refresh = function() {
      $container.innerHTML = '';
      if (!!__preview) {
        const $previewContent = new PreviewContent(__preview, docUI);
        $container.appendChild($previewContent.getContainer());
        const $navigation = new PreviewNavigation(__$ref, $container);
        __closePreviousPopup();
        $container.__spPreviewPopup = $UI($container).popup('preview', {
          title: __preview.getTitle(),
          width: __preview.getWidth(),
          height: __preview.getHeight(),
          callbackOnClose : function() {
            $navigation.destroy();
          }
        });
        $navigation.refresh();
      }
    };
    this.refresh();
  }

  const PreviewContent = function(preview, doc) {
    const $container = doc.createElement('div');
    $container.classList.add('content');
    const $img = docUI.createElement('img');
    $img.setAttribute('width', preview.getWidth());
    $img.setAttribute('height', preview.getHeight());
    $img.setAttribute('src', preview.getImgUrl());
    $container.appendChild($img);
    this.getContainer = function() {
      return $container;
    };
  }

  const PreviewNavigation = function($ref, $hostContainer) {
    const $baseContainer = $UI($hostContainer);
    let previousIndex = -1;
    let nextIndex = -1;
    const allDocumentPreviews = $W.sp.element.querySelectorAll(".preview-file", docSrc);
    if (allDocumentPreviews && allDocumentPreviews.length > 1) {
      previousIndex = $Src.inArray($ref, allDocumentPreviews) - 1;
      if (previousIndex < 0) {
        previousIndex = allDocumentPreviews.length - 1;
      }
      nextIndex = $Src.inArray($ref, allDocumentPreviews) + 1;
      if (nextIndex >= allDocumentPreviews.length) {
        nextIndex = 0;
      }
    }
    // Function to navigate between images
    const __previousOrNextPreview = function(e) {
      const keyCode = Number(e.keyCode);
      if (previousIndex >= 0 && 37 <= keyCode && keyCode <= 40) {
        e.preventDefault();
        let previousOrNextPreviewTarget = null;
        if (38 === keyCode) {
          // Up
          previousOrNextPreviewTarget = allDocumentPreviews[previousIndex];
        } else if (40 === keyCode) {
          // Down
          previousOrNextPreviewTarget = allDocumentPreviews[nextIndex];
        }
        $Src(previousOrNextPreviewTarget).click();
        return false;
      }
      return true;
    };
    let $previousButton;
    let $nextButton;
    if (previousIndex >= 0) {
      $previousButton = __buildButton($baseContainer,
          'previousPreview',
          allDocumentPreviews[previousIndex]);
      $nextButton = __buildButton($baseContainer,
          'nextPreview',
          allDocumentPreviews[nextIndex]);
      $baseContainer.append($previousButton);
      $baseContainer.append($nextButton);
      $UI(docUI).bind('keydown', __previousOrNextPreview);
    }
    this.refresh = function() {
      // Final event/UI configurations (that need the dialog opened)
      if (previousIndex >= 0) {
        __configureButtonPosition('previousPreview', $baseContainer, $previousButton);
        __configureButtonPosition('nextPreview', $baseContainer, $nextButton);
      }
    };
    this.destroy = function() {
      if (previousIndex >= 0) {
        $UI(docUI).unbind('keydown', __previousOrNextPreview);
      }
    };
  }

  /**
   * Private function that centralizes an button construction
   */
  function __buildButton($baseContainer, type, nextDomObjectPreview) {
    // Initializing
    const $buttonContainer = $UI('<div>')
            .addClass('dialog-popup-button')
            .css('display', 'none')
            .css('position', 'absolute')
            .css('top', '0px')
            .css('left', '0px');
    // Help
    let titlePropertyKey;
    if (type === 'previousPreview') {
      titlePropertyKey = 'GML.preview.help.file.previous';
    } else {
      titlePropertyKey = 'GML.preview.help.file.next';
    }
    $buttonContainer.attr('title', sp.i18n.get(titlePropertyKey));
    // This first call permits to load required images for a button hover event
    __configureVisualButtonAspect(type, true);
    // This second call permits to load required images for a simple button
    const $button = __configureVisualButtonAspect(type, false);
    $buttonContainer.html($button);
    // Setting onclick result
    $buttonContainer.click(function() {
      $UI(nextDomObjectPreview).click();
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
    let iconFileName;
    if (type === 'previousPreview') {
      iconFileName = (!isHover) ? 'arrowUp.gif' : 'arrowUpModal.png';
    } else {
      iconFileName = (!isHover) ? 'arrowDown.gif' : 'arrowDownModal.png';
    }
    // Initializing the image if necessary
    if (!$button) {
      $button = $UI('<img>').addClass('dialog-popup-button-image');
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
    let top = $UI(docUI).scrollTop();
    if (type !== 'previousPreview') {
      top += ($target.outerHeight(true) - $buttonContainer.outerHeight(true));
    }
    // Left
    let left = $UI(docUI).scrollLeft() + (($target.outerWidth(true) / 2) - ($buttonContainer.outerWidth(true) / 2));
    // Changing the position
    $buttonContainer.offset({top: top, left: left});
  }
})();
