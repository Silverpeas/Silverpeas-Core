/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * Silverpeas plugin build upon JQuery to display a document view.
 * It uses the JQuery UI framework.
 */
(function($){

  $.view = {
    webServiceContext : webContext + '/services'
  };

  /**
   * The different view methods handled by the plugin.
   */
  var methods = {

    /**
     * Does nothing
     */
    init : function( options ) {
      // Nothing to do at all
    },

    /**
     * Handles the document view.
     * It accepts one parameter that is an object with two mandatory attributes:
     * - componentInstanceId : the id of the current component instance,
     * - attachmentId : the id of the aimed attachment.
     */
    viewAttachment : function( options ) {

      // Light checking
      if (!options.componentInstanceId || !options.attachmentId) {
        console.error("Bad component instance id or attachment id");
        return false;
      }

      // Dialog
      return __openView($(this), options);
    }
  };

  /**
   * The view Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the view namespace in JQuery.
   */
  $.fn.view = function( method ) {

    if (!$().popup) {
      console.error("Silverpeas Popup JQuery Plugin is required.");
      return false;
    }

    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.view.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.view' );
    }
  };

  /**
   * Private function that handles the view opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openView($this, options) {

    if (!$this.length)
      return $this;

    return $this.each(function() {
      var $_this = $(this);

      // Waiting animation
      if (!options.insertMode) {
        $.popup.showWaiting();
      } else {
        var imageUrl = popupViewGeneratorIconPath + '/inProgress.gif';
        $_this.html($('<img>').attr('src', imageUrl).attr('width', '32').attr('height', '32'));
      }

      // Getting view
      var url = $.view.webServiceContext;
      url += "/view/" + options.componentInstanceId;
      url += "/attachment/" + options.attachmentId;
      if (options.lang) {
        url += "?lang=" + options.lang;
      }
      $.ajax({
        url : url,
        type : 'GET',
        dataType : 'json',
        cache : false,
        success : function(data, status, jqXHR) {
          if (options.insertMode) {
            __insertView($_this, data);
          } else {
            __openDialogView($_this, data);
          }
          $.popup.hideWaiting();
        },
        error : function(jqXHR, textStatus, errorThrown) {
          $.popup.hideWaiting();
          sp.log.error("view", "technical error for", JSON.stringify(options), errorThrown);
        }
      });
    })
  }

  /**
   * Private function that centralizes the dialog view construction.
   */
  function __insertView($this, view) {
    __adjustViewSize(view);

    // Initializing the resulting html container
    var $baseContainer = $("<div>")
                      .css('display', 'block')
                      .css('border', '0px')
                      .css('padding', '0px')
                      .css('margin', '0px auto')
                      .css('text-align', 'center')
                      .css('background-color', 'white');
    $this.html($baseContainer);

    // Popup
    var $documentViewer = __setView($baseContainer, view, true);

    // Player
    $documentViewer.embedPlayer({
      url : view.viewerUri,
      width : view.width,
      height : view.height
    });
  }

  /**
   * Private function that centralizes the dialog view construction.
   */
  function __openDialogView($this, view) {
    sp.navigation.mute();

    __adjustViewSize(view);

    // Initializing the resulting html container
    var $baseContainer = $("#documentView");
    if ($baseContainer.length !== 0) {
      $baseContainer.remove();
    }
    $baseContainer = $("<div>")
                      .attr('id', 'documentView')
                      .css('display', 'block')
                      .css('border', '0px')
                      .css('padding', '0px')
                      .css('margin', '0px auto')
                      .css('text-align', 'center')
                      .css('background-color', 'white');
    $baseContainer.insertAfter($this);

    // Settings
    var settings = {
      title : view.originalFileName,
      width : view.width,
      height : view.height,
      callbackOnClose: function() {
        sp.navigation.unmute();
      }
    };

    // Popup
    var $documentViewer = __setView($baseContainer, view);

    // Player
    $documentViewer.embedPlayer({
      url : view.viewerUri,
      width : view.width,
      height : view.height
    });

    $baseContainer.popup('view', settings);
  }

  /**
   * Private function that adjust size of view (size limitations)
   */
  function __adjustViewSize(view) {
    var isDefaultView = view.viewMode === 'Default';

    // Screen size
    var offsetWidth = isDefaultView ? 1 : (view.width < view.height ? 2 : 1.75);
    var parentWidth = $(window).width() * 0.9;
    var parentHeight = $(window).height() * 0.9;

    // Document size
    var width = view.width * offsetWidth;
    var height = view.height;

    // Maximum size
    if (width > parentWidth) {
      height = height * (parentWidth / width);
      width = parentWidth;
    }
    if (height > parentHeight) {
      width = width * (parentHeight / height);
      height = parentHeight;
    }

    // Size
    if (isDefaultView)  {
      view.height = (height < 480) ? 480 : height;
      view.width = (width < 680) ? 680 : width;
    } else {
      view.height = height;
      view.width = width;
    }
  }

  /**
   * Private function that sets the view container.
   */
  function __setView($baseContainer, view, insertMode) {
    var documentViewer = $('<div>')
        .css('display', 'block')
        .css('margin', '0px')
        .css('padding', '0px');
    var $viewerContainer = $('<div>')
        .attr('id','viewercontainer')
        .css('display', 'block')
        .css('margin', '0px')
        .css('padding', '0px')
        .css('text-align', 'center')
        .append(documentViewer);
    $baseContainer.html($viewerContainer);
    if (!insertMode) {
      $viewerContainer.css('width', view.width + 'px')
          .css('height', view.height + 'px');
      documentViewer.css('width', view.width + 'px')
          .css('height', view.height + 'px')
          .css('background-color', '#222222');
    }
    return documentViewer;
  }


  /*
  ATTACHMENT VIEWER AS CONTENT
   */

  var DISPLAY_AS_CONTENT_COMPONENT_NAMES = ViewSettings.get("dac.cns");

  window.AttachmentsAsContentViewer = function(options) {
    var __context = {
      options : extendsObject({
        domSelector : undefined,
        parentContainer : undefined,
        componentInstanceId : undefined,
        resourceId : undefined,
        resourceType : undefined,
        contentLanguage : 'fr',
        documentType : 'attachment',
        highestUserRole : undefined,
        enableImage : true,
        enableVideo : true,
        enableAudio : true,
        enableViewable : true,
        enableSimpleText : true
      }, options)
    };
    if (!__context.options.componentInstanceId || !__context.options.resourceId ||
        !__context.options.resourceType) {
      sp.log.error("componentInstanceId ("+__context.options.componentInstanceId+"), resourceId ("+__context.options.resourceId+") or resourceType ("+__context.options.resourceType+") is missing");
      return;
    }
    if (__context.options.parentContainer) {
      __context.options.parentContainer = sp.element.asVanillaOne(__context.options.parentContainer);
    } else if (!__context.options.domSelector) {
      sp.log.error("domSelector or parentContainer is missing");
      return;
    }

    var componentName = sp.component.extractNameFromInstanceId(__context.options.componentInstanceId);
    if (!DISPLAY_AS_CONTENT_COMPONENT_NAMES.getElement(componentName)) {
      sp.log.debug("Not activated for component name " + componentName);
      return;
    }

    var __loadAttachments = function() {
      return sp.ajaxRequest(webContext + "/services/documents/" +
          __context.options.componentInstanceId + "/resource/" + __context.options.resourceId +
          "/types/" + __context.options.documentType + "/" +
          __context.options.contentLanguage)
          .withParam("viewIndicators", true)
          .withParam("highestUserRole", __context.options.highestUserRole)
          .sendAndPromiseJsonResponse();
    };
    var __initPromise = __loadAttachments();

    var __renderContainer = function(attachment, __renderCallback) {
      var $attContainer = document.createElement('div');
      $attContainer.classList.add('attachment-container');
      var $title = document.createElement('h3');
      $title.classList.add('title');
      $title.innerText = attachment.title ? attachment.title : attachment.fileName;
      var $description = document.createElement('p');
      $description.classList.add('description');
      $description.innerText = attachment.description;
      $attContainer.appendChild($title);
      if (StringUtil.isDefined(attachment.description)) {
        $attContainer.appendChild($description);
      }
      var $renderedContent = __renderCallback(attachment);
      $renderedContent.classList.add('content');
      $attContainer.appendChild($renderedContent);
      return $attContainer;
    };

    var __renderSimpleText = function(attachment) {
      var $simpleText = document.createElement("div");
      sp.ajaxRequest(webContext + attachment.downloadUrl).send().then(function(request) {
        $simpleText.innerText = request.responseText;
      });
      return $simpleText;
    };
    var __renderImage = function(attachment) {
      var $imageContainer = document.createElement("div");
      var $img = document.createElement("img");
      $img.src = webContext + attachment.downloadUrl.replace(/(\/lang\/[a-z]+\/)(name\/)/g, '$1size/600x/$2');
      $imageContainer.appendChild($img);
      return $imageContainer;
    };
    var __renderMedia = function(attachment) {
      var $mediaContainer = document.createElement("div");
      jQuery($mediaContainer).embedPlayer({
        url : webContext + attachment.downloadUrl
      });
      return $mediaContainer;
    };
    var __renderViewable = function(attachment) {
      var $viewableContainer = document.createElement("div");
      jQuery($viewableContainer).view("viewAttachment", {
        componentInstanceId: attachment.instanceId,
        attachmentId: attachment.id,
        lang: attachment.lang,
        insertMode : true
      });
      return $viewableContainer;
    };
    var domInit = function() {
      if (__context.options.parentContainer) {
        this.$rootContainer = document.createElement('div');
        this.$rootContainer.classList.add('attachments-as-content');
        __context.options.parentContainer.appendChild(this.$rootContainer);
      } else {
        this.$rootContainer = document.querySelector(__context.options.domSelector);
      }
      __initPromise.then(function(attachments) {
        attachments.forEach(function(attachment) {
          if (!attachment.displayAsContent) {
            return;
          }
          var $renderedContainer;
          if (__context.options.enableViewable && attachment.viewable) {
            $renderedContainer = __renderContainer(attachment, __renderViewable);
            $renderedContainer.classList.add('viewable');
          } else if (__context.options.enableAudio && attachment.contentType.startsWith('audio')) {
            $renderedContainer = __renderContainer(attachment, __renderMedia);
            $renderedContainer.classList.add('audio');
          } else if (__context.options.enableVideo && attachment.contentType.startsWith('video')) {
            $renderedContainer = __renderContainer(attachment, __renderMedia);
            $renderedContainer.classList.add('video');
          } else if (__context.options.enableImage && attachment.contentType.startsWith('image')) {
            $renderedContainer = __renderContainer(attachment, __renderImage);
            $renderedContainer.classList.add('image');
          } else if (__context.options.enableSimpleText && attachment.contentType.startsWith('text')) {
            $renderedContainer = __renderContainer(attachment, __renderSimpleText);
            $renderedContainer.classList.add('simple-text');
          } else {
            sp.log.debug("AttachmentsAsContentViewer - no renderer for " + JSON.stringify(attachment));
          }
          if ($renderedContainer) {
            this.$rootContainer.appendChild($renderedContainer);
          }
        }.bind(this));
      }.bind(this));
    };
    if (!__context.options.parentContainer) {
      whenSilverpeasReady(domInit);
    } else {
      domInit();
    }
  }
})(jQuery);
