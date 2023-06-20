/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

  const commonAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(
      webContext + '/util/javaScript/vuejs/components/silverpeas-common-templates.jsp');

  /**
   * silverpeas-operation-creation-area is an HTML element which is built to provide the equivalent
   * to <view:areaOfOperationOfCreation/>.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-operation-creation-area></silverpeas-operation-creation-area>
   */
  SpVue.component('silverpeas-operation-creation-area', {
    template : '<div id="menubar-creation-actions" ref="container"></div>',
    created : function() {
      YAHOO.util.Event.onContentReady("menuwithgroups", function() {
        const $creationArea = $(this.$refs.container);
        if ($creationArea.length > 0) {
          $('.menubar-creation-actions-item').appendTo($creationArea);
          $('a', $creationArea).css({'display' : ''});
          $creationArea.css({'display' : 'block'});
        }
        setTimeout(applyTokenSecurityOnMenu, 0);
      }.bind(this));
    }
  });

  /**
   * silverpeas-inline-message is an HTML element to render a Silverpeas's inline message by VueJs.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-inline-message>
   *            <span>My message</span>
   *          </silverpeas-inline-message>
   */
  SpVue.component('silverpeas-inline-message', {
    template : '<div class="inlineMessage"><slot></slot></div>'
  });

  /**
   * silverpeas-button-pane is an HTML element which is built to contain silverpeas-button elements.
   * Nevertheless, all kinds of HTML or VueJS component can be included.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-button-pane>
   *            <silverpeas-button v-bind:click="...">OK<silverpeas-button>
   *            <silverpeas-button onclick="...">Cancel<silverpeas-button>
   *          </silverpeas-button-pane>
   */
  SpVue.component('silverpeas-button-pane', {
    template : '<div class="silverpeas-button-pane sp_buttonPane"><slot></slot></div>'
  });

  /**
   * silverpeas-button is an HTML element to render a button in a Silverpeas way by using the
   * VueJS framework.
   *
   * Two ways to perform a click event:
   * - one way to execute a method of a VueJS component. In that case 'v-bind' directive and
   * 'native' modifier will be required. The 'native' modifier is mandatory when the native event
   * on a VueJS component is aimed. See example 1.
   * - the second way to execute an external JS code. Just fill 'onclick' attribute. See example 2.
   *
   * @param title (Optional) if filled it adds a title on the button.
   * @param iconUrl (Optional) if filled the button has icon clickable behavior.
   *
   * @example 1 <silverpeas-button v-bind:click="methodOfVue()">OK<silverpeas-button>
   * @example 2 <silverpeas-button onclick="methodOfVanillaJs">Cancel<silverpeas-button>
   */
  SpVue.component('silverpeas-button',
    commonAsyncComponentRepository.get('button', {
      emits : ['click'],
      props : {
        title : {
          'type' : String,
          'default' : ''
        },
        iconUrl : {
          'type' : String,
          'default' : undefined
        }
      },
      computed : {
        isIconBehavior : function() {
          return !!this.iconUrl;
        }
      }
    }));

  /**
   * silverpeas-list is an HTML element to render a common list in a Silverpeas way by using the
   * VueJS framework.
   *
   * One attribute is handled:
   * @param items permits to handle some UI displays.
   * @param itemFeminineGender (Optional) permits to indicate the gender of the managed data (for
   *     built-in labels).
   * @param noItemLabel (Optional) permits to override the default label displayed when no item
   *     exists.
   *
   * There is 3 template zones handled:
   * - the "before" one
   * - the "default" one (the body)
   * - the "after" one
   *
   * @see silverpeas-list-item documentation in order to get example about the template zone use.
   * @example <silverpeas-list>
   *            <silverpeas-list-item v-for="item in items">{{item}}</silverpeas-list-item>
   *          <silverpeas-list>
   */
  SpVue.component('silverpeas-list',
    commonAsyncComponentRepository.get('list', {
      mixins : [VuejsI18nTemplateMixin],
      emits : ['before-enter', 'enter', 'after-enter', 'before-leave', 'leave', 'after-leave'],
      props : {
        items : {
          'type' : Array,
          'required' : true
        },
        itemFeminineGender : {
          'type' : Boolean,
          'default' : false
        },
        noItemLabel : {
          'type' : String,
          'default' : 'N/A'
        },
        withFadeTransition : {
          'type' : Boolean,
          'default' : false
        }
      },
      computed : {
        isData : function() {
          return this.items.length > 0;
        },
        noItemMessage : function() {
          const label = this.noItemLabel !== 'N/A'
              ? this.noItemLabel
              : this.itemFeminineGender
                  ? this.messages.noItemLabelFemale
                  : this.messages.noItemLabel;
          return '<span>' + label + '</span>';
        }
      }
    }));

  /**
   * silverpeas-list is an HTML element to render a common list in a Silverpeas way by using the
   * VueJS framework.
   *
   * There is 4 template zones handled:
   * - the "header" one
   * - the "default" one (the body)
   * - the "actions" one
   * - the "footer" one
   *
   * @example 1 <silverpeas-list-item>
   *              <div>Content</div>
   *            <silverpeas-list-item>
   *
   * @example 2 <silverpeas-list-item>
   *              <span v-slot:header>An header</span>
   *              <div>Content</div>
   *              <template v-slot:actions>
   *                <silverpeas-button>Save</silverpeas-button>
   *                <silverpeas-button>Delete</silverpeas-button>
   *              </template>
   *              <span v-slot:footer>An footer</span>
   *            <silverpeas-list-item>
   */
  SpVue.component('silverpeas-list-item',
    commonAsyncComponentRepository.get('list-item'));

  /**
   */
  SpVue.component('silverpeas-popin',
    commonAsyncComponentRepository.get('popin', {
      mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
      emits : ['open', 'close'],
      props : {
        'type' : {
          'type' : String,
          'default' : 'validation'
        },
        'title' : {
          'type' : String,
          'default' : ''
        },
        'dialogClass' : {
          'type' : String,
          'default' : ''
        },
        'minWidth' : {
          'type' : Number,
          'default' : 500
        },
        'maxWidth' : {
          'type' : Number,
          'default' : 800
        },
        'openPromise' : {
          'type' : Promise,
          'default' : undefined
        }
      },
      data : function() {
        return {
          jqDialog : undefined
        };
      },
      created : function() {
        this.extendApiWith({
          open : this.open,
          close : this.close
        });
      },
      methods : {
        open : function(options) {
          Vue.nextTick(function() {
            if (!this.jqDialog) {
              setTimeout(function() {
                this.jqDialog.on('dialogclose', function() {
                  this.$emit('close');
                }.bind(this));
              }.bind(this), 0);
            }
            this.jqDialog = jQuery(this.$refs.container);
            const settings = extendsObject({
              dialogClass : this.dialogClass,
              title : this.title,
              minWidth : this.minWidth,
              maxWidth : this.maxWidth,
              openPromise : this.openPromise
            }, options);
            const __onOpen = function() {
              this.$emit('open');
            }.bind(this);
            if (sp.promise.isOne(settings.openPromise)) {
              settings.openPromise = settings.openPromise.then(__onOpen);
            } else {
              setTimeout(__onOpen, 0);
            }
            this.jqDialog.popup(this.type, settings);
          }.bind(this));
        },
        close : function() {
          this.jqDialog.popup('close');
        }
      }
    }));

  /**
   * This mixin is a common way to initialize the management of a popin which is using a
   * silverpeas-pane.
   *
   * When the different APIs are retrieved, the API of the form pane popin is initialized.
   * It is possible to complete the default API by implementing <code>initFormPanePopinApi</code>
   * method.
   *
   * Please call the right setter on right api event.
   *
   * Default method <code>open</code> is implemented and can be overridden if necessary.
   *
   * Method <code>validate</code> MUST be implemented. It is called after that the user validate
   * the form and just before optional manual popin callback.
   *
   * Example of template:
   * <pre>
   *   ...
   *   <silverpeas-popin v-on:api="setPopinApi"
   *                     v-bind:title="title"
   *                     type="validation">
   *     <silverpeas-form-pane v-on:api="setFormPaneApi"
   *                           v-bind:manual-actions="true"
   *                           v-bind:mandatory-legend="true">
   *       <silverpeas-add-files-form v-on:api="setFormApi"
   *                                  v-bind:is-document-template-enabled="${isDocumentTemplateEnabled}"
   * v-bind:is-i18n-content="isI18nContent"
   * v-bind:i18n-content-language="i18nContentLanguage"></silverpeas-add-files-form>
   *        ...
   * </pre>
   */
  const __initApiWhenReady = function(cmpInstance) {
    if (!cmpInstance.apiInitialized && cmpInstance.popinApi && cmpInstance.formPaneApi &&
        cmpInstance.formApi) {
      cmpInstance.apiInitialized = true;
      cmpInstance.extendApiWith(cmpInstance.initFormPanePopinApi({
        open : cmpInstance.open,
        formApi : cmpInstance.formApi
      }));
    }
  };
  window.VuejsDefaultFormPanePopinApiMixin = {
    mixins : [VuejsApiMixin],
    data : function() {
      return {
        apiInitialized : false,
        popinApi : undefined,
        formPaneApi : undefined,
        formApi : undefined
      };
    },
    methods : {
      setPopinApi : function(popinApi) {
        this.popinApi = popinApi;
        __initApiWhenReady(this);
      },
      setFormPaneApi : function(formPaneApi) {
        this.formPaneApi = formPaneApi;
        __initApiWhenReady(this);
      },
      setFormApi : function(formApi) {
        this.formApi = formApi;
        __initApiWhenReady(this);
      },
      initFormPanePopinApi : function(defaultApi) {
        return defaultApi;
      },
      open : function(options) {
        const __settings = options || {};
        const __callback = __settings.callback;
        const __callbackOnClose = __settings.callbackOnClose;
        __settings.callback = function() {
          return this.formPaneApi.validate().then(function(formPaneData) {
            const promises = [];
            let finalResult;
            function __chainResults(result) {
              if (sp.promise.isOne(result)) {
                promises.push(result);
              } else {
                finalResult = result;
              }
            }
            __chainResults(this.validate(formPaneData));
            if (typeof __callback === 'function') {
              __chainResults(__callback(formPaneData));
            }
            if (promises.length) {
              formPaneData.validationFormPromise = sp.promise.whenAllResolved(promises);
              return formPaneData.validationFormPromise;
            }
            return finalResult;
          }.bind(this));
        }.bind(this);
        __settings.callbackOnClose = function() {
          if (__callbackOnClose) {
            return __callbackOnClose();
          }
        }
        this.popinApi.open(__settings);
      },
      validate : function(formPaneData) {
        throw new Error("validate method MUST be implemented when VuejsDefaultFormPanePopinApiMixin is used");
      }
    }
  }

  /**
   * Common implementation for components which have to display a popin from body layout to the top
   * fo the layout.
   *
   * 1) Add it to a component or a vue instance by attribute mixins. For example:
   * <pre>
   *    SpVue.component('my-component', {
   *      ...
   *      mixins : [VuejsTopPopinMixin]
   *      ...
   *    });
   * </pre>
   *
   * 2) Register into data a variable dedicated to the popin api instance. For example:
   * <pre>
   *    SpVue.component('my-component', {
   *      ...
   *      data : function() {
   *        return {
   *          topPopin : undefined
   *        };
   *      }
   *      ...
   *    });
   * </pre>
   *
   * 3) Initialize the popin api from "created" hook by calling the method
   * 'registerTopPopinApiName' with the name of the variable defined in step 2. For example:
   * <pre>
   *    SpVue.component('my-component', {
   *      ...
   *      created : function() {
   *        ...
   *        this.registerTopPopinApiName('topPopin');
   *        ...
   *      }
   *      ...
   *    });
   * </pre>
   *
   * 4) Opening by calling method open on the defined top popin api. For example:
   * <pre>
   *   ...
   *   const popinSettings = extendsObject({
   *     title : this.messages.acceptCharterTitle,
   *     minWidth : 680
   *   }, params);
   *   this.topPopinApi.open('acceptation', popinSettings, function(ctx) {
   *     return __promiseCharterContentWith(ctx, this.community);
   *   }.bind(this));
   *   ...
   * </pre>
   * The method open has 3 parameters:
   * <pre>
   *   type: the type of the popin (cf. silverpeas-popup plugin)
   *   popinSettings : the settings dedicated to the popin (cf. silverpeas-popup plugin)
   *   renderFct : a function in charge to provide the HTML (a root DOM element). This method has
   *               one parameter called ctx which is an object with several attributes: {
   *                  $W : the instance of the top window,
   *                  $Doc : the instance of document from top window,
   *                  $rootContainer : the instance of the root container of the popin,
   *                  popinSettings : an object about the settings of the popin (cf.
   *                                  silverpeas-popup plugin)
   *               }
   * </pre>
   */
  const __uniqueID = new function() {
    let count = 0;
    this.newId = function() {
      return count++;
    };
  };
  window.VuejsTopPopinMixin = {
    methods : {
      registerTopPopinApiName : function(popinApiName) {
        const $W = top.window;
        const $Doc = top.window.document;
        const rootDomId = 'topPopin-' + __uniqueID.newId() + '-' + popinApiName;
        const __cleanDom = function() {
          const $rootContainer = $Doc.querySelector('#' + rootDomId);
          if ($rootContainer) {
            $rootContainer.remove();
          }
        };
        this[popinApiName] = {
          jqDialog : undefined,
          spPopinProps : undefined,
          open : function(type, popinSettings, renderFct) {
            this.spPopinProps = extendsObject({
              'type' : 'validation',
              'title' : '',
              'dialogClass' : '',
              'minWidth' : 500,
              'maxWidth' : 800,
              'maxHeight' : jQuery($W).height() * 0.8,
              'openPromise' : undefined,
              'jsSrcUrls' : [],
              'cssHrefUrls' : []
            }, popinSettings);
            sp.navigation.mute();
            // Initializing the resulting html container
            __cleanDom();
            const $rootContainer = $Doc.createElement('div');
            $rootContainer.setAttribute('id', rootDomId);
            $rootContainer.setAttribute('class', 'silverpeas-top-popin');
            $Doc.body.appendChild($rootContainer);
            // Promised Content
            const htmlContent = renderFct({
              $W : $W,
              $Doc : $Doc,
              $rootContainer : $rootContainer,
              popinSettings : this.spPopinProps
            });
            if (htmlContent) {
              $rootContainer.appendChild(htmlContent);
            }
            if (this.spPopinProps.jsSrcUrls) {
              this.spPopinProps.jsSrcUrls.forEach(function(params) {
                const url = extendsObject({
                  src : undefined,
                  deferred : sp.promise.deferred()
                }, sp.param.singleToObject('src', params));
                const $script = $Doc.createElement('script');
                $script.addEventListener('load', function() {
                  url.deferred.resolve();
                });
                $script.setAttribute('src', url.src);
                $script.setAttribute('type', 'text/javascript');
                $script.setAttribute('language', 'Javascript');
                $rootContainer.appendChild($script);
              });
            }
            if (this.spPopinProps.cssHrefUrls) {
              this.spPopinProps.cssHrefUrls.forEach(function(params) {
                const url = extendsObject({
                  href : undefined,
                  deferred : sp.promise.deferred()
                }, sp.param.singleToObject('href', params));
                const $css = $Doc.createElement('link');
                $css.addEventListener('load', function() {
                  url.deferred.resolve();
                });
                $css.setAttribute('href', url.href);
                $css.setAttribute('type', 'text/css');
                $css.setAttribute('rel', 'stylesheet');
                $rootContainer.appendChild($css);
              });
            }
            const _callbackOnClose = popinSettings.callbackOnClose;
            this.spPopinProps.callbackOnClose = function() {
              sp.navigation.unmute();
              setTimeout(function() {
                this.close();
              }.bind(this), 0);
              if (typeof _callbackOnClose === 'function') {
                _callbackOnClose();
              }
            }.bind(this);
            this.jqDialog = $W.jQuery($rootContainer);
            this.jqDialog.popup(type, this.spPopinProps);
          },
          close : function() {
            if (this.jqDialog) {
              this.jqDialog.popup('destroy');
              this.jqDialog = undefined;
            }
            __cleanDom();
          }
        };
      },
    }
  };

  /**
   * silverpeas-attached-popin handles the display of a popin attached to an HTML element.
   *
   * It defines several attributes:
   * {toElement} - String or HTML element - the css selector of the INPUT instance to manage.
   * {minWidth} - Number (optional) - the minimal width of the popin.
   * {maxWidth} - Number (optional) - the maximal width of the popin.
   * {minHeight} - Number (optional) -  the minimal height of the popin.
   * {maxHeight} - Number (optional) -  the maximal height of the popin.
   * {openPromise} - Promise (optional) - a promise which MUST be resolved before displaying the popin.
   * {scrollEndEvent} - Number (optional) - If filled, it activates the management of scroll event
   * which permits to send event when the scroll is at end (or almost). The value to filled is the
   * size in pixel before the real scroll end at which 'scroll-end' event is emitted.
   *
   * The following example illustrates the only one possible use of the directive:
   * <silverpeas-attached-popin ...>...</silverpeas-attached-popin>
   */
  const VuejsAttachedPopinMixin = {
    emits : ['scroll-end'],
    props : {
      'toElement' : {
        'required' : true
      },
      'minWidth' : {
        'type' : Number,
        'default' : undefined
      },
      'maxWidth' : {
        'type' : Number,
        'default' : undefined
      },
      'minHeight' : {
        'type' : Number,
        'default' : undefined
      },
      'maxHeight' : {
        'type' : Number,
        'default' : undefined
      },
      scrollEndEvent : {
        'type' : Number,
        'default' : undefined
      },
      anchor : {
        'type' : String,
        'default' : 'left'
      },
      fadeDurationType : {
        'type' : String,
        'default' : 'normal'
      }
    }
  };
  SpVue.component('silverpeas-attached-popin',
    commonAsyncComponentRepository.get('attached-popin', {
      mixins : [VuejsAttachedPopinMixin],
      data : function() {
        return {
          elBase : undefined,
          domContext : {
            lastScrollHeight : 0,
            scrollEndEventEmitted : false
          }
        };
      },
      created : function() {
        if (typeof this.toElement === 'string') {
          this.elBase = document.querySelector('#' + this.toElement);
        } else {
          this.elBase = this.toElement;
        }
      },
      mounted : function() {
        this.positionMonitor.position();
        if (typeof this.scrollEndEvent === 'number') {
          this.$refs.content.addEventListener('scroll', this.scrollListener);
        }
        window.addEventListener('resize', function() {
          this.positionMonitor.position();
        }.bind(this));
      },
      beforeUnmount : function() {
        this.$refs.content.removeEventListener('scroll', this.scrollListener);
      },
      methods : {
        scrollListener : function() {
          const lastScrollHeight = this.domContext.lastScrollHeight;
          const scrollHeight = this.$refs.content.scrollHeight;
          if (this.domContext.scrollEndEventEmitted && lastScrollHeight === scrollHeight) {
            return;
          }
          this.domContext.lastScrollHeight = scrollHeight;
          this.domContext.scrollEndEventEmitted = false;
          const currentScroll = scrollHeight - this.$refs.content.scrollTop - this.$refs.content.offsetHeight;
          if (currentScroll <= this.scrollEndEvent) {
            this.domContext.scrollEndEventEmitted = true;
            this.$emit('scroll-end');
          }
        }
      },
      computed : {
        positionMonitor : function() {
          const positionMonitor = sp.element.createPositionManager(this.$refs.popin, this.elBase);
          const __options = {
            anchorPoint : {
              ofBase : 'bottom-right',
              ofAttached : 'top-right'
            }
          };
          if (this.anchor === 'center') {
            __options.anchorPoint.ofBase = 'bottom-center';
            __options.anchorPoint.ofAttached = 'top-center';
          } else if (this.anchor === 'left') {
            __options.anchorPoint.ofBase = 'bottom-left';
            __options.anchorPoint.ofAttached = 'top-left';
          }
          positionMonitor.setOptions(__options);
          return positionMonitor;
        }
      }
    }));

  /**
   * silverpeas-query-input-select handles the display of a popin attached to an INPUT element
   * dedicated to perform simple text queries.
   *
   * The component will not create an input into the DOM but will decorate an existant one with
   * additional behavior. When the user enter new characters into the input, 'query' event with the
   * query value is triggered if it exists a minimum of characters (5 by default) and after a
   * debounce time. When the caller gives the list of items, each item is displayed into an
   * attached popin, as a list. When the user selects an item, 'select' event with item instance is
   * triggered.
   *
   * The attributes of silverpeas-attached-popin are exposed.
   * It defines also additional attributes:
   * {inputTitle} - String (optional) - a title to set to input.
   * {inputPlaceholder} - String (optional) - a placeholder to set to input.
   * {items} - Array - an array of elements to be displayed
   * {minQueryLength} - Number (optional) - the length threshold to reach before the query is taken
   *                    into account.
   * {queryDebounce} - Number (optional) - the time in ms after that the query is taken into
   *                   account. 300ms by default.
   *
   * The following example illustrates a use of the component:
   * @example:
   * <silverpeas-query-input-select v-bind:toElement="target"
   *                                v-bind:items="addresses"
   *                                v-bind:min-query-length="5"
   *                                v-bind:query-debounce="500"
   *                                v-on:query="performQuery"
   *                                v-on:select="$emit('select', $event)"
   *                                v-slot="{ item }">
   *   {{ item.getLabel() }}
   * </silverpeas-query-input-select>
   */
  SpVue.component('silverpeas-query-input-select',
    commonAsyncComponentRepository.get('query-input-select', {
      emits : ['query', 'select'],
      mixins : [VuejsApiMixin, VuejsAttachedPopinMixin],
      props : {
        inputTitle : {
          'type' : String,
          'default' : undefined
        },
        inputPlaceholder : {
          'type' : String,
          'default' : undefined
        },
        items : {
          'type' : Array,
          'required' : true
        },
        minQueryLength : {
          'type' : Number,
          'default' : 3
        },
        queryDebounce : {
          'type' : Number,
          'default' : 300
        }
      },
      data : function() {
        return {
          activeIndex : -1,
          lastValue : undefined,
          mouseOver : false,
          forceClose : false
        };
      },
      created : function() {
        this.extendApiWith({
          replayQuery : function() {
            this.performQuery();
          }
        });
      },
      mounted : function() {
        if (StringUtil.isDefined(this.inputTitle)) {
          this.toElement.setAttribute('title', this.inputTitle);
        }
        if (StringUtil.isDefined(this.inputPlaceholder)) {
          this.toElement.setAttribute('placeholder', this.inputPlaceholder);
        }
        this.toElement.setAttribute('autocomplete', 'off');
        this.toElement.addEventListener('focus', function() {
          this.mouseOver = false;
          this.forceClose = false;
        }.bind(this));
        this.toElement.addEventListener('blur', function() {
          if (!this.mouseOver) {
            this.forceClose = true;
          }
        }.bind(this));
        this.toElement.addEventListener('keyup', function(e) {
          e.preventDefault();
          e.stopPropagation();
          if (e.keyCode === 13) {
            this.selectCurrent();
          } else if (e.keyCode === 38) {
            this.previousActive();
          } else if (e.keyCode === 40) {
            this.nextActive();
          }
        }.bind(this));
        this.toElement.addEventListener('keyup', sp.debounce(function(e) {
          this.lastValue = this.value;
          this.value = e.target.value;
          if (e.keyCode !== 13 && this.lastValue !== this.value && this.performQuery()) {
            this.forceClose = false;
          }
        }.bind(this), this.queryDebounce));
      },
      methods : {
        setActiveIndex : function(index) {
          this.activeIndex = index;
        },
        previousActive : function() {
          if (this.display) {
            if (this.activeIndex === 0) {
              this.activeIndex = this.items.length - 1;
            } else {
              this.activeIndex--;
            }
          }
        },
        nextActive : function() {
          if (this.display) {
            if (this.activeIndex === (this.items.length - 1)) {
              this.activeIndex = 0;
            } else {
              this.activeIndex++;
            }
          }
        },
        selectCurrent : function() {
          if (this.display && this.activeIndex !== -1) {
            this.forceClose = true;
            this.$emit('select', this.items[this.activeIndex]);
          }
        },
        performQuery : function() {
          if (StringUtil.isDefined(this.value) && this.value.length >= this.minQueryLength) {
            this.$emit('query', this.value);
            return true;
          }
          return false;
        }
      },
      computed : {
        display : function() {
          if (this.items && this.activeIndex > (this.items.length - 1)) {
            this.activeIndex = this.items.length - 1;
          }
          return !this.forceClose && this.items && this.items.length > 0;
        }
      }
    }));

  /**
   * silverpeas-permalink is an HTML element to render a permalink in a Silverpeas's way by using
   * the VueJS framework.
   *
   * Attributes are listed into 'props' declarations:
   * @param link: the link representing the permalink.
   * @param label: the label displayed (into simple mode).
   * @param help: the help displayed (into simple mode).
   * @param iconUrl: (optional) if filled it replace the default icon url.
   * @param simple: (default true) if true, the VueJS version is displayed, otherwise the server
   *     version is rendered.
   * @param noHrefHook: (default false) if true, the spWindow Silverpeas's plugin does not handle
   *     the permalink.
   *
   * Some examples:
   * @example <silverpeas-permalink link="/Publication/3"></silverpeas-permalink>
   * @example <silverpeas-permalink link="/Publication/3"
   *     v-bind:simple="false"></silverpeas-permalink>
   */
  SpVue.component('silverpeas-permalink',
    commonAsyncComponentRepository.get('permalink', {
      mixins : [VuejsI18nTemplateMixin],
      props : {
        link : String,
        label : String,
        help : String,
        iconUrl : String,
        simple : {
          'type' : Boolean,
          'default' : true
        },
        noHrefHook : {
          'type' : Boolean,
          'default' : false
        }
      },
      methods : {
        renderFullTemplateUrl : function() {
          sp.ajaxRequest(webContext +
              '/util/javaScript/vuejs/components/silverpeas-permalink-wrapper.jsp')
              .withParam('link', encodeURIComponent(this.getFormattedPermalinkForWrapper()))
              .withParam('label', this.label ? encodeURIComponent(this.label) : this.label)
              .withParam('help', this.help ? encodeURIComponent(this.help) : this.help)
              .withParam('iconUrl', this.iconUrl)
              .send().then(function(request) {
            sp.updateTargetWithHtmlContent(this.$refs.fullContainer, request.responseText);
          }.bind(this));
        },
        getFormattedPermalinkForWrapper : function() {
          let result = this.link;
          if (this.link.startsWith(silverpeasUrl)) {
            result = this.link.replace(silverpeasUrl, webContext);
          } else if (!this.link.startsWith(webContext)) {
            result = webContext + this.link;
          }
          return result;
        },
        copyLink : function() {
          const $input = this.$el.querySelector('input');
          $input.select();
          document.execCommand('copy');
          notyInfo(this.messages.copyOk);
        }
      },
      computed : {
        isFull : function() {
          if (!this.simple) {
            this.renderFullTemplateUrl();
          }
          return !this.simple;
        }
      }
    }));

  /**
   * silverpeas-form-pane is an HTML element to render a form in a Silverpeas's way by using
   * the VueJS framework.
   * The aim is to get a standard for all entity to set by a form that the user fills.
   *
   * This component has to include into its body some components which are using
   *     'VuejsFormApiMixin'. Otherwise it will do nothing.
   *
   * There is 4 template zones handled:
   * - the "header" one
   * - the "default" one (the body)
   * - the "footer" one
   * - the "legend" one
   *
   * @param mandatoryLegend (Optional) if true, displaying the mandatory legend just before
   *     buttons.
   * @param manualActions (Optional) if true, handling manually the actions on the form with the
   *     api.
   *
   * @event data-update when the user has performed validation of data successfully and the new
   *     data are ready. If the attribute <code>validationFormPromise</code> is set on data given
   *     with this event, then the caller can handle a promise against the result of its processing.
   * @event validation-fail when validation of data has failed.
   * @event cancel when validation has been cancelled.
   */
  SpVue.component('silverpeas-form-pane',
    commonAsyncComponentRepository.get('form-pane', {
      mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
      emits : ['data-update', 'validation-fail', 'cancel'],
      provide : function() {
        return {
          rootFormApi : this.api,
          rootFormMessages : this.messages
        };
      },
      props : {
        mandatoryLegend : {
          "type" : Boolean,
          "default" : true
        },
        manualActions : {
          "type" : Boolean,
          "default" : false
        }
      },
      created : function() {
        function __sortComponentsByOffsets(compA, compB) {
          let result = compA.$el.offsetTop - compB.$el.offsetTop;
          if (result === 0) {
            result = compA.$el.offsetLeft - compB.$el.offsetLeft;
          }
          return result;
        }

        const formLabelValidationRegistry = [];
        const formInputValidationRegistry = [];
        const formValidationRegistry = [];

        /**
         * Performs validation input on all linked forms
         */
        const _validateInputs = function() {
          const __validationPromises = [];
          formInputValidationRegistry.sort(__sortComponentsByOffsets);
          for (let i = 0; i < formInputValidationRegistry.length; i++) {
            const validation = formInputValidationRegistry[i].api.validateFormInput();
            const validationType = typeof validation;
            const isPromiseValidation = sp.promise.isOne(validation);
            if (!isPromiseValidation && validationType === 'undefined') {
              sp.log.error('VuejsFormInputMixin - validate method must return a promise or a boolean value');
              return sp.promise.rejectDirectlyWith();
            }
            __validationPromises.push(isPromiseValidation ? validation : sp.promise.resolveDirectlyWith(validation));
          }
          let existsAtLeastOneError = false;
          return sp.promise.whenAllResolved(__validationPromises).then(function(validationResults) {
            validationResults.forEach(function(validationResult) {
              validationResult = typeof validationResult !== 'undefined' ? validationResult : true;
              existsAtLeastOneError = !validationResult || existsAtLeastOneError;
            });
            return (!SilverpeasError.show() && !existsAtLeastOneError)
                ? sp.promise.resolveDirectlyWith()
                : sp.promise.rejectDirectlyWith();
          });
        }.bind(this);

        /**
         * Performs validation on all linked forms
         */
        const _validate = function() {
          const __validationPromises = [];
          formValidationRegistry.sort(__sortComponentsByOffsets);
          for (let i = 0; i < formValidationRegistry.length; i++) {
            const validation = formValidationRegistry[i].api.validateForm();
            const validationType = typeof validation;
            const isPromiseValidation = sp.promise.isOne(validation);
            if (!isPromiseValidation && validationType === 'undefined') {
              sp.log.error('VuejsFormApiMixin - validate method must return a promise or a boolean value');
              return sp.promise.rejectDirectlyWith();
            }
            __validationPromises.push(isPromiseValidation ? validation : sp.promise.resolveDirectlyWith(validation));
          }
          let existsAtLeastOneError = false;
          return sp.promise.whenAllResolved(__validationPromises).then(function(validationResults) {
            validationResults.forEach(function(validationResult) {
              validationResult = typeof validationResult !== 'undefined' ? validationResult : true;
              existsAtLeastOneError = !validationResult || existsAtLeastOneError;
            });
            return (!SilverpeasError.show() && !existsAtLeastOneError)
                ? sp.promise.resolveDirectlyWith()
                : sp.promise.rejectDirectlyWith();
          });
        }.bind(this);

        /**
         * Performs updating of source data from internal one.
         */
        const _updateData = function() {
          let data = {};
          formValidationRegistry.forEach(function(formComp) {
            const result = formComp.api.updateFormData(data);
            if (result) {
              data = result;
            }
          });
          return data;
        }.bind(this);

        const __setLinkedCmpIfNotSet = function(thisCmp, linkedName, cmp) {
          if (!thisCmp['linked' + linkedName]) {
            thisCmp['linked' + linkedName] = cmp;
          }
        };

        const __unsetLinkedCmpIfSet = function(thisCmp, linkedName) {
          if (thisCmp['linked' + linkedName]) {
            thisCmp['linked' + linkedName] = undefined;
          }
        };

        this.extendApiWith({
          handleFormLabelComponent : function(formLabelComponent) {
            formLabelValidationRegistry.push(formLabelComponent);
            const labelIdRef = 'label-' + formLabelComponent.forId;
            formLabelValidationRegistry[labelIdRef] = formLabelComponent;
            const linkedInput = formInputValidationRegistry[labelIdRef];
            if (linkedInput) {
              __setLinkedCmpIfNotSet(linkedInput, 'LabelCmp', formLabelComponent);
              __setLinkedCmpIfNotSet(formLabelComponent, 'InputCmp', linkedInput);
            }
          },
          unhandleFormLabelComponent : function(formLabelComponent) {
            formLabelValidationRegistry.removeElement(formLabelComponent);
            const labelIdRef = 'label-' + formLabelComponent.forId;
            delete formLabelValidationRegistry[labelIdRef];
            const linkedInput = formInputValidationRegistry[labelIdRef];
            if (linkedInput) {
              __unsetLinkedCmpIfSet(linkedInput, 'LabelCmp');
            }
          },
          handleFormInputComponent : function(formInputComponent) {
            formInputValidationRegistry.push(formInputComponent);
            const labelIdRef = 'label-' + formInputComponent.linkedLabelId;
            formInputValidationRegistry[labelIdRef] = formInputComponent;
            const linkedLabel = formLabelValidationRegistry[labelIdRef];
            if (linkedLabel) {
              __setLinkedCmpIfNotSet(linkedLabel, 'InputCmp', formInputComponent);
              __setLinkedCmpIfNotSet(formInputComponent, 'LabelCmp', linkedLabel);
            }
          },
          unhandleFormInputComponent : function(formInputComponent) {
            formInputValidationRegistry.removeElement(formInputComponent);
            const labelIdRef = 'label-' + formInputComponent.linkedLabelId;
            delete formInputValidationRegistry[labelIdRef];
            const linkedLabel = formLabelValidationRegistry[labelIdRef];
            if (linkedLabel) {
              __unsetLinkedCmpIfSet(linkedLabel, 'InputCmp');
            }
          },
          handleFormComponent : function(formComp) {
            formValidationRegistry.push(formComp);
          },
          unhandleFormComponent : function(formComp) {
            formValidationRegistry.removeElement(formComp);
          },
          validate : function() {
            notyReset();
            const __errMsg = "silverpeas-form - no data updated...";
            return _validateInputs().then(function() {
              return _validate().then(function() {
                const data = _updateData();
                if (data) {
                  this.$emit('data-update', data);
                  if (sp.promise.isOne(data.validationFormPromise)) {
                    return data.validationFormPromise;
                  }
                  return data;
                } else {
                  sp.log.error(__errMsg);
                  return sp.promise.rejectDirectlyWith(__errMsg);
                }
              }.bind(this))['catch'](function() {
                sp.log.debug(__errMsg);
                this.$emit('validation-fail', {
                  failOnInputValidation : false,
                  failOnFormValidation : true
                });
                return sp.promise.rejectDirectlyWith(__errMsg);
              }.bind(this));
            }.bind(this))['catch'](function() {
              sp.log.debug(__errMsg);
              this.$emit('validation-fail', {
                failOnInputValidation : true,
                failOnFormValidation : false
              });
              return sp.promise.rejectDirectlyWith(__errMsg);
            }.bind(this));
          },
          cancel : function() {
            notyReset();
            this.$emit('cancel');
          },
          errorMessage : function() {
            return {
              add : function(message) {
                SilverpeasError.add(message);
              },
              none : function() {
                return !SilverpeasError.existsAtLeastOne();
              },
              show : function() {
                return SilverpeasError.show();
              }
            }
          }
        });
      },
      computed : {
        isHeader : function() {
          return !!this.$slots['header'];
        },
        isBody : function() {
          return !!this.$slots['default'];
        },
        isFooter : function() {
          return !!this.$slots['footer'];
        },
        isLegend : function() {
          return !!this.$slots['legend'] || this.mandatoryLegend;
        },
        isManualActions : function() {
          return this.manualActions;
        }
      }
    }));

  /**
   * silverpeas-link is an HTML element which display an HTML link.
   *
   * @param iconUrl (Optional) can be used to override the default icon url.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-mandatory-indicator></silverpeas-mandatory-indicator>
   */
  SpVue.component('silverpeas-link',
      commonAsyncComponentRepository.get('link', {
        props : {
          title : {
            'type': String,
            'default' : ''
          }
        },
        data : function() {
          return {
            ready : false,
            tipApi : undefined
          }
        },
        mounted : function() {
          this.ready = true;
        },
        beforeUnmount : function() {
          if (this.tipApi) {
            this.tipApi.destroy(true);
          }
        },
        methods : {
          hideTitle : function() {
            if (this.tipApi) {
              this.tipApi.hide();
            }
          }
        },
        computed : {
          help : function() {
            if (this.ready) {
              if (this.title) {
                if (!this.tipApi) {
                  this.tipApi = TipManager.simpleHelp(this.$el, this.title, {
                    show : {
                      delay : 1000
                    }
                  });
                } else {
                  this.tipApi.set('content.text', this.title);
                }
              } else if (this.tipApi) {
                this.tipApi.destroy(true);
                this.tipApi = undefined;
              }
            }
            return this.title;
          }
        }
      }));

  /**
   * silverpeas-mandatory-indicator is an HTML element which display an indicator of mandatory form
   * input.
   *
   * @param iconUrl (Optional) can be used to override the default icon url.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-mandatory-indicator></silverpeas-mandatory-indicator>
   */
  SpVue.component('silverpeas-mandatory-indicator', {
    template : '<span>&#160;<img v-bind:src="iconUrl" height="5" width="5" alt=""/></span>',
    props : {
      iconUrl : {
        'type' : String,
        'default' : webContext + '/util/icons/mandatoryField.gif'
      }
    }
  });

  SpVue.component('silverpeas-label',
      commonAsyncComponentRepository.get('label', {
        inject : {
          rootFormApi : {
            'default' : undefined
          },
          rootFormMessages : {
            'default' : undefined
          }
        },
        props : {
          id: {
            'type': String,
            'default': undefined
          },
          'for': {
            'type': String,
            'mandatory': true
          },
          value : {
            'type': String,
            'mandatory': true
          },
          mandatory : {
            'type': Boolean,
            'default': false
          }
        },
        data : function() {
          return {
            linkedInputCmp : undefined
          }
        },
        mounted : function() {
          this.rootFormApi.handleFormLabelComponent(this);
        },
        unmounted : function() {
          this.rootFormApi.unhandleFormLabelComponent(this);
        },
        computed : {
          forId : function() {
            return this['for'];
          },
          isMandatory : function() {
            return this.mandatory && (!this.linkedInputCmp || this.linkedInputCmp.isMandatory)
          }
        }
      }));

  const __FormInputMixin = {
    mixins : [VuejsFormInputMixin],
    props : {
      placeholder: {
        'type': String,
        'default': ''
      },
      size: {
        'type': Number,
        'default': 60
      },
      maxlength: {
        'type': Number,
        'default': 150
      }
    },
    mounted : function() {
      this.updateInputElementAttribute('placeholder');
    },
    methods : {
      getInputElementName : function() {
        return 'input';
      }
    },
    watch : {
      'placeholder' : function() {
        this.updateInputElementAttribute('placeholder');
      }
    }
  };

  SpVue.component('silverpeas-text-input',
      commonAsyncComponentRepository.get('text-input', {
        mixins : [__FormInputMixin]
      }));

  SpVue.component('silverpeas-hidden-input',
      commonAsyncComponentRepository.get('hidden-input', {
        mixins : [__FormInputMixin]
      }));

  SpVue.component('silverpeas-url-input',
      commonAsyncComponentRepository.get('url-input', {
        mixins : [__FormInputMixin],
        created : function() {
          this.extendApiWith({
            validateFormInput : function() {
              let urlError;
              try {
                StringUtil.isDefined(this.modelValue) && new URL(this.modelValue);
                urlError = false;
              } catch (error) {
                urlError = true;
              }
              if (this.rootFormApi) {
                if (urlError) {
                  this.rootFormApi.errorMessage().add(
                      this.formatMessage(this.rootFormMessages.mustContainsURLMessage,
                          this.getLabelByForAttribute(this.id)));
                }
              }
              return !urlError;
            }
          });
        }
      }));

  SpVue.component('silverpeas-multiline-text-input',
      commonAsyncComponentRepository.get('multiline-text-input', {
        mixins : [VuejsFormInputMixin],
        props : {
          cols: {
            'type': Number,
            'default': 60
          },
          rows: {
            'type': Number,
            'default': 7
          },
          maxlength: {
            'type': Number,
            'default': 2000
          },
          autoresize: {
            'type': Boolean,
            'default': false
          }
        },
        created : function() {
          this.extendApiWith({
            validateFormInput : function() {
              const mandatoryError = this.validateMandatory();
              const maxLengthError = this.modelValue && this.modelValue.nbChars() > this.maxlength;
              if (this.rootFormApi) {
                if (maxLengthError) {
                  this.rootFormApi.errorMessage().add(
                      this.formatMessage(this.rootFormMessages.nbMax,
                          [this.getLabelByForAttribute(this.labelId ? this.labelId : this.id), this.maxlength]));
                }
              }
              return !mandatoryError && !maxLengthError;
            }
          });
        },
        mounted : function() {
          if (this.autoresize) {
            sp.dom.includePlugin('autoresize').then(function() {
              jQuery(this.$el.querySelector('textarea')).autoResize();
            }.bind(this));
          }
        },
        methods : {
          getInputElementName : function() {
            return 'textarea';
          }
        }
      }));

  const __FormRadioOrCheckboxMixin = {
    mixins : [VuejsFormInputMixin],
    props : {
      value : {
        'type' : String,
        'default' : ''
      }
    },
    methods : {
      getInputElementName : function() {
        return 'input';
      }
    },
    computed : {
      cssClasses : function() {
        const cssClasses = this.inputClass.split(' ');
        if (this.value === this.model) {
          cssClasses.push('checked');
        }
        return cssClasses;
      }
    }
  };

  SpVue.component('silverpeas-radio-input',
      commonAsyncComponentRepository.get('radio-input', {
        mixins : [__FormRadioOrCheckboxMixin]
      }));

  SpVue.component('silverpeas-checkbox-input',
      commonAsyncComponentRepository.get('checkbox-input', {
        mixins : [__FormRadioOrCheckboxMixin]
      }));

  const __FormSelectMixin = {
    mixins : [VuejsFormInputMixin],
    methods : {
      getInputElementName : function() {
        return 'select';
      }
    }
  };

  SpVue.component('silverpeas-select',
      commonAsyncComponentRepository.get('select', {
        mixins : [__FormSelectMixin]
  }));

  SpVue.component('silverpeas-select-language',
      commonAsyncComponentRepository.get('select-language', {
        mixins : [__FormSelectMixin]
      }));

  SpVue.component('silverpeas-file-input',
      commonAsyncComponentRepository.get('file-input', {
        mixins : [__FormInputMixin],
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
          handledTypes : {
            'type' : Array,
            'default' : ['*/*']
          },
          modelValue : {
            'type': Object,
            'default': {}
          }
        },
        data : function() {
          return {
            deleteOriginal : false,
            file : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            clear : this.clear,
            /**
             * Mandatory implementation needed by {@link VuejsFormInputMixin}.
             * @returns {boolean}
             */
            validateFormInput : function() {
              let isError = false;
              if (this.isMandatory && !this.fileName) {
                isError = true;
                this.rootFormApi.errorMessage().add(
                    this.formatMessage(this.rootFormMessages.mandatory,
                        this.getLabelByForAttribute(this.id)));
              }
              return !isError;
            }
          });
          this.clear();
        },
        methods : {
          clear : function() {
            this.deleteOriginal = false;
            this.file = undefined;
            this.refreshState();
          },
          updateModel : function() {
            const model = {};
            if (this.file) {
              model.fileInputName = this.name;
              model.file = this.file;
            }
            if(this.deleteOriginal) {
              model.deleteOriginal = this.deleteOriginal;
            }
            this.$emit('update:modelValue', model);
          },
          newFile : function() {
            const [file] = this.$refs.newFile.files;
            if (file) {
              if (this.checkFile(file)) {
                this.deleteOriginal = false;
                this.file = file;
              }
              this.refreshState();
            }
          },
          deleteFile : function() {
            this.file = undefined;
            this.deleteOriginal = true;
            this.refreshState();
          },
          refreshState : function() {
            if (!this.file && this.$refs.newFile) {
              this.$refs.newFile.value = '';
            }
            this.updateModel();
          },
          checkFile : function(file) {
            if (this.getSpecifiedHandledMimeTypes.length && this.getSpecifiedHandledMimeTypes.indexOf(file.type) < 0) {
              SilverpeasError.add(this.formatMessage(this.messages.badFormatErrMsg, [
                file.name,
                this.getSpecifiedHandledExtensions.joinWith({
                  separator : ', ',
                  lastSeparator : ' ' + this.messages.orMsgPart + ' '
                })
              ]));
            }
            return !SilverpeasError.show();
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
          displayDelAction : function () {
            return !this.deleteOriginal && !this.isMandatory && this.fileName;
          },
          titleHelp : function() {
            const help = [];
            help.push(this.title);
            if (this.getSpecifiedHandledExtensions.length) {
              help.push(this.formatMessage(this.messages.expectedFormatMsg, [
                this.getSpecifiedHandledExtensions.joinWith({
                  separator : ', ',
                  lastSeparator : ' ' + this.messages.orMsgPart + ' '
                })
              ],{
                styles : {
                  bold : false
                }
              }));
            }
            return help
                .filter(function(value) {
                  return !!value;
                })
                .join('\n');
          },
          titleHtml : function() {
            return this.titleHelp.convertNewLineAsHtml();
          },
          getSpecifiedHandledExtensions : function() {
            return this.getSpecifiedHandledMimeTypes.map(function(mimeType) {
              return mimeType.replace(/^.*\/+([^/]+)/g, '$1');
            });
          },
          getSpecifiedHandledMimeTypes : function() {
            return this.handledTypes.filter(function(mimeType) {
              return mimeType !== '*/*';
            });
          },
          acceptedTypes : function () {
            return this.handledTypes.join(',');
          }
        }
      }));

  /**
   * silverpeas-link is an HTML element which display an HTML link.
   *
   * @param iconUrl (Optional) can be used to override the default icon url.
   *
   * The following example illustrates a possible use of the component:
   * @example <silverpeas-mandatory-indicator></silverpeas-mandatory-indicator>
   */
  SpVue.component('silverpeas-event-period',
      commonAsyncComponentRepository.get('event-period', {
        props : {
          period : {
            'type': Object,
            'required' : true
          }
        },
        computed : {
          startAsDate : function() {
            return this.$filters.displayAsDate(this.period.getStartDate());
          },
          startAsTime : function() {
            return this.$filters.displayAsTime(this.period.getStartDate());
          },
          endAsDate : function() {
            return this.$filters.displayAsDate(this.period.getEndDateForUI());
          },
          endAsTime : function() {
            return this.$filters.displayAsTime(this.period.getEndDateForUI());
          },
          isInDays : function() {
            return this.period.isInDays();
          },
          onSameDay : function() {
            return !this.period.onSeveralDays();
          }
        }
      }));
})();
