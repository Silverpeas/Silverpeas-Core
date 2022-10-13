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
package org.silverpeas.core.webapi.media.streaming;

import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.web.http.RequestParameterDecoder.decode;
import static org.silverpeas.core.webapi.media.streaming.StreamingProviderDataEntity.from;

/**
 * A common service to play external streaming into an embed context.
 * <p>
 * This service provide also the possibility to get information from external streaming providers
 * by using oembed mechanism.
 * </p>
 * @author silveryocha
 */
@WebService
@Path(StreamingPlayerResource.PATH)
public class StreamingPlayerResource extends RESTWebService {

  static final String PATH = "media/streaming";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets the provider data of a streaming from its url. If it doesn't exist, a 404 HTTP code is
   * returned. If the user isn't authenticated, a 401 HTTP code is returned. If a problem occurs
   * when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request content of the asked streaming.
   */
  @GET
  @Path("providerData")
  @Produces(MediaType.APPLICATION_JSON)
  public StreamingProviderDataEntity getStreamingProviderDataFromUrl() {
    final EmbedStreamingPlayerParams params = decode(getHttpServletRequest(), EmbedStreamingPlayerParams.class);
    try {
      checkMandatoryParams(params);
      final String url = params.getUrl();
      final StreamingProviderDataEntity entity = getHandledStreamingProvider(url);
      return entity.withURI(getUri().getRequestUri());
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets a view on the content with the HTML that permits to play the streaming.
   * @return a descriptor of the renderer to use to display the streaming content.
   */
  @Path("player")
  @GET
  public View getPlayerContent() {
    final EmbedStreamingPlayerParams params = decode(getHttpServletRequest(), EmbedStreamingPlayerParams.class);
    try {
      checkMandatoryParams(params);
      final String url = params.getUrl();
      final Definition definition = params.getDefinition();
      final StreamingProviderDataEntity streamingEntity = getHandledStreamingProvider(url);
      getHttpServletRequest().setAttribute("entity", streamingEntity);
      getHttpServletRequest().setAttribute("definition", definition);
      return new View("/media/jsp/embedStreaming.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  private StreamingProviderDataEntity getHandledStreamingProvider(final String url) {
    final StreamingProviderDataEntity streamingEntity = from(url).orElseThrow(
        () -> new WebApplicationException(String.format("Streaming URL '%s' is not Handled", url)));
    if (getHttpRequest().isSecure()) {
      // Replacing HTTP scheme by HTTPS one
      streamingEntity.forceSecureEmbedUrl();
    }
    return streamingEntity;
  }

  @Override
  public String getComponentId() {
    return null;
  }

  /**
   * Checks mandatory parameters.
   * @param params the parameters.
   */
  private void checkMandatoryParams(final EmbedStreamingPlayerParams params) {
    List<String> errorMessages = new ArrayList<>();
    if (isNotDefined(params.getUrl())) {
      errorMessages.add("url is not defined");
    }
    if (!errorMessages.isEmpty()) {
      throw new WebApplicationException(errorMessages.stream().collect(Collectors.joining(", ")),
          BAD_REQUEST);
    }
  }
}
