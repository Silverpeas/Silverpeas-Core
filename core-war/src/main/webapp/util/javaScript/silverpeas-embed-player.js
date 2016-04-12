/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
  var methods = {

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
      var $container = $(this);

      // Player configuration
      var defaultParams = {
        url : undefined,
        width : '600px',
        height : '400px',
        playerParameters : undefined
      };
      var config = $.extend({}, defaultParams);
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
    $container.css('width', config.width);
    $container.css('height', config.height);
    var $embed = $('<iframe>');
    $embed.attr('class', 'embed');
    $embed.attr('frameborder', '0');
    $embed.attr('width', config.width);
    $embed.attr('height', config.height);
    $embed.attr('scrolling', 'no');
    $embed.attr('webkitallowfullscreen', 'true');
    $embed.attr('mozallowfullscreen', 'true');
    $embed.attr('allowfullscreen', 'true');
    $container.append($embed);
    setTimeout(function() {
      if (config.playerParameters) {
        for (var paramName in config.playerParameters) {
          if (paramName) {
            config.url += config.url.indexOf('?') ? '&' : '?';
            config.url += paramName + '=' + encodeURIComponent(config.playerParameters[paramName]);
          }
        }
      }
      $embed.attr('src', config.url);
    }, 0)
  }

})(jQuery, undefined);
