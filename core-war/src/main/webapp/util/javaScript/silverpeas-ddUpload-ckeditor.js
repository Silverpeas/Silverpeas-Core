/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

(function() {

  const TEN_SECONDS_IN_MILLISECONDS = 10 * 1000;
  let ckeditorDragAndDropUploadDebug = false;

  // Web Context
  if (!webContext) {
    window.webContext = '/silverpeas';
  }

  let __called = false;
  let __internalQueue = sp.promise.newQueue();
  let $deferredUploadReady = sp.promise.deferred();
  let resourceDocumentContext;

  /**
   * This method must be called in order to configure properly the drag & drop on WYSIWYG editor.
   * @param options
   */
  window.configureCkEditorDdUpload = function(options) {
    if (__called) {
      __logDebug("configuration has already been done...");
      return;
    }
    __called = true;

    __logDebug("Starting the configuration");

    let config = extendsObject({
      componentInstanceId : false,
      resourceId : false,
      indexIt : false,
      documentType : 'image'
    }, options);

    if (!config.componentInstanceId || !config.resourceId) {
      __logError("componentInstanceId and resourceId options are mandatory ");
      return;
    }

    let $div = document.createElement('div');
    $div.classList.add('ckEditorDdUpload');
    $div.style.display = 'none';
    document.body.appendChild($div);

    __logDebug("Hidden DIV attached to body");

    let ddUploadConfig = {
      domSelector : '.ckEditorDdUpload',
      componentInstanceId : config.componentInstanceId,
      onCompletedUrl : sp.url.format(webContext + '/DragAndDrop/drop', {
        ComponentId : config.componentInstanceId,
        ResourceId : config.resourceId,
        IndexIt : config.indexIt,
        DocumentType : config.documentType
      }),
      onCompletedUrlSuccess : onCompletedUrlSuccess,
      onCompletedUrlError : onCompletedUrlSuccess
    };

    $deferredUploadReady.resolve(initDragAndDropUploadAndReload(ddUploadConfig));
    $deferredUploadReady.promise.then(function() {
      __logDebug("Drag & Drop plugin loaded");
    });

    /**
     * Context about documents linked to a WYSIWYG content.
     */
    resourceDocumentContext = new function() {

      let lastRefresh = 0;
      let list = [];

      /**
       * Refreshing the list of documents linked to WYSIWYG.
       * @returns {*} the promise of Ajax Request.
       */
      this.refresh = function(force) {
        let currentTime = new Date().getTime();
        let timeDiffMs = currentTime -  lastRefresh;
        lastRefresh = new Date().getTime();
        if (!force && timeDiffMs <= TEN_SECONDS_IN_MILLISECONDS) {
          __logDebug("document context - working on cache because of only " + timeDiffMs + "ms between two upload requests");
          __logDebug("document context - cached list contains " + list.length + " document(s)");
          return sp.promise.resolveDirectlyWith();
        }
        return sp.ajaxRequest(webContext + '/services/documents/' + config.componentInstanceId +
            '/resource/' + config.resourceId + '/types/' + config.documentType + '/fr?viewWidthAndHeight=true')
            .sendAndPromiseJsonResponse().then(function(json) {
              list = json;
              list.forEach(function(doc) {
                doc.spFileId = resourceDocumentContext.buildSpFileIdFrom(doc.fileName, doc.size);
              });
              __logDebug("document context - list refreshed with " + list.length + " document(s)");
              // The list is private
              return sp.promise.resolveDirectlyWith();
            });
      };

      /**
       * Gets by file browser definition.
       * @param spFileId an id built internally by the plugin.
       * @returns {*}
       */
      this.getBySpFileId = function(spFileId) {
        return list.getElement({spFileId : spFileId}, 'spFileId');
      };

      /**
       * Builds an id from file data.
       * @param fileName name of a file.
       * @param fileSize size of file.
       * @returns {string}
       */
      this.buildSpFileIdFrom = function(fileName, fileSize) {
        return StringUtil.normalizeByRemovingAccent(fileName + '@@@' + fileSize);
      }
    };
  };

  let ckeditorEvents = [];

  let onCompletedUrlSuccess = function() {
    __logDebug("upload terminated");
    resourceDocumentContext.refresh(true).then(function() {
      ckeditorEvents.forEach(activateFileFromCKEditorEvent);
      ckeditorEvents = [];
    });
  };

  let onFileSendError = function(file) {
    __logDebug("upload error for file " + file.name);
    let spFileId = resourceDocumentContext.buildSpFileIdFrom(file.name, file.size);
    let ckeditorEvent = ckeditorEvents.getElement({spFileId : spFileId}, 'spFileId');
    if (ckeditorEvent) {
      __updateCKEditorEventInErrorStatus(ckeditorEvent);
      ckeditorEvents.removeElement(ckeditorEvent);
    } else {
      __logDebug("can not aborting " + file.name + " as it was not in ckeditor event cache");
    }
  };

  let activateFileFromCKEditorEvent = function(ckeditorEvent) {
    __internalQueue.push(function() {
      let doc = resourceDocumentContext.getBySpFileId(ckeditorEvent.spFileId);
      if (doc) {
        __updateCKEditorEventInSuccessStatus(ckeditorEvent, doc);
        __finalizeActivation(ckeditorEvent);
      } else {
        __updateCKEditorEventInErrorStatus(ckeditorEvent);
      }
    });
  };

  let __finalizeActivation = function(ckeditorEvent) {
    let $editable = ckeditorEvent.editor.editable();
    let fileLoader = ckeditorEvent.data.fileLoader;
    let $images = sp.element.querySelectorAll("img", $editable.$);
    for (let i = 0 ; i < $images.length ; i++) {
      let $img = $images[i];
      if (!$img['sp-img-already-in-content']
          && $img.getAttribute('data-cke-saved-src') === fileLoader.url) {
        if (fileLoader.widthInPixel) {
          $img.width = fileLoader.widthInPixel;
          $img.height = fileLoader.heightInPixel;
        }
        $img['sp-img-already-in-content'] = true;
        let currentCover = document.querySelector('.cke_dialog_background_cover');
        if (currentCover && currentCover.style.display !== 'none') {
          __logDebug("image setting dialog already open");
          return;
        }
        let imgWidth = $img.width;
        let maxWidth = $editable.$.offsetWidth - 5;
        if (imgWidth <= maxWidth) {
          __logDebug("image width is smaller than the width of editor, no image resizing");
          return;
        }
        __logDebug("resizing image");
        let imgHeight = $img.height;
        if (imgHeight) {
          let ratio = imgHeight / imgWidth;
          $img.height = maxWidth * ratio;
        }
        $img.width = maxWidth;
        return;
      }
    }
    __logDebug("cannot open image setting dialog as HTML img element has not been found");
  };

  let __updateCKEditorEventInSuccessStatus = function(ckeditorEvent, doc) {
    let fileLoader = ckeditorEvent.data.fileLoader;
    __logDebug("activating " + doc.fileName);
    let documentName = doc.fileName;
    let documentSize = doc.size;
    let documentWidthInPixel = doc.widthInPixel;
    let documentHeightInPixel = doc.heightInPixel;
    let documentUrl = webContext + doc.downloadUrl;
    if (documentWidthInPixel) {
      fileLoader.widthInPixel = documentWidthInPixel;
      fileLoader.heightInPixel = documentHeightInPixel;
    }
    fileLoader.uploadTotal = documentSize;
    fileLoader.uploaded = documentSize;
    fileLoader.url = documentUrl;
    fileLoader.fileName = documentName;
    fileLoader.message = '';
    fileLoader.responseData = {
      uploaded: 1,
      fileName: documentName,
      url : documentUrl
    };
    fileLoader.changeStatus('uploaded');
    ckeditorEvent.editor.fire('change');
  };

  let __updateCKEditorEventInErrorStatus = function(ckeditorEvent) {
    let fileLoader = ckeditorEvent.data.fileLoader;
    __logDebug("aborting " + ckeditorEvent.spFile.name);
    fileLoader.uploadTotal = 0;
    fileLoader.uploaded = 0;
    fileLoader.message = '';
    fileLoader.url = undefined;
    fileLoader.responseData = {
      uploaded : 0,
      fileName : undefined,
      url : undefined,
      error : {
        message : ''
      }
    };
    fileLoader.changeStatus('error');
    ckeditorEvent.editor.fire('change');
  };

  CKEDITOR.on('instanceCreated', function(event) {
    let editor = event.editor;
    $deferredUploadReady.promise.then(function(dragAndDropUploadApi) {
      dragAndDropUploadApi.addEventListener('filesenderror', onFileSendError);
      dragAndDropUploadApi.ready(function() {
        let __processQueue = sp.promise.newQueue();

        editor.on('dataReady', function(event) {
          let $imgs = [].slice.call(event.editor.editable().$.querySelectorAll("img"), 0);
          __logDebug("Marking " + $imgs.length + " existing image(s)...");
          $imgs.forEach(function($img) {
            $img['sp-img-already-in-content'] = true;
          });
        });

        editor.on('notificationShow', function(notificationEvt) {
          notificationEvt.stop();
          notificationEvt.cancel();
          notificationEvt.removeListener();
        });

        editor.on('fileUploadRequest', function(ckeditorEvent) {
          __processQueue.push(function() {
            return resourceDocumentContext.refresh().then(function() {
              let file = ckeditorEvent.data.requestData.upload.file;
              ckeditorEvent.spFile = file;
              ckeditorEvent.spFileId = resourceDocumentContext.buildSpFileIdFrom(file.name, file.size);
              if (!resourceDocumentContext.getBySpFileId(ckeditorEvent.spFileId)) {
                __logDebug("uploading file...");
                ckeditorEvents.addElement(ckeditorEvent);
                dragAndDropUploadApi.sendFilesManually(file);
              } else {
                __logDebug("file already exists...");
                activateFileFromCKEditorEvent(ckeditorEvent);
              }
            });
          });
          ckeditorEvent.stop();
        });

        editor.on('fileUploadResponse', function(ckeditorEvent) {
          ckeditorEvent.stop();
        });

        __logDebug("file upload hook installed");
      });
    });
  });

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("CKEditor D&D File Upload - ERROR - " + message);
  }

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logDebug(message) {
    if (ckeditorDragAndDropUploadDebug) {
      let mainDebug = sp.log.debugActivated;
      sp.log.debugActivated = true;
      sp.log.debug("CKEditor D&D File Upload - " + message);
      sp.log.debugActivated = mainDebug;
    }
  }

  function __displayError(error) {
    notyError(error);
  }
})();
