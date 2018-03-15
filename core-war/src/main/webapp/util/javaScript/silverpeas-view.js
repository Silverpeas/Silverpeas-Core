/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
        alert("Bad component instance id or attachment id");
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
      alert("Silverpeas Popup JQuery Plugin is required.");
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
      $.popup.showWaiting();

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
          __openDialogView($_this, data);
          $.popup.hideWaiting();
        },
        error : function(jqXHR, textStatus, errorThrown) {
          $.popup.hideWaiting();
          alert(errorThrown);
        }
      });
    })
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
    __setView($baseContainer, view);

    // Player
    $('#documentViewer').embedPlayer({
      url : sp.ajaxRequest(view.viewerUri)
              .withParam('documentId', view.documentId)
              .withParam('language', view.language)
              .getUrl(),
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
  function __setView($baseContainer, view) {
    $baseContainer.html($('<div>')
        .attr('id','viewercontainer')
        .css('display', 'block')
        .css('margin', '0px')
        .css('padding', '0px')
        .css('width', view.width + 'px')
        .css('height', view.height + 'px')
        .css('text-align', 'center')
        .append($('<div>')
            .attr('id','documentViewer')
            .css('display', 'block')
            .css('margin', '0px')
            .css('padding', '0px')
            .css('width', view.width + 'px')
            .css('height', view.height + 'px')
            .css('background-color', '#222222')));
  }
})(jQuery);
