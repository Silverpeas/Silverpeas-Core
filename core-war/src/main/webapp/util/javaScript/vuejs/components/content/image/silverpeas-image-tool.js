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
      '/util/javaScript/vuejs/components/content/image/silverpeas-image-tool-templates.jsp');

  const HANDLED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];

  const imageBanksPromise = sp.ajaxRequest(webContext + '/services/components')
      .withParam('filter', 'imagebanks')
      .sendAndPromiseJsonResponse()
      .then(function(ims) {
        return ims.map(function(im) {
          im.instanceId = im.name + im.id;
          return im;
        })
      });

  let imageBankWindow = window;

  /**
   * This image file input is compatible with <code>&lt;silverpeas-form-pane&gt;</code> vuejs
   * component.<br/>
   * The model returned is an object which is written but never read by the input. It is
   * somehow used to indicate the input state.<br/>
   * The model structure : <pre>
   *   {
   *      file : the file indicated to the DOM input if any (not filled otherwise),
   *      fileInputName : name of the HTML file input in the DOM (not filled if file is not defined),
   *      imageBankUrl : the url of the image used into an image bank if any,
   *      cropData : the crop data if any, its structure is the same as the cropData prop -> {
   *                previewWidth : the width of the final cropped image (mandatory),
   *                previewHeight : the width of the final cropped image (mandatory),
   *                box : (optional) {
   *                    offsetX : the X offset from top left image corner to start the crop,
   *                    offsetY : the Y offset from top left image corner to start the crop,
   *                    width : the width of the crop into the original image,
   *                    height : the height of the crop into the original image
   *                }
   *             },
   *      deleteOriginal : a boolean with true value if image has been deleted (not filled otherwise)
   *   }
   * </pre>
   */
  SpVue.component('silverpeas-image-file-input',
      templateRepository.get('image-file-input', {
        mixins : [VuejsFormInputMixin],
        props : {
          originalName : {
            'type' : String,
            'default' : undefined
          },
          originalSize : {
            'type' : Number,
            'default' : -1
          },
          displayFileData : {
            'type' : Boolean,
            'default' : false
          },
          previewImageUrl : {
            'type' : String,
            'default' : undefined
          },
          fullImageUrl : {
            'type' : String,
            'default' : undefined
          },
          cropEnabled : {
            'type' : Boolean,
            'default' : false
          },
          name : {
            'type' : String,
            'default' : 'WAIMGVAR0'
          },
          imageBanksEnabled : {
            'type' : Boolean,
            'default' : false
          },
          imageBankFieldName : {
            'type' : String,
            'default' : 'ImageTool'
          },
          cropData : {
            'type' : Object,
            'default' : undefined
          },
          handledImageTypes : {
            'type' : Array,
            'default' : HANDLED_IMAGE_TYPES
          },
          modelValue : {
            'type': Object,
            'default': {}
          }
        },
        data : function() {
          return {
            ready : true,
            loadedImageBanks : undefined,
            imageBankChoiceCallbackSet : [],
            deleteOriginal : false,
            file : undefined,
            fileUrl : undefined,
            imageBankUrl : undefined,
            cropCtx : {
              popin : undefined,
              pane : undefined,
              formEnabled : undefined,
              previewData : undefined
            }
          };
        },
        created : function() {
          this.extendApiWith({
            clear : this.clear,
            getHandledImageMimeTypes : function() {
              return this.handledImageTypes;
            },
            getHandledImageExtensions : function() {
              return this.getHandledImageExtensions();
            },
            checkImageMustBeFilled : function() {
              return this.checkImageMustBeFilled();
            },
            /**
             * Mandatory implementation needed by {@link VuejsFormInputMixin}.
             * @returns {boolean}
             */
            validateFormInput : function() {
              let isError = false;
              if (this.isMandatory && !this.previewUrl) {
                isError = true;
                this.rootFormApi.errorMessage().add(
                    this.formatMessage(this.rootFormMessages.mandatory,
                        this.getLabelByForAttribute(this.id)));
              }
              return !isError;
            }
          });
          imageBanksPromise.then(function(ib) {
            this.ready = true;
            this.loadedImageBanks = ib;
          }.bind(this));
          this.clear();
        },
        methods : {
          clear : function() {
            this.deleteOriginal = false;
            this.file = undefined;
            this.fileUrl = undefined;
            this.imageBankUrl = undefined;
            this.resetCropData();
            this.refreshState();
          },
          getInputElementName : function() {
            // this method is used by silverpeas-form-pane APIs
          },
          updateModel : function() {
            const model = {};
            if (this.file) {
              model.fileInputName = this.name;
              model.file = this.file;
            } else if (this.imageBankUrl) {
              model.imageBankUrl = this.imageBankUrl;
            }
            if (this.cropCtx.previewData) {
              const newCropData = extendsObject({}, this.cropData);
              newCropData.box = this.cropCtx.previewData.box;
              model.cropData = newCropData;
            }
            if(this.deleteOriginal) {
              model.deleteOriginal = this.deleteOriginal;
            }
            this.$emit('update:modelValue', model);
          },
          newImageFile : function() {
            const [imageFile] = this.$refs.newImageFile.files;
            if (imageFile) {
              if (this.checkImageFile(imageFile)) {
                this.deleteOriginal = false;
                this.file = imageFile;
                this.fileUrl = URL.createObjectURL(imageFile);
                this.resetCropData();
              }
              this.refreshState();
            }
          },
          getHandledImageExtensions : function() {
            return this.handledImageTypes.map(function(format) {
              return format.replace(/[^/]*\//g, '');
            });
          },
          checkImageFile : function(imageFile) {
            if (this.handledImageTypes.indexOf(imageFile.type) < 0) {
              SilverpeasError.add(this.formatMessage(this.messages.badFormatErrMsg, [
                  imageFile.name,
                  this.getHandledImageExtensions().joinWith({
                    separator : ', ',
                    lastSeparator : ' ' + this.messages.orMsgPart + ' '
                  })
              ]));
            }
            return !SilverpeasError.show();
          },
          checkImageMustBeFilled : function() {
            return this.mandatory && !this.previewUrl;
          },
          openImageBank : function() {
            let imageBankInstanceId = this.imageBanks[0].instanceId;
            if (this.imageBanks.length > 1) {
              imageBankInstanceId = this.imageBanks[this.$refs.imageBankSelector.selectedIndex - 1].instanceId;
              this.$refs.imageBankSelector.selectedIndex = 0;
            }
            const url = sp.url.format(webContext + '/gallery/jsp/wysiwygBrowser.jsp', {
              'ComponentId' : imageBankInstanceId,
              'Language' : currentUser.language,
              'FieldName' : this.imageBankFieldName
            })
            if (!imageBankWindow.closed && imageBankWindow.name === "imageBankWindow") {
              imageBankWindow.close();
            }
            imageBankWindow = SP_openWindow(url, 'imageBankWindow', "820", "600", 'directories=0,menubar=0,toolbar=0, alwaysRaised');
            if (this.imageBankChoiceCallbackSet.indexOf(this.imageBankFieldName)) {
              this.imageBankChoiceCallbackSet.push(this.imageBankFieldName);
              const __super = window['choixImageInGallery' + this.imageBankFieldName];
              window['choixImageInGallery' + this.imageBankFieldName] = function(url) {
                this.resetCropData();
                this.deleteOriginal = false;
                this.file = undefined;
                this.imageBankUrl = url;
                if (__super) {
                  __super(url);
                }
                this.refreshState();
              }.bind(this);
            }
          },
          goBack : function() {
            if (this.cropCtx.previewData) {
              this.resetCropData();
            } else if (this.file) {
              this.file = undefined;
            } else if (this.imageBankUrl) {
              this.imageBankUrl = undefined;
            }
            this.refreshState();
          },
          deleteImage : function() {
            this.resetCropData();
            this.file = undefined;
            this.imageBankUrl = undefined;
            this.deleteOriginal = true;
            this.refreshState();
          },
          resetCropData : function() {
            this.cropCtx.previewData = undefined;
          },
          cropImage : function() {
            this.cropCtx.popin.open({
              callback : this.cropCtx.pane.validate,
              callbackOnClose : function() {
                this.cropCtx.formEnabled = false;
              }.bind(this)
            });
          },
          refreshState : function() {
            if (this.file) {
              this.imageBankUrl = undefined;
            } else {
              this.fileUrl = undefined;
              if (this.$refs.newImageFile) {
                this.$refs.newImageFile.value = '';
              }
            }
            this.updateModel();
          },
          validateCrop : function(data) {
            const previewData = data.computeCropPreview(this.croppedPreviewWidthAndHeight);
            this.cropCtx.previewData = {
              box : data.cropBox,
              previewCss : previewData.previewCss
            };
            this.updateModel();
          }
        },
        computed : {
          fileName : function() {
            const fileName = this.file ? this.file.name : this.originalName;
            return fileName ? fileName.replace(/.*\/+([^/])/g, '$1') : undefined;
          },
          fileSize : function() {
            return this.file ? this.file.size : this.originalSize;
          },
          humanReadableFileSize : function() {
            return sp.file.humanReadableSize(this.fileSize);
          },
          previewUrl : function() {
            return this.fileUrl || this.imageBankUrl || this.previewImageUrl || this.fullImageUrl;
          },
          isMediaBankPreviewUrl : function() {
            return this.previewUrl.indexOf('/dummy') > 0;
          },
          displayCropAction : function () {
            return this.cropEnabled && this.previewUrl && !this.isMediaBankPreviewUrl;
          },
          displayGoBackAction : function () {
            return this.cropCtx.previewData ||
                ((this.file || this.imageBankUrl) && (this.previewImageUrl || this.fullImageUrl));
          },
          displayDelAction : function () {
            return !this.isMandatory && this.previewUrl;
          },
          acceptedTypes : function () {
            return this.handledImageTypes.join(',');
          },
          imageBanks : function() {
            let ib = [];
            if (this.imageBanksEnabled && this.loadedImageBanks) {
              ib = this.loadedImageBanks;
            }
            return ib;
          },
          cropAspectRatio : function() {
            return this.cropData.previewWidth / this.cropData.previewHeight;
          },
          cropImageUrl : function() {
            if (this.file) {
              return this.fileUrl;
            } else {
              return this.fullImageUrl;
            }
          },
          croppedPreviewWidthAndHeight : function() {
            const $preview = this.$refs.croppedPreview || this.$refs.preview;
            return {
              height : $preview.offsetHeight,
              width : ($preview.offsetHeight * this.cropAspectRatio).roundHalfUp(0)
            };
          },
          croppedPreviewWidthAndHeightStyle : function() {
            const style = {
              width : this.croppedPreviewWidthAndHeight.width + 'px',
              height : this.croppedPreviewWidthAndHeight.height + 'px'
            };
            return style;
          },
          currentCropData : function() {
            if (!this.cropData) {
              return;
            }
            const cropData = extendsObject({}, this.cropData);
            if (this.file || this.isMediaBankPreviewUrl) {
              delete cropData.box;
            }
            if (this.cropCtx.previewData) {
              cropData.box = this.cropCtx.previewData.box;
            }
            return cropData;
          },
          expectedFormatHelp : function() {
            return this.formatMessage(this.messages.expectedFormatMsg, [
              this.getHandledImageExtensions().joinWith({
                separator : ', ',
                lastSeparator : ' ' + this.messages.orMsgPart + ' '
              })
            ],{
              styles : {
                bold : false
              }
            });
          }
        }
      }));

  const __computeCropPreview = function(cropPreview, coords, croppedPreview) {
    const cropBox = {};
    if (parseInt(coords.w) > 0) {
      const rx = croppedPreview.width / coords.w;
      const ry = croppedPreview.height / coords.h;
      const cropPreviewWidth = (rx * cropPreview.width).roundHalfUp(0);
      const cropPreviewHeight = (ry * cropPreview.height).roundHalfUp(0);
      const xStart = (rx * coords.x).roundHalfUp(0);
      const yStart = (ry * coords.y).roundHalfUp(0);
      cropBox.offsetX = coords.x.roundHalfUp(0);
      cropBox.offsetY = coords.y.roundHalfUp(0);
      cropBox.width = coords.w.roundHalfUp(0);
      cropBox.height = coords.h.roundHalfUp(0);
      cropBox.previewCss = {
        width : cropPreviewWidth + 'px',
        height : cropPreviewHeight + 'px',
        marginLeft : '-' + xStart + 'px',
        marginTop : '-' + yStart + 'px'
      };
      return cropBox;
    }
  }

  SpVue.component('silverpeas-crop-image-form',
      templateRepository.get('crop-image-form', {
        mixins : [VuejsFormApiMixin, VuejsI18nTemplateMixin],
        emits : ['initialized'],
        props : {
          fullImageUrl : {
            'type' : String,
            'default' : undefined
          },
          cropData : {
            'type' : Object,
            'default' : {
              previewWidth : 200,
              previewHeight : 200,
              box : {
                width : 100,
                height : 100,
                offsetX : 0,
                offsetY : 0
              }
            }
          }
        },
        data : function() {
          return {
            jcropApi : undefined,
            cropBox : undefined,
            lastCoords : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            initFormData : function() {
              this.initFormData();
            },
            validateForm : function() {
              return !!this.lastCoords && this.rootFormApi.errorMessage().none();
            },
            updateFormData : function(formPaneData) {
              formPaneData.cropBox = extendsObject({}, this.cropBox);
              delete formPaneData.cropBox.previewCss;
              const cropPreview = extendsObject({}, this.cropPreviewWidthAndHeight);
              const lastCoords = extendsObject({}, this.lastCoords);
              formPaneData.computeCropPreview = function(croppedPreview) {
                return __computeCropPreview(cropPreview, lastCoords, croppedPreview);
              };
            }
          });
        },
        mounted : function() {
          this.initFormData();
        },
        beforeUnmount : function() {
          this.jcropApi.destroy();
        },
        methods : {
          initFormData : function(){
            this.cropBox = extendsObject({
              width : 100,
              height : 100,
              offsetX : 0,
              offsetY : 0
            }, this.cropData.box);
            ['width', 'height'].forEach(function(attr) {
              if (this.cropBox[attr] === 0) {
                this.cropBox[attr] = 100;
              }
            }.bind(this));
            const setJcropApi = function(jcropApi) {
              this.jcropApi = jcropApi;
              this.$emit('initialized');
            }.bind(this);
            jQuery(this.$refs.cropbox).Jcrop({
              onChange: this.showPreview,
              onSelect: this.showPreview,
              aspectRatio: this.aspectRatio,
              boxWidth: 800,
              boxHeight: 330,
              setSelect : [
                this.cropBox.offsetX,
                this.cropBox.offsetY,
                this.cropBox.offsetX + this.cropBox.width,
                this.cropBox.offsetY + this.cropBox.height
              ]
            }, function() {
              setJcropApi(this);
            });
          },
          showPreview : function(coords) {
            if (parseInt(coords.w) > 0) {
              this.lastCoords = coords;
              const cropPreview = this.cropPreviewWidthAndHeight;
              const croppedPreview = {
                width : this.cropData.previewWidth,
                height : this.cropData.previewHeight
              }
              extendsObject(this.cropBox,
                  __computeCropPreview(cropPreview, coords, croppedPreview));
              jQuery(this.$refs.preview).css(this.cropBox.previewCss);
            } else {
              this.lastCoords = undefined;
            }
          }
        },
        computed : {
          aspectRatio : function() {
            return this.cropData.previewWidth / this.cropData.previewHeight;
          },
          cropPreviewWidthAndHeight : function() {
            const box = {};
            const $cropbox = this.$refs.cropbox;
            if ($cropbox) {
              box.width = $cropbox.width;
              box.height = $cropbox.height;
            }
            return box;
          }
        }
      }));
})();
