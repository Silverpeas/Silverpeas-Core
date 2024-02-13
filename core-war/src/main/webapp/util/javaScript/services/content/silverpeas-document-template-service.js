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

  /**
   * Services dedicated to provide into Silverpeas's application instances document template data.
   * These services MUST not be used into context of administration.
   * @param componentInstanceId optional identifier of a component instance. Allows to indicate the
   *     context into which services are used and provide more document templates according to
   *     theirs restrictions.
   * @constructor
   */
  window.DocumentTemplateService = function(componentInstanceId) {
    const documentTemplateRepository = new Repository(componentInstanceId);

    /**
     * Gets document template from its identifier.
     * @param id identifier of a document template.
     * @returns {*}
     */
    this.getDocumentTemplate = function(id) {
      return documentTemplateRepository.get(id);
    }

    /**
     * Lists all document templates.
     * @returns {*}
     */
    this.listDocumentTemplates = function() {
      return documentTemplateRepository.listAll();
    }
  };

  const DocumentTemplate = function() {
    this.type = 'DocumentTemplate';
    this.getId = function() {
      return this.id;
    };
    this.getName = function() {
      return this.name;
    };
    this.getDescription = function() {
      return this.description;
    };
    this.getPreviewUrl = function() {
      return this.preview.url;
    };
    this.getPreviewDocumentType = function() {
      return this.preview.documentType;
    };
  };

  const Repository = function(componentInstanceId) {
    const baseUri = webContext + "/services/documentTemplates/";
    const baseAdapter = RESTAdapter.get(baseUri, DocumentTemplate);
    const __buildParams = function(id) {
      return {
        url : baseAdapter.url + id,
        criteria : baseAdapter.criteria({
          instanceIdFilter : componentInstanceId
        })
      }
    }

    /**
     * Gets document template from its identifier.
     * @param id identifier of a document template.
     * @returns {*}
     */
    this.get = function(id) {
      return baseAdapter.find(__buildParams(id));
    }

    /**
     * Lists all document templates.
     * @returns {*}
     */
    this.listAll = function() {
      return baseAdapter.find(__buildParams(''));
    }
  };
})();
