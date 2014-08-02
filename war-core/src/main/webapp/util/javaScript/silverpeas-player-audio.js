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
 * Silverpeas plugin build upon JQuery to manage audio player.
 */
(function($, undefined) {
  document.createElement('video');
  document.createElement('audio');
  document.createElement('track');

  // Web Context
  if (!webContext) {
    var webContext = '/silverpeas';
  }

  // Player URL
  var flashPlayerURL = webContext + '/util/flash/flowplayer/flowplayer-3.2.18.swf';

  /**
   * The different player methods handled by the plugin.
   */
  var methods = {

    /**
     * Prepare UI and behavior for audio playing
     */
    audio : function(options) {
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
  $.fn.audioPlayer = function(method) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.audio.apply(this, arguments);
    } else {
      return methods.audio.apply(this, arguments);
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
      var config = $.extend({}, options);
      var defaultParams = {
        container : {
          width : '600px',
          height : '400px'
        },
        clip : {
          url : undefined,
          mimeType : undefined,
          posterUrl : undefined,
          bufferLength : 10,
          scaling : 'fit',
          autoPlay : false,
          autoBuffering : true,
          provider : 'audio'
        },
        play : {
          label : "",
          replayLabel : ""
        },
        canvas : {
          backgroundColor : 'transparent',
          backgroundGradient : 'none'
        },
        plugins : {
          controls : {
            fullscreen : false,
            autoHide : false
          },
          audio : {
            url : 'flowplayer.audio-3.2.11.swf'
          }
        }
      };
      config = $.extend(config, defaultParams);

      // Finilize the player configuration
      if (typeof options === 'string') {
        config.clip.url = options;
      } else if (options) {
        if (options.container) {
          $.extend(config.container, options.container);
        }
        if (options.clip) {
          $.extend(config.clip, options.clip);
        }
        if (options.canvas) {
          $.extend(config.canvas, options.canvas);
        }
        if (options.play) {
          $.extend(config.play, options.play);
        }
        if (options.clip.posterUrl) {
          config.clip.coverImage = {
            url : options.clip.posterUrl,
            scaling : 'orig'
          }
        }
      } else {
        alert('Not yet implemented');
        return false;
      }

      // Container configuration
      __configurePlayerContainer($container, config);

      // Load (and start ?) the player);
      flowplayer(this, flashPlayerURL, config)
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
    if (!$container.attr('id')) {
      $container.attr('id', __buildUniqueId());
    }
    $container.css('display', 'block');
    $container.css('width', config.container.width);
    $container.css('height', config.container.height);

    $container.on('closePlayer', function() {
      $f().close();
    });
    $container.on('enterFullscreenPlayer', function() {
      if (!$f().isFullscreen()) {
        $f().toggleFullscreen();
      }
      return true;
    });
    $container.on('exitFullscreenPlayer', function() {
      if ($f().isFullscreen()) {
        $f().toggleFullscreen();
      }
      return true;
    });
  }

  /**
   * Builds an unique id.
   * @returns {string}
   * @private
   */
  function __buildUniqueId() {
    return "id" + (new Date()).getMilliseconds();
  }

})(jQuery, undefined);
