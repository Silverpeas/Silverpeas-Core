/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
Vue.directive('init', {
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
Vue.directive('focus', {
  inserted : function(el) {
    setTimeout(function() {
      el.focus();
    }, 0);
  }
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
          var styles = extendsObject(false, options);
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
 * Common implementation for components which have to provide an API in context of silverpeas form.
 * Add it to a component or a vue instance by attribute mixins.
 *
 * @example:
 * Vue.component('my-form', {
 *   mixins : [VuejsFormApiMixin]
 * });
 *
 * The component using the mixin must be included at least into a silverpeas-form one or an
 *     extension of it. This is this component which is providing the 'rootFormApi' injection.
 *
 * @example:
 * <silverpeas-form>
 *   <my-form></my-form>
 * </silverpeas-form>
 */
window.VuejsFormApiMixin = {
  inject : ['rootFormApi'],
  mixins : [VuejsApiMixin],
  props : {
    formPriority : {
      "type" : Number,
      "default" : 0
    }
  },
  created : function() {
    this.extendApiWith({
      getFormPriority : function() {
        return this.formValidationPriority
      },
      validate : function() {
        sp.log.error('VuejsFormApiMixin - Validation is not implemented!!! Please implement api.validate method');
        return !this.api.errorMessage().existsAtLeastOne();
      },
      updateData : function(dataToUpdate) {
        sp.log.error('VuejsFormApiMixin - Data update is not implemented!!! Please implement api.updateData method');
        return false;
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
  mounted : function() {
    this.rootFormApi.handleFormApi(this.api);
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