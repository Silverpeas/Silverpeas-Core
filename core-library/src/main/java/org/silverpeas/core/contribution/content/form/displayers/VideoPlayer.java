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
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.ElementAttributes;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.html.WebPlugin;

import java.text.MessageFormat;
import java.util.Random;

import static org.silverpeas.core.html.SupportedWebPlugin.Constants.EMBEDPLAYER;
import static org.silverpeas.core.ui.DisplayI18NHelper.getDefaultLanguage;

/**
 * An (X)HTML video player with javascript. The video player is set up and rendered by using
 * javascript.
 */
public class VideoPlayer {

  private static final int DEFAULT_WIDTH = 425;
  private static final int DEFAULT_HEIGHT = 300;
  private static final Random RANDOM_GENERATOR = new Random();
  private static final String TEMPLATE_SCRIPT =
      "jQuery(document).ready(function() '{'" +
        "jQuery(''{0}'').embedPlayer('{'" +
          "url : ''{1}''," +
          "width : {2}, " +
          "height : {3}," +
          "playerParameters : '{'" +
            "posterUrl : ''{4}''," +
            "mimeType : ''{5}''," +
            "autoPlay : {6}," +
            "backgroundColor : ''{7}'' " +
          "'}' " +
        "'}');" +
      "'}')";
  private String videoURL = "";
  private String posterURL = "";
  private String mimeType = "";
  private boolean autoplay = false;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private String backgroundColor = "";

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
    // Nothing to do.
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
    this.videoURL = videoURL == null ? "" : videoURL;
  }

  /**
   * Gets the mime-type of the video.
   * @return the mime-type.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime-type of the video.
   * @param mimeType the mime-type.
   */
  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Gets the URL of the poster to display before the video starts.
   * @return the poster URL.
   */
  public String getPosterURL() {
    return posterURL;
  }

  /**
   * Sets the URL of the poster to display before the video starts.
   * @param posterURL the URL of the poster.
   */
  public void setPosterURL(final String posterURL) {
    this.posterURL = posterURL;
  }

  /**
   * Gets the height of the video player (300 by default).
   * @return the video height.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets the height of the video player(in pixels).
   * @param height the video height.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Gets the width of the video player (425 by default).
   * @return the video width.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets the width of the video player (in pixels).
   * @param width the video width.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets the background color (CSS RGB) of the player.
   * @return the background color.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Sets the background color of the player.
   * @param backgroundColor the background color.
   */
  public void setBackgroundColor(final String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  private String generateId() {
    return "video-player" + RANDOM_GENERATOR.nextInt();
  }

  /**
   * Initializes the video player by declaring the required ressources (script, stylesheets, ...).
   * This method is required before any call of the display method.
   * @param element an XHTML element into which the resources declaration will be rendered.
   */
  public void init(final ConcreteElement element) {
    include(element, WebPlugin.get().getHtml(EMBEDPLAYER, getDefaultLanguage()));
  }

  /**
   * Renders the video player into the specified (X)HTML element.
   * @param element an (X)HTML element into which the video player will be rendered.
   */
  public void renderIn(final ConcreteElement element) {
    String videoId = generateId();
    if (!getVideoURL().isEmpty()) {
      div video = new div();
      video.setID(videoId);
      include(element, video);
    }
    script player = new script(MessageFormat
        .format(TEMPLATE_SCRIPT, '#' + videoId, getVideoURL(), String.valueOf(getWidth()),
            String.valueOf(getHeight()), getPosterURL(), getMimeType(), isAutoplay(),
            getBackgroundColor())).setType("text/javascript");
    include(element, player);
  }

  private void include(final ConcreteElement xhtml, final ElementAttributes inclusion) {
    if (xhtml instanceof ElementContainer) {
      ((ElementContainer) xhtml).addElement(inclusion);
    } else {
      xhtml.addElementToRegistry(inclusion);
    }
  }
}
