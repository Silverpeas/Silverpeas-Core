/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

// Check for the various File API support.
var dragAndDropUploadEnabled = window.File;

(function() {

  if (!window.DragAndDropUploadBundle) {
    window.DragAndDropUploadBundle = new SilverpeasPluginBundle();
  }

  window.dragAndDropUploadDebug = false;

  // Web Context
  if (!webContext) {
    var webContext = '/silverpeas';
  }

  var firstDropEventHandled = false;

  /**
   * Drag & Drop Upload File.
   * It handles the rendering of the display container.
   * The identifier of the container and the server url are mandatory.
   * @param options
   * @constructor
   */
  DragAndDropUpload = function(options) {
    var __ready = new Promise(function(resolve, reject) {
      this.containerRendered = resolve;
    }.bind(this));
    if (!options.domSelector && !options.domSelector.length) {
      __logError("The dom identifier is mandatory");
    }

    this.context = extendsObject({
      mute : false,
      componentInstanceId : '',
      onAllFilesProcessed : [],
      beforeSend : function(fileUpload) {
        return Promise.resolve();
      },
      onCompletedUrl : null,
      onCompletedUrlHeaders : {},
      onCompletedUrlSuccess : null,
      isFileApi : window.File,
      currentUploadSession : false,
      uploadSessionPerDrop : false,
      helpForceDisplay : false,
      helpHighlightSelector : '',
      helpContentUrl : '',
      helpCoverClass : ''
    }, options);

    this.addEventListener = function(type, callback) {
      switch (type) {
        case 'allfilesprocessed':
          this.context.onAllFilesProcessed.push(callback);
          break;
      }
    };

    this.monitor = new DragAndDropUploadMonitor(this);

    document.addEventListener('DOMContentLoaded', function() {
      this.container = __renderContainer(this);
      this.container.addEventListener('drop', function(event) {
        if (!this.context.mute) {
          event.stopPropagation();
          event.preventDefault();
          this.container.hideOverlay();
          var uploadSession;
          if (this.context.uploadSessionPerDrop) {
            uploadSession = new UploadSession(this);
          } else {
            if (!this.context.currentUploadSession) {
              this.context.currentUploadSession = new UploadSession(this);
            }
            uploadSession = this.context.currentUploadSession;
          }
          __performSendFromEvent(event, uploadSession);
        }
      }.bind(this));

      if (!firstDropEventHandled) {
        firstDropEventHandled = true;
        var _root = document.querySelector('body').parentNode;
        _root.addEventListener('dragover', function(event) {
          if (!this.context.mute) {
            event.preventDefault();
            return false;
          }
        }.bind(this));
        _root.addEventListener('dragleave', function(event) {
          event.preventDefault();
          return false;
        });
        _root.addEventListener('drop', function(event) {
          if (!this.context.mute) {
            event.stopPropagation();
            event.preventDefault();
            this.container.hideOverlay();
            __displayError(DragAndDropUploadBundle.get("droparea.miss"));
          }
        }.bind(this));
      }
      this.containerRendered();
    }.bind(this));
    this.ready = function(callback) {
      __ready.then(function() {
        callback.call(this, this);
      }.bind(this));
    }.bind(this);
    this.mute = function() {
      this.context.mute = true;
      __ready.then(function() {
        __logDebug("mute - the drop area is not activated");
        this.container.hideOverlay();
      }.bind(this));
    }.bind(this);
    this.unmute = function() {
      this.context.mute = false;
      __logDebug("unmute - the drop area is again activated");
    }.bind(this);
  };

  /**
   * Handles the display monitoring of a DragAndDropUpload instance.
   * @param uploadInstance
   * @constructor
   */
  var DragAndDropUploadMonitor = function(uploadInstance) {
    var monitorCtx = {};
    this.show = function() {
      if (!monitorCtx.notyInstance) {
        monitorCtx.notyInstanceReady = new Promise(function(resolve, reject) {
          this.resolveNotyDisplayed = resolve;
        }.bind(this));
        monitorCtx.notyInstance = notyInfo(container, {
          timeout : false, callback : {
            afterClose : function() {
              __logDebug("Monitor notifier closed.");
            }, afterShow : function() {
              __logDebug("Monitor notifier displayed.");
              this.resolveNotyDisplayed();
            }.bind(this)
          }
        });
      }
    };
    this.close = function() {
      __logDebug("Closing monitor notifier...");
      if (monitorCtx.notyInstance) {
        monitorCtx.notyInstanceReady.then(function() {
          monitorCtx.notyInstance.close();
          monitorCtx.notyInstance = undefined;
        });
      }
    };
    this.reset = function() {
      __logDebug("Resetting monitor notifier...");
      this.close();
      monitorCtx.totalFiles = 0;
      monitorCtx.performedTotalFiles = 0;
      monitorCtx.total = 0;
      monitorCtx.uploadedTotal = 0;
      __logDebug("Monitor notifier reset.");
    }.bind(this);
    this.reset();

    var percentBar = document.createElement('span');
    percentBar.classList.add('percentage-progress-bar');
    var percentValue = document.createElement('span');
    percentValue.classList.add('percentage-progress-value');
    var percentContainer = document.createElement('div');
    percentContainer.classList.add('percentage-progress');
    percentContainer.appendChild(percentBar);
    percentContainer.appendChild(percentValue);
    var container = document.createElement('div');
    container.classList.add('upload-state');
    container.innerHTML = DragAndDropUploadBundle.get("upload.inprogress");
    container.appendChild(percentContainer);

    var __update = function() {
      if (monitorCtx.total || monitorCtx.performedTotalFiles) {
        var uploadedTotalPercentage = monitorCtx.total ?
            parseInt((monitorCtx.uploadedTotal / monitorCtx.total) * 100) : 100;
        percentBar.style.width = uploadedTotalPercentage + '%';
        percentValue.innerHTML = monitorCtx.performedTotalFiles + '/' + monitorCtx.totalFiles;
        var allFilesUploaded = (monitorCtx.performedTotalFiles === monitorCtx.totalFiles);
        if (allFilesUploaded) {
          this.close();
        }
      }
    }.bind(this);

    uploadInstance.addEventListener('allfilesprocessed', function() {
      __internalQueuePromise.then(function() {
        __update();
      });
    }.bind(this));

    this.add = function(fileUpload) {
      fileUpload.addEventListener('startsend', function() {
        __internalQueuePromise.then(function() {
          this.show();
        }.bind(this));
      }.bind(this));
      fileUpload.addEventListener('sendsuccess', function() {
        __internalQueuePromise.then(function() {
          monitorCtx.performedTotalFiles++;
          __update();
        });
      }.bind(this));
      fileUpload.addEventListener('senderror', function(fileUpload) {
        __internalQueuePromise.then(function() {
          monitorCtx.total -= fileUpload.size;
          monitorCtx.uploadedTotal -= fileUpload.uploadedSize;
          monitorCtx.performedTotalFiles++;
          __update();
        });
      }.bind(this));
      fileUpload.addEventListener('progress', function(fileUpload) {
        __internalQueuePromise.then(function() {
          monitorCtx.uploadedTotal -= fileUpload.previousUploadedSize;
          monitorCtx.uploadedTotal += fileUpload.uploadedSize;
          __update();
        });
      }.bind(this));
      monitorCtx.total += fileUpload.size;
      monitorCtx.totalFiles++;
    }.bind(this);
  };

  /**
   * Handle an upload session.
   * @param uploadInstance
   * @constructor
   */
  var UploadSession = function(uploadInstance) {
    this.uploadInstance = uploadInstance;
    this.onCompleted = extendsObject({}, {
      "url" : uploadInstance.context.onCompletedUrl,
      "urlHeaders" : uploadInstance.context.onCompletedUrlHeaders,
      "urlSuccess" : uploadInstance.context.onCompletedUrlSuccess
    });
    uploadInstance.monitor.reset();

    var firstDropPerformed = false;
    var firstLoadPending = false;
    var firstLoadCompleted = false;
    var nbFilePerformed = 0;
    this.id = '';
    this.severalFilesToUpload = false;
    this.existsAtLeastOneFolder = false;
    this.filesToSend = [];
    this.currentSending = [];
    this.processedFiles = [];
    this.consume = function() {
      if (!firstDropPerformed) {
        firstDropPerformed = true;
        __logDebug("First drop performed");
        if (this.filesToSend.length > 0 && !firstLoadPending && !firstLoadCompleted) {
          _consume();
        }
      }
    }.bind(this);
    this.push = function(file) {
      __logDebug("Pushing '" + file.fullPath + "' in file sending queue...");

      internalQueue.push(function() {
        file.componentInstanceId = uploadInstance.context.componentInstanceId;
        var fileUpload = new FileUpload(uploadInstance, this, file);
        uploadInstance.monitor.add(fileUpload);
        this.filesToSend.push(fileUpload);
        nbFilePerformed++;
        this.severalFilesToUpload = (nbFilePerformed > 1);
        if ((firstDropPerformed || this.filesToSend.length > 1) && !firstLoadPending &&
            !firstLoadCompleted) {
          _consume();
        }
      }.bind(this));

    }.bind(this);

    /**
     * Must be called at the end of the last upload of a session.
     * @type {function(this:UploadSession)|*}
     * @private
     */
    var _performUploadEnd = function() {
      uploadInstance.context.currentUploadSession = false;
      if (jQuery && jQuery.progressMessage) {
        jQuery.progressMessage();
      }
      __logDebug("performUploadEnd - calling onAllFilesProcessed callback");
      for (var i = 0; i < uploadInstance.context.onAllFilesProcessed.length; i++) {
        uploadInstance.context.onAllFilesProcessed[i].call(this);
      }
      if (this.onCompleted.url) {
        __logDebug("performUploadEnd - performing ajax call on '" + this.onCompleted.url + "'");
        var onCompletedUrlHeaders = {
          "X-UPLOAD-SESSION" : this.id
        };
        if (typeof this.onCompleted.urlHeaders === 'object') {
          onCompletedUrlHeaders = extendsObject(onCompletedUrlHeaders, this.onCompleted.urlHeaders);
        }
        silverpeasAjax({
          method : 'POST',
          url : this.onCompleted.url,
          headers : onCompletedUrlHeaders
        }).then(function(request) {
          if (typeof this.onCompleted.urlSuccess === 'function') {
            __logDebug("performUploadEnd - calling onCompletedUrlSuccess callback");
            this.onCompleted.urlSuccess.call(this, request.response);
            jQuery.closeProgressMessage();
          }
        }.bind(this), function(request) {
          __logDebug("performUploadEnd - error for session '" + this.id);
          console.log(Error(request.statusText));
          jQuery.closeProgressMessage();
        }.bind(this));

      }
    }.bind(this);

    /**
     * Consumes the queue of files to send.
     * @type {function(this:UploadSession)|*}
     * @private
     */
    var _consume = function() {
      if (!this.filesToSend.length || sendMonitor.isFull()) {
        if (!this.filesToSend.length) {
          __logDebug("consume - no more file to send");
          if (firstLoadCompleted) {
            _performUploadEnd();
          }
        } else {
          __logDebug("consume - sendMonitor is full");
        }
        return;
      }

      if (this.currentSending[this.filesToSend[0].fullPath]) {
        __logDebug("consume - a version of '" + this.filesToSend[0].fullPath +
            "' is currently uploading... upload will be performed after");
        return;
      }

      sendMonitor.register();
      var file;
      if (!firstLoadPending && !firstLoadCompleted) {
        firstLoadPending = true;
        file = this.filesToSend.shift();
        __logDebug("consume - first load with '" + file.fullPath + "'");
        _send(file).then(function(params) {

          this.id = params.response.uploadSessionId;
          __logDebug("consume - first load consumed for '" + file.fullPath +
              "' and retrieve upload session id " + this.id);

          firstLoadPending = false;
          firstLoadCompleted = true;
          internalQueue.push(function() {
            _consume();
          }.bind(this));

        }.bind(this), function() {

          __logDebug("consume - first load rejected for '" + file.fullPath +
              "', so trying with a next one");

          firstLoadPending = false;
          internalQueue.push(function() {
            _consume();
          }.bind(this));

        }.bind(this));
      } else if (firstLoadCompleted) {
        file = this.filesToSend.shift();
        file.uploadSessionId = this.id;
        _send(file);
      } else {
        sendMonitor.unregister();
      }
    }.bind(this);

    /**
     * Performs the sending of a file.
     * It centralizes the call of FileUpload.send().
     * @type {function(this:UploadSession)|*}
     * @private
     */
    var _send = function(file) {
      if (file.isSent) {

        __logDebug("_send - file '" + file.fullPath + "' already sent");

        return null;
      }
      this.currentSending[file.fullPath] = file;
      return file.send()

        // Event are controlled
          .then(function(params) {
            sendMonitor.unregister();

            // Send has been successfully performed
            file.isSent = true;
            internalQueue.push(function() {
              delete this.currentSending[file.fullPath];
              this.processedFiles.push(file);
              _consume();
            }.bind(this));

            return params;
          }.bind(this), function(params) {
            sendMonitor.unregister();

            // An error has been captured during the send
            console.log(params.error);
            console.log(params.sentFile);
            internalQueue.push(function() {
              delete this.currentSending[file.fullPath];
              _consume();
            }.bind(this));

            return Promise.reject(params);
          }.bind(this));
    }.bind(this);
  };
  /**
   * Permits to manage a treatment queue.
   */
  var __internalQueuePromise = Promise.resolve();
  var internalQueue = new function() {
    this.push = function(callback) {
      __internalQueuePromise.then(callback);
    };
  };

  /**
   * Handles the Send File Request.
   * @param uploadInstance the current instance.
   * @param uploadSession the current upload session.
   * @param file the file to send.
   * @constructor
   */
  var FileUpload = function(uploadInstance, uploadSession, file) {
    extendsObject(this, file);
    this.uploadSession = uploadSession;
    var onstartsend = [];
    var onprogress = [];
    var onsenderror = [];
    var onsendsuccess = [];
    this.addEventListener = function(type, callback) {
      switch (type) {
        case 'startsend':
          onstartsend.push(callback);
          break;
        case 'progress' :
          onprogress.push(callback);
          break;
        case 'senderror' :
          onsenderror.push(callback);
          break;
        case 'sendsuccess' :
          onsendsuccess.push(callback);
          break;
      }
    };
    this.uploadSessionId = '';
    this.uploadedSize = 0;
    this.uploadedRatio = 0;
    this.previousUploadedSize = 0;
    var __error = function(request) {

      __logDebug("FileUpload.send() - '" + file.fullPath +
          "' not uploaded due to errors (HTTP status in error)");

      for (var i = 0; i < onsenderror.length; i++) {
        onsenderror[i].call(this, this);
      }

      return Promise.reject({
        "sentFile" : file, "error" : Error(request.statusText)
      });
    }.bind(this);

    var sendFile = function() {

      var headers = {
        "Content-Type" : 'application/octet-stream',
        "X-FULL-PATH" : encodeURIComponent(file.fullPath)
      };

      if (this.uploadSessionId) {
        headers['X-UPLOAD-SESSION'] = this.uploadSessionId;
        __logDebug("FileUpload.send() - sending '" + file.fullPath + "' to upload session id " +
            this.uploadSessionId);
      } else {
        __logDebug("FileUpload.send() - sending '" + file.fullPath + "'");
      }

      if (file.componentInstanceId) {
        headers['X-COMPONENT-INSTANCE-ID'] = file.componentInstanceId;
      }

      return silverpeasAjax({
        method : 'POST',
        url : (webContext + '/services/fileUpload'),
        headers : headers,
        data : file,
        onprogress : function(event) {
          if (event.lengthComputable) {
            this.previousUploadedSize = this.uploadedSize;
            this.uploadedSize = event.loaded;
            this.uploadedRatio = event.loaded / this.size;
            for (var i = 0; i < onprogress.length; i++) {
              onprogress[i].call(this, this);
            }
            __logDebug("FileUpload.send() - upload in progress for '" + file.fullPath +
                "' -> ratio=" + this.uploadedRatio);
          }
        }.bind(this)
      }).then(function(request) {
        var element = document.createElement('div');
        element.innerHTML = request.response;
        var jsonAsString = element.firstChild.innerHTML;
        var fileInfo = extendsObject({
          "uploadSessionId" : '',
          "fullPath" : '',
          "name" : '',
          "size" : 0,
          "formattedSize" : '',
          "iconUrl" : ''
        }, JSON.parse(jsonAsString));

        __logDebug("FileUpload.send() - '" + file.fullPath + "' uploaded successfully");

        this.uploadSessionId = fileInfo.uploadSessionId;

        for (var i = 0; i < onsendsuccess.length; i++) {
          onsendsuccess[i].call(this, this);
        }

        return {
          "sentFile" : file, "response" : fileInfo
        }
      }.bind(this), __error);
    }.bind(this);

    this.send = function() {

      var headers = {};
      if (this.uploadSessionId) {
        headers['X-UPLOAD-SESSION'] = this.uploadSessionId;
      }
      if (file.componentInstanceId) {
        headers['X-COMPONENT-INSTANCE-ID'] = file.componentInstanceId;
      }

      var data = new FormData();
      data.append('fullPath', file.fullPath);
      data.append('name', file.name);
      data.append('size', file.size);
      return silverpeasAjax({
        method : 'POST',
        url : (webContext + '/services/fileUpload/verify'),
        headers : headers,
        data : data
      }).then(function() {
        __logDebug("FileUpload.send() - verifying send of '" + file.fullPath + "'");
        return uploadInstance.context.beforeSend(this).then(function() {
          // Resolve case
          __logDebug("performUploadEnd - calling onStartSend callback");
          for (var i = 0; i < onstartsend.length; i++) {
            onstartsend[i].call(this);
          }
          return sendFile();
        }, function() {
          // Reject case
          uploadInstance.context.currentUploadSession = false;
          uploadInstance.monitor.close();
        });
      }.bind(this), __error);
    }.bind(this);
  };

  /**
   * Avoids to get more than 3 ajax upload between all instances of DragAndDropUpload.
   */
  var sendMonitor = new (function() {
    var parallelSend = 3;
    var nbSend = 0;
    this.register = function() {
      nbSend++;
      __logDebug("sendMonitor - register - nbSend=" + nbSend);
    };
    this.unregister = function() {
      nbSend--;
      __logDebug("sendMonitor - unregister - nbSend=" + nbSend);
    };
    this.isFull = function() {
      return nbSend >= parallelSend;
    }
  });

  /**
   * Performs the drop event.
   * @param event
   * @param uploadSession
   * @private
   */
  function __performSendFromEvent(event, uploadSession) {
    if (!event.target.files) {
      var items = (event.dataTransfer ? event.dataTransfer.items :
          event.originalEvent.dataTransfer.items);

      if (items) {
        var len = items.length, i, entry;
        for (i = 0; i < len; i++) {
          entry = items[i];

          if (entry.getAsEntry) {
            //Standard HTML5 API
            entry = entry.getAsEntry();
          } else if (entry.webkitGetAsEntry) {
            //WebKit implementation of HTML5 API.
            entry = entry.webkitGetAsEntry();
          }
          if (entry) {
            if (entry.isFile) {
              //Handle FileEntry
              __readFile(entry, uploadSession);
            } else if (entry.isDirectory) {
              //Handle DirectoryEntry
              uploadSession.existsAtLeastOneFolder = true;
              __readFileTree(entry, uploadSession);
            }
          }
        }
        internalQueue.push(function() {
          uploadSession.consume();
        });
      } else {
        var files = (event.dataTransfer ? event.dataTransfer.files :
            event.originalEvent.dataTransfer.files);
        __performFileFromOldWay(uploadSession, files)
      }
    } else if (event.target.files) {
      __performFileFromOldWay(uploadSession, event.target.files)
    } else if (event.files) {
      __performFileFromOldWay(uploadSession, event.files)
    } else {
      __displayFolderReadError(uploadSession);
    }
  }

  function __performFileFromOldWay(uploadSession, files) {
    if (files && files.length) {
      for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file.type || file.size) {
          file.fullPath = '/' + file.name;
          uploadSession.push(file);
        } else {
          console.log(file);
          __displayFolderReadError(uploadSession);
        }
      }
      internalQueue.push(function() {
        uploadSession.consume();
      });
    } else {
      __displayFolderReadError(uploadSession);
    }
  }

  function __displayFolderReadError(uploadSession) {
    if (!uploadSession.folderError) {
      uploadSession.folderError = notyError(DragAndDropUploadBundle.get('droparea.folder.error'), {
        timeout : false, callback : {
          onClose : function() {
            uploadSession.folderError = undefined;
          }
        }
      });
    } else {
      if (uploadSession.folderError.closed) {
        uploadSession.folderError.show();
      }
    }
  }

  /**
   * Explore trough the file tree
   * @Traverse recursively trough File and Directory entries.
   * @param itemEntry
   * @param instance
   * @private
   */
  var __readFileTree = function(itemEntry, instance) {
    if (itemEntry.isFile) {
      __readFile(itemEntry, instance);
    } else if (itemEntry.isDirectory) {
      var dirReader = itemEntry.createReader();
      dirReader.readEntries(function(entries) {
        var idx = entries.length;
        while (idx--) {
          __readFileTree(entries[idx], instance);
        }
      });
    }
  };

  /**
   * Read FileEntry to get Native File object.
   * @param fileEntry
   * @param uploadSession
   * @private
   */
  var __readFile = function(fileEntry, uploadSession) {
    //Get File object from FileEntry
    fileEntry.file(function(instance, fileEntry, file) {
      if (instance && fileEntry) {
        file.fullPath = fileEntry.fullPath;
        uploadSession.push(file);
      }
    }.bind(this, uploadSession, fileEntry));
  };

  /**
   * Render the container.
   * @param uploadInstance
   * @private
   */
  function __renderContainer(uploadInstance) {
    var pluginContainer = document.querySelector(uploadInstance.context.domSelector);
    pluginContainer.classList.add('droparea-cover');

    var pluginHelp = document.createElement('div');
    pluginHelp.classList.add('droparea-cover-help');
    if (uploadInstance.context.helpCoverClass) {
      pluginHelp.classList.add(uploadInstance.context.helpCoverClass);
    }
    pluginHelp.innerHTML = '<img src="' + webContext + '/util/icons/dndHelp.png" alt="?" />';
    pluginContainer.appendChild(pluginHelp);

    var pluginOverlay = document.createElement('div');
    pluginOverlay.classList.add('droparea-cover-overlay');
    pluginContainer.appendChild(pluginOverlay);

    var isHelpForceDisplay = function() {
      if (!window.MutationObserver || uploadInstance.context.mute) {
        return false;
      }
      if (typeof uploadInstance.context.helpForceDisplay === 'function') {
        return uploadInstance.context.helpForceDisplay.call(this);
      }
      return uploadInstance.context.helpForceDisplay;
    };

    // Drop area events
    var mouseEnter = function() {
      if (!uploadInstance.context.mute && !isHelpForceDisplay()) {
        pluginContainer.showHelp();
        return false;
      }
    };
    var mouseLeave = function() {
      if (!isHelpForceDisplay()) {
        pluginContainer.hideHelp();
        return false;
      }
    };
    pluginContainer.addEventListener('mouseenter', mouseEnter);
    pluginContainer.addEventListener('mouseover', mouseEnter);
    pluginContainer.addEventListener('mouseleave', mouseLeave);
    pluginContainer.addEventListener('mouseout', mouseLeave);
    pluginContainer.addEventListener('dragover', function(event) {
      if (!uploadInstance.context.mute) {
        event.preventDefault();
        pluginContainer.showOverlay();
        return false;
      }
    });
    pluginOverlay.addEventListener('dragleave', function(event) {
      event.preventDefault();
      pluginContainer.hideOverlay();
      return false;
    });

    var getHelpHighlightContainer = function() {
      var helpHighlightContainer = !uploadInstance.context.helpHighlightSelector ? '' :
          pluginContainer.querySelector(uploadInstance.context.helpHighlightSelector);
      if (!helpHighlightContainer) {
        helpHighlightContainer = pluginContainer;
      }
      return helpHighlightContainer;
    };
    var helpHighlightContainer;
    var originalHelpContainerBorderStyle;
    pluginContainer.showHelp = function() {
      if (pluginHelp.style.display !== 'block') {
        pluginHelp.style.display = 'block';
        helpHighlightContainer = getHelpHighlightContainer();
        var computedStyle = helpHighlightContainer.currentStyle ||
            getComputedStyle(helpHighlightContainer);
        originalHelpContainerBorderStyle = computedStyle.borderStyle;
        if (!originalHelpContainerBorderStyle || originalHelpContainerBorderStyle === 'none') {
          originalHelpContainerBorderStyle =
              computedStyle.borderTopStyle === 'none' ? '' : computedStyle.borderTopStyle;
        }
        if (!originalHelpContainerBorderStyle) {
          helpHighlightContainer.classList.add('droparea-cover-help-highlight');
        } else {
          helpHighlightContainer.style.borderStyle =
              originalHelpContainerBorderStyle.style === 'dashed' ? 'solid' : 'dashed';
        }
      }
    };
    pluginContainer.hideHelp = function() {
      if (pluginHelp.style.display !== 'none') {
        pluginHelp.style.display = 'none';
        if (helpHighlightContainer) {
          if (!originalHelpContainerBorderStyle) {
            helpHighlightContainer.classList.remove('droparea-cover-help-highlight');
          } else {
            helpHighlightContainer.style.borderStyle = '';
          }
        }
      }
    };
    if (window.MutationObserver && uploadInstance.context.helpForceDisplay) {
      var observer = new MutationObserver(function() {
        if (isHelpForceDisplay()) {
          pluginContainer.showHelp();
        } else {
          pluginContainer.hideHelp();
        }
      }.bind(this));
      observer.observe(pluginContainer, {
        childList : true, characterData : true, subtree : true
      });
    }

    pluginContainer.showOverlay = function() {
      pluginOverlay.style.display = 'block';
      pluginContainer.hideHelp();
    };
    pluginContainer.hideOverlay = function() {
      pluginOverlay.style.display = 'none';
      if (isHelpForceDisplay()) {
        pluginContainer.showHelp();
      }
    };

    // Help
    if (uploadInstance.context.helpContentUrl) {
      silverpeasAjax(uploadInstance.context.helpContentUrl).then(function(request) {
        var helpContentContainer = document.createElement('div');
        helpContentContainer.classList.add('droparea-cover-help-content');
        helpContentContainer.innerHTML = request.response;
        var qtipOptions = {
          prerender : true, style : {
            classes : "qtip-shadow qtip-yellow"
          }, content : {
            text : helpContentContainer.outerHTML
          }, position : {
            my : "center left", at : "center right", adjust : {
              method : "flipinvert"
            }, viewport : jQuery(window)
          }, show : {
            delay : 250
          }
        };

        jQuery('img', pluginHelp).qtip(qtipOptions);
      });
    }

    return pluginContainer;
  }

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    console.log("D&D File Upload - ERROR - " + message);
  }

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logDebug(message) {
    if (window.dragAndDropUploadDebug) {
      console.log("D&D File Upload - DEBUG - " + message);
    }
  }

  function __displayError(error) {
    notyError(error);
  }
})();

