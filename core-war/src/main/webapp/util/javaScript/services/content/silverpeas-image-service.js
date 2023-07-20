/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

  window.ImageService = function() {
    const attachmentRepository = new ImageAttachmentRepository();

    /**
     * Gets image attachment from its src URL.
     * @param attachmentSrc an src URL.
     * @returns {*}
     */
    this.getImageAttachmentsBySrc = function(attachmentSrc) {
      return attachmentRepository.getBySrc(attachmentSrc);
    }

    /**
     * Deletes an image attachment.
     * @param attachment an attachment object.
     * @returns {*}
     */
    this.deleteImageAttachment = function(attachment) {
      return attachmentRepository.deleteAttachment(attachment);
    }

    /**
     * Deletes all the image attachments linked to a contribution.
     * @param contributionId a contribution identifier.
     * @returns {*}
     */
    this.deleteAllImageAttachments = function(contributionId) {
      return attachmentRepository.deleteAllAttachments(contributionId);
    }

    /**
     * Gets all image attachments linked to a contribution.
     * @param contributionId a contribution identifier.
     * @returns {*}
     */
    this.getAllImageAttachmentsByContributionId = function(contributionId) {
      return attachmentRepository.getAllByContributionId(contributionId);
    }
  };

  const ImageAttachment = function() {
    let preview;
    this.type = 'ImageAttachment';
    this.getPreview = function() {
      if (!preview) {
        const previewUrl = webContext + "/services/preview/" + this.instanceId + "/attachment/" + this.id;
        return sp.ajaxRequest(previewUrl).withParam('lang', this.lang).sendAndPromiseJsonResponse().then(function(p) {
          preview = p;
          return p;
        });
      }
      return sp.promise.resolveDirectlyWith(preview);
    };
    this.getFullDownloadUrl = function() {
      return webContext + this.downloadUrl;
    };
  };

  const ImageAttachmentRepository = function() {
    const baseUri = webContext + "/services/";
    const baseAdapter = RESTAdapter.get(baseUri, ImageAttachment);

    /**
     * Deletes an attachment.
     * @param attachment an attachment object.
     * @returns {*}
     */
    this.deleteAttachment = function(attachment) {
      return __deleteDocument({
        instanceId : attachment.instanceId,
        localId : attachment.id
      });
    }

    /**
     * Deletes all image attachments linked to a contribution.
     * @param contributionId a contribution identifier.
     * @returns {*}
     */
    this.deleteAllAttachments = function(contributionId) {
      return this.getAllByContributionId(contributionId).then(function(attachements) {
        const __queue = sp.promise.newQueue();
        let promise = sp.promise.resolveDirectlyWith();
        attachements.forEach(function(attachment) {
          promise = __queue.push(function() {
            return this.deleteAttachment(attachment);
          }.bind(this));
        }.bind(this))
        return promise;
      }.bind(this));
    }

    /**
     * Gets image attachment from its src URL.
     * @param attachmentSrc an src URL.
     * @returns {*}
     */
    this.getBySrc = function(attachmentSrc) {
      const resourceId = {};
      const pattern = /^.+[/]attached_file[/]componentId[/]([^/]+)[/]attachmentId[/]([^/]+).+$/g;
      const match = pattern.exec(attachmentSrc);
      if (match && match.length === 3) {
        resourceId.instanceId = match[1];
        resourceId.localId = match[2];
        return __getDocument(resourceId);
      }
      return sp.promise.resolveDirectlyWith();
    }

    /**
     * Gets all image attachments linked to a contribution.
     * @param contributionId a contribution identifier.
     * @returns {*}
     */
    this.getAllByContributionId = function(contributionId) {
      return __listDocuments(contributionId);
    }

    const __deleteDocument = function(resourceId) {
      const urlSuffix = "documents/" + resourceId.instanceId + "/document/" + resourceId.localId;
      return baseAdapter['delete'](baseAdapter.url + urlSuffix, {});
    };

    const __getDocument = function(resourceId, criteria) {
      const urlSuffix = "documents/" + resourceId.instanceId + "/document/" + resourceId.localId + "/fr";
      return __find(baseAdapter.url + urlSuffix, criteria);
    };

    const __listDocuments = function(resourceId, criteria) {
      const urlSuffix = "documents/" + resourceId.instanceId + "/resource/" + resourceId.localId + "/types/image/fr";
      return __find(baseAdapter.url + urlSuffix, criteria);
    };

    const __find = function(url, criteria) {
      return baseAdapter.find({
        url : url,
        criteria : baseAdapter.criteria(extendsObject({}, criteria))
      });
    };
  };
})();
