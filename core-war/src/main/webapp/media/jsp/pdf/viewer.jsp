<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="contentUrl" value="${requestScope.contentUrl}"/>
<c:set var="downloadEnabled" value="${requestScope.downloadEnabled}"/>
<c:set var="editionToolsEnabled" value="${requestScope.editionToolsEnabled}"/>
<c:set var="userLanguage" value="${requestScope.userLanguage}"/>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta name="google" content="notranslate">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <view:includePlugin name="minimalsilverpeas"/>
  <script type="module">
    //# sourceURL=/silverpeas/media/jsp/pdf/viewer.html.js
    const __spFluentBundleResolver = new function() {
      const titlePattern = 'flt(.title)';
      this.resolve = function(value) {
        return value.replace(titlePattern, '\n    .title = ');
      };
      this.remove = function(value) {
        return value.replace(titlePattern, '');
      };
    };
    window.SP_PDF_VIEWER_L10N_TEXT_MODIFIER = function(pdfjsFltText) {
      return new Promise(function(resolve) {
        let finalText = pdfjsFltText;
        PdfViewerBundle.keys().forEach(function(key) {
          const value = PdfViewerBundle.get(key);
          const resolvedValue = __spFluentBundleResolver.resolve(value);
          const isTitle = value !== resolvedValue;
          const dataToInsert = '\n' + key + ' =' + (isTitle ? '' : ' ') + resolvedValue;
          const index = finalText.indexOf(key);
          if (index < 0) {
            finalText += dataToInsert;
          } else {
            const firstEndKeyLineIndex = finalText.indexOf('\n', index);
            let suffixPartIndex = firstEndKeyLineIndex;
            if (isTitle) {
              suffixPartIndex = finalText.indexOf('\n', firstEndKeyLineIndex + 1);
            }
            finalText = finalText.substr(0, index - 1) + dataToInsert + finalText.substr(suffixPartIndex);
          }
        });
        resolve(finalText);
      });
    };
    window.SP_PDF_VIEWER_DEFERRED = sp.promise.deferred();
    parent.document.addEventListener('webviewerloaded', function(e) {
      ['PDFViewerApplication', 'PDFViewerApplicationOptions'].forEach(function(varName) {
        if (window[varName]) {
          delete window[varName];
        } else {
          sp.log.warning("PDFJS variables have been changed")
        }
      });
      const viewerApp = e.detail.viewerApp;
      const viewerAppOptions = e.detail.viewerAppOptions;
      viewerAppOptions.set("cMapPacked", true);
      viewerAppOptions.set("imageResourcesPath", PdfViewerSettings.get('p.i.p'));
      viewerAppOptions.set("workerSrc", PdfViewerSettings.get('p.w.s'));
      viewerAppOptions.set("sandboxBundleSrc", PdfViewerSettings.get('p.s.b.s'));
      viewerAppOptions.set("cMapUrl", PdfViewerSettings.get('p.c.p'));
      viewerAppOptions.set("defaultUrl", '${silfn:escapeJs(contentUrl)}');
      viewerAppOptions.set("locale", '${userLanguage}');
      viewerAppOptions.set("disablePreferences", true);
      viewerAppOptions.set("disableHistory", true);
      viewerAppOptions.set("textLayerMode", ${downloadEnabled} ? 1 : 0);
      viewerAppOptions.set("verbosity", 0);
      viewerAppOptions.set("sidebarViewOnLoad", 0);
      const viewerConfig = e.detail.viewerConfiguration;
      <c:if test="${not downloadEnabled}">
      viewerConfig.toolbar.download.remove();
      viewerConfig.secondaryToolbar.downloadButton.remove();
      </c:if>
      <c:if test="${not downloadEnabled or not editionToolsEnabled}">
      for (let name in viewerConfig.toolbar) {
        if (name.startsWith('editor')) {
          viewerConfig.toolbar[name].remove();
        }
      }
      </c:if>
      // the version of viewer does not expose anymore button on primary toolbar 'viewerConfig.toolbar'
      // for opening and bookmarking pdf
      viewerConfig.secondaryToolbar.openFileButton.remove();
      viewerConfig.secondaryToolbar.viewBookmarkButton.remove();
      viewerConfig.secondaryToolbar.documentPropertiesButton.remove();
      const fullscreenMode = function () {
        if (spFscreen.fullscreenElement()) {
          spFscreen.exitFullscreen();
        } else {
          spFscreen.requestFullscreen(viewerConfig.appContainer);
          viewerConfig.secondaryToolbar.toggleButton.click();
        }
      };
      spFscreen.addEventListener('fullscreenchange', function() {
        const l10nIdAttrName = 'data-l10n-id';
        sp.element.querySelectorAll("[" + l10nIdAttrName + "^='pdfjs-presentation-mode-button']").forEach(function(el) {
          const isFullscreen = spFscreen.fullscreenElement();
          const currentKey = el.getAttribute(l10nIdAttrName);
          const isLeaveIdPart = currentKey.indexOf('leave') > 0;
          if (isFullscreen && !isLeaveIdPart) {
            el.setAttribute(l10nIdAttrName, currentKey.replace('-button', '-button-leave'));
            viewerApp.l10n.translate(el);
          } else if (!isFullscreen && isLeaveIdPart) {
            el.setAttribute(l10nIdAttrName, currentKey.replace('-button-leave', '-button'));
            viewerApp.l10n.translate(el);
          }
        })
      });
      window.SP_PDF_VIEWER_DEFERRED.promise.then(function() {
        [viewerConfig.secondaryToolbar.presentationModeButton]
            .forEach(function(fullscreenModeButton) {
              fullscreenModeButton = sp.element.removeAllEventListenerOfAndGettingClone(fullscreenModeButton);
              fullscreenModeButton.addEventListener('click', fullscreenMode);
            });
      });
      <c:if test="${not downloadEnabled}">
      viewerConfig.toolbar.print.remove();
      viewerConfig.secondaryToolbar.printButton.remove();
      viewerConfig.secondaryToolbar.cursorSelectToolButton.remove();
      window.SP_PDF_VIEWER_DEFERRED.promise.then(function() {
        viewerConfig.secondaryToolbar.cursorHandToolButton.click();
      });
      </c:if>
    });
  </script>
  <view:includePlugin name="pdfviewer"/>
