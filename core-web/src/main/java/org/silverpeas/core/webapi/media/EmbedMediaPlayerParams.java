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
package org.silverpeas.core.webapi.media;

import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;

import javax.ws.rs.FormParam;

/**
 * This class contains the parameter values of an embed media player request.<br>
 * To get a loaded container, use {@link RequestParameterDecoder#decode(HttpRequest, Class)}.
 * @author Yohann Chastagnier
 */
class EmbedMediaPlayerParams {

  @FormParam("url")
  private String url;

  @FormParam("mimeType")
  private String mimeType;

  @FormParam("posterUrl")
  private String posterUrl;

  @FormParam("playerType")
  private String playerType;

  @FormParam("backgroundColor")
  private String backgroundColor;

  @FormParam("autoPlay")
  private Boolean autoPlay;

  @FormParam("width")
  private Integer width;

  @FormParam("height")
  private Integer height;

  String getUrl() {
    return url;
  }

  String getMimeType() {
    return mimeType;
  }

  String getPosterUrl() {
    return posterUrl;
  }

  String getPlayerType() {
    return playerType;
  }

  String getBackgroundColor() {
    return backgroundColor;
  }

  boolean getAutoPlay() {
    return autoPlay != null ? autoPlay : false;
  }

  public Definition getDefinition() {
    return width != null && height != null ? Definition.of(width, height) : Definition.NULL;
  }
}
