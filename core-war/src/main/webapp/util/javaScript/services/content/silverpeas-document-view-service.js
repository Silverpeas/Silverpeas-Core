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

(function() {

  window.ViewService = function() {
    const documentPreviewRepository = new DocumentPreviewRepository();
    const documentViewRepository = new DocumentViewRepository();

    /**
     * Gets document preview from given data.
     * @param attachmentId identifier of an attachment.
     * @param componentInstanceId identifier of a component instance.
     * @param contentLanguage the content language into which the attachment is requested.
     * @returns {*}
     */
    this.getDocumentPreview = function(attachmentId, componentInstanceId, contentLanguage) {
      return documentPreviewRepository.get(attachmentId, componentInstanceId, contentLanguage);
    }

    /**
     * Gets document view from given data.
     * @param attachmentId identifier of an attachment.
     * @param componentInstanceId identifier of a component instance.
     * @param contentLanguage the content language into which the attachment is requested.
     * @returns {*}
     */
    this.getDocumentView = function(attachmentId, componentInstanceId, contentLanguage) {
      return documentViewRepository.get(attachmentId, componentInstanceId, contentLanguage);
    }
  };

  const DocumentPreview = function() {
    this.type = 'DocumentPreview';
    this.getImgUrl = function() {
      return this.url;
    };
    this.getTitle = function() {
      return this.originalFileName;
    };
    this.getWidth = function() {
      return this.width;
    };
    this.getHeight = function() {
      return this.height;
    };
  };

  const DocumentPreviewRepository = function() {
    const baseUri = webContext + "/services/preview/";
    const baseAdapter = RESTAdapter.get(baseUri, DocumentPreview);

    /**
     * Gets document preview from given data.
     * @param attachmentId identifier of an attachment.
     * @param componentInstanceId identifier of a component instance.
     * @param contentLanguage the content language into which the attachment is requested.
     * @returns {*}
     */
    this.get = function(attachmentId, componentInstanceId, contentLanguage) {
      if (attachmentId && componentInstanceId) {
        const urlSuffix = componentInstanceId + "/attachment/" + attachmentId;
        return baseAdapter.find({
          url : baseAdapter.url + urlSuffix,
          criteria : baseAdapter.criteria({
            "lang" : contentLanguage
          })
        });
      }
      return sp.promise.resolveDirectlyWith();
    }
  };

  const DocumentView = function() {
    this.type = 'DocumentView';
    this.getViewerUrl = function() {
      return this.viewerUri;
    };
    this.getTitle = function() {
      return this.originalFileName;
    };
    this.getWidth = function() {
      return this.width;
    };
    this.getHeight = function() {
      return this.height;
    };
  };

  const DocumentViewRepository = function() {
    const baseUri = webContext + "/services/view/";
    const baseAdapter = RESTAdapter.get(baseUri, DocumentView);

    /**
     * Gets document view from given data.
     * @param attachmentId identifier of an attachment.
     * @param componentInstanceId identifier of a component instance.
     * @param contentLanguage the content language into which the attachment is requested.
     * @returns {*}
     */
    this.get = function(attachmentId, componentInstanceId, contentLanguage) {
      if (attachmentId && componentInstanceId) {
        const urlSuffix = componentInstanceId + "/attachment/" + attachmentId;
        return baseAdapter.find({
          url : baseAdapter.url + urlSuffix,
          criteria : baseAdapter.criteria({
            "lang" : contentLanguage
          })
        });
      }
      return sp.promise.resolveDirectlyWith();
    }
  };
})();
