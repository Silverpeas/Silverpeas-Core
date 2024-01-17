/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function($) {

  // Check for the various File API support.
  const isFileAPI = window.File;

  // Web Context
  if (!window.webContext) {
    window.webContext = '/silverpeas';
  }

  // Event definitions
  const EVENT = {
    SEND_FILE : 'SEND_REQUEST',
    FILES_TO_SEND : 'FILES_TO_SEND',
    UPLOADED_FILE_CHANGED : 'UPLOADED_FILE_CHANGED',
    DELETE_FILE : 'DELETE_FILE'
  };

  /**
   * The different fileUpload methods handled by the plugin.
   */
  const methods = {
    /**
     * Prepare UI and behavior
     */
    init : function(options) {
      return __init($(this), options);
    },

    /**
     * Gets API of first jQuery instance found.
     */
    api : function() {
      const $instances = $(this);
      if ($instances.length) {
        return __getApi($instances[0]);
      }
      return undefined;
    }
  };

  /**
   * The fileUpload Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the fileUpload namespace in JQuery.
   */
  $.fn.fileUpload = function(method) {

    if (!$().popup) {
      console.error("Silverpeas Popup JQuery Plugin is required.");
      return false;
    }

    if (!window.notyError) {
      console.error("Silverpeas Notifier JQuery Plugin is required.");
      return false;
    }

    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.fileUpload');
    }
    return false;
  };

  /**
   * Private method that prepares UI and behavior.
   * @param $targets
   * @param options
   * @return {*}
   * @private
   */
  function __init($targets, options) {

    if (!$targets.length) {
      return $targets;
    }

    return $targets.each(function() {
      const $target = $(this);
      const $container = $('<div>').addClass('fileUpload-container');
      $target.append($container);

      const params = {
        $container : $container,
        containerOriginId : $target.attr('id'),
        uploadCount : 0
      };

      __setApi($target, params);

      // Options
      const _options = __buildOptions(options);

      // Context
      __buildContext(params, _options);

      // Rendering the file uploader container
      __renderFileUploadContainer(params);
    });
  }

  /**
   * Private method that prepares UI and behavior.
   * @param params
   * @private
   */
  function __renderFileUploadContainer(params) {
    __handleFormSubmits(params);
    __renderBlocs(params);
  }

  /**
   * Sets API methods.
   * @param $target
   * @param params
   * @private
   */
  function __setApi($target, params) {
    const _api = {
      /**
       * Resets the plugin.
       */
      reset : function() {
        const $fileToDelete = $('.uploaded-file', params.$uploadedFileList);
        $fileToDelete.trigger(EVENT.DELETE_FILE);
      },
      /**
       * Verifies is the system is currently uploading files.
       * True if an upload is currently performing, false otherwise.
       */
      verifyIsCurrentlySendingFiles : function() {
        return (__getNbFilesBeingTransfer(params) > 0);
      },
      /**
       * Checks that it does not exist an upload. Nothing is done if none.
       * An exception is sent otherwise (with the appropriate i18n message).
       */
      checkNoFileSending : function() {
        if (_api.verifyIsCurrentlySendingFiles()) {
          throw params.options.labels.sendingWaitingWarning;
        }
      },
      /**
       * Encodes a set of form elements as a string for submission.
       */
      serialize : function() {
        let serialization = '';
        _api.serializeArray().forEach(function(input) {
          if (serialization.length) {
            serialization += '&'
          }
          serialization += input.name + '=' + input.value
        });
        return serialization;
      },
      /**
       * Encodes a set of form elements as an array of names and values.
       */
      serializeArray : function() {
        const serialization = [];
        $('input, textarea', params.$uploadedFileList).each(function(index, input) {
          const $input = $(input);
          serialization.push({name : $input.attr("name"), value : $input.val()});
        });
        return serialization;
      },
      /**
       * Gets an array of all current uploaded file data.
       * For each uploaded file, there is the attribute 'input' which is an array of all form
       * inputs.
       * @returns {*[]}
       */
      getUploadedFiles : function() {
        const uploadedFiles = [];
        let uploadedFile;
        $('input, textarea', params.$uploadedFileList).each(function(index, input) {
          const $input = $(input);
          const __uploadedFile = $input.data('uploadedFileData');
          if (__uploadedFile) {
            uploadedFile = extendsObject({}, __uploadedFile);
            uploadedFile.inputs = [];
            uploadedFiles.push(uploadedFile);
          }
          uploadedFile.inputs.push({name : $input.attr("name"), value : $input.val()});
        });
        return uploadedFiles;
      }
    };
    params._api = _api;
    $target.data('fileUploadApi', _api);
  }

  /**
   * Gets API methods.
   * @param target
   * @private
   */
  function __getApi(target) {
    return $(target).data('fileUploadApi');
  }

  /**
   * Handle form submits.
   * @param params
   * @private
   */
  function __handleFormSubmits(params) {
    if (params.options.jqueryFormSelector.length > 0) {
      $(document).ready(function() {
        $(params.options.jqueryFormSelector).on("submit", function() {
          if (__getNbFilesBeingTransfer(params) > 0) {
            notyInfo(params.options.labels.sendingWaitingWarning);
            return false;
          }
          return true;
        });
      });
    }
  }

  /**
   *
   * @param params
   * @private
   */
  function __renderBlocs(params) {
    params.$container.append(__renderUploadLimitBloc(params), __renderUploadBloc(params),
            __renderWaitingUploadList(params), __renderUploadedFilesList(params));

    // Handling the file upload limit.
    if (params.options.nbFileLimit > 0) {
      params.$container.on(EVENT.FILES_TO_SEND, function(event, nbFilesToSend) {
        __toggleUploadBloc(params, ((__getNbPossibleFileSends(params) - nbFilesToSend) <= 0));
      });
      params.$container.on(EVENT.UPLOADED_FILE_CHANGED, function() {
        __toggleUploadBloc(params, (__getNbPossibleFileSends(params) <= 0));
      });
    }
  }

  /**
   * Handles display of limit message and upload bloc.
   * @param params
   * @param fileLimitReached
   * @private
   */
  function __toggleUploadBloc(params, fileLimitReached) {
    if (fileLimitReached) {
      params.$uploadLimitContainer.show();
      params.$uploadContainer.hide();
    } else {
      params.$uploadLimitContainer.hide();
      params.$uploadContainer.show();
    }
  }

  /**
   *
   * @param params
   * @return {*|HTMLElement}
   * @private
   */
  function __renderUploadLimitBloc(params) {
    const $uploadLimitBloc = $('<div>').addClass('upload-limit').append($('<span>').addClass('legendeFileUpload').append(__getUploadLimitReachedMessage(params))).hide();
    params.$uploadLimitContainer = $uploadLimitBloc;
    return $uploadLimitBloc;
  }

  /**
   * Gets the message to display when upload file limit is reached.
   * @param params
   * @return {string}
   * @private
   */
  function __getUploadLimitWarningMessage(params) {
    let message = params.options.labels.limitFileWarning;
    if (params.options.nbFileLimit > 1) {
      message =
              params.options.labels.limitFilesWarning.replace(/@number@/, params.options.nbFileLimit);
    }
    return message;
  }

  /**
   * Gets the message to display when upload file limit is reached.
   * @param params
   * @return {string}
   * @private
   */
  function __getUploadLimitReachedMessage(params) {
    let message = params.options.labels.limitFileReached;
    if (params.options.nbFileLimit > 1) {
      message =
              params.options.labels.limitFilesReached.replace(/@number@/, params.options.nbFileLimit);
    }
    return message;
  }

  /**
   *
   * @param params
   * @return {*|HTMLElement}
   * @private
   */
  function __renderUploadBloc(params) {
    const $uploadBloc = $('<div>').addClass('actions');
    params.$uploadContainer = $uploadBloc;
    __appendFileInputs(params, $uploadBloc);
    return $uploadBloc;
  }

  /**
   *
   * @param params
   * @return {*|HTMLElement}
   * @private
   */
  function __renderWaitingUploadList(params) {
    const $list = $('<div>').addClass('waiting-list');
    params.$waitingUploadList = $list;
    return $list;
  }

  /**
   *
   * @param params
   * @return {*|HTMLElement}
   * @private
   */
  function __renderUploadedFilesList(params) {
    const $list = $('<div>').addClass('uploaded-file-list');
    params.$uploadedFileList = $list;
    $list.on(EVENT.SEND_FILE, function(event, uploadHandler) {
      uploadHandler.send();
    });
    return $list;
  }

  /**
   * Clear and append a new bloc that permits to add files.
   * @param $uploadBloc
   * @param params
   * @private
   */
  function __appendFileInputs(params, $uploadBloc) {

    let chooseFileLabel = params.options.labels.chooseFiles;
    if (!params.options.multiple) {
      chooseFileLabel = params.options.labels.chooseFile;
    }

    // File
    $uploadBloc.empty();
    let $img = undefined;
    if (params.options.dragAndDropDisplayIcon) {
      $img = $('<div>').append($('<img>', {
        title: chooseFileLabel,
        alt: chooseFileLabel,
        src: webContext + '/util/icons/create-action/addFile.png'
      })).addClass('icon');
    }
    const $fileInputs = $('<div>').addClass('fileinputs').addClass('input');
    const $form = $('<form>', {
      id : 'form-' + params.containerOriginId,
      method : 'post',
      action : '#'
    });
    const $fileInput = $('<input>', {
      multiple : params.options.multiple,
      type : 'file',
      length : 40
    }).addClass('dragAndDrop');
    $form.append($fileInput);
    $fileInputs.append($form);
    if (isFileAPI && params.options.dragAndDropDisplay) {
      $fileInput.css('position', 'absolute');
      $fileInput.css('top', '-10000px');
      $fileInput.css('right', '0');
      if ($img) {
        $img.addClass('dng');
      }
      $uploadBloc.addClass('dng');
      const $buttonInput = $('<div>').addClass('sp_button').addClass('button').append($('<a>').append(params.options.labels.browse).click(function() {
            $fileInput.click();
            return false;
          }));
      const $areaInput = $('<div>').addClass('droparea').append($('<span>').append((
              params.options.multiple ? params.options.labels.dragAndDropFiles :
              params.options.labels.dragAndDropFile))).click(function() {
        $('a', $buttonInput).click();
        return false;
      });
      $fileInputs.append($areaInput, $buttonInput);

      // DnD on right area
      $areaInput.on('dragover', function() {
        $areaInput.addClass('hover');
        return false;
      });
      $areaInput.on('dragleave', function() {
        $areaInput.removeClass('hover');
        return false;
      });
      $areaInput.on('drop', function(event) {
        event.stopPropagation();
        event.preventDefault();
        $areaInput.removeClass('hover');
        const files = __extractFilesFromEvent(event);
        if (files) {
          __appendHtml5Upload(params, files);
          __appendFileInputs(params, $uploadBloc);
        }
        return false;
      });
    }
    $uploadBloc.append($img, $fileInputs);

    // On change event
    $fileInput.on("change", function() {
      if (isFileAPI) {
        __appendHtml5Upload(params, $fileInput[0].files);
      } else {
        __appendInputFileUpload(params, $fileInput);
      }
      __appendFileInputs(params, $uploadBloc);
    });
  }

  /**
   * Extract files from an event.
   * @param event
   * @return {*|FileList}
   * @private
   */
  function __extractFilesFromEvent(event) {
    let files = event.target.files;
    if (!files && event.dataTransfer) {
      files = event.dataTransfer.files;
    }
    if (!files && event.originalEvent) {
      files = event.originalEvent.dataTransfer.files;
    }
    return files;
  }

  /**
   * Append a new upload.
   * @param params
   * @param $inputOfFiles
   * @private
   */
  function __appendInputFileUpload(params, $inputOfFiles) {

    // Verify that upload is possible
    if (__verifyUploadIsPossible(params, 1)) {

      const uploadCommons = __performAppendUploadCommons(params, $inputOfFiles);
      const $fileUploadContainer = $('<div>', {
        id : uploadCommons.uploadContext.uploadId
      });
      const $form = $('<form>', {
        id : uploadCommons.uploadContext.formId,
        method : 'post',
        action : uploadCommons.uploadContext.uploadUrl
      });
      $inputOfFiles.attr({
        name: 'file_upload'
      }).hide();

      params.$container.append($fileUploadContainer.append($form.append($inputOfFiles)));

      // Perform uploads
      __appendUpload(params, new __UploadHandler(uploadCommons.uploadContext,
              uploadCommons.$waitingEndOfUploadContainer, $fileUploadContainer, null));
    }
  }

  /**
   * Append a new upload.
   * @param params
   * @param files
   * @private
   */
  function __appendHtml5Upload(params, files) {

    // Verify that upload is possible
    if (__verifyUploadIsPossible(params, files.length)) {

      if (!params.options.multiple) {
        params._api.reset();
      }

      setTimeout(function() {
        // One sending per file
        $(files).each(function(index, file) {
          if (!params.options.multiple && index > 0) {
            // One file only
            return;
          }

          const uploadCommons = __performAppendUploadCommons(params, [file]);

          // Perform uploads
          __appendUpload(params, new __UploadHandler(uploadCommons.uploadContext,
              uploadCommons.$waitingEndOfUploadContainer, null, file));
        });
      }, 0);
    }
    return true;
  }

  /**
   * Verify that upload is possible. This method handles the UI according to the result of the
   * verify.
   * @param params
   * @param nbFilesToSend
   * @return {boolean}
   * @private
   */
  function __verifyUploadIsPossible(params, nbFilesToSend) {
    if (params.options.nbFileLimit > 0) {
      const nbPossibleFileSends = __getNbPossibleFileSends(params);
      if (nbPossibleFileSends < nbFilesToSend) {
        notyError(__getUploadLimitWarningMessage(params));
        return false;
      } else {
        params.$container.trigger(EVENT.FILES_TO_SEND, nbFilesToSend);
      }
    }
    return true;
  }

  /**
   * Compute common data for upload.
   * @param params
   * @param files
   * @private
   */
  function __performAppendUploadCommons(params, files) {

    // Gets a new upload identifier
    const uploadContext = __buildUploadContext(params);

    // Nb files in upload
    uploadContext.nbFiles = 1;
    if (isFileAPI && files) {
      uploadContext.nbFiles = files.length;
    }

    // Prepare waiting message
    let waitingMessage = params.options.labels.sendingFile.replace(/@name@/, (
            (isFileAPI && files) ? files[0].name : files.val()));
    if (isFileAPI && uploadContext.nbFiles > 1) {
      waitingMessage =
              params.options.labels.sendingFiles.replace(/@number@/, uploadContext.nbFiles);
    }
    const $waitingEndOfUploadContainer = $('<div>').addClass('inlineMessage-waiting').append(waitingMessage);

    // Adding to the DOM the waiting message
    params.$waitingUploadList.append($waitingEndOfUploadContainer);

    // Build iuploaded file UI
    uploadContext.fileUI = new __FileUI(params);
    uploadContext.$uploadedFileList.append(uploadContext.fileUI.getContainer());

    // Results
    return  {
      uploadContext: uploadContext,
      $waitingEndOfUploadContainer: $waitingEndOfUploadContainer
    };
  }

  /**
   * Treatment that has to be called after that the type of upload is set.
   * @param params
   * @param uploadHandler
   * @private
   */
  function __appendUpload(params, uploadHandler) {
    params.$uploadedFileList.trigger(EVENT.SEND_FILE, uploadHandler);
  }

  /**
   * Compute the number of possible send to append.
   * @param params
   * @private
   */
  function __getNbPossibleFileSends(params) {

    // Total number of files
    const nbFiles = params.$uploadedFileList.children().length;

    // Result
    return params.options.nbFileLimit - nbFiles;
  }

  /*
   #################
   Upload management.
   #################
   */

  /**
   * Object that handle an upload.
   * @private
   */
  function __UploadHandler(uploadContext, $waitingEndOfUploadContainer, $formUploadContainer,
          file) {
    const self = this;
    let xhr = null;

    /**
     * Aborts the send.
     */
    this.abort = function() {
      if (xhr) {
        if (xhr && xhr.abort) {
          xhr.abort();
          window.console &&
                  window.console.log('Silverpeas File Upload JQuery Plugin - INFO - File sending aborted successfully');
          xhr = null;
        }
      }
    };

    /**
     * Handles the send of files.
     */
    this.send = function() {
      // Sending
      if ($formUploadContainer) {
        __renderUploadFile(uploadContext, self, $(":file", $formUploadContainer).val(),
                $waitingEndOfUploadContainer);
        // HTML4 upload way (use of jquery-iframe-transport.js plugin)
        applyTokenSecurity('#' + uploadContext.containerOriginId);
        $("form", $formUploadContainer).iframeAjaxFormSubmit({
          sendFilesOnly: false,
          complete: function(uploadedFilesAsJson) {
            $formUploadContainer.remove();
            self.sendComplete(uploadedFilesAsJson);
            __triggerUploadedListChanged(uploadContext);
          },
          error: function(errorThrown) {
            uploadContext.fileUI.getContainer().trigger(EVENT.DELETE_FILE);
            window.console &&
                    window.console.log(('Silverpeas File Upload JQuery Plugin - ERROR - ' + (
                    errorThrown && errorThrown.length > 0 ? errorThrown :
                    'Maybe due to an upload of a wrong type of file...')));
          }
        }).submit();
      } else if (isFileAPI) {
        // HTML5 upload way
        let xhr = new XMLHttpRequest();
        if (xhr.upload && file) {
          __renderUploadFile(uploadContext, self, file.name, $waitingEndOfUploadContainer);
          xhr.onload = function() {
            notySetupRequestComplete.call(this, xhr);
            if (xhr.status === 200) {
              self.sendFile(file);
            } else {
              xhr.onerror.call(this);
            }
          };

          // Error
          xhr.onerror = function() {
            uploadContext.fileUI.getContainer().trigger(EVENT.DELETE_FILE);
            xhr = null;
          };

          xhr.open("POST", uploadContext.uploadUrl + '/verify', true);
          const data = new FormData();
          data.append('fullPath', file.name);
          data.append('name', file.name);
          data.append('size', file.size);
          xhr.send(data);
        }
      } else {
        console.error('Technical error ...');
      }
    };

    // HTML5 upload way
    this.sendFile = function(file) {
      xhr = new XMLHttpRequest();

      // End of the upload
      xhr.onload = function() {
        notySetupRequestComplete.call(this, xhr);
        try {
          const uploadedFilesData = $.parseJSON($(this.responseText).html());
          self.sendComplete(uploadedFilesData);
          __triggerUploadedListChanged(uploadContext);
        } catch (e) {
          xhr.onerror.call(this);
        }
      };

      // Progress informations
      const $loadInfo = $('<span>').appendTo($waitingEndOfUploadContainer);
      const $loadBar = $('<div>').addClass('progress-bar').appendTo($waitingEndOfUploadContainer);
      xhr.upload.addEventListener("progress", function(event) {
        const ratio = event.loaded / event.total;
        const percentage = parseInt(ratio * 100);
        $loadInfo.empty();
        $loadInfo.append(' (').append(percentage).append('%)');
        $loadBar.height($waitingEndOfUploadContainer.height());
        $loadBar.width(parseInt($waitingEndOfUploadContainer.width() * ratio));
      }, false);

      // Error
      xhr.onerror = function() {
        uploadContext.fileUI.getContainer().trigger(EVENT.DELETE_FILE);
        window.console &&
                window.console.log(('Silverpeas File Upload JQuery Plugin - ERROR - ' + (
                this.responseText && this.responseText.length > 0 ? this.responseText :
                'Maybe due to an upload of a wrong type of file...')));
        xhr = null;
      };

      // Start upload
      xhr.open("POST", uploadContext.uploadUrl, true);
      if (uploadContext.options.componentInstanceId) {
        xhr.setRequestHeader('X-COMPONENT-INSTANCE-ID', uploadContext.options.componentInstanceId);
      }
      xhr.setRequestHeader('Content-Type', 'application/octet-stream');
      xhr.setRequestHeader('X-FULL-PATH', encodeURIComponent(file.name));
      xhr.send(file);
    };

    // Upload complete
    this.sendComplete = function(uploadedFiles) {
      xhr = null;
      __removeWaiting($waitingEndOfUploadContainer);
      $(uploadedFiles).each(function(index, file) {
        __renderUploadedFile(uploadContext, file);
      });
    };
  }

  /**
   * Centralizes the remove of the waiting UI.
   * @param $waitingEndOfUploadContainer
   * @private
   */
  function __removeWaiting($waitingEndOfUploadContainer) {
    if ($waitingEndOfUploadContainer) {
      $waitingEndOfUploadContainer.remove();
    }
  }

  /**
   * Indicates via an event that the uploaded file list has changed.
   * @param uploadContext
   * @private
   */
  function __triggerUploadedListChanged(uploadContext) {
    uploadContext.$container.trigger(EVENT.UPLOADED_FILE_CHANGED);
  }

  /**
   * Centralizes the render an uploaded file.
   * @param uploadContext
   * @param uploadHandler
   * @param fileName
   * @param $waitingEndOfUploadContainer
   * @private
   */
  function __renderUploadFile(uploadContext, uploadHandler, fileName,
          $waitingEndOfUploadContainer) {
    uploadContext.fileUI.setUploadFileData({
      uploadHandler: uploadHandler,
      fileName: fileName,
      $waitingEndOfUploadContainer: $waitingEndOfUploadContainer
    });
  }

  /**
   * Centralizes the render an uploaded file.
   * @param uploadContext
   * @param file
   * @private
   */
  function __renderUploadedFile(uploadContext, file) {
    uploadContext.fileUI.setUploadedFileData(file);
  }

  /**
   * Handle the UI of a file.
   * @param params
   * @private
   */
  function __FileUI(params) {
    const self = this;

    $('fieldset#' + params.containerOriginId).css('height', 'auto');
    const $file = $('<div>').addClass('uploaded-file');
    const $fileDetails = $('<div>').addClass('details');
    const $fileInfos = $('<div>').addClass('infos');
    const $fileTitleInfo = $('<div>').addClass('infos-title');
    if (!params.options.infoInputs) {
      $fileInfos.css("display", "none");
    }
    const $fileDescriptionInfo = $('<div>').addClass('infos-description');
    $file.append($fileDetails, $fileInfos.append($fileTitleInfo, $fileDescriptionInfo));

    let uploadHandler = null;
    let $waitingEndOfUploadContainer = null;
    let uploadedFileData = null;

    // Header - details
    $file.on(EVENT.DELETE_FILE, function() {
      __removeWaiting($waitingEndOfUploadContainer);
      if (uploadHandler) {
        uploadHandler.abort();
      }
      if (uploadedFileData) {
        __deleteFile(uploadedFileData.uploadSessionId);
      }
      $file.fadeOut(200, function() {
        $file.remove();
        __triggerUploadedListChanged(params);
      });
    });
    $fileDetails.append($('<span>'));
    const $deleteAction = $('<a>').attr('href', '#').addClass('delete-file').append($('<img>', {
      title : params.options.labels.deleteFile,
      alt : params.options.labels.deleteFile,
      src : webContext + '/util/icons/cross.png'
    })).click(function() {
      $file.trigger(EVENT.DELETE_FILE);
      return false;
    });
    $fileDetails.append($deleteAction.hide());

    // Body - title and description
    const dummyBaseId = new Date().getMilliseconds();
    const $fileTitle = $('<input>', {
      type : 'text',
      id : dummyBaseId + '-title',
      name : dummyBaseId + '-title',
      maxLength : 150,
      length : 40,
      placeholder : params.options.labels.title
    });
    if (!isFileAPI) {
      $fileTitleInfo.append($('<label>').attr('for',
              dummyBaseId + '-title').addClass('txtlibform').append(params.options.labels.title +
              "<br/>"));
    }
    $fileTitleInfo.append($fileTitle);
    const $fileDescription = $('<textarea>', {
      id : dummyBaseId + '-description',
      name : dummyBaseId + '-description',
      rows : 2,
      cols : 40,
      placeholder : params.options.labels.description
    });
    if (!isFileAPI) {
      $fileDescriptionInfo.append($('<label>').attr('for', dummyBaseId +
              '-description').addClass('txtlibform').append(params.options.labels.description +
              "<br/>"));
    }
    $fileDescriptionInfo.append($fileDescription);

    /**
     * Gets the DOM container.
     * @return {*}
     */
    this.getContainer = function() {
      return $file;
    };

    /**
     * Sets the file icon.
     * @param iconUrl
     */
    this.setFileIcon = function(iconUrl) {
      let $img = $('img.file-icon', $fileDetails);
      if ($img.length === 0) {
        $img = $('<img>').attr('alt', '').addClass('file-icon').prependTo($fileDetails);
      }
      $img.attr('src', iconUrl);
    };

    /**
     * The file informations from the beginning of a file upload.
     * @param file
     */
    this.setUploadFileData = function(file) {
      uploadHandler = file.uploadHandler;
      $waitingEndOfUploadContainer = file.$waitingEndOfUploadContainer;
      $('span', $fileDetails).html(file.fileName);
      self.setFileIcon(webContext + '/util/icons/uploading.gif');
      $deleteAction.hide().show();
    };

    /**
     * The file informations from an successful file upload.
     * @param file
     */
    this.setUploadedFileData = function(file) {
      uploadedFileData = file;

      // Hidden technical input
      const $input = $('<input>', {
        type: 'hidden',
        name: 'uploaded-file-' + uploadedFileData.uploadSessionId,
        value: uploadedFileData.uploadSessionId
      });
      $input.data('uploadedFileData', uploadedFileData);
      $file.prepend($input);

      // Header - details
      self.setFileIcon(uploadedFileData.iconUrl);
      $('span', $fileDetails).empty().append(uploadedFileData.name, ' - ',
              uploadedFileData.formattedSize);

      // Body - title and description
      $fileTitle.attr('id', uploadedFileData.uploadSessionId + '-title');
      $fileTitle.attr('name', uploadedFileData.uploadSessionId + '-title');
      if (!isFileAPI) {
        $('label', $fileTitleInfo).attr('for', uploadedFileData.uploadSessionId + '-title');
      }
      $fileDescription.attr('id', uploadedFileData.uploadSessionId + '-description');
      $fileDescription.attr('name', uploadedFileData.uploadSessionId + '-description');
      if (!isFileAPI) {
        $('label', $fileDescriptionInfo).attr('for', uploadedFileData.uploadSessionId + '-description');
      }
    };
  }

  /**
   * Gets the number of files being transfer.
   * @param params
   * @private
   */
  function __getNbFilesBeingTransfer(params) {
    return params.$waitingUploadList.children().length;
  }

  /**
   * Build a context for an upload (of one or several files).
   * @param params
   * @private
   */
  function __buildUploadContext(params) {
    const uploadId = params.containerOriginId + '-' + params.uploadCount;
    const uploadContext = $.extend({
      uploadId : uploadId,
      formId : 'form-' + uploadId,
      inputId : 'input-' + uploadId,
      uploadUrl : webContext + '/services/fileUpload'
    }, params);
    params.uploadCount++;
    return uploadContext;
  }

  /**
   * Delete a file on the server from its identifier obtained after a successful upload.
   * @return {boolean}
   * @private
   */
  function __deleteFile(uploadSessionId) {
    $.ajax({
      url: webContext + '/services/fileUpload',
      type: 'DELETE',
      cache: false,
      headers : {
        "X-UPLOAD-SESSION" : uploadSessionId
      },
      error: function(jqXHR, textStatus, errorThrown) {
        window.console &&
                window.console.log('Silverpeas File Upload JQuery Plugin - ERROR - ' + errorThrown);
      }
    });
    return true;
  }

  /**
   * Build options.
   * @param options
   * @return {*}
   * @private
   */
  function __buildOptions(options) {
    const aggregatedOptions = {
      componentInstanceId : undefined,
      multiple : true,
      dragAndDropDisplay : true,
      dragAndDropDisplayIcon : true,
      infoInputs : true,
      jqueryFormSelector : '',
      nbFileLimit : 0,
      labels : {
        browse : '',
        chooseFile : '',
        chooseFiles : '',
        dragAndDropFile : '',
        dragAndDropFiles : '',
        sendingFile : '',
        sendingFiles : '',
        sendingWaitingWarning : '',
        limitFileWarning : '',
        limitFilesWarning : '',
        limitFileReached : '',
        limitFilesReached : '',
        title : '',
        description : '',
        deleteFile : ''
      }
    };
    const _options = $.extend(aggregatedOptions, options);
    if (!isFileAPI) {
      _options.multiple = false;
      _options.dragAndDropDisplay = false;
    }
    return _options;
  }

  /**
   * Build a context.
   * @param params
   * @param options
   * @return {{options: *}}
   * @private
   */
  function __buildContext(params, options) {
    const context = {
      options : options
    };
    params.options = options;
    params.context = context;
    return context;
  }

})(jQuery);