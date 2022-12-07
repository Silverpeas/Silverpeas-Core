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

/**
 * Making a proxy super method for calling Super.
 * This will applicable for mixin methods and extended component methods
 */
Vue.prototype.$super = function(__options) {
  // Creating a Proxy instance with options. This options will be our class/mixin/component name
  return new Proxy(__options, {
    get : function(_options, name) {
      // Checking if the given method is exist of method objects/list
      if (_options.methods && _options.methods[name]) {
        // If YES? just call and return the data
        return _options.methods[name].bind(this);
      }
    }.bind(this)
  })
};

(function() {
  window.VueJsAsyncComponentTemplateRepository = function(src) {
    const templatePromise = sp.ajaxRequest(src).send().then(function(request) {
      return request.responseText
    });
    this.get = function(name, componentConfiguration) {
      return function(resolve) {
        const templateIdentifier = '<silverpeas-component-template name="' + name + '">';
        return templatePromise.then(function(templates) {
          let msg;
          let start = templates.indexOf(templateIdentifier);
          if (start < 0) {
            msg = "template '" + name + "' does not exist from repository '" + src + "'";
            sp.log.error("TemplateRepository - " + msg);
            return "<div>" + msg + "</div>";
          }
          start = start + templateIdentifier.length;
          const end = templates.indexOf('</silverpeas-component-template>', start);
          if (end < 0) {
            msg = "template '" + name + "' is not well defined into repository '" + src + "'";
            sp.log.error("TemplateRepository - " + msg);
            return "<div>" + msg + "</div>";
          }
          componentConfiguration = componentConfiguration ? componentConfiguration : {};
          componentConfiguration.template = templates.substring(start, end);
          resolve(componentConfiguration);
        });
      };
    };
  }

  /**
   * This directive permits to initialize some data from the template by using mustache notations.
   * The HTML element holding the directive is removed from the DOM after the interpretations.
   */
  Vue.directive('sp-init', {
    bind : function(el) {
      el.style.display = 'none';
    },
    inserted : function(el) {
      el.remove();
    }
  });

  const __focusEl = function(el) {
    setTimeout(function() {
      sp.element.focus(el);
    }, 0);
  }

  const __focusSilverpeasInput = function(el) {
    setTimeout(function() {
      const potentials = [];
      ['input', 'textarea', 'select'].forEach(function(i) {
        const $i = el.querySelector(i);
        if ($i) {
          potentials.push($i);
        }
      });
      potentials.push(el);
      sp.element.focus(potentials[0]);
    }, 0);
  }

  /**
   * This directive permits to handle a focus.
   */
  Vue.directive('sp-focus', {
    inserted : function(el) {
      if (el.classList.contains('silverpeas-input')) {
        __focusSilverpeasInput(el);
      } else {
        __focusEl(el);
      }
    }
  });

  /**
   * This directive permits to handle a scroll to a component on display.
   */
  Vue.directive('sp-scroll-to', {
    inserted : function(el) {
      sp.element.scrollToWhenSilverpeasEntirelyLoaded(el);
    }
  });

  /**
   * This directive permits to render an HTML element as disabled.
   */
  Vue.directive('sp-disable-if', function(el, binding) {
    if (binding.value) {
      el.classList.add('silverpeas-disabled');
      const disableElement = function(elToDisable) {
        const tabIndex = elToDisable.tabIndex;
        if (typeof tabIndex === 'number' && tabIndex !== -1) {
          elToDisable.__sp_lastTabIndexValue = elToDisable.tabIndex;
          elToDisable.tabIndex = -1;
        }
      };
      disableElement(el);
      el.blur();
      sp.element.querySelectorAll('a,input', el).forEach(disableElement);
    } else {
      el.classList.remove('silverpeas-disabled');
      const enableElement = function(elToEnable) {
        if (typeof elToEnable.__sp_lastTabIndexValue === 'number') {
          if (elToEnable.__sp_lastTabIndexValue === 0) {
            elToEnable.removeAttribute('tabIndex');
          } else {
            elToEnable.setAttribute('tabIndex', elToEnable.__sp_lastTabIndexValue);
          }
          delete elToEnable.__sp_lastTabIndexValue;
        }
      };
      enableElement(el);
      sp.element.querySelectorAll('a,input', el).forEach(enableElement);
    }
  });

  /**
   * This filter permits to display simple array as joined string values separated with given string
   * or space by default.
   */
  Vue.filter('joinWith', function(array, options) {
    if (Array.isArray(array)) {
      return array.joinWith(options);
    }
    return array;
  });

  /**
   * This filter permits to transform javascript newlines into html newlines.
   */
  Vue.filter('newlines', function(text) {
    return text ? text.convertNewLineAsHtml() : text;
  });

  /**
   * This filter permits to transform javascript newlines into html newlines.
   */
  Vue.filter('noHTML', function(text) {
    return text ? text.noHTML() : text;
  });

  /**
   * This filter permits to transform ISO String date into a readable date.
   */
  Vue.filter('displayAsDate', function(dateAsText) {
    return dateAsText ? sp.moment.displayAsDate(dateAsText) : dateAsText;
  });

  /**
   * This filter permits to transform ISO String date time into a readable time.
   */
  Vue.filter('displayAsTime', function(dateAsText) {
    return dateAsText ? sp.moment.displayAsTime(dateAsText) : dateAsText;
  });

  /**
   * This filter permits to transform ISO String date time into a readable one.
   */
  Vue.filter('displayAsDateTime', function(dateAsText) {
    return dateAsText ? sp.moment.displayAsDateTime(dateAsText) : dateAsText;
  });

  /**
   * Common implementation for components which have to handle i18n by template.
   * Add it to a component or a vue instance by attribute mixins.
   *
   * Provide following methods:
   * - formatMessage(message[, params[, options]]), format a given message:
   * -- message: the message '{0} as more than {1} digits' for example
   * -- params : an array of message parameters. If one parameter into the message, it is possibleto
   * give a string instead of an array.
   * -- options: permits to specify the style of the parameters (bold, italic,...). Bold by default.
   * For example, italic underlined : {
   *   bold: false,
   *   italic: true,
   *   underline: true
   * }
   * - addMessages(messages): set messages to this.messages. Typically used from template definition
   * with Silverpeas's v-sp-init directive. For example:
   * <div v-sp-init>
   *   {{addMessages({
   *   incumbentLabel : '${silfn:escapeJs(incumbentLabel)}',
   *   substituteLabel : '${silfn:escapeJs(substituteLabel)}',
   *   startDateLabel : '${silfn:escapeJs(startDateLabel)}',
   *   endDateLabel : '${silfn:escapeJs(endDateLabel)}'
   *   })}}
   * </div>
   *
   * @example:
   * Vue.component('my-component', {
   *   mixins : [VuejsI18nTemplateMixin]
   * });
   */
  window.VuejsI18nTemplateMixin = {
    data : function() {
      return {
        messages : {}
      }
    },
    methods : {
      formatMessage: function(message, params, options) {
        if (typeof params === 'string') {
          params = [params];
        }
        options = extendsObject(false, {
          styles : {
            classes : undefined,
            bold : true,
            italic : false,
            underline : false
          }
        }, options);
        let result = message;
        let __valid = true;
        if (StringUtil.isNotDefined(message)) {
          __valid = false;
          sp.log.error("Vuejsi18nTemplateMixin - formatMessage - message is not defined");
        }
        if (!Array.isArray(params)) {
          __valid = false;
          sp.log.error("Vuejsi18nTemplateMixin - formatMessage - params must be an array");
        }
        if (__valid) {
          for (let i = 0; i < params.length ; i++) {
            let value = params[i];
            let styles = extendsObject(false, {}, options.styles);
            if (typeof value === 'function') {
              value = value.call(this);
            } else if (typeof value === 'object') {
              value = value.value ? value.value : '';
              styles = extendsObject(styles, value.styles);
            }
            if (StringUtil.isDefined(styles.classes)) {
              value = '<span class="' + styles.classes + '">' + value + '</span>'
            }
            if (styles.bold) {
              value = '<b>' + value + '</b>'
            }
            if (styles.italic) {
              value = '<i>' + value + '</i>'
            }
            if (styles.underline) {
              value = '<u>' + value + '</u>'
            }
            result = result.replaceAllByRegExpAsString('[{]' + i + '[}]', value);
          }
        }
        return result;
      },
      addMessages : function(messages) {
        if (typeof messages === 'object') {
          for (let key in messages) {
            let message = messages[key];
            if (typeof message === 'function') {
              message = message.bind(this);
            }
            this.$set(this.messages, key, message);
          }
          this.$emit('messages', this.messages);
        } else {
          sp.log.error("Vuejsi18nTemplateMixin - addMessages - messages must be an object");
        }
      }
    }
  };

  /**
   * Common implementation for components which have to handle i18n content.
   * Add it to a component or a vue instance by attribute mixins.
   *
   * Provide following property:
   * - isI18nContent: Boolean, true if enabled, false (default) otherwise
   * - i18nContentLanguage: String, the default content language
   *
   * @example:
   * Vue.component('my-component', {
   *   mixins : [VuejsI18nContentMixin]
   * });
   */
  window.VuejsI18nContentMixin = {
    props : {
      isI18nContent : {
        'type' : Boolean,
        'default' : false
      },
      i18nContentLanguage : {
        'type' : String,
        'default' : ''
      }
    }
  };

  /**
   * Common implementation for components which have to provide an API.
   * Add it to a component or a vue instance by attribute mixins.
   *
   * @example:
   * Vue.component('my-component', {
   *   mixins : [VuejsApiMixin]
   * });
   */
  window.VuejsApiMixin = {
    data : function() {
      return {
        api : {}
      }
    },
    methods : {
      extendApiWith : function(apiExtensions) {
        if (typeof apiExtensions === 'object') {
          for (let key in apiExtensions) {
            let extension = apiExtensions[key];
            if (typeof extension === 'function') {
              extension = extension.bind(this);
            }
            this.$set(this.api, key, extension);
          }
          this.$emit('api', this.api);
        } else {
          sp.log.error("VuejsApiMixin - extendApiWith - apiExtensions must be an object");
        }
      }
    }
  };

  /**
   * Common implementation for components which have to provide an API in context of silverpeas form
   * pane. Add it to a component or a vue instance by attribute mixins.
   *
   * The component using this mixin must implement its api with the following methods at least:
   * - getFormPriority: returning 0 as default, permits to validate form by priority
   * - validateForm: called on silverpeas-form-pane validation (use rootFormApi.errorMessage inorder
   * to register errors)
   * - updateFormData: called after a successful silverpeas-form-pane validation. The data handledby
   * the form part must be set on given data. If the data is returned by the method, is is givento
   * the next form validate method call if any.
   *
   * @example:
   * Vue.component('my-form', {
   *   mixins : [VuejsFormApiMixin]
   * });
   *
   * The component using the mixin must be included at least into a silverpeas-form-pane one or an
   *     extension of it. This is this component which is providing the 'rootFormApi' injection.
   *
   * @example:
   * <silverpeas-form-pane>
   *   <my-form></my-form>
   * </silverpeas-form-pane>
   */
  window.VuejsFormApiMixin = {
    inject : ['rootFormApi'],
    mixins : [VuejsApiMixin],
    created : function() {
      this.extendApiWith({
        validateForm : function() {
          sp.log.error('VuejsFormApiMixin - Validation is not implemented!!! Please implement api.validate method');
          return !this.api.errorMessage().existsAtLeastOne();
        },
        updateFormData : function(dataToUpdate) {
          sp.log.error('VuejsFormApiMixin - Data update is not implemented!!! Please implement api.updateData method');
          return false;
        }
      });
    },
    mounted : function() {
      this.rootFormApi.handleFormComponent(this);
    },
    destroyed : function() {
      this.rootFormApi.unhandleFormComponent(this);
    }
  };

  /**
   * Common implementation for components which provide user input handled by a
   * silverpeas-form-pane component.
   * Add it to a component by attribute mixins.
   *
   * The component using this mixin must implement its api with the following methods at least:
   * - validate: called on silverpeas-form-pane validation (use rootFormApi.errorMessage in order
   * to register errors, rootFormMessages for common message repository and formatMessage method of
   * VuejsI18nTemplateMixin for message formatting)
   *
   * Provide following methods:
   * - getLabelByForAttribute(id): retrieve the inner text of HTML element with for attribute equal
   * to the given identifier.
   *
   * @example:
   * Vue.component('my-form-input', {
   *   mixins : [VuejsFormInputMixin]
   * });
   *
   * The component using the mixin must be included at least into a silverpeas-form-pane one or an
   *     extension of it. This is this component which is providing the 'rootFormApi' injection.
   *
   * If not included into silverpeas-form-pane, nothing is plugged and the component works anyway.
   *
   * @example:
   * <silverpeas-form>
   *   <my-form-input></my-form-input>
   * </silverpeas-form>
   */
  window.VuejsFormInputMixin = {
    mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
    inject : {
      rootFormApi : {
        'default' : undefined
      },
      rootFormMessages : {
        'default' : undefined
      }
    },
    model: {
      prop: 'modelValue',
      event: 'update:modelValue'
    },
    props : {
      id: {
        'type': String,
        'required': true
      },
      labelId: {
        'type': String,
        'default': ''
      },
      name: {
        'type': String,
        'default': ''
      },
      title: {
        'type': String,
        'default': ''
      },
      inputClass: {
        'type': String,
        'default': ''
      },
      disabled: {
        'type': Boolean,
        'default': false
      },
      readOnly : {
        'type' : Boolean,
        'default' : false
      },
      mandatory : {
        'type': Boolean,
        'default': false
      },
      modelValue : {
        'type': String,
        'default': ''
      }
    },
    data : function() {
      return {
        linkedLabelCmp : undefined
      }
    },
    created : function() {
      this.extendApiWith({
        validateFormInput : function() {
          return !this.validateMandatory();
        }
      });
    },
    methods : {
      getLabelByForAttribute : function(id) {
        const $label = sp.element.querySelector("[for='" + id + "']");
        return ($label ? StringUtil.defaultStringIfNotDefined($label.innerText) : '').trim();
      },
      getInputElementName : function() {
        throw new Error("getInputElementName method MUST be implemented");
      },
      updateInputElementAttribute : function(attrName) {
        if (!this.$el.querySelector || !this.getInputElementName()) {
          return;
        }
        const $input = this.$el.querySelector(this.getInputElementName());
        const attrValue = this[attrName];
        if (attrValue) {
          $input.setAttribute(attrName, attrValue);
        } else {
          $input.removeAttribute(attrName);
        }
      },
      updateInputElementReadOnly : function() {
        if (!this.$el.querySelector || !this.getInputElementName()) {
          return;
        }
        const $input = this.$el.querySelector(this.getInputElementName());
        if (this.readOnly) {
          $input.setAttribute('readonly', 'readonly');
        } else {
          $input.removeAttribute('readonly');
        }
      },
      updateInputElementDisabled : function() {
        if (!this.$el.querySelector || !this.getInputElementName()) {
          return;
        }
        const $input = this.$el.querySelector(this.getInputElementName());
        if (this.disabled) {
          $input.setAttribute('disabled', '');
        } else {
          $input.removeAttribute('disabled');
        }
      },
      validateMandatory : function() {
        const mandatoryError = this.isMandatory && StringUtil.isNotDefined(this.modelValue);
        if (this.includedIntoFormPane) {
          if (mandatoryError) {
            this.rootFormApi.errorMessage().add(
                this.formatMessage(this.rootFormMessages.mandatory,
                    this.getLabelByForAttribute(this.linkedLabelId)));
          }
        }
        return mandatoryError;
      }
    },
    mounted : function() {
      if (this.$el.classList) {
        this.$el.classList.add('silverpeas-input');
      }
      if (this.includedIntoFormPane) {
        this.rootFormApi.handleFormInputComponent(this);
      }
      this.updateInputElementAttribute('title');
      this.updateInputElementReadOnly();
      this.updateInputElementDisabled();
    },
    destroyed : function() {
      if (this.includedIntoFormPane) {
        this.rootFormApi.unhandleFormInputComponent(this);
      }
    },
    watch : {
      'title' : function() {
        this.updateInputElementAttribute('title');
      },
      'readOnly' : function() {
        this.updateInputElementReadOnly();
      },
      'disabled' : function() {
        this.updateInputElementDisabled();
      }
    },
    computed : {
      includedIntoFormPane : function() {
        return !!this.rootFormApi;
      },
      model: {
        get : function() {
          return this.modelValue;
        },
        set : function(value) {
          this.$emit('update:modelValue', value);
        }
      },
      linkedLabelId : function() {
        return this.labelId ? this.labelId : this.id;
      },
      isMandatory : function() {
        return !this.disabled && !this.readOnly &&
            (this.mandatory || (this.linkedLabelCmp && this.linkedLabelCmp.mandatory));
      }
    }
  };

  /**
   * Common implementation for components which have to show or hide progress message.
   *
   * @example:
   * Vue.component('my-form', {
   *   mixins : [VuejsProgressMessageMixin]
   * });
   */
  window.VuejsProgressMessageMixin = {
    methods : {
      showProgressMessage : function() {
        const api = window.spProgressMessage
            ? window.spProgressMessage
            : top.spProgressMessage;
        api.show();
      },
      hideProgressMessage : function() {
        const api = window.spProgressMessage
            ? window.spProgressMessage
            : top.spProgressMessage;
        api.hide();
      }
    }
  };

  /**
   * Silverpeas's fade transition.
   */
  Vue.component('silverpeas-fade-transition', {
    template : '<transition v-bind:name="name" appear ' +
                 'v-on:before-enter="$emit(\'before-enter\',$event)" ' +
                 'v-on:enter="$emit(\'enter\',$event)" ' +
                 'v-on:after-enter="$emit(\'after-enter\',$event)" ' +
                 'v-on:before-leave="$emit(\'before-leave\',$event)" ' +
                 'v-on:leave="$emit(\'leave\',$event)" ' +
                 'v-on:after-leave="$emit(\'after-leave\',$event)"><slot></slot></transition>',
    props : {
      durationType : {
        'type' : String,
        'default' : 'normal'
      }
    },
    computed : {
      name : function() {
        return this.durationType + '-fade';
      }
    }
  });

  /**
   * Silverpeas's fade transition group.
   */
  Vue.component('silverpeas-fade-transition-group', {
    template : '<transition-group v-bind:name="name" v-bind:tag="tag" appear ' +
        'v-on:before-enter="$emit(\'before-enter\',$event)" ' +
        'v-on:enter="$emit(\'enter\',$event)" ' +
        'v-on:after-enter="$emit(\'after-enter\',$event)" ' +
        'v-on:before-leave="$emit(\'before-leave\',$event)" ' +
        'v-on:leave="$emit(\'leave\',$event)" ' +
        'v-on:after-leave="$emit(\'after-leave\',$event)"><slot></slot></transition-group>',
    props : {
      tag : {
        'type' : String,
        'default' : 'ul'
      },
      durationType : {
        'type' : String,
        'default' : 'normal'
      }
    },
    computed : {
      name : function() {
        return this.durationType + '-fade';
      }
    }
  });
})();