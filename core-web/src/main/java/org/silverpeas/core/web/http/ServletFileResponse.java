/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.http;

import org.silverpeas.core.io.file.SilverpeasFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import static org.silverpeas.kernel.util.StringUtil.isNotDefined;

/**
 * Centralizing the code which permits to send a file, and if necessary or asked to send it
 * partially.
 * @author Yohann Chastagnier
 */
public class ServletFileResponse extends FileResponse {

  private static final String CONTENT_LENGTH = "Content-Length";

  /**
   * Hidden constructor.
   * @param request the current http request.
   * @param response the current http response.
   */
  ServletFileResponse(final HttpServletRequest request, final HttpServletResponse response) {
    super(request, response);
  }

  /**
   * Centralization of getting of silverpeas file content. By default, the file will be for
   * download and not for viewing its content directly in the client (as the file content can
   * contain corrupting code).
   * @param file the silverpeas file to send.
   */
  public void sendSilverpeasFile(final SilverpeasFile file) {
    if (isNotDefined(forcedMimeType)) {
      forceMimeType(file.getMimeType());
    }
    sendPath(Paths.get(file.toURI()));
  }

  /**
   * Centralization of getting of a file content.
   * @param path the file to send.
   * headers.
   */
  void sendPath(final Path path) {
    try {
      Path absoluteFilePath = path.toAbsolutePath();
      String fileName = getFileName(absoluteFilePath);
      String fileMimeType = getMimeType(absoluteFilePath);
      int fullContentLength = (int) Files.size(absoluteFilePath);
      Matcher partialMatcher = getPartialMatcher();
      boolean isPartialRequest = partialMatcher.matches();

      response.setContentType(fileMimeType);
      final String filename = encodeAttachmentFilenameAsUtf8(fileName);
      response.setHeader("Content-Disposition", filename);
      if (isPartialRequest) {
        // Handling here a partial response (pseudo streaming)
        final ContentRangeData data = getContentRangeData(partialMatcher, fullContentLength);
        response.setHeader(CONTENT_LENGTH, String.format("%s", data.partContentLength));
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", getFileIdentifier(path));
        response.setDateHeader("Last-Modified", getLastModifiedDate(path).getTime());
        response.setDateHeader("Expires", getExpirationDate().getTime());
        response.setHeader("Content-Range", data.headerValue);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        partialOutputStream(absoluteFilePath, data, response.getOutputStream());
      } else {
        // Handling here a full response
        response.setHeader(CONTENT_LENGTH, String.valueOf(fullContentLength));
        fullOutputStream(path, response.getOutputStream());
      }

    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public ServletFileResponse forceFileId(final String fileId) {
    super.forceFileId(fileId);
    return this;
  }

  @Override
  public ServletFileResponse forceMimeType(final String mimeType) {
    super.forceMimeType(mimeType);
    return this;
  }

  @Override
  public ServletFileResponse forceCharacterEncoding(final String forcedCharacterEncoding) {
    super.forceCharacterEncoding(forcedCharacterEncoding);
    return this;
  }

  @Override
  public ServletFileResponse forceFileName(final String fileName) {
    super.forceFileName(fileName);
    return this;
  }

  @Override
  public ServletFileResponse noCache() {
    super.noCache();
    return this;
  }
}
