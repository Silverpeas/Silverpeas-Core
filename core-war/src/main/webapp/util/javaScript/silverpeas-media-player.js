/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * Silverpeas plugin build upon JQuery to manage video and audio player.
 * It provide a single way to play video or audio content and it has an interface role between the
 * Silverpeas UI code and the external plugin used (flowplayer for now)
 */
(function($window, undefined) {
  document.createElement('video');
  document.createElement('audio');
  document.createElement('track');

  if (!$window.MediaPlayerSettings) {
    $window.MediaPlayerSettings = new SilverpeasPluginSettings();
  }

  $window.mediaPlayerDebug = false;

  var FLOWPLAYER_SWF_URL = $window.MediaPlayerSettings.get("media.player.flowplayer.swf");
  var FLOWPLAYER_SWF_HLS_URL = $window.MediaPlayerSettings.get("media.player.flowplayer.swf.hls");

  var DEFAULT_VIDEO_MIME_TYPE = "video/mp4";

  var SilverpeasMediaPlayer = SilverpeasClass.extend({
    initialize: function(target, options) {
      // Target could be an HTML element or a CSS selector
      this.container = target;
      if (typeof target === 'string') {
        this.container = document.querySelector(target);
      }
      // Default player configuration
      var defaultConfig = {
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
          autoBuffering : true
        },
        play : {
          label : "",
          replayLabel : ""
        },
        canvas : {
          backgroundColor : 'transparent',
          backgroundGradient : 'none'
        }
      };

      this.config = extendsObject(defaultConfig, options);
      this.configureContainer();
      this.externalApi = this.loadExternalPlayer();
    },
    configureContainer : function() {
      var $container = this.getContainer();
      var $config = this.getConfig();
      $container.innerHTML = "";
      if (!$container.getAttribute('id')) {
        $container.setAttribute('id', "id" + (new Date()).getMilliseconds());
      }
      $container.style.display = 'block';
      $container.style.width = $config.container.width;
      $container.style.height = $config.container.height;
      return this;
    },
    loadExternalPlayer:function() {
      __logError("No External Player !");
      return false;
    },
    getExternalPlayer : function() {
      return this.externalApi;
    },
    getContainer : function() {
      return this.container;
    },
    getConfig : function() {
      return this.config;
    }
  });

  var FlowPlayer = SilverpeasMediaPlayer.extend({
    configureContainer : function() {
      this._super();
      var $container = this.getContainer();
      $container.classList.add("fp-mute");
      $container.addEventListener('closePlayer', function() {
        this.getExternalPlayer().shutdown();
      });
      $container.addEventListener('enterFullscreenPlayer', function() {
        if (!this.getExternalPlayer().isFullscreen()) {
          this.getExternalPlayer().fullscreen();
        }
        return true;
      });
      $container.addEventListener('exitFullscreenPlayer', function() {
        if (this.getExternalPlayer().isFullscreen()) {
          this.getExternalPlayer().fullscreen();
        }
        return true;
      });
      return this;
    },
    loadExternalPlayer:function() {
      return flowplayer(this.getContainer(), this.getTranslatedConfig());
    },
    getContainer : function() {
      return this.container;
    },
    getConfig : function() {
      return this.config;
    },
    getTranslatedConfig : function() {
      var translatedConfig = {
        share : false,
        debug : false,
        embed : false,
        tooltip : false,
        autoplay : this.getConfig().clip.autoPlay,
        poster : this.getConfig().clip.posterUrl,
        clip : {
          sources : [{
            type : this.getConfig().clip.mimeType,
            src : this.getConfig().clip.url
          }]
        },
        fullscreen : true,
        swf : FLOWPLAYER_SWF_URL,
        swfHls : FLOWPLAYER_SWF_HLS_URL
      };
      __logDebug("getTranslatedConfig() - " + JSON.stringify(translatedConfig));
      return translatedConfig;
    }
  });
  
  var VideoFlowPlayer = FlowPlayer.extend({
    getTranslatedConfig : function() {
      var translatedConfig = this._super();
      var mimeType = this.getConfig().clip.mimeType;
      var translatedSource = translatedConfig.clip.sources[0];
      if (mimeType.indexOf('flv') > 0 || mimeType.indexOf('flash') > 0) {
        __logDebug("getTranslatedConfig() - Changing '" + translatedSource.type +
            "' into 'video/flash' mime-type");
        translatedSource.type = 'video/flash';
      } else if (mimeType.indexOf('mov') > 0 || mimeType.indexOf('quick') > 0) {
        __logDebug("getTranslatedConfig() - Changing '" + translatedSource.src +
            "' into default '" + DEFAULT_VIDEO_MIME_TYPE + "' mime-type");
        translatedSource.src = sp.url.format(translatedSource.src, {
          "forceMimeType" : DEFAULT_VIDEO_MIME_TYPE
        });
        translatedSource.type = DEFAULT_VIDEO_MIME_TYPE;
      }
      return translatedConfig;
    }
  });
  
  var AudioFlowPlayer = FlowPlayer.extend({
  });

  $window.spMediaPlayer = new function() {
    this.loadVideoPlayer = function(target, config) {
      return new VideoFlowPlayer(target, config);
    };
    this.loadAudioPlayer = function(target, config) {
      return new AudioFlowPlayer(target, config);
    };
  };

  /**
   * Logs errors.
   * @param message
   * @private
   */
  function __logError(message) {
    sp.log.error("Media Player - " + message);
  }

  /**
   * Logs debug messages.
   * @param message
   * @private
   */
  function __logDebug(message) {
    if ($window.mediaPlayerDebug) {
      sp.log.debug("Media Player - " + message);
    }
  }

})(window, undefined);
