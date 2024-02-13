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

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/content/silverpeas-document-template-templates.jsp');

  SpVue.component('silverpeas-document-template-input',
      templateRepository.get('input', {
        mixins : [VuejsFormInputMixin],
        props : {
          componentInstanceId : {
            'type': String,
            'default': undefined
          },
          inputName : {
            'type' : String,
            'default' : 'documentTemplateId'
          }
        },
        data : function() {
          return {
            selectedId : undefined,
            selectedDocumentTemplate : undefined,
            selectPopinApi : undefined,
            documentTemplates : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            getSelectedDocumentTemplate : function() {
              return this.selectedDocumentTemplate;
            },
            clear : function() {
              this.clear();
            },
            /**
             * Mandatory implementation needed by {@link VuejsFormInputMixin}.
             * @returns {boolean}
             */
            validateFormInput : function() {
              let isError = false;
              if (this.isMandatory && !this.selectedDocumentTemplate) {
                isError = true;
                this.rootFormApi.errorMessage().add(
                    this.formatMessage(this.rootFormMessages.mandatory,
                        this.getLabelByForAttribute(this.id)));
              }
              return !isError;
            }
          });
        },
        mounted : function() {
          this.loadDocumentTemplates();
        },
        methods : {
          getInputElementName : function() {
            // this method is used by silverpeas-form-pane APIs
          },
          loadDocumentTemplates : function() {
            this.service.listDocumentTemplates().then(function(documentTemplates) {
              this.documentTemplates = documentTemplates;
            }.bind(this));
          },
          clear : function() {
            this.selectedId = undefined;
            this.selectedDocumentTemplate = undefined;
          },
          selectDocumentTemplate : function(documentTemplate) {
            this.selectedId = documentTemplate.getId();
            this.selectedDocumentTemplate = documentTemplate;
            this.closeSelection();
          },
          openSelection : function() {
            this.selectPopinApi.open({
              forceBlurFirstElementOnOpen : true
            });
          },
          closeSelection : function() {
            this.selectPopinApi.close();
          }
        },
        computed : {
          service : function() {
            return new DocumentTemplateService(this.componentInstanceId);
          }
        },
        watch : {
          'componentInstanceId' : function() {
            this.loadDocumentTemplates();
          }
        }
      }));

  SpVue.component('document-template',
      templateRepository.get('document-template', {
        emits : ['select', 'unselect'],
        props : {
          documentTemplate : {
            'type' : Object,
            'required' : true
          },
          unselect : {
            'type' : Boolean,
            'default' : false
          },
          selectLabel : {
            'type' : String,
            'default' : ''
          }
        },
        methods : {
          viewDocumentTemplate : function(service) {
            jQuery(this.$el.querySelector('.actions .' + service + "-button"))[service]("document", {
              documentId: this.documentTemplate.getId(),
              documentType: this.documentTemplate.getPreviewDocumentType(),
              lang: currentUser.language,
              noNavigation : true
            });
          }
        },
        computed : {
          title : function() {
            return this.documentTemplate.getName().noHTML().convertNewLineAsHtml();
          },
          description : function() {
            return this.documentTemplate.getDescription().noHTML().convertNewLineAsHtml();
          }
        }
      }));
})();
