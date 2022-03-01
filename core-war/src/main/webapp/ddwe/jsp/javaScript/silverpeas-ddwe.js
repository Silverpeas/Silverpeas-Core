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
      const plugins = ['gjs-preset-newsletter', 'gjs-plugin-ckeditor'];
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
        'gjs-preset-newsletter' : {
          cmdBtnDesktopLabel : gI18n.deviceManager.devices.desktop,
          cmdBtnTabletLabel : gI18n.deviceManager.devices.tablet,
          cmdBtnMobileLabel : gI18n.deviceManager.devices.mobilePortrait,
          cmtTglImagesLabel : sp.i18n.get('cmtTglImagesLabel'),
          tableStyle : {
            'min-height': '35px',
            margin: '0 auto 10px auto',
            padding: '5px 5px 5px 5px',
            width: '100%'
          }
        },
        'gjs-plugin-ckeditor': {
          position: 'center',
          options: ckConfig
        }
      };
      const initOptions = {
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
          urlStore: webContext + '/Rddwe/jsp/store',
          urlLoad: webContext + '/Rddwe/jsp/load',
          headers: {
            access_token : __options.userToken,
            file_id : __options.fileId,
            initialization : _context.initialization
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
        initOptions.storageManager.headers[tkn.name] = tkn.value;
      }
      if (__options.componentCssUrl) {
        initOptions.canvas = {
          styles : Array.isArray(__options.componentCssUrl)
              ? __options.componentCssUrl
              : [__options.componentCssUrl]
        };
      }
      const gEditor = grapesjs.init(initOptions);
      gEditor.on('storage:start:store', function(data) {
        data.inlinedHtml = gEditor.runCommand('gjs-get-inlined-html');
      });
      gEditor.on('storage:end:load', function(data) {
        if (_context.initialization) {
          _context.initialization = false;
          gEditor.StorageManager.getConfig().headers.initialization = false;
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
      });
      const pnm = gEditor.Panels;
      // Removes presets button
      pnm.removeButton('options', 'gjs-open-import-template');
      // Add info command
      pnm.addButton('options', [{
        id: 'undo',
        className: 'fa fa-undo',
        attributes: {title: sp.i18n.get('cmdBtnUndoLabel')},
        command: function(){ gEditor.runCommand('core:undo') }
      },{
        id: 'redo',
        className: 'fa fa-repeat',
        attributes: {title: sp.i18n.get('cmdBtnRedoLabel')},
        command: function(){ gEditor.runCommand('core:redo') }
      },{
        id: 'clear-all',
        className: 'fa fa-trash icon-blank',
        attributes : {
          title : sp.i18n.get('clearCanvas')
        },
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
      __adjustComponentToolbars(gEditor);
      __adjustStyles(gEditor);
      __setupImageManagement(gEditor, options);
      __addRichTextEditorPositionHandler(gEditor);
      __addConnectorButtons(gEditor, options);
      __extendHtmlSrcExport(gEditor);
      __addHtmlSrcEdition(gEditor);
      __adjustButtonOrder(gEditor);
      self.notifyReady();
    });
  };

  function __adjustStyles(editor) {
    const sms = editor.StyleManager.getSectors();
    const sms_super_reset = sms.reset;
    const sms_super_add = sms.add;
    sms.reset = function() {
      // avoid the use be plugin
    };
    sms.add = function() {
      // avoid the use be plugin
    };
    editor.on('load', function() {
      setTimeout(function() {
        sms.reset = sms_super_reset;
        sms.add = sms_super_add;
      }, 0);
    });
  }

  function __adjustComponentToolbars(editor) {
    editor.on('component:selected', function(model) {
      const defaultToolbar = model.get('toolbar');
      defaultToolbar.forEach(function(menu) {
        const attr = menu.attributes;
        if (!attr.title && attr['class']) {
          attr['class'].split(' ').forEach(function(aClass) {
            const key = aClass.replace(/[-]/g, '_') + '_Label';
            const label = sp.i18n.get(key);
            if (label.indexOf(key) < 0) {
              attr.title = label;
            }
          });
        }
      });
    });
  }

  function __setupImageManagement(editor, initOptions) {
    editor.BlockManager.get('image').attributes.content.attributes = {
      src : initOptions.defaultEditorImageSrc
    };
  }

  function __addRichTextEditorPositionHandler(editor) {
    const __posUpdate = function() {
      const $toolbarEl = editor.RichTextEditor.getToolbarEl();
      $toolbarEl.style.display = '';
      const $frameEl = editor.Canvas.getElement();
      const rteOffsets = sp.element.offset($toolbarEl);
      const hiddenRteWidth = rteOffsets.left + $toolbarEl.offsetWidth - $frameEl.offsetWidth;
      if (hiddenRteWidth > 0) {
        $toolbarEl.style.left = (0 - hiddenRteWidth) + 'px';
      }
    };
    editor.on('rteToolbarPosUpdate', function() {
      const $toolbarEl = editor.RichTextEditor.getToolbarEl();
      $toolbarEl.style.display = 'none';
      setTimeout(__posUpdate, 0);
    });
  }

  function __adjustButtonOrder(instance) {
    const manualOrders = [{
      'classSelector' : 'fa-edit',
      order : 45
    }, {
      'classSelector' : 'fa-warning',
      order : 65
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
        className: 'fa fa-validate sp_button',
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
        className: 'fa fa-cancel sp_button',
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
      className : 'fa fa-edit',
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