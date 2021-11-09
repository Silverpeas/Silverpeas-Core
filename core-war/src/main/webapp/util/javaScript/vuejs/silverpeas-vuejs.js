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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
  window.VueJsAsyncComponentTemplateRepository = function(src) {
    var templatePromise = sp.ajaxRequest(src).send().then(function(request) {
      return request.responseText
    });
    this.get = function(name, componentConfiguration) {
      return function(resolve) {
        var templateIdentifier = '<silverpeas-component-template name="' + name + '">';
        return templatePromise.then(function(templates) {
          var msg;
          var start = templates.indexOf(templateIdentifier);
          if (start < 0) {
            msg = "template '" + name + "' does not exist from repository '" + src + "'";
            sp.log.error("TemplateRepository - " + msg);
            return "<div>" + msg + "</div>";
          }
          start = start + templateIdentifier.length;
          var end = templates.indexOf('</silverpeas-component-template>', start);
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
})();

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

/**
 * This directive permits to handle a focus.
 */
Vue.directive('sp-focus', {
  inserted : function(el) {
    setTimeout(function() {
      el.focus();
    }, 0);
  }
});

/**
 * This directive permits to render an HTML element as disabled.
 */
Vue.directive('sp-disable-if', function(el, binding) {
  if (binding.value) {
    el.classList.add('silverpeas-disabled');
    var disableElement = function(elToDisable) {
      var tabIndex = elToDisable.tabIndex;
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
    var enableElement = function(elToEnable) {
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
 * Centralizing the 'isDefined' validator.
 * @param value the value to validate.
 * @returns {*} false if the value is not defined, true otherwise.
 */
const isDefinedValidator = function(value) {
  return StringUtil.isDefined(value);
};

/**
 * Common implementation for components which have to handle i18n by template.
 * Add it to a component or a vue instance by attribute mixins.
 *
 * Provide following methods:
 * - formatMessage(message[, params[, options]]), format a given message:
 * -- message: the message '{0} as more than {1} digits' for example
 * -- params : an array of message parameters. If one parameter into the message, it is possible to
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
      var result = message;
      var __valid = true;
      if (StringUtil.isNotDefined(message)) {
        __valid = false;
        sp.log.error("Vuejsi18nTemplateMixin - formatMessage - message is not defined");
      }
      if (!Array.isArray(params)) {
        __valid = false;
        sp.log.error("Vuejsi18nTemplateMixin - formatMessage - params must be an array");
      }
      if (__valid) {
        for (var i=0; i < params.length ; i++) {
          var value = params[i];
          var styles = extendsObject(false, {}, options.styles);
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
          result = result.replaceAll('[{]' + i + '[}]', value);
        }
      }
      return result;
    },
    addMessages : function(messages) {
      if (typeof messages === 'object') {
        for (var key in messages) {
          var message = messages[key];
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
        for (var key in apiExtensions) {
          var extension = apiExtensions[key];
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
 * - validateForm: called on silverpeas-form-pane validation (use rootFormApi.errorMessage in order
 * to register errors)
 * - updateFormData: called after a successful silverpeas-form-pane validation. The data handled by
 * the form part must be set on given data. If the data is returned by the method, is is given to
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
  }
};

/**
 * Common implementation for components which provide user input handled by a silverpeas-form-pane
 * component.
 * Add it to a component by attribute mixins.
 *
 * The component using this mixin must implement its api with the following methods at least:
 * - validate: called on silverpeas-form-pane validation (use rootFormApi.errorMessage in order to
 * register errors, rootFormMessages for common message repository and formatMessage method of
 * VuejsI18nTemplateMixin for message formatting)
 *
 * Provide following methods:
 * - getLabelByForAttribute(id): retrieve the inner text of HTML element with for attribute equal to
 * the given identifier.
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
  created : function() {
    this.extendApiWith({
      validateFormInput : function() {
        sp.log.error(
            'VuejsFormInputMixin - Validation is not implemented!!! Please implement api.validate method');
        return false;
      }
    });
  },
  methods : {
    getLabelByForAttribute : function(id) {
      var $label = sp.element.querySelector("[for='" + id + "']");
      return $label ? $label.innerText : '';
    }
  },
  mounted : function() {
    this.rootFormApi.handleFormInputComponent(this);
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
      spProgressMessage.show();
    },
    hideProgressMessage : function() {
      spProgressMessage.hide();
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