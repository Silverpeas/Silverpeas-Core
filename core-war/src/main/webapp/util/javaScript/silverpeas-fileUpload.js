/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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

(function($) {

  // Check for the various File API support.
  var isFileAPI = window.File;

  // Web Context
  if (!webContext) {
    var webContext = '/silverpeas';
  }

  // Event definitions
  var EVENT = {
    SEND_FILE: 'SEND_REQUEST',
    FILES_TO_SEND: 'FILES_TO_SEND',
    UPLOADED_FILE_CHANGED: 'UPLOADED_FILE_CHANGED',
    DELETE_FILE: 'DELETE_FILE'
  };

  /**
   * The different fileUpload methods handled by the plugin.
   */
  var methods = {
    /**
     * Prepare UI and behavior
     */
    init: function(options) {
      return __init($(this), options);
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
      alert("Silverpeas Popup JQuery Plugin is required.");
      return false;
    }

    if (!window.notyError) {
      alert("Silverpeas Notifier JQuery Plugin is required.");
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
      var $target = $(this);
      var $container = $('<div>').addClass('fileUpload-container');
      $target.append($container);

      var params = {
        $container: $container,
        containerOriginId: $target.attr('id'),
        uploadCount: 0
      };

      // Options
      var _options = __buildOptions(options);

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
    var $uploadLimitBloc = $('<div>').addClass('upload-limit').append($('<span>').addClass('legendeFileUpload').append(__getUploadLimitReachedMessage(params))).hide();
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
    var message = params.options.labels.limitFileWarning;
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
    var message = params.options.labels.limitFileReached;
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
    var $uploadBloc = $('<div>').addClass('actions');
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
    var $list = $('<div>').addClass('waiting-list');
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
    var $list = $('<div>').addClass('uploaded-file-list');
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

    var chooseFileLabel = params.options.labels.chooseFiles;
    if (!params.options.multiple) {
      chooseFileLabel = params.options.labels.chooseFile;
    }

    // File
    $uploadBloc.empty();
    var $img = $('<div>').append($('<img>', {
      title: chooseFileLabel,
      alt: chooseFileLabel,
      src: webContext + '/util/icons/create-action/addFile.png'
    })).addClass('icon');
    var $fileInputs = $('<div>').addClass('fileinputs').addClass('input');
    var $form = $('<form>', {
      id: 'form-' + params.containerOriginId,
      method: 'post',
      action: '#'
    });
    var $fileInput = $('<input>', {
      multiple: params.options.multiple,
      type: 'file',
      size: 40
    }).addClass('dragAndDrop');
    $form.append($fileInput);
    $fileInputs.append($form);
    if (isFileAPI && params.options.dragAndDropDisplay) {
      $fileInput.css('position', 'absolute');
      $fileInput.css('top', '-10000px');
      $fileInput.css('right', '0');
      $img.addClass('dng');
      $uploadBloc.addClass('dng');
      var $buttonInput = $('<div>').addClass('milieuBoutonV5').addClass('button').append($('<a>').append(params.options.labels.browse).click(function() {
        $fileInput.click();
        return false;
      }));
      var $areaInput = $('<div>').addClass('droparea').append($('<span>').append((
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
        var files = __extractFilesFromEvent(event);
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
    var files = event.target.files;
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

      var uploadCommons = __performAppendUploadCommons(params, $inputOfFiles);
      var $fileUploadContainer = $('<div>', {
        id: uploadCommons.uploadContext.uploadId
      });
      var $form = $('<form>', {
        id: uploadCommons.uploadContext.formId,
        method: 'post',
        action: uploadCommons.uploadContext.uploadUrl
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

      // One sending per file
      $(files).each(function(index, file) {
        var uploadCommons = __performAppendUploadCommons(params, [file]);

        // Perform uploads
        __appendUpload(params, new __UploadHandler(uploadCommons.uploadContext,
                uploadCommons.$waitingEndOfUploadContainer, null, file));
      });
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
      var nbPossibleFileSends = __getNbPossibleFileSends(params);
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
    var uploadContext = __buildUploadContext(params);

    // Nb files in upload
    uploadContext.nbFiles = 1;
    if (isFileAPI && files) {
      uploadContext.nbFiles = files.length;
    }

    // Prepare waiting message
    var waitingMessage = params.options.labels.sendingFile.replace(/@name@/, (
            (isFileAPI && files) ? files[0].name : files.val()));
    if (isFileAPI && uploadContext.nbFiles > 1) {
      waitingMessage =
              params.options.labels.sendingFiles.replace(/@number@/, uploadContext.nbFiles);
    }
    var $waitingEndOfUploadContainer = $('<div>').addClass('inlineMessage-waiting').append(waitingMessage);

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
    var nbFiles = params.$uploadedFileList.children().length;

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
    var self = this;
    var xhr = null;

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
        var xhr = new XMLHttpRequest();
        if (xhr.upload && file) {
          __renderUploadFile(uploadContext, self, file.name, $waitingEndOfUploadContainer);
          xhr.onload = function() {
            notySetupRequestComplete.call(this, xhr);
            if (xhr.status == 200) {
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
          var data = new FormData();
          data.append('fullPath', file.name);
          data.append('name', file.name);
          data.append('size', file.size);
          xhr.send(data);
        }
      } else {
        alert('Technical error ...');
      }
    };

    // HTML5 upload way
    this.sendFile = function(file) {
      xhr = new XMLHttpRequest();

      // End of the upload
      xhr.onload = function() {
        notySetupRequestComplete.call(this, xhr);
        try {
          var uploadedFilesData = $.parseJSON($(this.responseText).html());
          self.sendComplete(uploadedFilesData);
          __triggerUploadedListChanged(uploadContext);
        } catch (e) {
          xhr.onerror.call(this);
        }
      };

      // Progress informations
      var $loadInfo = $('<span>').appendTo($waitingEndOfUploadContainer);
      var $loadBar = $('<div>').addClass('progress-bar').appendTo($waitingEndOfUploadContainer);
      xhr.upload.addEventListener("progress", function(event) {
        var ratio = event.loaded / event.total;
        var percentage = parseInt(ratio * 100);
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
    var self = this;

    $('fieldset#' + params.containerOriginId).css('height', 'auto');
    var $file = $('<div>').addClass('uploaded-file');
    var $fileDetails = $('<div>').addClass('details');
    var $fileInfos = $('<div>').addClass('infos');
    var $fileTitleInfo = $('<div>').addClass('infos-title');
    var $fileDescriptionInfo = $('<div>').addClass('infos-description');
    $file.append($fileDetails, $fileInfos.append($fileTitleInfo, $fileDescriptionInfo));

    var uploadHandler = null;
    var $waitingEndOfUploadContainer = null;
    var uploadedFileData = null;

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
    var $deleteAction = $('<a>').attr('href', '#').addClass('delete-file').append($('<img>', {
      title: params.options.labels.deleteFile,
      alt: params.options.labels.deleteFile,
      src: webContext + '/util/icons/cross.png'
    })).click(function() {
      $file.trigger(EVENT.DELETE_FILE);
      return false;
    });
    $fileDetails.append($deleteAction.hide());

    // Body - title and description
    var dummyBaseId = new Date().getMilliseconds();
    var $fileTitle = $('<input>', {
      type: 'text',
      id: dummyBaseId + '-title',
      name: dummyBaseId + '-title',
      maxLength: 150,
      size: 40,
      placeholder: params.options.labels.title
    });
    if (!isFileAPI) {
      $fileTitleInfo.append($('<label>').attr('for',
              dummyBaseId + '-title').addClass('txtlibform').append(params.options.labels.title +
              "<br/>"));
    }
    $fileTitleInfo.append($fileTitle);
    var $fileDescription = $('<textarea>', {
      id: dummyBaseId + '-description',
      name: dummyBaseId + '-description',
      rows: 2,
      cols: 40,
      placeholder: params.options.labels.description
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
      var $img = $('img.file-icon', $fileDetails);
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
      $file.prepend($('<input>', {
        type: 'hidden',
        name: 'uploaded-file-' + uploadedFileData.uploadSessionId,
        value: uploadedFileData.uploadSessionId
      }));

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
    var uploadId = params.containerOriginId + '-' + params.uploadCount;
    var uploadContext = $.extend({
      uploadId: uploadId,
      formId: 'form-' + uploadId,
      inputId: 'input-' + uploadId,
      uploadUrl: webContext + '/services/fileUpload'
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
    var agregatedOptions = {
      multiple: true,
      dragAndDropDisplay: true,
      jqueryFormSelector: '',
      nbFileLimit: 0,
      labels: {
        browse: '',
        chooseFile: '',
        chooseFiles: '',
        dragAndDropFile: '',
        dragAndDropFiles: '',
        sendingFile: '',
        sendingFiles: '',
        sendingWaitingWarning: '',
        limitFileWarning: '',
        limitFilesWarning: '',
        limitFileReached: '',
        limitFilesReached: '',
        title: '',
        description: '',
        deleteFile: ''
      }
    };
    var _options = $.extend(agregatedOptions, options);
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
    var context = {
      options: options
    };
    params.options = options;
    params.context = context;
    return context;
  }

})(jQuery);