/**
 * Initializing methods
 */

/**
 * This method permits to initialize by an easily way a drag & drop container.
 * The default options are the followings:
 * - domSelector: ".dragAndDropUpload"
 * - onCompletedUrlSuccess : uploadCompleted function (so if this method exists it is called on a
 * successful upload).
 *
 * If FileAPI is not supported, calling this method has no effect.
 *
 * @param options the onCompletedUrl option must be set in order to get the system working.
 *     Please fill also componentInstanceId option in order to wire the transverse component
 *     checks...
 *     If helpContentUrl is filled, then an help icon is shown when the cursor is over the
 *     drag&drop container. The url must aim a file that is part of HTML content without the HTML,
 *     HEAD and BODY tags.
 *     Finally, the parameter beforeSend permits to specify a function that is called just after
 *     the verifications and just before the effective upload. This method takes the current
 *     FileUpload instance as parameter and must return a Promise.
 * @returns {*}
 */
function initDragAndDropUploadAndReload(options) {
  if (!dragAndDropUploadEnabled) {
    jQuery(document).ready(function() {
      jQuery('.dragAndDropUploadCheck').remove();
    });
    return false;
  }
  return new DragAndDropUpload(extendsObject({
    domSelector : ".dragAndDropUpload", onCompletedUrl : '', onCompletedUrlSuccess : function() {
      console.log("D&D File Upload - WARNING - no complete URL success function specified");
    }
  }, options));
}
