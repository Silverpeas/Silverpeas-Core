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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Silverpeas plugin build upon JQuery to manage embed media player.
 */
(function($, undefined) {

  /**
   * The different player methods handled by the plugin.
   */
  const methods = {

    /**
     * Prepare UI and behavior for embed playing
     */
    init : function(options) {
      return __init($(this), options);
    }
  };

  /**
   * The player Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the player namespace in JQuery.
   */
  $.fn.embedPlayer = function(method) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      return methods.init.apply(this, arguments);
    }
  };

  /**
   * Private method that prepares UI and behavior.
   */
  function __init($targets, options) {

    if (!$targets.length) {
      return $targets;
    }

    return $targets.each(function() {
      const $container = $(this);

      // Player configuration
      const defaultParams = {
        url : undefined,
        width : '600px',
        height : '400px',
        playerParameters : undefined,
        messageEventHandlerConfig : {
          origin : undefined,
          handler : undefined
        }
      };
      const config = $.extend({}, defaultParams);
      $.extend(config, options);

      // Container configuration
      __configurePlayerContainer($container, config);
    });
  }

  /**
   * Configures the container of the player.
   * @param $container
   * @param config
   * @private
   */
  function __configurePlayerContainer($container, config) {
    $container.empty();
    setTimeout(function() {
      const playerParameters = extendsObject(config.playerParameters, {
        'embedPlayer' : true,
        'width' : config.width,
        'height' : config.height,
        '_' : new Date().getTime()
      });
      for (let paramName in playerParameters) {
        if (paramName) {
          config.url += (config.url.indexOf('?') > 0) ? '&' : '?';
          config.url += paramName + '=' + encodeURIComponent(playerParameters[paramName]);
        }
      }
      let $iframe = document.createElement('iframe');
      $iframe.setAttribute('src', config.url);
      $iframe.setAttribute("class", "embed");
      $iframe.setAttribute("frameborder", "0");
      $iframe.setAttribute('width', config.width);
      $iframe.setAttribute('height', config.height);
      $iframe.setAttribute('scrolling', 'no');
      $iframe.setAttribute('webkitallowfullscreen', 'true');
      $iframe.setAttribute('mozallowfullscreen', 'true');
      $iframe.setAttribute('allowfullscreen', 'true');
      $container.append(jQuery($iframe));
      const messageEventHandlerConfig = config.messageEventHandlerConfig;
      if (typeof messageEventHandlerConfig === 'object') {
        if (StringUtil.isNotDefined(messageEventHandlerConfig.origin)) {
          const errorMsg = "silverpeas-embed-player - config.messageEventHandlerConfig.origin MUST be defined";
          sp.log.error(errorMsg);
          throw new Error(errorMsg);
        }
        if (typeof messageEventHandlerConfig.handler === 'function') {
          $iframe.contentWindow.addEventListener("message", function(event) {
            if (event.origin === messageEventHandlerConfig.origin) {
              event.$iframe = $iframe;
              messageEventHandlerConfig.handler(event);
            }
          }, false);
        }
      }
    }, 0)
  }

})(jQuery, undefined);
