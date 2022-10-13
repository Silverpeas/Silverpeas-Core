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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.media;

import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * A common service to play video or sound (for now) with an embed player.
 * @author Yohann Chastagnier
 */
@WebService
@Path(EmbedMediaPlayerResource.PATH)
public class EmbedMediaPlayerResource extends RESTWebService {

  private static final int DEFAULT_WIDTH = 600;
  private static final int DEFAULT_HEIGHT = 400;

  static final String PATH = "media/player/embed";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets a view on the content with the embed media player.
   * @return a descriptor of the renderer to use to display the media content.
   */
  @GET
  public View getEmbedContent() {
    EmbedMediaPlayerParams params =
        RequestParameterDecoder.decode(getHttpServletRequest(), EmbedMediaPlayerParams.class);
    try {
      checkMandatoryParams(params);

      Definition definition = params.getDefinition();
      if (definition == Definition.NULL) {
        definition = Definition.of(DEFAULT_WIDTH, DEFAULT_HEIGHT);
      }

      getHttpServletRequest().setAttribute("mediaUrl", params.getUrl());
      getHttpServletRequest().setAttribute("posterUrl", params.getPosterUrl());
      getHttpServletRequest().setAttribute("playerType", params.getPlayerType());
      getHttpServletRequest().setAttribute("mimeType", params.getMimeType());
      getHttpServletRequest().setAttribute("definition", definition);
      getHttpServletRequest().setAttribute("backgroundColor", params.getBackgroundColor());
      getHttpServletRequest().setAttribute("autoPlay", params.getAutoPlay());

      return new View("/media/jsp/embed.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return null;
  }

  /**
   * Checks mandatory parameters.
   * @param params the parameters.
   */
  private void checkMandatoryParams(final EmbedMediaPlayerParams params) {
    List<String> errorMessages = new ArrayList<>();
    if (isNotDefined(params.getUrl())) {
      errorMessages.add("url is not defined");
    }
    if (isNotDefined(params.getMimeType())) {
      errorMessages.add("player type is not defined");
    }
    if (!"video".equals(params.getPlayerType()) && !"audio".equals(params.getPlayerType())) {
      errorMessages.add("player type is not rightly defined");
    }
    if (!errorMessages.isEmpty()) {
      throw new WebApplicationException(errorMessages.stream().collect(Collectors.joining(", ")),
          Response.Status.BAD_REQUEST);
    }
  }
}
