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

//# sourceURL=/util/javaScript/ddwe/silverpeas-ddwe-component.js

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
      __setupEventComponent(editor, params.options, params.gI18n);
      __setupImageLinkComponent(editor, params.options, params.gI18n);
      __setupSimpleBlockComponent(editor, params.options, params.gI18n);
    })
  });

  const UTILS = new function() {
    this.setupRunCommandOnEvent = function(componentType, command, editor, model) {
      let models;
      if (Array.isArray(model)) {
        models = model;
      } else {
        models = model ? [model] : [];
      }
      models.forEach(function(m) {
        if (m.get('type') === componentType) {
          editor.select(m);
          editor.runCommand(command);
        }
      });
    };
  };

  const __addPickupBasketElementMenu = function(commandToAdd) {
    const defaultToolbar = this.get('toolbar');
    const commandExists = defaultToolbar.some(function(item) {
      return item.command === commandToAdd;
    });
    if (!commandExists) {
      defaultToolbar.push({
        label : '<svg viewBox="0 0 24 24"><path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" /></svg>',
        command : commandToAdd
      });
    }
  };

  /**
   * Stores into the targeted application the thumbnail of the contribution referred by the
   * specified item in the selection basket. By storing the thumbnail as a regular uploaded image
   * in the application, we ensure the image can be managed as it is got from the image selector.
   * This will also ensure the image to not be broken in the case the contribution is deleted or
   * its thumbnail updated or removed.
   * @param requesterOptions options to request the operation to the remote service in Silverpeas
   * @param basketElement the item in the basket referring an existing contribution in Silverpeas.
   * @returns {string} the URL of the new duplicated thumbnail.
   * @private
   */
  function __storeThumbnail(requesterOptions, basketElement) {
    return sp.ajaxRequest(webContext + '/Rddwe/jsp/thumbnail')
        .withHeaders({
          access_token : requesterOptions.userToken,
          file_id : requesterOptions.fileId,
          initialization : true
        })
        .byPostMethod()
        .sendAndPromiseJsonResponse({
          id: basketElement.getId(),
          thumbnailUrl: basketElement.getImageSrc()
        });
  }

  const __isTheComponent = function(model, componentType) {
    return model.get('type') === componentType;
  }

  function __updateModelWithUpdaters(model, basketElement, updaters) {
    updaters.forEach(function(updater) {
      model.find(updater.cssSelector).forEach(function(cmp) {
        updater.updateWith(cmp, basketElement);
      });
    });
  }

  function __componentTypeChecker(componentType) {
    return function (el) {
      if (el?.tagName && el.tagName.toLowerCase() === 'div' && el.classList.contains(componentType)) {
        return {
          tagName: 'div',
          type: componentType
        };
      }
    }
  }

  /**
   * Sets up the behaviour of a GraphJS component deriving from an element of the specified
   * type in the selection basket.
   * @param editor {object} the GrapeJS editor
   * @param component {string} the name of the component in the editor
   * @param componentType {string} the type of the component
   * @param basketElementType {string} the type of the element in the selection basket with
   * which the
   * component can be provided.
   * @private
   */
  function __setUpEditorComponentBehaviour(editor, component, componentType,
                                           basketElementType) {
    // setting up now the behaviour of our component in GrapeJS
    editor.on('component:hovered', function(model) {
      const $highlighterEl = editor.Canvas.getHighlighter(model.view);
      const $badgeEl = editor.Canvas.getBadgeEl(model.view);
      if (__isTheComponent(model, componentType)) {
        $highlighterEl.classList.add(component);
        $badgeEl.classList.add(component);
      } else {
        $highlighterEl.classList.remove(component);
        $badgeEl.classList.remove(component);
      }
    });
    editor.on('component:selected', function(model) {
      if (__isTheComponent(model, componentType)) {
        const $toolbar = editor.Canvas.getToolbarEl();
        $toolbar.classList.add(component);
      }
    });
    editor.on('component:deselected', function(model) {
      if (__isTheComponent(model, componentType)) {
        const $toolbar = editor.Canvas.getToolbarEl();
        $toolbar.classList.remove(component);
      }
    });
    editor.on('block:drag:stop', function(model) {
      UTILS.setupRunCommandOnEvent(componentType, 'tlb-sp-basket-selector-' + basketElementType, editor, model);
    });
  }
  
  /**
   * Setups the Contribution component.
   * @param editor grapes instance.
   * @param initOptions the options given at initialization of DragAndDropWebEditorManager instance.
   * @param gI18n the i18n stuffs.
   * @private
   */
  function __setupContributionComponent(editor, initOptions, gI18n) {
    const componentType = 'contribution-header';
    gI18n.domComponents.names[componentType] = sp.i18n.get('contributionBlockTitle');

    // model of a Silverpeas contribution in GrapeJS
    const contributionModel = {
      defaults : {
        tagName: 'div',
        className: componentType,
        attributes: {
          'class': componentType
        },
        components : [{
          tagName : 'div',
          attributes : {
            'class': 'blocImageContribution'
          },
          components : [{
            type : 'image',
            tagName : 'img',
            attributes : {
              'src' : initOptions.defaultEditorImageSrc,
              'class': 'imageContribution'
            }
          }]
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
        __addPickupBasketElementMenu.call(this, 'tlb-sp-basket-selector-contribution');
      }
    };

    // the updaters of a component model from an element (id est an existing Silverpeas
    // contribution) taken from the selection basket. Each updater takes in charge to set the value
    // of a given model's attribute.
    const contributionModelUpdaters = [{
      cssSelector : 'img',
      updateWith : function(model, basketElement) {
        if (basketElement.getImageSrc()) {
          __storeThumbnail(initOptions, basketElement).then(function(item) {
            model.set({
              src: item.thumbnailUrl
            });
          });
        }
      }
    }, {
      cssSelector : '.titreContribution',
      updateWith : function(model, basketElement) {
        model.components(basketElement.getTitle().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.textContribution',
      updateWith : function(model, basketElement) {
        model.components(basketElement.getDescription().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.lienContribution',
      updateWith : function(model, basketElement) {
        model.addAttributes({
          'href' : basketElement.getLink()
        });
      }
    }];

    // adding our contribution component into the GrapeJS editor by specifying, among others things,
    // its model and how to set up its attributes.
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : __componentTypeChecker(componentType),
      model : contributionModel,
      view : {
        onRender : function() {
          const nonCopyable = {
            copyable : false
          };
          this.model
              .find('.blocImageContribution, .imageContribution, .editorialContribution, .titreContribution, .textContribution, .lienContribution')
              .forEach(function(model) {
            model.set(nonCopyable);
            initOptions.tools.updateToolbar(model);
          });
        }
      }
    });

    __setUpEditorComponentBehaviour(editor, 'contribution-header', componentType, 'contribution');

    // when the contribution component is dragged into the content area, open the selection basket
    // UI to allow the user to choose the contribution to set within the component body
    editor.Commands.add('tlb-sp-basket-selector-contribution', {
      run : function(ed, sender, opts) {
        if (opts) {
          initOptions.basketSelectionApi.open({
            filter : function(element) {
              return !BasketService.Filters.eventItems(element);
            },
            select : function(basketElement) {
              const selectedModel = ed.getSelected();
              __updateModelWithUpdaters(selectedModel, basketElement, contributionModelUpdaters);
            }
          });
        }
      }
    });
    // define how and where the contribution component is rendered within the component blocks
    // selector in GrapeJS
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('contributionBlockTitle'),
      media : '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M20 5L20 19L4 19L4 5H20M20 3H4C2.89 3 2 3.89 2 5V19C2 20.11 2.89 21 4 21H20C21.11 21 22 20.11 22 19V5C22 3.89 21.11 3 20 3M18 15H6V17H18V15M10 7H6V13H10V7M12 9H18V7H12V9M18 11H12V13H18V11Z" /></path></svg>',
      category: sp.i18n.get('silverpeasCategoryLabel'),
      content: {
        type : componentType
      }
    });
  }

  /**
   * Setups the Event component.
   * @param editor grapes instance.
   * @param initOptions the options given at initialization of DragAndDropWebEditorManager instance.
   * @param gI18n the i18n stuffs.
   * @private
   */
  function __setupEventComponent(editor, initOptions, gI18n) {
    const componentType = 'calendar-event';
    gI18n.domComponents.names[componentType] = sp.i18n.get('eventBlockTitle');

    // model of a Silverpeas calendar event in GrapeJS
    const calendarEventModel = {
      defaults : {
        tagName: 'div',
        className: componentType,
        attributes: {
          'class': componentType
        },
        components : [{
          type : 'text',
          tagName : 'h3',
          attributes: {
            'class': 'event-title'
          },
          components : sp.i18n.get('eventBlockContentTitle')
        }, {
          tagName : 'div',
          attributes: {
            'class': 'event-date'
          },
          components : [{
            type : 'text',
            tagName : 'span',
            attributes: {
              'class': 'event-start-date'
            },
            components : sp.i18n.get('eventBlockContentFrom') + ' ...'
          }, {
            type : 'text',
            tagName : 'span',
            attributes: {
              'class': 'event-end-date'
            },
            components : '&#160;' + sp.i18n.get('eventBlockContentTo') + ' ...'
          }]
        }, {
          type : 'text',
          tagName : 'div',
          attributes: {
            'class': 'event-description'
          },
          components : sp.i18n.get('eventBlockDescription')
        }, {
          type : 'link',
          tagName : 'a',
          attributes: {
            'class': 'event-link',
            'target': '_blank',
            'rel': 'noopener noreferrer'
          },
          components : sp.i18n.get('eventBlockOpen')
        }]
      },
      init : function() {
        __addPickupBasketElementMenu.call(this, 'tlb-sp-basket-selector-event');
      }
    };

    // the updaters of a component model from an element (id est an existing Silverpeas
    // calendar event) taken from the selection basket. Each updater takes in charge to set the
    // value of a given model's attribute.
    const calendarEventModelUpdaters = [{
      cssSelector : '.event-title',
      updateWith : function(model, basketElement) {
        model.components(basketElement.getTitle().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.event-start-date',
      updateWith : function(model, basketElement) {
        model.components(basketElement.getPeriod().formatStartDate().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.event-end-date',
      updateWith : function(model, basketElement) {
        model.components('&#160;' + basketElement.getPeriod().formatEndDate().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.event-description',
      updateWith : function(model, basketElement) {
        model.components(basketElement.getDescription().noHTML().convertNewLineAsHtml());
      }
    }, {
      cssSelector : '.event-link',
      updateWith : function(model, basketElement) {
        model.addAttributes({
          'href' : basketElement.getLink()
        });
      }
    }];

    // adding our calendar event component into the GrapeJS editor by specifying, among others
    // things, its model
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : __componentTypeChecker(componentType),
      model : calendarEventModel,
      view : {
        onRender : function() {
          const nonCopyable = {
            copyable : false
          };
          this.model.find('.event-title, .event-date, .event-start-date, .event-end-date, .event-description, .event-link').forEach(function(model) {
            model.set(nonCopyable);
            initOptions.tools.updateToolbar(model);
          });
        }
      }
    });

    __setUpEditorComponentBehaviour(editor, 'calendar-event', componentType, 'event');

    // when the calendar event component is dragged into the content area, open the selection
    // basket UI to allow the user to choose the calendar event to set within the component body
    editor.Commands.add('tlb-sp-basket-selector-event', {
      run : function(ed, sender, opts) {
        if (opts) {
          initOptions.basketSelectionApi.open({
            filter : function(element) {
              return BasketService.Filters.eventItems(element);
            },
            select : function(basketElement) {
              const selectedModel = ed.getSelected();
              __updateModelWithUpdaters(selectedModel, basketElement, calendarEventModelUpdaters);
            }
          });
        }
      }
    });
    // define how and where the calendar event component is rendered within the component blocks
    // selector in GrapeJS
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('eventBlockTitle'),
      media : '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M7 11H9V13H7V11M21 5V19C21 20.11 20.11 21 19 21H5C3.89 21 3 20.1 3 19V5C3 3.9 3.9 3 5 3H6V1H8V3H16V1H18V3H19C20.11 3 21 3.9 21 5M5 7H19V5H5V7M19 19V9H5V19H19M15 13V11H17V13H15M11 13V11H13V13H11M7 15H9V17H7V15M15 17V15H17V17H15M11 17V15H13V17H11Z" /></path></svg>',
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

    // model of a Silverpeas image link in GrapeJS
    const imageLinkModel = {
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
            'href': 'javascript:void(0)',
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
          });
          model.addAttributes(newAttr);
        }.bind(this));
      }
    };

    // adding our image link component into the GrapeJS editor by specifying, among others things,
    // its model.
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : __componentTypeChecker(componentType),
      model : imageLinkModel,
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
            });
          });
          this.model.find('img').forEach(function(model) {
            model.set({
              copyable : false,
              draggable : false,
              removable : false
            });
            initOptions.tools.updateToolbar(model);
          });
        }
      }
    });

    // define how and where the image link component is rendered within the component blocks
    // selector in GrapeJS
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('imageWithLinkBlockTitle'),
      media : '<svg viewBox="0 0 24 24"><path fill="currentColor" d="M21,3H3C2,3 1,4 1,5V19A2,2 0 0,0 3,21H21C22,21 23,20 23,19V5C23,4 22,3 21,3M5,17L8.5,12.5L11,15.5L14.5,11L19,17H5Z"></path></svg>',
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

    // adding a custom simple textual block by specifying, among others things, its model
    editor.DomComponents.addType(componentType, {
      /**
       * This method permits to identify a component when code is added manually.
       * @param el an HTML element.
       * @returns {{type: string}}
       */
      isComponent : function(el) {
        if (el?.tagName && el.tagName.toLowerCase() === 'div' &&
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

    // define how and where the simple block component is rendered within the component blocks
    // selector in GrapeJS
    editor.BlockManager.add(componentType, {
      label : sp.i18n.get('simpleBlockTitle'),
      media: '<svg viewBox="0 0 24 24"> <path fill="currentColor" d="M2 20h20V4H2v16Zm-1 0V4a1 1 0 0 1 1-1h20a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1Z"/> </svg>',
      attributes : {
        'class' : 'sp-panel-btn-simple-bloc'
      },
      content: {
        type : componentType
      }
    });
  }
})();