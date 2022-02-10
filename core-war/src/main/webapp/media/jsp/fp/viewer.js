/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
(function($window){

  var FP_CONSTANTS = {
    // FlexPaper languages
    languages : {
      "en" : "en_US",
      "us" : "en_US",
      "fr" : "fr_FR",
      "zh" : "zh_CN",
      "cn" : "zh_CN",
      "es" : "es_ES",
      "br" : "pt_BR",
      "pt" : "pt_BR",
      "ru" : "ru_RU",
      "fi" : "fi_FN",
      "fn" : "fi_FN",
      "de" : "de_DE",
      "nl" : "nl_NL",
      "tr" : "tr_TR",
      "se" : "se_SE",
      "el" : "el_EL",
      "da" : "da_DN",
      "dn" : "da_DN",
      "cz" : "cz_CS",
      "cs" : "cz_CS",
      "it" : "it_IT",
      "pl" : "pl_PL",
      "pv" : "pv_FN"
    }
  };

  $window.renderViewer = function(options) {
    var documentUrl = decodeURIComponent(options.url);
    var swfUrl = documentUrl;
    var jsonUrl = '';
    if (options.documentSplit) {
      swfUrl = "{" + documentUrl.replace(/page[.]swf$/g, 'page-[*,0].swf') + "," + options.nbPages + "}";
      if (options.searchDataComputed) {
        jsonUrl = documentUrl + "_{page}.js";
      }
    }
    var __docViewApi = false;
    var $viewer = $('#viewer');
    $viewer.FlexPaperViewer({
      config : {
        flashDirectory : options.displayViewerPath,
        jsDirectory : (webContext + '/media/jsp/fp/core'),
        SwfFile : swfUrl,
        JSONFile : jsonUrl,
        key : options.displayLicenseKey,
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
        InitViewMode : (options.displayLicenseKey.length == 0 ? 'Portrait' : 'TwoPage' ),
        PrintPaperAsBitmap : false,

        ViewModeToolsVisible : true,
        ZoomToolsVisible : true,
        NavToolsVisible : true,
        CursorToolsVisible : true,
        SearchToolsVisible : (!options.documentSplit || options.searchDataComputed),

        BackgroundColor : '#222222',
        PanelColor : '#555555',
        EnableCornerDragging : true,

        WMode : 'transparent',
        localeChain : __getFlexPaperLanguage(options)
      }
    });
    loadFlexPaperHandlers();

    $viewer.bind('onDocumentLoaded', function(e,totalPages){
      if (!__docViewApi) {
        __docViewApi = getDocViewer('viewer');
      }
    });

    // Function to navigate between images
    jQuery(document).bind('keydown', function(e) {
      var keyCode = Number(e.keyCode);
      if (__docViewApi && 37 <= keyCode && keyCode <= 40) {
        e.preventDefault();
        if (39 === keyCode) {
          // Right
          __docViewApi.nextPage();
        } else if (37 === keyCode) {
          // Left
          __docViewApi.prevPage();
        }
        return false;
      }
      return true;
    });
  };

  /**
   * Loading handlers (dynamic load)
   */
  function loadFlexPaperHandlers() {
    jQuery.ajax({
      url : webContext + '/media/jsp/fp/core/flexpaper_handlers.js',
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
  function __getFlexPaperLanguage(options) {
    var language = FP_CONSTANTS.languages[options.language];
    if (language == null || language.length === 0) {
      language = FP_CONSTANTS.languages["fr"];
    }
    return language;
  }
})(window);
