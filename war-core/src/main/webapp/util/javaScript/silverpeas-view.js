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
 * Silverpeas plugin build upon JQuery to display a document view.
 * It uses the JQuery UI framework.
 */
(function( $ ){

  $.view = {
    webServiceContext : webContext + '/services',
    initialized: false,
    doInitialize : function() {
      if (! $.view.initialized) {
        $.i18n.properties({
          name: 'generalMultilang',
          path: webContext + '/services/bundles/org/silverpeas/multilang/',
          language: '$$', /* by default the language of the user in the current session */
          mode: 'map'
        });
        $.view.initialized = true;
      }
    }
  }

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

    $.view.doInitialize();
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
      if (options.versioned) {
    	  url += "/version/" + options.attachmentId;
      } else {
    	  url += "/attachment/" + options.attachmentId;
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
   * Private function that centralizes the dialog view construction
   */
  function __openDialogView($this, view) {

    // Initializing the resulting html container
    var $baseContainer = $("#documentView");
    if ($baseContainer.size() == 0) {
      $baseContainer = $("<div>")
                        .attr('id', 'documentView')
                        .css('display', 'block')
                        .css('border', '0px')
                        .css('padding', '0px')
                        .css('margin', '0px auto')
                        .css('text-align', 'center')
                        .css('background-color', 'white');
      $baseContainer.insertAfter($this);
    } else {
      $baseContainer.dialog("destroy");
    }

    // View content
    var index = 0;

    // Settings
    var settings = {
        title : view.pages[0].originalFileName,
        width : view.width + 50,
        height : view.height + 50,
        keydown : function (e) {
          var keyCode = eval(e.keyCode);
          if (37 <= keyCode && keyCode <= 40) {
            e.preventDefault();
            if (view.pages.length > 1) {
              if (39 == keyCode) {
                // Right
                index = __nextPage($baseContainer, view.pages, index, true);
              } else if (37 == keyCode) {
                // left
                index = __nextPage($baseContainer, view.pages, index, false);
              }
              $baseContainer.dialog("widget").focus();
            }
          }
        }
    };

    // Popup
    var $pageIndicator = __setImage($baseContainer, view.pages.length, 0, view.pages[0]);
    $baseContainer.popup('view', settings);
    __configurePageIndicatorPosition($baseContainer, $pageIndicator);
  }

  function __setImage($baseContainer, nbPages, index, page) {
    $viewContent = $('<div>')
                      .css('display', 'block')
                      .css('margin', '0px')
                      .css('padding', '0px')
                      .css('text-align', 'center');

    $viewContent.append($('<img>').attr('src', page.url)
                                  .attr('width', page.width)
                                  .attr('height', page.height));
    var $pageIndicator = __buildPageIndicator($baseContainer, nbPages, index)
    $viewContent.append($pageIndicator);
    $baseContainer.html($viewContent);
    return $pageIndicator;
  }

  function __nextPage($baseContainer, pages, curIndex, isNext) {
    $.popup.showWaiting();
    if (isNext) {
      curIndex++;
      if (curIndex >= pages.length ) {
        curIndex = 0;
      }
    } else {
      curIndex--;
      if (curIndex < 0) {
        curIndex = (pages.length - 1);
      }
    }

    $.ajax({
      url : pages[curIndex].uri,
      type : 'GET',
      dataType : 'json',
      cache : false,
      success : function(data, status, jqXHR) {
        var $pageIndicator = __setImage($baseContainer, pages.length, curIndex, data);
        __configurePageIndicatorPosition($baseContainer, $pageIndicator);
        $.popup.hideWaiting();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        $.popup.hideWaiting();
        alert(errorThrown);
      }
    });

    return curIndex;
  }

  /**
   * Private function that centralizes an page indicator construction
   */
  function __buildPageIndicator($baseContainer, nbPages, index) {

    // Initializing
    var $pageIndicatorContainer = $('<div>')
                            .addClass('preview-button')
                            .css('display', 'none')
                            .css('position', 'absolute')
                            .css('top', '0px')
                            .css('left', '0px')
                            .css('cursor', 'pointer');

    // This second call permits to load required images for a simple button
    var $pageIndicator = $("<span>");
    $pageIndicator.html((index + 1) + "/" + nbPages);
    $pageIndicatorContainer.html($pageIndicator);

    // Setting baseContainer event (print buttons)
    $baseContainer.mouseenter(function() {
      $pageIndicatorContainer.fadeIn(200);
    });
    $baseContainer.mouseleave(function() {
      $pageIndicatorContainer.fadeOut(400);
    });
    return $pageIndicatorContainer;
  }

  /**
   * Private function that centralizes the position configuration of a page indicator
   */
  function __configurePageIndicatorPosition($target, $pageIndicatorContainer) {

    // Top
    var top = 0;

    // Left
    var left = ($target.outerWidth(true) - ($pageIndicatorContainer.outerWidth(true)));

    // Changing the position
    $pageIndicatorContainer.offset({ top: 0, left: left });
  }
})( jQuery );
