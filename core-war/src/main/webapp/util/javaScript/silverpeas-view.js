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
 * Silverpeas plugin build upon JQuery to display a document view.
 * It uses the JQuery UI framework.
 */
(function( $ ){

  $.view = {
    webServiceContext : webContext + '/services',
    initialized: false,
    languages : null,
    doInitialize : function() {
      if (! $.view.initialized) {
        $.view.initialized = true;
        // FlexPaper languages
        $.view.languages = [];
        $.view.languages["en"] = "en_US";
        $.view.languages["us"] = "en_US";
        $.view.languages["fr"] = "fr_FR";
        $.view.languages["zh"] = "zh_CN";
        $.view.languages["cn"] = "zh_CN";
        $.view.languages["es"] = "es_ES";
        $.view.languages["br"] = "pt_BR";
        $.view.languages["pt"] = "pt_BR";
        $.view.languages["ru"] = "ru_RU";
        $.view.languages["fi"] = "fi_FN";
        $.view.languages["fn"] = "fi_FN";
        $.view.languages["de"] = "de_DE";
        $.view.languages["nl"] = "nl_NL";
        $.view.languages["tr"] = "tr_TR";
        $.view.languages["se"] = "se_SE";
        $.view.languages["pt"] = "pt_PT";
        $.view.languages["el"] = "el_EL";
        $.view.languages["da"] = "da_DN";
        $.view.languages["dn"] = "da_DN";
        $.view.languages["cz"] = "cz_CS";
        $.view.languages["cs"] = "cz_CS";
        $.view.languages["it"] = "it_IT";
        $.view.languages["pl"] = "pl_PL";
        $.view.languages["pv"] = "pv_FN";
      }
    }
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
    __adjustViewSize(view);

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
    }

    // Settings
    var settings = {
        title : view.originalFileName,
        width : view.width,
        height : view.height
    };

    // Popup
    __setView($baseContainer, view);
    __configureFlexPaper(view);
    $baseContainer.popup('view', settings);
  }

  /**
   * Private function that adjust size of view (size limitations)
   */
  function __adjustViewSize(view) {

    // Screen size
    var offsetWidth = view.displayLicenseKey.length == 0 ? 1 : (view.width < view.height ? 2 : 1.75);
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
    if (view.displayLicenseKey.length == 0)  {
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

  /**
   * Private function that configures FlexPaper plugin.
   */
  function __configureFlexPaper(view) {
    var viewUrl = decodeURIComponent(view.url);
    var swfUrl = viewUrl;
    var jsonUrl = '';
    if (view.documentSplit) {
      swfUrl = "{" + viewUrl.replace(/page[.]swf$/g, 'page-[*,0].swf') + "," + view.nbPages + "}";
      if (view.searchDataComputed) {
        jsonUrl = viewUrl + "_{page}.js";
      }
    }
    $('#documentViewer').FlexPaperViewer({
      config : {
        flashDirectory : view.displayViewerPath,
        jsDirectory : (webContext + '/util/javaScript/flexpaper'),
        SwfFile : swfUrl,
        JSONFile : jsonUrl,
        key : view.displayLicenseKey,
        Scale : 0.6,
        ZoomTransition : 'easeOut',
        ZoomTime : 0.5,
        ZoomInterval : 0.2,
        FitPageOnLoad : true,
        FitWidthOnLoad : false,
        FullScreenAsMaxWindow : false,
        ProgressiveLoading : true,
        MinZoomSize : 0.2,
        MaxZoomSize : 5,
        SearchMatchAll : false,
        InitViewMode : (view.displayLicenseKey.length == 0 ? 'Portrait' : 'TwoPage' ),
        PrintPaperAsBitmap : false,

        ViewModeToolsVisible : true,
        ZoomToolsVisible : true,
        NavToolsVisible : true,
        CursorToolsVisible : true,
        SearchToolsVisible : (!view.documentSplit || view.searchDataComputed),

        BackgroundColor : '#222222',
        PanelColor : '#555555',
        EnableCornerDragging : true,

        WMode : 'transparent',
        localeChain : __getFlexPaperLanguage(view)
      }
    });
    loadFlexPaperHandlers();
  }

  /**
   * Loading handlers (dynamic load)
   */
  function loadFlexPaperHandlers() {
    $.ajax({
      url : webContext + '/util/javaScript/flexpaper/flexpaper_handlers.js',
      dataType : "script",
      cache : true
    });
  }

  /**
   * Private function that returns the FlexPaper locale chain.
   * FlexPaper knows following languages :
   * en_US (English)
   * fr_FR (French)
   * zh_CN (Chinese, Simple)
   * es_ES (Spanish)
   * pt_BR (Brazilian Portugese)
   * ru_RU (Russian)
   * fi_FN (Finnish)
   * de_DE (German)
   * nl_NL (Netherlands)
   * tr_TR (Turkish)
   * se_SE (Swedish)
   * pt_PT (Portugese)
   * el_EL (Greek)
   * da_DN (Danish)
   * cz_CS (Czech)
   * it_IT (Italian)
   * pl_PL (Polish)
   * pv_FN (Finnish)
   * hu_HU (Hungarian)
   */
  function __getFlexPaperLanguage(view) {
    var language = $.view.languages[view.language];
    if (language == null || language.length == 0) {
      language = $.view.languages["en"];
    }
    return language;
  }
})( jQuery );