</head>
<body tabindex="1">
<div id="outerContainer">

  <div id="sidebarContainer">
    <div id="toolbarSidebar">
      <div id="toolbarSidebarLeft">
        <div id="sidebarViewButtons" class="splitToolbarButton toggled" role="radiogroup">
          <button id="viewThumbnail" class="toolbarButton toggled" title="Show Thumbnails" tabindex="2" data-l10n-id="pdfjs-thumbs-button" role="radio" aria-checked="true" aria-controls="thumbnailView">
            <span data-l10n-id="pdfjs-thumbs-button-label">Thumbnails</span>
          </button>
          <button id="viewOutline" class="toolbarButton" title="Show Document Outline (double-click to expand/collapse all items)" tabindex="3" data-l10n-id="pdfjs-document-outline-button" role="radio" aria-checked="false" aria-controls="outlineView">
            <span data-l10n-id="pdfjs-document-outline-button-label">Document Outline</span>
          </button>
          <button id="viewAttachments" class="toolbarButton" title="Show Attachments" tabindex="4" data-l10n-id="pdfjs-attachments-button" role="radio" aria-checked="false" aria-controls="attachmentsView">
            <span data-l10n-id="pdfjs-attachments-button-label">Attachments</span>
          </button>
          <button id="viewLayers" class="toolbarButton" title="Show Layers (double-click to reset all layers to the default state)" tabindex="5" data-l10n-id="pdfjs-layers-button" role="radio" aria-checked="false" aria-controls="layersView">
            <span data-l10n-id="pdfjs-layers-button-label">Layers</span>
          </button>
        </div>
      </div>

      <div id="toolbarSidebarRight">
        <div id="outlineOptionsContainer">
          <div class="verticalToolbarSeparator"></div>

          <button id="currentOutlineItem" class="toolbarButton" disabled="disabled" title="Find Current Outline Item" tabindex="6" data-l10n-id="pdfjs-current-outline-item-button">
            <span data-l10n-id="pdfjs-current-outline-item-button-label">Current Outline Item</span>
          </button>
        </div>
      </div>
    </div>
    <div id="sidebarContent">
      <div id="thumbnailView">
      </div>
      <div id="outlineView" class="hidden">
      </div>
      <div id="attachmentsView" class="hidden">
      </div>
      <div id="layersView" class="hidden">
      </div>
    </div>
    <div id="sidebarResizer"></div>
  </div>  <!-- sidebarContainer -->

  <div id="mainContainer">
    <div class="findbar hidden doorHanger" id="findbar">
      <div id="findbarInputContainer">
            <span class="loadingInput end">
              <input id="findInput" class="toolbarField" title="Find" placeholder="Find in document…" tabindex="91" data-l10n-id="pdfjs-find-input" aria-invalid="false">
            </span>
        <div class="splitToolbarButton">
          <button id="findPrevious" class="toolbarButton" title="Find the previous occurrence of the phrase" tabindex="92" data-l10n-id="pdfjs-find-previous-button">
            <span data-l10n-id="pdfjs-find-previous-button-label">Previous</span>
          </button>
          <div class="splitToolbarButtonSeparator"></div>
          <button id="findNext" class="toolbarButton" title="Find the next occurrence of the phrase" tabindex="93" data-l10n-id="pdfjs-find-next-button">
            <span data-l10n-id="pdfjs-find-next-button-label">Next</span>
          </button>
        </div>
      </div>

      <div id="findbarOptionsOneContainer">
        <input type="checkbox" id="findHighlightAll" class="toolbarField" tabindex="94">
        <label for="findHighlightAll" class="toolbarLabel" data-l10n-id="pdfjs-find-highlight-checkbox">Highlight All</label>
        <input type="checkbox" id="findMatchCase" class="toolbarField" tabindex="95">
        <label for="findMatchCase" class="toolbarLabel" data-l10n-id="pdfjs-find-match-case-checkbox-label">Match Case</label>
      </div>
      <div id="findbarOptionsTwoContainer">
        <input type="checkbox" id="findMatchDiacritics" class="toolbarField" tabindex="96">
        <label for="findMatchDiacritics" class="toolbarLabel" data-l10n-id="pdfjs-find-match-diacritics-checkbox-label">Match Diacritics</label>
        <input type="checkbox" id="findEntireWord" class="toolbarField" tabindex="97">
        <label for="findEntireWord" class="toolbarLabel" data-l10n-id="pdfjs-find-entire-word-checkbox-label">Whole Words</label>
      </div>

      <div id="findbarMessageContainer" aria-live="polite">
        <span id="findResultsCount" class="toolbarLabel"></span>
        <span id="findMsg" class="toolbarLabel"></span>
      </div>
    </div>  <!-- findbar -->

    <div class="editorParamsToolbar hidden doorHangerRight" id="editorHighlightParamsToolbar">
      <div id="highlightParamsToolbarContainer" class="editorParamsToolbarContainer">
        <div id="editorHighlightColorPicker" class="colorPicker">
          <span id="highlightColorPickerLabel" class="editorParamsLabel" data-l10n-id="pdfjs-editor-highlight-colorpicker-label">Highlight color</span>
        </div>
      </div>
    </div>

    <div class="editorParamsToolbar hidden doorHangerRight" id="editorFreeTextParamsToolbar">
      <div class="editorParamsToolbarContainer">
        <div class="editorParamsSetter">
          <label for="editorFreeTextColor" class="editorParamsLabel" data-l10n-id="pdfjs-editor-free-text-color-input">Color</label>
          <input type="color" id="editorFreeTextColor" class="editorParamsColor" tabindex="102">
        </div>
        <div class="editorParamsSetter">
          <label for="editorFreeTextFontSize" class="editorParamsLabel" data-l10n-id="pdfjs-editor-free-text-size-input">Size</label>
          <input type="range" id="editorFreeTextFontSize" class="editorParamsSlider" value="10" min="5" max="100" step="1" tabindex="103">
        </div>
      </div>
    </div>

    <div class="editorParamsToolbar hidden doorHangerRight" id="editorInkParamsToolbar">
      <div class="editorParamsToolbarContainer">
        <div class="editorParamsSetter">
          <label for="editorInkColor" class="editorParamsLabel" data-l10n-id="pdfjs-editor-ink-color-input">Color</label>
          <input type="color" id="editorInkColor" class="editorParamsColor" tabindex="104">
        </div>
        <div class="editorParamsSetter">
          <label for="editorInkThickness" class="editorParamsLabel" data-l10n-id="pdfjs-editor-ink-thickness-input">Thickness</label>
          <input type="range" id="editorInkThickness" class="editorParamsSlider" value="1" min="1" max="20" step="1" tabindex="105">
        </div>
        <div class="editorParamsSetter">
          <label for="editorInkOpacity" class="editorParamsLabel" data-l10n-id="pdfjs-editor-ink-opacity-input">Opacity</label>
          <input type="range" id="editorInkOpacity" class="editorParamsSlider" value="100" min="1" max="100" step="1" tabindex="106">
        </div>
      </div>
    </div>

    <div class="editorParamsToolbar hidden doorHangerRight" id="editorStampParamsToolbar">
      <div class="editorParamsToolbarContainer">
        <button id="editorStampAddImage" class="secondaryToolbarButton" title="Add image" tabindex="107" data-l10n-id="pdfjs-editor-stamp-add-image-button">
          <span class="editorParamsLabel" data-l10n-id="pdfjs-editor-stamp-add-image-button-label">Add image</span>
        </button>
      </div>
    </div>

    <div id="secondaryToolbar" class="secondaryToolbar hidden doorHangerRight">
      <div id="secondaryToolbarButtonContainer">
        <button id="secondaryOpenFile" class="secondaryToolbarButton" title="Open File" tabindex="51" data-l10n-id="pdfjs-open-file-button">
          <span data-l10n-id="pdfjs-open-file-button-label">Open</span>
        </button>

        <button id="secondaryPrint" class="secondaryToolbarButton visibleMediumView" title="Print" tabindex="52" data-l10n-id="pdfjs-print-button">
          <span data-l10n-id="pdfjs-print-button-label">Print</span>
        </button>

        <button id="secondaryDownload" class="secondaryToolbarButton visibleMediumView" title="Save" tabindex="53" data-l10n-id="pdfjs-save-button">
          <span data-l10n-id="pdfjs-save-button-label">Save</span>
        </button>

        <div class="horizontalToolbarSeparator"></div>

        <button id="presentationMode" class="secondaryToolbarButton" title="Switch to Presentation Mode" tabindex="54" data-l10n-id="pdfjs-presentation-mode-button">
          <span data-l10n-id="pdfjs-presentation-mode-button-label">Presentation Mode</span>
        </button>

        <a href="#" id="viewBookmark" class="secondaryToolbarButton" title="Current Page (View URL from Current Page)" tabindex="55" data-l10n-id="pdfjs-bookmark-button">
          <span data-l10n-id="pdfjs-bookmark-button-label">Current Page</span>
        </a>

        <div id="viewBookmarkSeparator" class="horizontalToolbarSeparator"></div>

        <button id="firstPage" class="secondaryToolbarButton" title="Go to First Page" tabindex="56" data-l10n-id="pdfjs-first-page-button">
          <span data-l10n-id="pdfjs-first-page-button-label">Go to First Page</span>
        </button>
        <button id="lastPage" class="secondaryToolbarButton" title="Go to Last Page" tabindex="57" data-l10n-id="pdfjs-last-page-button">
          <span data-l10n-id="pdfjs-last-page-button-label">Go to Last Page</span>
        </button>

        <div class="horizontalToolbarSeparator"></div>

        <button id="pageRotateCw" class="secondaryToolbarButton" title="Rotate Clockwise" tabindex="58" data-l10n-id="pdfjs-page-rotate-cw-button">
          <span data-l10n-id="pdfjs-page-rotate-cw-button-label">Rotate Clockwise</span>
        </button>
        <button id="pageRotateCcw" class="secondaryToolbarButton" title="Rotate Counterclockwise" tabindex="59" data-l10n-id="pdfjs-page-rotate-ccw-button">
          <span data-l10n-id="pdfjs-page-rotate-ccw-button-label">Rotate Counterclockwise</span>
        </button>

        <div class="horizontalToolbarSeparator"></div>

        <div id="cursorToolButtons" role="radiogroup">
          <button id="cursorSelectTool" class="secondaryToolbarButton toggled" title="Enable Text Selection Tool" tabindex="60" data-l10n-id="pdfjs-cursor-text-select-tool-button" role="radio" aria-checked="true">
            <span data-l10n-id="pdfjs-cursor-text-select-tool-button-label">Text Selection Tool</span>
          </button>
          <button id="cursorHandTool" class="secondaryToolbarButton" title="Enable Hand Tool" tabindex="61" data-l10n-id="pdfjs-cursor-hand-tool-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-cursor-hand-tool-button-label">Hand Tool</span>
          </button>
        </div>

        <div class="horizontalToolbarSeparator"></div>

        <div id="scrollModeButtons" role="radiogroup">
          <button id="scrollPage" class="secondaryToolbarButton" title="Use Page Scrolling" tabindex="62" data-l10n-id="pdfjs-scroll-page-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-scroll-page-button-label">Page Scrolling</span>
          </button>
          <button id="scrollVertical" class="secondaryToolbarButton toggled" title="Use Vertical Scrolling" tabindex="63" data-l10n-id="pdfjs-scroll-vertical-button" role="radio" aria-checked="true">
            <span data-l10n-id="pdfjs-scroll-vertical-button-label" >Vertical Scrolling</span>
          </button>
          <button id="scrollHorizontal" class="secondaryToolbarButton" title="Use Horizontal Scrolling" tabindex="64" data-l10n-id="pdfjs-scroll-horizontal-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-scroll-horizontal-button-label">Horizontal Scrolling</span>
          </button>
          <button id="scrollWrapped" class="secondaryToolbarButton" title="Use Wrapped Scrolling" tabindex="65" data-l10n-id="pdfjs-scroll-wrapped-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-scroll-wrapped-button-label">Wrapped Scrolling</span>
          </button>
        </div>

        <div class="horizontalToolbarSeparator"></div>

        <div id="spreadModeButtons" role="radiogroup">
          <button id="spreadNone" class="secondaryToolbarButton toggled" title="Do not join page spreads" tabindex="66" data-l10n-id="pdfjs-spread-none-button" role="radio" aria-checked="true">
            <span data-l10n-id="pdfjs-spread-none-button-label">No Spreads</span>
          </button>
          <button id="spreadOdd" class="secondaryToolbarButton" title="Join page spreads starting with odd-numbered pages" tabindex="67" data-l10n-id="pdfjs-spread-odd-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-spread-odd-button-label">Odd Spreads</span>
          </button>
          <button id="spreadEven" class="secondaryToolbarButton" title="Join page spreads starting with even-numbered pages" tabindex="68" data-l10n-id="pdfjs-spread-even-button" role="radio" aria-checked="false">
            <span data-l10n-id="pdfjs-spread-even-button-label">Even Spreads</span>
          </button>
        </div>

        <div class="horizontalToolbarSeparator"></div>

        <button id="documentProperties" class="secondaryToolbarButton" title="Document Properties…" tabindex="69" data-l10n-id="pdfjs-document-properties-button" aria-controls="documentPropertiesDialog">
          <span data-l10n-id="pdfjs-document-properties-button-label">Document Properties…</span>
        </button>
      </div>
    </div>  <!-- secondaryToolbar -->

    <div class="toolbar">
      <div id="toolbarContainer">
        <div id="toolbarViewer">
          <div id="toolbarViewerLeft">
            <button id="sidebarToggle" class="toolbarButton" title="Toggle Sidebar" tabindex="11" data-l10n-id="pdfjs-toggle-sidebar-button" aria-expanded="false" aria-controls="sidebarContainer">
              <span data-l10n-id="pdfjs-toggle-sidebar-button-label">Toggle Sidebar</span>
            </button>
            <div class="toolbarButtonSpacer"></div>
            <button id="viewFind" class="toolbarButton" title="Find in Document" tabindex="12" data-l10n-id="pdfjs-findbar-button" aria-expanded="false" aria-controls="findbar">
              <span data-l10n-id="pdfjs-findbar-button-label">Find</span>
            </button>
            <div class="splitToolbarButton hiddenSmallView">
              <button class="toolbarButton" title="Previous Page" id="previous" tabindex="13" data-l10n-id="pdfjs-previous-button">
                <span data-l10n-id="pdfjs-previous-button-label">Previous</span>
              </button>
              <div class="splitToolbarButtonSeparator"></div>
              <button class="toolbarButton" title="Next Page" id="next" tabindex="14" data-l10n-id="pdfjs-next-button">
                <span data-l10n-id="pdfjs-next-button-label">Next</span>
              </button>
            </div>
            <span class="loadingInput start">
                  <input type="number" id="pageNumber" class="toolbarField" title="Page" value="1" min="1" tabindex="15" data-l10n-id="pdfjs-page-input" autocomplete="off">
                </span>
            <span id="numPages" class="toolbarLabel"></span>
          </div>
          <div id="toolbarViewerRight">
            <div id="editorModeButtons" class="splitToolbarButton toggled" role="radiogroup">
              <button id="editorHighlight" class="toolbarButton" hidden="true" disabled="disabled" title="Highlight" role="radio" aria-checked="false" aria-controls="editorHighlightParamsToolbar" tabindex="31" data-l10n-id="pdfjs-editor-highlight-button">
                <span data-l10n-id="pdfjs-editor-highlight-button-label">Highlight</span>
              </button>
              <button id="editorFreeText" class="toolbarButton" disabled="disabled" title="Text" role="radio" aria-checked="false" aria-controls="editorFreeTextParamsToolbar" tabindex="32" data-l10n-id="pdfjs-editor-free-text-button">
                <span data-l10n-id="pdfjs-editor-free-text-button-label">Text</span>
              </button>
              <button id="editorInk" class="toolbarButton" disabled="disabled" title="Draw" role="radio" aria-checked="false" aria-controls="editorInkParamsToolbar" tabindex="33" data-l10n-id="pdfjs-editor-ink-button">
                <span data-l10n-id="pdfjs-editor-ink-button-label">Draw</span>
              </button>
              <button id="editorStamp" class="toolbarButton" disabled="disabled" title="Add or edit images" role="radio" aria-checked="false" aria-controls="editorStampParamsToolbar" tabindex="34" data-l10n-id="pdfjs-editor-stamp-button">
                <span data-l10n-id="pdfjs-editor-stamp-button-label">Add or edit images</span>
              </button>
            </div>

            <div id="editorModeSeparator" class="verticalToolbarSeparator"></div>

            <button id="print" class="toolbarButton hiddenMediumView" title="Print" tabindex="41" data-l10n-id="pdfjs-print-button">
              <span data-l10n-id="pdfjs-print-button-label">Print</span>
            </button>

            <button id="download" class="toolbarButton hiddenMediumView" title="Save" tabindex="42" data-l10n-id="pdfjs-save-button">
              <span data-l10n-id="pdfjs-save-button-label">Save</span>
            </button>

            <div class="verticalToolbarSeparator hiddenMediumView"></div>

            <button id="secondaryToolbarToggle" class="toolbarButton" title="Tools" tabindex="43" data-l10n-id="pdfjs-tools-button" aria-expanded="false" aria-controls="secondaryToolbar">
              <span data-l10n-id="pdfjs-tools-button-label">Tools</span>
            </button>
          </div>
          <div id="toolbarViewerMiddle">
            <div class="splitToolbarButton">
              <button id="zoomOut" class="toolbarButton" title="Zoom Out" tabindex="21" data-l10n-id="pdfjs-zoom-out-button">
                <span data-l10n-id="pdfjs-zoom-out-button-label">Zoom Out</span>
              </button>
              <div class="splitToolbarButtonSeparator"></div>
              <button id="zoomIn" class="toolbarButton" title="Zoom In" tabindex="22" data-l10n-id="pdfjs-zoom-in-button">
                <span data-l10n-id="pdfjs-zoom-in-button-label">Zoom In</span>
              </button>
            </div>
            <span id="scaleSelectContainer" class="dropdownToolbarButton">
                  <select id="scaleSelect" title="Zoom" tabindex="23" data-l10n-id="pdfjs-zoom-select">
                    <option id="pageAutoOption" title="" value="auto" selected="selected" data-l10n-id="pdfjs-page-scale-auto">Automatic Zoom</option>
                    <option id="pageActualOption" title="" value="page-actual" data-l10n-id="pdfjs-page-scale-actual">Actual Size</option>
                    <option id="pageFitOption" title="" value="page-fit" data-l10n-id="pdfjs-page-scale-fit">Page Fit</option>
                    <option id="pageWidthOption" title="" value="page-width" data-l10n-id="pdfjs-page-scale-width">Page Width</option>
                    <option id="customScaleOption" title="" value="custom" disabled="disabled" hidden="true" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 0 }'>0%</option>
                    <option title="" value="0.5" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 50 }'>50%</option>
                    <option title="" value="0.75" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 75 }'>75%</option>
                    <option title="" value="1" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 100 }'>100%</option>
                    <option title="" value="1.25" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 125 }'>125%</option>
                    <option title="" value="1.5" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 150 }'>150%</option>
                    <option title="" value="2" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 200 }'>200%</option>
                    <option title="" value="3" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 300 }'>300%</option>
                    <option title="" value="4" data-l10n-id="pdfjs-page-scale-percent" data-l10n-args='{ "scale": 400 }'>400%</option>
                  </select>
                </span>
          </div>
        </div>
        <div id="loadingBar">
          <div class="progress">
            <div class="glimmer">
            </div>
          </div>
        </div>
      </div>
    </div>

    <div id="viewerContainer" tabindex="0">
      <div id="viewer" class="pdfViewer"></div>
    </div>
  </div> <!-- mainContainer -->

  <div id="dialogContainer">
    <dialog id="passwordDialog">
      <div class="row">
        <label for="password" id="passwordText" data-l10n-id="pdfjs-password-label">Enter the password to open this PDF file:</label>
      </div>
      <div class="row">
        <input type="password" id="password" class="toolbarField">
      </div>
      <div class="buttonRow">
        <button id="passwordCancel" class="dialogButton"><span data-l10n-id="pdfjs-password-cancel-button">Cancel</span></button>
        <button id="passwordSubmit" class="dialogButton"><span data-l10n-id="pdfjs-password-ok-button">OK</span></button>
      </div>
    </dialog>
    <dialog id="documentPropertiesDialog">
      <div class="row">
        <span id="fileNameLabel" data-l10n-id="pdfjs-document-properties-file-name">File name:</span>
        <p id="fileNameField" aria-labelledby="fileNameLabel">-</p>
      </div>
      <div class="row">
        <span id="fileSizeLabel" data-l10n-id="pdfjs-document-properties-file-size">File size:</span>
        <p id="fileSizeField" aria-labelledby="fileSizeLabel">-</p>
      </div>
      <div class="separator"></div>
      <div class="row">
        <span id="titleLabel" data-l10n-id="pdfjs-document-properties-title">Title:</span>
        <p id="titleField" aria-labelledby="titleLabel">-</p>
      </div>
      <div class="row">
        <span id="authorLabel" data-l10n-id="pdfjs-document-properties-author">Author:</span>
        <p id="authorField" aria-labelledby="authorLabel">-</p>
      </div>
      <div class="row">
        <span id="subjectLabel" data-l10n-id="pdfjs-document-properties-subject">Subject:</span>
        <p id="subjectField" aria-labelledby="subjectLabel">-</p>
      </div>
      <div class="row">
        <span id="keywordsLabel" data-l10n-id="pdfjs-document-properties-keywords">Keywords:</span>
        <p id="keywordsField" aria-labelledby="keywordsLabel">-</p>
      </div>
      <div class="row">
        <span id="creationDateLabel" data-l10n-id="pdfjs-document-properties-creation-date">Creation Date:</span>
        <p id="creationDateField" aria-labelledby="creationDateLabel">-</p>
      </div>
      <div class="row">
        <span id="modificationDateLabel" data-l10n-id="pdfjs-document-properties-modification-date">Modification Date:</span>
        <p id="modificationDateField" aria-labelledby="modificationDateLabel">-</p>
      </div>
      <div class="row">
        <span id="creatorLabel" data-l10n-id="pdfjs-document-properties-creator">Creator:</span>
        <p id="creatorField" aria-labelledby="creatorLabel">-</p>
      </div>
      <div class="separator"></div>
      <div class="row">
        <span id="producerLabel" data-l10n-id="pdfjs-document-properties-producer">PDF Producer:</span>
        <p id="producerField" aria-labelledby="producerLabel">-</p>
      </div>
      <div class="row">
        <span id="versionLabel" data-l10n-id="pdfjs-document-properties-version">PDF Version:</span>
        <p id="versionField" aria-labelledby="versionLabel">-</p>
      </div>
      <div class="row">
        <span id="pageCountLabel" data-l10n-id="pdfjs-document-properties-page-count">Page Count:</span>
        <p id="pageCountField" aria-labelledby="pageCountLabel">-</p>
      </div>
      <div class="row">
        <span id="pageSizeLabel" data-l10n-id="pdfjs-document-properties-page-size">Page Size:</span>
        <p id="pageSizeField" aria-labelledby="pageSizeLabel">-</p>
      </div>
      <div class="separator"></div>
      <div class="row">
        <span id="linearizedLabel" data-l10n-id="pdfjs-document-properties-linearized">Fast Web View:</span>
        <p id="linearizedField" aria-labelledby="linearizedLabel">-</p>
      </div>
      <div class="buttonRow">
        <button id="documentPropertiesClose" class="dialogButton"><span data-l10n-id="pdfjs-document-properties-close-button">Close</span></button>
      </div>
    </dialog>
    <dialog id="altTextDialog" aria-labelledby="dialogLabel" aria-describedby="dialogDescription">
      <div id="altTextContainer">
        <div id="overallDescription">
          <span id="dialogLabel" data-l10n-id="pdfjs-editor-alt-text-dialog-label" class="title">Choose an option</span>
          <span id="dialogDescription" data-l10n-id="pdfjs-editor-alt-text-dialog-description">
                Alt text (alternative text) helps when people can’t see the image or when it doesn’t load.
              </span>
        </div>
        <div id="addDescription">
          <div class="radio">
            <div class="radioButton">
              <input type="radio" id="descriptionButton" name="altTextOption" tabindex="0" aria-describedby="descriptionAreaLabel" checked>
              <label for="descriptionButton" data-l10n-id="pdfjs-editor-alt-text-add-description-label">Add a description</label>
            </div>
            <div class="radioLabel">
                  <span id="descriptionAreaLabel" data-l10n-id="pdfjs-editor-alt-text-add-description-description">
                    Aim for 1-2 sentences that describe the subject, setting, or actions.
                  </span>
            </div>
          </div>
          <div class="descriptionArea">
            <textarea id="descriptionTextarea" placeholder="For example, “A young man sits down at a table to eat a meal”" aria-labelledby="descriptionAreaLabel" data-l10n-id="pdfjs-editor-alt-text-textarea" tabindex="0"></textarea>
          </div>
        </div>
        <div id="markAsDecorative">
          <div class="radio">
            <div class="radioButton">
              <input type="radio" id="decorativeButton" name="altTextOption" aria-describedby="decorativeLabel">
              <label for="decorativeButton" data-l10n-id="pdfjs-editor-alt-text-mark-decorative-label">Mark as decorative</label>
            </div>
            <div class="radioLabel">
                  <span id="decorativeLabel" data-l10n-id="pdfjs-editor-alt-text-mark-decorative-description">
                    This is used for ornamental images, like borders or watermarks.
                  </span>
            </div>
          </div>
        </div>
        <div id="buttons">
          <button id="altTextCancel" tabindex="0"><span data-l10n-id="pdfjs-editor-alt-text-cancel-button">Cancel</span></button>
          <button id="altTextSave" tabindex="0"><span data-l10n-id="pdfjs-editor-alt-text-save-button">Save</span></button>
        </div>
      </div>
    </dialog>
    <dialog id="printServiceDialog" style="min-width: 200px;">
      <div class="row">
        <span data-l10n-id="pdfjs-print-progress-message">Preparing document for printing…</span>
      </div>
      <div class="row">
        <progress value="0" max="100"></progress>
        <span data-l10n-id="pdfjs-print-progress-percent" data-l10n-args='{ "progress": 0 }' class="relative-progress">0%</span>
      </div>
      <div class="buttonRow">
        <button id="printCancel" class="dialogButton"><span data-l10n-id="pdfjs-print-progress-close-button">Cancel</span></button>
      </div>
    </dialog>
  </div>  <!-- dialogContainer -->

</div> <!-- outerContainer -->
<div id="printContainer"></div>

<input type="file" id="fileInput" class="hidden">
</body>
</html>
