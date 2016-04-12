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

package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.util.URLUtil;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.script;

import java.text.MessageFormat;
import java.util.Random;

/**
 * An (X)HTML video player with javascript. The video player is set up and rendered by using
 * javascript.
 */
public class VideoPlayer {

  private static final String webContext = URLUtil.getApplicationURL();
  private static final String playerUrl =
      webContext + "/util/flash/flowplayer/flowplayer-3.2.7.swf";
  private static final Random randomGenerator = new Random();
  private static final String flowPlayerJS = webContext
      + "/util/javaScript/flowplayer/flowplayer-3.2.6.min.js";
  private static final String flowPlayerCSS = webContext + "/util/styleSheets/flowplayer.css";
  private static final String playerStyle = "<link type='text/css' href='" + flowPlayerCSS
      + "' rel='stylesheet' />";
  private static final String templateScript =
      "flowplayer(''{0}'', ''{1}'', '{'wmode: ''opaque'', "
      + "clip: '{' autoBuffering: {2}, autoPlay: {3} '}' '}')";
  private String videoURL = "";
  private boolean autoplay;
  private String width = "425px";
  private String height = "300px";

  /**
   * Creates a new displayer of a video player that will play the video at the specified URL. By
   * default the width and the height of the video player is respectively 425 pixels and 300 pixels.
   * @param videoURL the URL of the video to play.
   * @param autoplay the video playing should be autostarted?
   */
  public VideoPlayer(String videoURL, boolean autoplay) {
    this.videoURL = videoURL;
    this.autoplay = autoplay;
  }

  /**
   * Creates a new displayer of a video player with the default settings: no video to play, autoplay
   * set at false, the width at 425 pixels and the height at 300 pixels.
   */
  public VideoPlayer() {
  }

  /**
   * Should the video player autostart?
   * @return true if the video should be autoplayed, false otherwise.
   */
  public boolean isAutoplay() {
    return autoplay;
  }

  /**
   * Sets the video autoplaying property
   * @param autoplay true to autostart the video, false otherwise.
   */
  public void setAutoplay(boolean autoplay) {
    this.autoplay = autoplay;
  }

  /**
   * Gets the URL of the video to play.
   * @return the video URL.
   */
  public String getVideoURL() {
    return videoURL;
  }

  /**
   * Sets the URL of the video to play.
   * @param videoURL the URL of the video to play.
   */
  public void setVideoURL(String videoURL) {
    this.videoURL = (videoURL == null ? "" : videoURL);
  }

  /**
   * Gets the height of the video player (in CSS instruction, by default 425px).
   * @return the video height.
   */
  public String getHeight() {
    return height;
  }

  /**
   * Sets the height of the video player in CSS (for example: 425px for 425 pixels).
   * @param height the video height.
   */
  public void setHeight(String height) {
    this.height = height;
  }

  /**
   * Gets the width of the video player (in CSS instruction, by default 425px).
   * @return the video width.
   */
  public String getWidth() {
    return width;
  }

  /**
   * Sets the width of the video player in CSS (for example: 425px for 425 pixels).
   * @param width the video width.
   */
  public void setWidth(String width) {
    this.width = width;
  }

  private String generateId() {
    return "player" + randomGenerator.nextInt();
  }

  /**
   * Initializes the video player by declaring the required ressources (script, stylesheets, ...).
   * This method is required before any call of the display method.
   * @param element an XHTML element into which the resources declaration will be rendered.
   */
  public void init(final ConcreteElement element) {
    script cssLoading = new script("$(document.head).append(\"" + playerStyle + "\")").setType(
        "text/javascript");
    script jsInclusion = new script().setType("text/javascript").setSrc(flowPlayerJS);
    if (element instanceof ElementContainer) {
      ElementContainer container = (ElementContainer) element;
      container.addElement(cssLoading).
          addElement(jsInclusion);
    } else {
      element.addElementToRegistry(cssLoading).
          addElementToRegistry(jsInclusion);
    }
  }

  /**
   * Renders the video player into the specified (X)HTML element.
   * @param element an (X)HTML element into which the video player will be rendered.
   */
  public void renderIn(final ConcreteElement element) {
    String videoId = generateId();
    if (!getVideoURL().isEmpty()) {
      a video = new a().setHref(getVideoURL());
      video.setStyle("display:block;width:" + getWidth() + ";height:" + getHeight() + ";");
      video.setID(videoId);
      if (element instanceof ElementContainer) {
        ((ElementContainer) element).addElement(video);
      } else {
        element.addElementToRegistry(video);
      }
    }
    script player = new script(MessageFormat.format(templateScript, videoId, playerUrl,
        !isAutoplay(),
        isAutoplay())).setType("text/javascript");
    if (element instanceof ElementContainer) {
      ((ElementContainer) element).addElement(player);
    } else {
      element.addElementToRegistry(player);
    }
  }
}
