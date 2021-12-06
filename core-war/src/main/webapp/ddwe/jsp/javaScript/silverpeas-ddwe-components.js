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

  /**
   * This event is dispatched during grapes initialization.
   * The detail of the event contains the following structured object:
   *  {
   *    plugins: array of plugins. In Silverpeas, a plugin is a function with a single one
   * parameter which is the editor instance.
   *    options: an object containing the options given to the initialization of a
   * DragAndDropWebEditorManager instance.
   *    gI18n: the structure object that permits to add the i18n data.
   *  }
   */
  document.addEventListener('ddwe-editor-plugins', function(event) {
    const params = event.detail;
    params.plugins.push(function(editor) {
      __setupContributionComponent(editor, params.options, params.gI18n);
      __setupImageLinkComponent(editor, params.options, params.gI18n);
      __setupSimpleBlockComponent(editor, params.options, params.gI18n);
    })
  })

  /**
   * Setup the Contribution component.
   * @param editor grapes instance.
   * @param initOptions the options given at initialization of DragAndDropWebEditorManager instance.
   * @param gI18n the i18n stuffs.
   * @private
   */
  function __setupContributionComponent(editor, initOptions, gI18n) {
    const componentType = 'contribution-header';
    gI18n.domComponents.names[componentType] = sp.i18n.get('contributionBlockTitle');
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : function(el) {
        if (el && el.tagName && el.tagName.toLowerCase() === 'div' && el.classList.contains(componentType)) {
          return {
            tagName: 'div',
            type : componentType
          };
        }
      },
      model : {
        defaults : {
          tagName: 'div',
          className: componentType,
          attributes: {
            'class': componentType
          },
          components : [{
            type : 'image',
            tagName : 'img',
            attributes : {
              'src' : initOptions.defaultEditorImageSrc,
              'class': 'imageContribution'
            }
          }, {
            tagName : 'div',
            attributes: {
              'class': 'editorialContribution'
            },
            components : [{
              type : 'text',
              tagName : 'h3',
              attributes: {
                'class': 'titreContribution'
              },
              components : sp.i18n.get('contributionBlockContentTitle')
            }, {
              type : 'text',
              tagName : 'div',
              attributes: {
                'class': 'textContribution'
              },
              components : sp.i18n.get('contributionBlockContent')
            }, {
              type : 'link',
              tagName : 'a',
              attributes: {
                'class': 'lienContribution',
                'target': '_blank',
                'rel': 'noopener noreferrer'
              },
              components : sp.i18n.get('contributionBlockContentReadMore')
            }]
          }]
        },
        init : function() {
          const commandToAdd = 'tlb-sp-basket-selector';
          const commandIcon = 'fa fa-sp-basket-selector';
          const defaultToolbar = this.get('toolbar');
          const commandExists = defaultToolbar.some(function(item) {
            return item.command === commandToAdd;
          });
          if (!commandExists) {
            defaultToolbar.push({
              attributes : {
                'class' : commandIcon
              },
              command : commandToAdd
            });
          }
        }
      },
      view : {
        events: {
          dblclick: function() {
            editor.runCommand('tlb-sp-basket-selector');
          }
        },
        onRender : function() {
          const nonCopyable = {
            copyable : false
          };
          this.model.find('.imageContribution, .editorialContribution, .titreContribution, .textContribution, .lienContribution').forEach(function(model) {
            model.set(nonCopyable);
            initOptions.tools.updateToolbar(model);
          }.bind(this.model));
        }
      }
    });
    editor.on('block:drag:stop', function(model) {
      if (model && model.get('type') === componentType) {
        editor.select(model);
        editor.runCommand('tlb-sp-basket-selector');
      }
    });
    editor.Commands.add('tlb-sp-basket-selector', {
      run : function(ed, sender, opts) {
        if (opts) {
          initOptions.basketSelectionApi.open({
            select : function(basketElement) {
              const selectedModel = ed.getSelected();
              [{
                cssSelector : 'img',
                updateWith : function(model) {
                  if (basketElement.getImageSrc()) {
                    model.set({
                      src : basketElement.getImageSrc()
                    });
                  }
                }
              }, {
                cssSelector : '.titreContribution',
                updateWith : function(model) {
                  model.components(basketElement.getTitle().noHTML().convertNewLineAsHtml());
                }
              }, {
                cssSelector : '.textContribution',
                updateWith : function(model) {
                  model.components(basketElement.getDescription().noHTML().convertNewLineAsHtml());
                }
              }, {
                cssSelector : '.lienContribution',
                updateWith : function(model) {
                  model.addAttributes({
                    'href' : basketElement.getLink()
                  });
                }
              }].forEach(function(target) {
                selectedModel.find(target.cssSelector).forEach(function(model) {
                  target.updateWith(model);
                });
              });
            }
          });
        }
      }
    });
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('contributionBlockTitle'),
      attributes : {
        'class' : 'fa fa-sp-contribution'
      },
      category: sp.i18n.get('silverpeasCategoryLabel'),
      content: {
        type : componentType
      }
    });
  }

  /**
   * Setup the Image Link component.
   * @param editor grapes instance.
   * @param initOptions the options given at initialization of DragAndDropWebEditorManager instance.
   * @param gI18n the i18n stuffs.
   * @private
   */
  function __setupImageLinkComponent(editor, initOptions, gI18n) {
    const componentType = 'bloc-image-link';
    gI18n.domComponents.names[componentType] = sp.i18n.get('imageWithLinkBlockTitle');
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : function(el) {
        if (el && el.tagName && el.tagName.toLowerCase() === 'div' && el.classList.contains(componentType)) {
          return {
            tagName: 'div',
            type : componentType
          };
        }
      },
      model : {
        defaults : {
          tagName: 'div',
          className: componentType,
          attributes: {
            'class': componentType
          },
          href: '',
          target : '_blank',
          traits:['id', 'title', {
            name:'href',
            changeProp:true
          }, {
            name:'target',
            type:'select',
            options: editor.TraitManager.getConfig().optionsTarget,
            changeProp:true
          }],
          components : {
            type : 'link',
            attributes: {
              'href': '',
              'title': '',
              'class': 'link-image',
              'target': '_blank',
              'rel': 'noopener noreferrer'
            },
            components : {
              type : 'image',
              attributes : {
                'src' : initOptions.defaultEditorImageSrc,
                'class': 'image-with-link',
                'alt': ''
              }
            }
          }
        },
        init : function() {
          this.on('change:href', this.handleCustomPropertyChange);
          this.on('change:target', this.handleCustomPropertyChange);
        },
        handleCustomPropertyChange : function() {
          this.find('.link-image').forEach(function(model) {
            const newAttr = {};
            const props = this.props();
            ['href', 'target'].forEach(function(name) {
              newAttr[name] = props[name];
            }.bind(this));
            model.addAttributes(newAttr);
          }.bind(this));
        }
      },
      view : {
        onRender : function() {
          const props = this.model.props();
          this.model.find('a').forEach(function(model) {
            model.set({
              copyable : false,
              draggable : false,
              removable : false,
              selectable : false,
              hoverable : false,
              highlightable : false
            });
            initOptions.tools.updateToolbar(model);
            ['href', 'target'].forEach(function(name) {
              props[name] = model.getAttributes()[name];
            }.bind(this));
          }.bind(this.model));
          this.model.find('img').forEach(function(model) {
            model.set({
              copyable : false,
              draggable : false,
              removable : false
            });
            initOptions.tools.updateToolbar(model);
          }.bind(this.model));
        }
      }
    });
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('imageWithLinkBlockTitle'),
      attributes : {
        'class' : 'fa fa-sp-image-link'
      },
      category: sp.i18n.get('silverpeasCategoryLabel'),
      content: {
        type : componentType
      }
    });
  }

  /**
   * Setups Simple Block component.
   * @param editor grapes instance.
   * @param initOptions the options given at initialization of DragAndDropWebEditorManager instance.
   * @param gI18n the i18n stuffs.
   * @private
   */
  function __setupSimpleBlockComponent(editor, initOptions, gI18n) {
    const componentType = 'simple-block';
    gI18n.domComponents.names[componentType] = sp.i18n.get('simpleBlockTitle');
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : function(el) {
        console.log(el)
        if (el && el.tagName && el.tagName.toLowerCase() === 'div' &&
            StringUtil.defaultStringIfNotDefined(el.getAttribute('sp-behavior')).indexOf(componentType) >= 0) {
          return {
            tagName: 'div',
            type : componentType,
            style: {
              'min-height': '20px'
            },
          };
        }
      },
      model : {
        defaults : {
          tagName: 'div',
          type : componentType,
          attributes: {
            'sp-behavior': componentType
          },
          style: {
            'min-height': '30px'
          },
          editable: true
        }
      }
    });
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('simpleBlockTitle'),
      attributes : {
        'class' : 'gjs-fonts gjs-f-b1 fa-sp-simple-bloc'
      },
      content: {
        type : componentType
      }
    });
  }
})();