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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.html.SupportedWebPlugin;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * From a Servlet or a Web Service, this class handles the forwarding of the request to the media
 * embed player services.<br>
 * The data necessary to perform this operation are set by the JavasScript plugin {@link
 * SupportedWebPlugin.Constants#EMBEDPLAYER}.
 * @author Yohann Chastagnier
 */
public class EmbedMediaPlayerDispatcher {

  private static final String SERVICES_URI = "/services";
  private static final String SERVICES_MEDIA_PLAYER_EMBED_URI = SERVICES_URI+ "/media/player/embed";
  private static final String EMBED_PLAYER_PARAMETER = "embedPlayer";
  private static final String MEDIA_URL_PARAMETER = "url";
  private static final String MIME_TYPE_PARAMETER = "mimeType";
  private static final String PLAYER_TYPE_PARAMETER = "playerType";
  private static final String CACHE_PARAMETER = "t_";
  private static final Pattern AUDIO_MIME_TYPE_PATTERN =
      Pattern.compile(".*\\b(audio|mp3|mpeg3|mpeg-3)\\b.*");

  private final HttpServletRequest request;
  private final HttpServletResponse response;

  /**
   * Hidden constructor.
   */
  private EmbedMediaPlayerDispatcher(final HttpServletRequest request,
      final HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Initializing the file response context.
   * @param request the current request.
   * @param response the current response.
   * @return the initialized file response.
   */
  public static EmbedMediaPlayerDispatcher from(final HttpServletRequest request,
      final HttpServletResponse response) {
    return new EmbedMediaPlayerDispatcher(request, response);
  }

  /**
   * Dispatches the current request to embed media player services if the ({@link
   * #EMBED_PLAYER_PARAMETER}) of the request is set to {@code true}.
   * @param file the silverpeas file to take into account into dispatch operation.
   * @return true the dispatching has been performed (explicit forward), false otherwise.
   */
  public boolean dispatchWithSilverpeasFile(SilverpeasFile file) {
    return dispatch(file, file.getMimeType());
  }

  /**
   * Returns response builder to redirect to embed media player services if the ({@link
   * #EMBED_PLAYER_PARAMETER}) of the request is set to {@code true}.
   * @param file the silverpeas file to take into account into dispatch operation.
   * @return {@link Response.ResponseBuilder} if a redirect is necessary, null otherwise.
   */
  public Response.ResponseBuilder seeOtherWithSilverpeasFile(SilverpeasFile file) {
    return seeOther(file, file.getMimeType());
  }

  /**
   * Performs the request dispatch if necessary.
   * @param fileToSend the file to send.
   * @param mimeType the mime type of the file to be played.
   * @return true the dispatching has been performed (explicit forward), false otherwise.
   */
  private boolean dispatch(final File fileToSend, String mimeType) {
    String forcedMimeType = request.getParameter(MIME_TYPE_PARAMETER);
    final String finalMimeType = isDefined(forcedMimeType) ? forcedMimeType : mimeType;
    boolean isEmbedPlayerRequested = isEmbedPlayerRequested();
    if (isEmbedPlayerRequested) {
      UriBuilder uriBuilder = UriBuilder.fromPath(SERVICES_MEDIA_PLAYER_EMBED_URI);
      applyCommonParameters(fileToSend, uriBuilder, finalMimeType);
      RequestDispatcher dispatcher = request.getRequestDispatcher(uriBuilder.build().toString());
      try {
        dispatcher.forward(request, response);
      } catch (ServletException | IOException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
    return isEmbedPlayerRequested;
  }

  /**
   * Performs the request dispatch if necessary.
   * @param fileToSend the file to send.
   * @param mimeType the mime type of the file to be played.
   * @return true the dispatching has been performed (explicit forward), false otherwise.
   */
  private Response.ResponseBuilder seeOther(final File fileToSend, String mimeType) {
    String forcedMimeType = request.getParameter(MIME_TYPE_PARAMETER);
    final String finalMimeType = isDefined(forcedMimeType) ? forcedMimeType : mimeType;
    if (isEmbedPlayerRequested()) {
      UriBuilder uriBuilder =
          UriBuilder.fromPath(SERVICES_MEDIA_PLAYER_EMBED_URI.substring(SERVICES_URI.length()));
      copyParametersFromRequest(uriBuilder);
      applyCommonParameters(fileToSend, uriBuilder, finalMimeType);
      return Response.seeOther(uriBuilder.build());
    }
    return null;
  }

  /**
   * Apply common parameters.
   * @param fileToSend the file to send.
   * @param uriBuilder the uri builder.
   * @param finalMimeType the mime-type.
   */
  private void applyCommonParameters(final File fileToSend, final UriBuilder uriBuilder,
      final String finalMimeType) {
    UriBuilder mediaUriBuilder = UriBuilder.fromPath(request.getRequestURI());
    copyParametersFromRequest(mediaUriBuilder);
    setCacheParameter(fileToSend, mediaUriBuilder);
    uriBuilder.replaceQueryParam(MEDIA_URL_PARAMETER, mediaUriBuilder.build().toString());
    uriBuilder.replaceQueryParam(MIME_TYPE_PARAMETER, finalMimeType);
    uriBuilder.replaceQueryParam(PLAYER_TYPE_PARAMETER, getPlayerType(finalMimeType));
    setCacheParameter(fileToSend, uriBuilder);
  }

  /**
   * Copies the parameters set on the original request.
   * @param uriBuilder the uri builder (destination)
   */
  private void copyParametersFromRequest(final UriBuilder uriBuilder) {
    Enumeration<String> params = request.getParameterNames();
    while (params.hasMoreElements()) {
      final String paramName = params.nextElement();
      if (!EMBED_PLAYER_PARAMETER.equals(paramName) && !"_".equals(paramName)) {
        uriBuilder.queryParam(paramName, request.getParameter(paramName));
      }
    }
  }

  /**
   * Sets the cache parameter into given URI builder.
   * @param fileToSend the file to send.
   * @param mediaUriBuilder the URI builder.
   */
  private void setCacheParameter(final File fileToSend, final UriBuilder mediaUriBuilder) {
    mediaUriBuilder.replaceQueryParam(CACHE_PARAMETER, fileToSend.lastModified());
  }

  /**
   * Indicated from the request if the embed media player is requested.
   * @return true if requested, false otherwise.
   */
  private boolean isEmbedPlayerRequested() {
    return StringUtil.getBooleanValue(request.getParameter(EMBED_PLAYER_PARAMETER));
  }

  /**
   * Gets the player type to display from the given mime type.
   * @param mimeType the mime type from which the player type is guessed.
   * @return the player type (video or audio).
   */
  private String getPlayerType(String mimeType) {
    String playerType = request.getParameter(PLAYER_TYPE_PARAMETER);
    if (isNotDefined(playerType)) {
      if (AUDIO_MIME_TYPE_PATTERN.matcher(mimeType).matches()) {
        playerType = "audio";
      } else {
        playerType = "video";
      }
    }
    return playerType;
  }
}
