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
      '/util/javaScript/vuejs/components/content/silverpeas-image-selector-templates.jsp');

  SpVue.component('silverpeas-image-selector',
      templateRepository.get('main', {
        inject : ['context'],
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, VuejsProgressMessageMixin],
        provide : function() {
          return {
            imageService: this.service
          }
        },
        data : function() {
          return {
            contributionId : undefined,
            dragAndDropApi : undefined,
            service : new ImageService(),
            popinApi : undefined,
            imgAttachments : [],
            imageUrlApi : undefined,
            initialSrc : undefined,
            selectedSrc : undefined,
            selectedAttachment : undefined,
            displayMediaBank : false
          };
        },
        created : function() {
          this.contributionId = this.context.contributionId;
          this.extendApiWith({
            open : function(contributionId, options) {
              this.open(contributionId, options);
            },
            updateSelectedImageMedia : function(url) {
              this.updateSelectedImageMedia(url);
            }
          });
        },
        methods : {
          uploadFilesManually : function(e) {
            const fileArray = [];
            Array.prototype.push.apply(fileArray, e.target.files);
            const newFiles = fileArray.filter(function(file) {
              return this.imgAttachments.filter(function(att) {
                return att.fileName === file.name;
              }).length === 0;
            }.bind(this));
            if (newFiles.length > 0) {
              this.dragAndDropApi.sendFilesManually(newFiles);
            }
          },
          whenDragAndDropReady : function() {
            if (!this.dragAndDropApi) {
              return new Promise(function(resolve) {
                const options = {
                  domSelector : this.$refs.ddContainer,
                  componentInstanceId : this.contributionId.instanceId,
                  onCompletedUrl : sp.url.format(webContext + "/DragAndDrop/drop", {
                    'ComponentId' : this.contributionId.instanceId,
                    'ResourceId' : this.contributionId.localId,
                    'IndexIt' : 'false',
                    'DocumentType' : 'image',
                  }),
                  onCompletedUrlSuccess : this.loadImageAttachments
                }
                this.dragAndDropApi = initDragAndDropUploadAndReload(options);
                this.dragAndDropApi.ready(function() {
                  resolve();
                });
              }.bind(this));
            }
            return sp.promise.resolveDirectlyWith();
          },
          loadImageAttachments : function() {
            const contributionId = this.contributionId;
            return this.service.getAllImageAttachmentsByContributionId(contributionId).then(
                function(attachments) {
                  this.imgAttachments = attachments;
                  this.hideProgressMessage();
                }.bind(this), function() {
                  this.hideProgressMessage();
                }.bind(this));
          },
          deleteImageAttachment : function(attachment) {
            jQuery.popup.confirm(this.messages.confirmDeleteMsg, function() {
              this.service.deleteImageAttachment(attachment).then(function() {
                this.loadImageAttachments();
              }.bind(this));
            }.bind(this));
          },
          deleteAllImageAttachments : function() {
            jQuery.popup.confirm(this.messages.confirmDeleteAllMsg, function() {
              this.showProgressMessage();
              const contributionId = this.contributionId;
              this.service.deleteAllImageAttachments(contributionId).then(function() {
                this.loadImageAttachments();
              }.bind(this));
            }.bind(this));
          },
          backToCurrentImage : function() {
            this.selectedAttachment = undefined;
            this.selectedSrc = this.initialSrc;
          },
          updateSelectedImageAttachment : function(attachment) {
            if (this.selectedAttachment !== attachment) {
              this.selectedAttachment = attachment;
              this.selectedSrc = undefined;
            }
          },
          updateSelectedImageMedia : function(url) {
            if (this.selectedSrc !== url) {
              this.selectedAttachment = undefined;
              this.selectedSrc = url;
            }
          },
          open : function(options) {
            if (typeof options.select !== 'function') {
              sp.log.error(
                  'function named select MUST be defined into options. ' +
                  'The method MUST takes as first parameter an image URL');
              return;
            }
            this.initialSrc = undefined;
            this.selectedSrc = undefined;
            this.selectedAttachment = undefined;
            this.imgAttachments = [];
            const promises = [this.whenDragAndDropReady()];
            options = extendsObject({
              currentImageSrc : undefined
            }, options);
            const safeSrc = StringUtil.defaultStringIfNotDefined(options.currentImageSrc);
            if (!safeSrc.startsWith("<svg ") && safeSrc.indexOf("svg+xml") < 0) {
              this.selectedSrc = options.currentImageSrc;
            }
            this.initialSrc = this.selectedSrc;
            promises.push(this.service.getAllImageAttachmentsByContributionId(this.contributionId).then(function(attachments) {
              this.imgAttachments = attachments;
            }.bind(this)));
            sp.promise.whenAllResolved(promises).then(function() {
              const popinOptions = {
                callback : function() {
                  options.select(this.imageUrlApi.getUrl());
                }.bind(this)
              };
              if (typeof options.close === 'function') {
                popinOptions.callbackOnClose = function() {
                  options.close();
                }
              }
              this.popinApi.open(popinOptions);
            }.bind(this));
          },
          close : function() {
            this.popinApi.close();
          }
        },
        computed : {
          isSelectedSrcSameAsInitial : function() {
            return this.selectedSrc === this.initialSrc;
          }
        }
      }));

  SpVue.component('image-url',
      templateRepository.get('image-url', {
        inject : ['imageService'],
        mixins : [VuejsApiMixin],
        emits : ['change'],
        props : {
          currentUrl : {
            'type' : String
          },
          currentAttachment : {
            'type' : Object
          }
        },
        data : function() {
          return {
            url : undefined,
            lastUrl : undefined,
            previewUrl : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            getUrl : function() {
              return this.url;
            }
          });
        },
        methods : {
          loadPreviewUrl : function() {
            const url = this.url
            if (this.lastUrl !== url) {
              this.lastUrl = url;
              this.imageService.getImageAttachmentsBySrc(url).then(function(attachment) {
                if (typeof attachment === 'object') {
                  attachment.getPreview().then(function(preview) {
                    this.previewUrl = preview.url;
                  }.bind(this), function() {
                    this.previewUrl = url;
                  }.bind(this));
                } else {
                  this.previewUrl = url;
                }
              }.bind(this));
            }
          }
        },
        watch : {
          currentUrl : function(newVal) {
            this.url = newVal;
            this.loadPreviewUrl();
          },
          currentAttachment : function(newVal) {
            if (newVal) {
              newVal.getPreview().then(function(preview) {
                this.lastUrl = newVal.getFullDownloadUrl();
                this.url = this.lastUrl;
                this.previewUrl = preview.url;
              }.bind(this));
            }
          },
          url : sp.debounce(function(newVal) {
            this.loadPreviewUrl();
            this.$emit('change', newVal);
          }, 750)
        }
      }));

  SpVue.component('media-bank',
      templateRepository.get('media-bank', {
        inject : ['context'],
        emits : ['loaded'],
        data : function() {
          return {
            mediaApps : undefined,
            mediaWindow : undefined
          };
        },
        mounted : function() {
          sp.ajaxRequest(webContext + '/services/components')
              .withParam('filter', 'imagebanks')
              .sendAndPromiseJsonResponse()
              .then(function(apps) {
                this.mediaApps = apps;
                this.$emit('loaded', this.mediaApps);
              }.bind(this));
        },
        methods : {
          openMediaFileManager : function(mediaApp) {
            let componentId = mediaApp.name + mediaApp.id;
            if (componentId) {
              const url = sp.url.format(webContext + '/gallery/jsp/wysiwygBrowser.jsp', {
                'ComponentId' : componentId,
                'Language' : this.context.currentUser.language
              });
              const windowName = 'mediaWindow';
              const width = '820';
              const height = '600';
              const windowParams = 'directories=0,menubar=0,toolbar=0,alwaysRaised';
              if (this.mediaWindow &&
                  !this.mediaWindow.closed &&
                  this.mediaWindow.name === 'mediaWindow') {
                this.mediaWindow.close();
              }
              this.mediaWindow = SP_openWindow(url, windowName, width, height, windowParams);
            }
          }
        }
      }));

  SpVue.component('image-attachment',
      templateRepository.get('image-attachment', {
        emits : ['select-image', 'delete-image-attachment'],
        props : {
          attachment : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            preview : undefined
          };
        },
        mounted : function() {
          this.attachment.getPreview().then(function(preview) {
            this.preview = preview;
          }.bind(this));
        },
        computed : {
          title : function() {
            return StringUtil.defaultStringIfNotDefined(this.attachment.title, this.attachment.fileName);
          },
          previewUrl : function() {
            if (this.preview) {
              return this.preview.url;
            }
            return false;
          }
        }
      }));
})();
