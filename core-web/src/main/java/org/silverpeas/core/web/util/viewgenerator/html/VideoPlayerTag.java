/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.contribution.content.form.displayers.VideoPlayer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.ElementContainer;

public class VideoPlayerTag extends TagSupport {
  private static final long serialVersionUID = 1425756234498404463L;

  private static final Integer DEFAULT_WIDTH = 360;

  private static final Integer DEFAULT_HEIGHT = 240;

  private String url;

  private int width = DEFAULT_WIDTH;

  private int height = DEFAULT_HEIGHT;

  private boolean autostart = false;

  @Override
  public int doStartTag() throws JspException {

    try {
      ElementContainer xhtmlContainer = new ElementContainer();
      VideoPlayer videoPlayer = new VideoPlayer(getUrl(), isAutostart());
      videoPlayer.setHeight(getHeight());
      videoPlayer.setWidth(getWidth());
      videoPlayer.init(xhtmlContainer);
      videoPlayer.renderIn(xhtmlContainer);
      xhtmlContainer.output(pageContext.getOut());
    } catch (Exception e) {
      throw new JspException("Can't display video player", e);
    }
    return SKIP_BODY;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width == null ? DEFAULT_WIDTH : width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height == null ? DEFAULT_HEIGHT : height;
  }

  public Boolean isAutostart() {
    return autostart;
  }

  public void setAutostart(Boolean autostart) {
    this.autostart = autostart;
  }

}
