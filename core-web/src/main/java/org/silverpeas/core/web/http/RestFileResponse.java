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
package org.silverpeas.core.web.http;

import org.silverpeas.core.io.file.SilverpeasFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * Centralizing the code which permits to send a file, and if necessary or asked to send it
 * partially.
 * @author Yohann Chastagnier
 */
public class RestFileResponse extends FileResponse {

  /**
   * Hidden constructor.
   * @param request the current http request.
   * @param response the current http response.
   */
  RestFileResponse(final HttpServletRequest request, final HttpServletResponse response) {
    super(request, response);
  }

  /**
   * Centralization of getting of silverpeas file content.
   * <p>
   * A download context flag is verified from parameters and attributes of the request.
   * </p>
   * @param file the silverpeas file to send.
   * @return the response builder.
   */
  public Response.ResponseBuilder silverpeasFile(final SilverpeasFile file) {
    return silverpeasFile(file, isDownloadContext());
  }

  /**
   * Centralization of getting of silverpeas file content.
   * <p>
   * Using directly this method means that the request downloadContext flag is ignored.
   * </p>
   * @param file the silverpeas file to send.
   * @param downloadContext indicating a download context in order to specify rightly response
   * headers.
   * @return the response builder.
   */
  public Response.ResponseBuilder silverpeasFile(final SilverpeasFile file,
      final boolean downloadContext) {
    if (isNotDefined(forcedMimeType)) {
      forceMimeType(file.getMimeType());
    }
    return path(Paths.get(file.toURI()), downloadContext);
  }

  /**
   * Centralization of getting of a file content.
   * @param path the file to send.
   * @param downloadContext indicating a download context in order to specify rightly response
   * headers.
   * @return the response.
   */
  private Response.ResponseBuilder path(final Path path, final boolean downloadContext) {
    try {
      final Path absoluteFilePath = path.toAbsolutePath();
      final String fileName = getFileName(absoluteFilePath);
      final String fileMimeType = getMimeType(absoluteFilePath);

      final int fullContentLength = (int) Files.size(absoluteFilePath);
      final Matcher partialMatcher = getPartialMatcher();
      final boolean isPartialRequest = partialMatcher.matches();

      final Response.ResponseBuilder responseBuilder;
      if (isPartialRequest) {
        // Handling here a partial response (pseudo streaming)
        responseBuilder =
            getPartialResponseBuilder(partialMatcher, absoluteFilePath, fullContentLength);
      } else {
        // Handling here a full response
        responseBuilder = getFullResponseBuilder(absoluteFilePath, fullContentLength);
      }

      final String filename = downloadContext
          ? encodeAttachmentFilenameAsUtf8(fileName)
          : encodeInlineFilenameAsUtf8(fileName);
      return responseBuilder.type(fileMimeType).header("Content-Disposition", filename);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public RestFileResponse forceFileId(final String fileId) {
    super.forceFileId(fileId);
    return this;
  }

  @Override
  public RestFileResponse forceMimeType(final String mimeType) {
    super.forceMimeType(mimeType);
    return this;
  }

  @Override
  public RestFileResponse forceCharacterEncoding(final String forcedCharacterEncoding) {
    super.forceCharacterEncoding(forcedCharacterEncoding);
    return this;
  }

  @Override
  public RestFileResponse forceFileName(final String fileName) {
    super.forceFileName(fileName);
    return this;
  }

  @Override
  public RestFileResponse noCache() {
    super.noCache();
    return this;
  }

  /**
   * Gets the full response builder according to given parameters.
   * @param path the full path of the file to send.
   * @param fullContentLength the full content length of the file to send.
   * @return the parametrized response builder.
   * @throws IOException if the file is not readable.
   */
  private Response.ResponseBuilder getPartialResponseBuilder(Matcher partialHeaders, Path path,
      final int fullContentLength) throws IOException {
    final ContentRangeData data = getContentRangeData(partialHeaders, fullContentLength);
    return Response.status(Response.Status.PARTIAL_CONTENT)
        .entity(getPartialOutputStream(path, data))
        .header("Content-Length", data.partContentLength)
        .header("Accept-Ranges", "bytes")
        .header("ETag", getFileIdentifier(path))
        .lastModified(getLastModifiedDate(path))
        .expires(getExpirationDate())
        .header("Content-Range", data.headerValue);
  }


  /**
   * Gets the partial streaming output.
   * @param path the path to physical content.
   * @param partialData the partial data.
   * @return the initialized streaming output.
   */
  private StreamingOutput getPartialOutputStream(final Path path,
      final ContentRangeData partialData) {
    return output -> partialOutputStream(path, partialData, output);
  }

  /**
   * Gets the full response builder according to given file.
   * @param path the file to send.
   * @param fullContentLength the full content length of the file to send.
   * @return the parametrized response builder.
   */
  private Response.ResponseBuilder getFullResponseBuilder(final Path path,
      final int fullContentLength) {
    StreamingOutput streamingOutput = output -> fullOutputStream(path, output);
    return Response.ok(streamingOutput).header("Content-Length", fullContentLength);
  }
}
