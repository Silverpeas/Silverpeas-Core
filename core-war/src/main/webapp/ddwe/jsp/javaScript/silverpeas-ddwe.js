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
  window.DragAndDropWebEditorManager = function(options) {
    const self = this;
    applyReadyBehaviorOn(this);
    applyEventDispatchingBehaviorOn(this);
    const __options = extendsObject({
      ckeditorSrc : '',
      componentCssUrl : '',
      defaultEditorImageSrc : '',
      imageSelectorApi : undefined,
      basketSelectionApi : undefined,
      userToken : undefined,
      fileId : undefined,
      foreignContributionId : undefined,
      language : currentUser.language,
      connectors : {
        validate : undefined,
        cancel : undefined
      }
    }, options);

    __options.store = {
      deferred : undefined,
      error : false
    };

    __options.tools = {
      updateToolbar : function(model) {
        if (!model.get('copyable')) {
          model.get('toolbar').removeElement({command : 'tlb-clone'}, 'command');
        }
        if (!model.get('draggable')) {
          model.get('toolbar').removeElement({command : 'tlb-move'}, 'command');
        }
        if (!model.get('removable')) {
          model.get('toolbar').removeElement({command : 'tlb-delete'}, 'command');
        }
      }
    }

    const instanceId = __options.foreignContributionId.getComponentInstanceId();
    const resourceType = __options.foreignContributionId.getType();
    const resourceId = __options.foreignContributionId.getLocalId();
    sp.editor.wysiwyg.configFor(instanceId, resourceType, resourceId)
        .then(function(uploadedCkConfig) {
          uploadedCkConfig.toolbar += '_ddwe';
          const ckMandatoryConfig = {
            startupFocus : true,
            extraAllowedContent : '*(*);*{*}', // Allows any class and any inline style
            allowedContent : true, // Disable auto-formatting, class removing, etc.
            enterMode : CKEDITOR.ENTER_BR
          };
          const ckDefaultConfig = {
            extraPlugins : 'sharedspace,justify,colorbutton,panelbutton,font',
            toolbar : [
              {name : 'styles', items : ['Font', 'FontSize']},
              ['Bold', 'Italic', 'Underline', 'Strike'],
              {name : 'paragraph', items : ['NumberedList', 'BulletedList']},
              {name : 'links', items : ['Link', 'Unlink']},
              {name : 'colors', items: [ 'TextColor', 'BGColor' ]},
            ],
          };
          if (uploadedCkConfig.customConfig) {
            return sp.loadScript(uploadedCkConfig.customConfig).then(function() {
              delete uploadedCkConfig.baseHref;
              delete uploadedCkConfig.customConfig;
              const uploadedEditorConfig = uploadedCkConfig;
              CKEDITOR.editorConfig(uploadedEditorConfig)
              extendsObject(uploadedEditorConfig, ckMandatoryConfig)
              if (!uploadedEditorConfig['toolbar_' + uploadedCkConfig.toolbar]) {
                uploadedEditorConfig['toolbar_' + uploadedCkConfig.toolbar] = ckDefaultConfig.toolbar;
              }
              return uploadedEditorConfig;
            });
          }
          return extendsObject(ckDefaultConfig, ckMandatoryConfig);
        })
        .then(function(ckConfig) {
      const _context = {
        initialization : true
      }
      // Set up GrapesJS editor with the Newsletter plugin
      const i18n = {
        // default locale
        // detectLocale: true, // by default, the editor will detect the language
        // localeFallback: 'en', // default fallback
        locale : __options.language,
        detectLocale: false,
        messages : {}
      };
      i18n.messages[__options.language] = gI18n;
      let bottomOffset = 95;
      if (!spLayout) {
        bottomOffset = 0;
      } else if (spLayout.getFooter().isShown()) {
        bottomOffset -= spLayout.getFooter().getContainer().offsetHeight;
      }
      const plugins = ['grapesjs-preset-newsletter', 'grapesjs-plugin-ckeditor'];
      document.dispatchEvent(new CustomEvent('ddwe-editor-plugins', {
        detail : {
          plugins : plugins,
          options : __options,
          gI18n : gI18n
        },
        bubbles : true,
        cancelable : true
      }));
      const pluginOptions = {
        'grapesjs-preset-newsletter' : {
          useCustomTheme : false,
          showStylesOnChange : false,
          block : function(blockId) {
            return {
              attributes : {
                'class' : 'sp-panel-btn-' + blockId
              }
            }
          },
          tableStyle : {
            'min-height': '35px',
            margin: '0 auto 10px auto',
            padding: '5px 5px 5px 5px',
            width: '100%'
          }
        },
        'grapesjs-plugin-ckeditor': {
          position: 'center',
          options: ckConfig,
          ckeditor : __options.ckeditorSrc,
          customRte : {
            parseContent: false
          }
        }
      };
      const initOptions = {
        noticeOnUnload : false,
        i18n: i18n,
        height: 'calc(100% - ' + bottomOffset + 'px)',
        container : '#gjs',
        fromElement: true,
        assetManager: {
          custom: {
            open : function(props) {
              let currentImageSrc;
              let isBackgroundImg = false;
              if (props.options.target) {
                currentImageSrc = props.options.target.get('src');
              } else {
                // Maybe a background image
                const sm = gEditor.StyleManager;
                let backgroundImage = sm.getSelected() && sm.getSelected().getStyle() && sm.getSelected().getStyle()['background-image'];
                if (backgroundImage) {
                  currentImageSrc = backgroundImage.replace(/^.*[(]+[']+(.*)[']+[)]+.*/, '$1');
                  isBackgroundImg = true;
                  if (currentImageSrc === 'none') {
                    currentImageSrc = undefined;
                  }
                }
              }
              __options.imageSelectorApi.open({
                currentImageSrc : currentImageSrc,
                select : function(src) {
                  props.options.select({
                    get : function(key) {
                      if (key === 'src') {
                        if (isBackgroundImg) {
                          return StringUtil.isDefined(src) ? src : 'none';
                        }
                        return src;
                      }
                    },
                    getSrc : function() {
                      return src;
                    }
                  });
                },
                close : function() {
                  gEditor.AssetManager.close();
                }
              });
            },
            close : function(props) {
              // Nothing to do here
            }
          },
        },
        storageManager: {
          type: 'remote',
          options : {
            remote : {
              urlStore: webContext + '/Rddwe/jsp/store',
              urlLoad: webContext + '/Rddwe/jsp/load',
              headers: {
                access_token : __options.userToken,
                file_id : __options.fileId,
                initialization : _context.initialization
              },
              onLoad : function(data) {
                if (_context.initialization) {
                  _context.initialization = false;
                  gEditor.StorageManager.getConfig().options.remote.headers.initialization = false;
                  if (data['tmp-inlinedHtml']) {
                    const confirmationUrl = webContext + '/wysiwyg/jsp/confirmUnvalidatedContentExistence.jsp';
                    const url = sp.url.format(confirmationUrl);
                    const deferredOpen = sp.promise.deferred();
                    jQuery.popup.load(url).show('confirmation', {
                      openPromise : deferredOpen.promise,
                      callback : function() {
                        setTimeout(function() {
                          gEditor.load();
                        }, 0);
                      },
                      alternativeCallback : function() {
                        setTimeout(function() {
                          gEditor.store();
                        }, 0);
                      }
                    }).then(function() {
                      document.querySelector('#unvalidated-wysiwyg-content-container').innerHTML = data['tmp-inlinedHtml'];
                      deferredOpen.resolve();
                    }.bind(this));
                  }
                }
                return sp.promise.resolveDirectlyWith(data);
              },
              onStore : function(data) {
                data['gjs-inlinedHtml'] = gEditor.runCommand('gjs-get-inlined-html');
                return sp.promise.resolveDirectlyWith(data);
              }
            }
          }
        },
        plugins: plugins,
        pluginsOpts: pluginOptions
      };
      const $form = document.createElement('form');
      const $div = document.createElement('div');
      $div.appendChild($form);
      applyTokenSecurity($div);
      const tkn = $form.querySelector('input');
      if (tkn) {
        initOptions.storageManager.options.remote.headers[tkn.name] = tkn.value;
      }
      if (__options.componentCssUrl) {
        initOptions.canvas = {
          styles : Array.isArray(__options.componentCssUrl)
              ? __options.componentCssUrl
              : [__options.componentCssUrl]
        };
      }
      const gEditor = grapesjs.init(initOptions);
      const pnm = gEditor.Panels;
      // Complete button data
      pnm.getButton('devices-c', 'set-device-desktop').set('attributes', {
        title : gI18n.deviceManager.devices.desktop
      });
      pnm.getButton('devices-c', 'set-device-tablet').set('attributes', {
        title : gI18n.deviceManager.devices.tablet
      });
      pnm.getButton('devices-c', 'set-device-mobile').set('attributes', {
        title : gI18n.deviceManager.devices.mobilePortrait
      });
      // Removes presets button
      pnm.removeButton('options', 'gjs-open-import-template');
      pnm.removeButton('options', 'canvas-clear');
      pnm.removeButton('options', 'undo');
      pnm.removeButton('options', 'redo');
      pnm.removeButton('options', 'preview');
      pnm.removeButton('options', 'fullscreen');
      // Complete button data & states
      pnm.getButton('options', 'sw-visibility').set('active', true);
      pnm.getButton('options', 'gjs-toggle-images').set('attributes', {
        'class' : 'sp-panel-top-toggle-image',
        title : sp.i18n.get('cmtTglImagesLabel')
      });
      // Add info command
      pnm.addButton('options', [{
        id: 'undo',
        label : '<svg viewBox="0 0 24 24"><path d="M20 13.5C20 17.09 17.09 20 13.5 20H6V18H13.5C16 18 18 16 18 13.5S16 9 13.5 9H7.83L10.91 12.09L9.5 13.5L4 8L9.5 2.5L10.92 3.91L7.83 7H13.5C17.09 7 20 9.91 20 13.5Z" /></svg>',
        attributes: {title: sp.i18n.get('cmdBtnUndoLabel')},
        command: 'core:undo'
      },{
        id: 'redo',
        label : '<svg viewBox="0 0 24 24"><path d="M10.5 18H18V20H10.5C6.91 20 4 17.09 4 13.5S6.91 7 10.5 7H16.17L13.08 3.91L14.5 2.5L20 8L14.5 13.5L13.09 12.09L16.17 9H10.5C8 9 6 11 6 13.5S8 18 10.5 18Z" /></svg>',
        attributes: {title: sp.i18n.get('cmdBtnRedoLabel')},
        command: 'core:redo'
      },{
        id: 'clear-all',
        label : '<svg viewBox="0 0 24 24"><path d="M9,3V4H4V6H5V19A2,2 0 0,0 7,21H17A2,2 0 0,0 19,19V6H20V4H15V3H9M9,8H11V17H9V8M13,8H15V17H13V8Z" /></svg>',
        className: 'sp-panel-top-trash',
        attributes : {title : sp.i18n.get('clearCanvas')},
        command: {
          run: function(editor, sender) {
            sender && sender.set('active', false);
            jQuery.popup.confirm(sp.i18n.get('clearCanvasConfirm'), function() {
              editor.DomComponents.clear();
              setTimeout(function() {
                localStorage.clear()
              });
            });
          }
        }
      }]);
      self.getEditor = function() {
        return gEditor;
      };
      self.getOptions = function() {
        return __options;
      };
      self.whenStoreProcessHasFinished = function(callback) {
        spProgressMessage.show();
        setTimeout(function() {
          (__options.store.deferred
              ? __options.store.deferred.promise
              : sp.promise.resolveDirectlyWith()).then(function() {
            callback();
          }, function() {
            spProgressMessage.hide();
          });
        }, 0);
      };
      __adjustRichTextEditor(gEditor);
      __setupLeavingPage(gEditor, __options);
      __adjustComponentToolbars(gEditor);
      __adjustStyles(gEditor);
      __setupImageManagement(gEditor, options);
      __addConnectorButtons(gEditor, options);
      __extendHtmlSrcExport(gEditor);
      __addHtmlSrcEdition(gEditor);
      __adjustButtonOrder(gEditor);
      self.notifyReady();
    });
  };

  function __adjustRichTextEditor(editor) {
    const rte = editor.RichTextEditor;
    const __customRte = rte.customRte;
    const __enable = rte.enable;
    const __getContent = rte.getContent;
    const __disable = rte.disable;
    rte.get('bold').attributes.title = gI18n.richTextEditor.actions.bold;
    rte.get('italic').attributes.title = gI18n.richTextEditor.actions.italic;
    rte.get('underline').attributes.title = gI18n.richTextEditor.actions.underline;
    rte.get('strikethrough').attributes.title = gI18n.richTextEditor.actions.strikethrough;
    rte.remove('link');
    rte.remove('wrap');
    const __fallBackOnGlobalRte = function() {
      const view = arguments[0];
      view.__sp_fallback = true;
      __setupCustomRte.apply(this, arguments);
    };
    const __setupCustomRte = function() {
      const view = arguments[0];
      if (view.__sp_fallback) {
        if (this.customRte) {
          this.globalRte = undefined;
        }
        this.getToolbarEl().classList.remove('sp-custom-rte');
        this.customRte = undefined;
        for(let eId in CKEDITOR.instances) {
          CKEDITOR.instances[eId].destroy();
        }
      } else {
        this.getToolbarEl().classList.add('sp-custom-rte');
        this.customRte = __customRte;
      }
    };
    rte.enable = function() {
      const __arguments = arguments;
      __setupCustomRte.apply(this, __arguments);
      return __enable.apply(this, __arguments)['catch'](function() {
        __fallBackOnGlobalRte.apply(this, __arguments);
        return __enable.apply(this, __arguments);
      }.bind(this));
    }.bind(rte)
    rte.getContent = function() {
      const __arguments = arguments;
      __setupCustomRte.apply(this, __arguments);
      return __getContent.apply(this, __arguments)['catch'](function() {
        __fallBackOnGlobalRte.apply(this, __arguments);
        return __getContent.apply(this, __arguments);
      }.bind(this));
    }.bind(rte)
    rte.disable = function() {
      __setupCustomRte.apply(this, arguments);
      __disable.apply(this, arguments);
    }.bind(rte)
  }

  function __adjustStyles(editor) {
    const sms = editor.StyleManager.getSectors();
    const sms_super_reset = sms.reset;
    const sms_super_add = sms.add;
    sms.reset = function() {
      // avoid the use by plugin
    };
    sms.add = function() {
      // avoid the use by plugin
    };
    editor.on('load', function() {
      setTimeout(function() {
        sms.reset = sms_super_reset;
        sms.add = sms_super_add;
      }, 0);
    });
  }

  function __adjustComponentToolbars(editor) {
    let labelTitleCommandMapping;
    editor.on('component:selected', function(model) {
      if (!labelTitleCommandMapping) {
        labelTitleCommandMapping = {};
        ['tlb-move',
         'tlb-clone',
         'tlb-delete',
         'tlb-sp-basket-selector'].forEach(function(command) {
           labelTitleCommandMapping[command] = sp.i18n.get(command.replace(/[-]/g, '_') + '_Label');
        });
      }
      const defaultToolbar = model.get('toolbar');
      defaultToolbar.forEach(function(menu, index) {
        let attr = menu.attributes;
        if (!attr) {
          attr = {
            'title' : index === 0 ? sp.i18n.get('selectParent_Label') : labelTitleCommandMapping[menu.command]
          };
          menu.attributes = attr;
        }
        if (!attr.title && typeof menu.command === 'string') {
          let cmd = menu.command;
          attr.title = labelTitleCommandMapping[cmd];
          while(!attr.title && cmd.indexOf('-') > 0) {
            cmd = cmd.replace(/-[^-]*$/, '');
            attr.title = labelTitleCommandMapping[cmd];
          }
        }
      });
    });
  }

  function __setupImageManagement(editor, initOptions) {
    editor.BlockManager.get('image').attributes.content.attributes = {
      src : initOptions.defaultEditorImageSrc
    };
  }

  function __setupLeavingPage(editor, completedOtions) {
    editor.on('storage:start:store', function(data) {
      if (completedOtions.store.deferred) {
        completedOtions.store.deferred.reject();
      }
      completedOtions.store.deferred = sp.promise.deferred();
    });
    editor.on('storage:end:store', function(data) {
      if (data && StringUtil.isDefined(data['gjs-inlinedHtml'])) {
        completedOtions.store.error = false;
        completedOtions.store.deferred.resolve();
      } else {
        SilverpeasError.add(sp.i18n.get('storeErrorMsg')).show();
        completedOtions.store.error = true;
        completedOtions.store.deferred.reject();
      }
      completedOtions.store.deferred = undefined;
    });
    window.addEventListener('beforeunload', function(e) {
      if (completedOtions.store.error) {
        e.preventDefault();
        spProgressMessage.hide();
        return e.returnValue = sp.i18n.get('storeWarningMsg');
      }
    }, {capture: true});
  }

  function __adjustButtonOrder(instance) {
    const manualOrders = [{
      'classSelector' : 'sp-panel-top-edit-html',
      order : 25
    }, {
      'classSelector' : 'sp-panel-top-toggle-image',
      order : 65
    }, {
      'classSelector' : 'sp-panel-top-trash',
      order : 70
    }];
    const elements = [];
    Array.prototype.push.apply(elements, instance.Panels.getPanelsEl().querySelectorAll('.gjs-pn-options .gjs-pn-buttons span'));
    elements.forEach(function($el, index) {
      const manualOrderValue = manualOrders.filter(function(manualOrder) {
        return $el.classList.contains(manualOrder.classSelector);
      }).map(function(manualOrder) {
        return manualOrder.order
      }).join();
      $el.style.order = manualOrderValue || (index + 1) * 10;
    });
  }

  function __addConnectorButtons(instance, plgOptions) {
    const buttons = [];
    if (typeof plgOptions.connectors.validate === 'function') {
      buttons.push({
        id: 'sp-validate',
        className: 'sp_button',
        label: sp.i18n.get('validate'),
        command: {
          run : function() {
            plgOptions.connectors.validate();
          }
        }
      });
    }
    if (plgOptions.connectors.cancel) {
      buttons.push({
        id: 'sp-cancel',
        className: 'sp_button',
        label: sp.i18n.get('cancel'),
        command: {
          run : function() {
            plgOptions.connectors.cancel();
          }
        }
      });
    }
    if (buttons.length > 0) {
      instance.Panels.addPanel({
        id : 'nav-connectors',
        visible : true,
        buttons : buttons
      });
    }
  }

  function __extendHtmlSrcExport(instance) {
    instance.on('load', function() {
      setTimeout(function() {
        const manager = new HtmlCodePopupManager(instance, true);
        instance.Commands.extend('export-template', {
          run : function(editor, sender) {
            const finalContent = instance.runCommand('gjs-get-inlined-html');
            manager.setModal(finalContent, {
              title : sp.i18n.get('finalHtml'), callback : function(code) {
                instance.setComponents(code.trim());
              }
            });
            sender && sender.set && sender.set('active', 0);
          }
        });
      }, 0);
    });
  }

  function __addHtmlSrcEdition(instance) {
    const manager = new HtmlCodePopupManager(instance);
    instance.Commands.add('html-edit', {
      run : function(editor, sender) {
        const innerHtml = editor.getHtml();
        const css = editor.getCss();
        manager.setModal(innerHtml + "<style>" + css + '</style>', {
          title : sp.i18n.get('htmlSource'),
          callback : function(code) {
            instance.setComponents("");
            instance.CssComposer.clear();
            instance.setComponents(code.trim());
          }
        });
        sender && sender.set && sender.set('active', 0);
      }
    });
    instance.Panels.addButton('options', [{
      id : 'edit',
      label : '<svg viewBox="0 0 24 24"><path d="M5,3C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V12H19V19H5V5H12V3H5M17.78,4C17.61,4 17.43,4.07 17.3,4.2L16.08,5.41L18.58,7.91L19.8,6.7C20.06,6.44 20.06,6 19.8,5.75L18.25,4.2C18.12,4.07 17.95,4 17.78,4M15.37,6.12L8,13.5V16H10.5L17.87,8.62L15.37,6.12Z" /></svg>',
      className : 'sp-panel-top-edit-html',
      command : 'html-edit',
      attributes : {
        title : sp.i18n.get('editHtml')
      }
    }]);
  }

  const HtmlCodePopupManager = function(instance, readOnly) {
    const codeViewer = instance.CodeManager.getViewer('CodeMirror').clone();
    codeViewer.set({
      codeName : 'htmlmixed',
      readOnly : !!readOnly ? 1 : 0,
      autoBeautify : true,
      autoCloseTags : true,
      autoCloseBrackets : true,
      lineWrapping : true,
      styleActiveLine : true,
      smartIndent : true,
      indentWithTabs : true
    });
    const container = document.createElement('div');
    container.style.minWidth = '750px';
    this.setModal = function(content, options) {
      options = extendsObject({
        minWidth : 800,
        title : '',
        callback : undefined
      }, options);
      if (typeof options.callback === 'function') {
        const __callback = options.callback;
        options.callback = function() {
          if (typeof options.callback === 'function') {
            const code = codeViewer.editor.getValue();
            __callback(code);
          }
        };
      }
      let viewer = codeViewer.editor;
      if (!viewer) {
        const txtarea = document.createElement('textarea');
        container.appendChild(txtarea);
        codeViewer.init(txtarea);
        viewer = codeViewer.editor;
      }
      codeViewer.setContent(content);
      jQuery.popup[readOnly ? 'info' : 'validate'](container, options);
      viewer.refresh();
    }
  }
})();