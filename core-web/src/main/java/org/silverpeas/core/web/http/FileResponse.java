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
package org.silverpeas.core.web.http;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.READ;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * Centralizing the code which permits to send a file, and if necessary or asked to send it
 * partially.
 * @author Yohann Chastagnier
 */
public abstract class FileResponse {

  private static final int MAX_PATH_LENGTH_IN_LOGS = 100;
  private static final int BUFFER_LENGTH = 1024 * 16;
  private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
  private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24l;

  final HttpServletResponse response;
  private final HttpServletRequest request;

  String forcedMimeType;
  private String forcedFileId;

  /**
   * Hidden constructor.
   * @param request the current http request.
   * @param response the current http response.
   */
  FileResponse(final HttpServletRequest request, final HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * Encodes the content disposition with inline filename as UTF8.
   * @param filename the filename to encode.
   * @return the encoded
   */
  public static String encodeInlineFilenameAsUtf8(String filename) {
    String normalized = StringUtil.normalize(filename);
    normalized = URLUtil.encodeURL(normalized).replace("+", "%20");
    return String.format("inline; filename*=UTF-8''%s", normalized);
  }

  /**
   * Encodes the content disposition with attachment filename as UTF8.
   * @param filename the filename to encode.
   * @return the encoded
   */
  public static String encodeAttachmentFilenameAsUtf8(String filename) {
    String normalized = StringUtil.normalize(filename);
    normalized = URLUtil.encodeURL(normalized).replace("+", "%20");
    return String.format("attachment; filename*=UTF-8''%s", normalized);
  }

  /**
   * Initializing the file response context.
   * @param request the current request.
   * @param response the current response.
   * @return the initialized file response.
   */
  public static RestFileResponse fromRest(final HttpServletRequest request,
      final HttpServletResponse response) {
    return new RestFileResponse(request, response);
  }

  /**
   * Initializing the file response context.
   * @param request the current request.
   * @param response the current response.
   * @return the initialized file response.
   */
  public static ServletFileResponse fromServlet(final HttpServletRequest request,
      final HttpServletResponse response) {
    return new ServletFileResponse(request, response);
  }

  /**
   * Gets mime type.
   * @param absoluteFilePath the absolute file path.
   * @return the mime type.
   */
  String getMimeType(final Path absoluteFilePath) {
    String mediaMimeType = request.getParameter("forceMimeType");
    if (isNotDefined(mediaMimeType)) {
      if (isDefined(forcedMimeType)) {
        mediaMimeType = forcedMimeType;
      } else {
        mediaMimeType = FileUtil.getMimeType(absoluteFilePath.toString());
      }
    }
    return mediaMimeType;
  }

  /**
   * Forces the file identifier.<br>
   * If not forced, the absolute path of the file into Base64 is
   * computed.
   * @param fileId the file identifier.
   * @return itself.
   */
  public FileResponse forceFileId(final String fileId) {
    this.forcedFileId = fileId;
    return this;
  }

  /**
   * Forces the mime type of the response.<br>
   * If not forced, the mime type is computed from the file itself.<br>
   * Even if a mime type has been forced, if the request contains into headers a valuated
   * {@code forceMimeType} parameter, the mime type from the request is taken into account.
   * @param mimeType the mime type to set.
   * @return itself.
   */
  public FileResponse forceMimeType(final String mimeType) {
    this.forcedMimeType = mimeType;
    return this;
  }

  /**
   * Fills partially the output response.
   * @param path the path of the file.
   * @param partialData the partial data.
   * @param output the output stream to write into.
   */
  void partialOutputStream(final Path path, final ContentRangeData partialData,
      final OutputStream output) throws IOException {
    SilverLogger.getLogger(this).debug("{0} - start at {1} - end at {2} - partLength {3}",
        StringUtil.abbreviate(path.toString(), path.toString().length(), MAX_PATH_LENGTH_IN_LOGS),
        partialData.start, partialData.end, partialData.partContentLength);
    try (SeekableByteChannel input = Files.newByteChannel(path, READ)) {
      input.position(partialData.start);
      int bytesRead;
      int bytesLeft = partialData.partContentLength;
      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
      while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
        buffer.clear();
        output.write(buffer.array(), 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
        bytesLeft -= bytesRead;
      }
      SilverLogger.getLogger(this).debug("{0} - all part content bytes sent", StringUtil
          .abbreviate(path.toString(), path.toString().length(), MAX_PATH_LENGTH_IN_LOGS));
    } catch (IOException ioe) {
      SilverLogger.getLogger(this).debug(
          "client stopping the streaming HTTP Request of file content represented by " +
              "''{0}'' identifier (original message ''{1}'')",
          StringUtil.abbreviate(path.toString(), path.toString().length(), MAX_PATH_LENGTH_IN_LOGS),
          ioe.getMessage(), ioe);
    }
  }

  /**
   * Fills fully the output response.
   * @param path the path of the file.
   * @param output the output stream to write into.
   */
  void fullOutputStream(final Path path, final OutputStream output) {
    try (final InputStream stream = FileUtils.openInputStream(path.toFile())) {
      IOUtils.copy(stream, output);
    } catch (IOException e) {
      throw new WebApplicationException(e, Response.Status.NOT_FOUND);
    }
  }

  /**
   * Gets the file identifier
   * @param path the path to the physical content.
   * @return the file identifier.
   * @throws IOException if the file is not readable.
   */
  String getFileIdentifier(final Path path) throws IOException {
    if (isDefined(forcedFileId)) {
      return forcedFileId;
    }
    final File file = path.toFile();
    final String sb =
            String.valueOf(FileUtils.checksumCRC32(file)) +
            "|" + file.getName() +
            "|" + file.length() +
            "|" + file.lastModified();
    return Base64.getEncoder().encodeToString(sb.getBytes());
  }

  /**
   * Gets expiration date.
   * @return the expiration date.
   */
  Date getExpirationDate() {
    return Date.from(Instant.ofEpochMilli(System.currentTimeMillis() + EXPIRE_TIME));
  }

  /**
   * Gets last modified date.
   * @param path the path of the file to send.
   * @return the last modified date.
   * @throws IOException if the file is not readable.
   */
  Date getLastModifiedDate(final Path path) throws IOException {
    return Date.from(Files.getLastModifiedTime(path).toInstant());
  }

  /**
   * Gets partial matcher.
   * @return the partial matcher.
   */
  Matcher getPartialMatcher() {
    String range = defaultStringIfNotDefined(request.getHeader("Range"), "");
    return RANGE_PATTERN.matcher(range);
  }

  /**
   * Gets the content range data.
   * @param partialHeaders the partial hints.
   * @param fullContentLength the full content of the file to send.
   * @return the content range data.
   */
  ContentRangeData getContentRangeData(final Matcher partialHeaders, final int fullContentLength) {
    final int endOfFullContent = fullContentLength - 1;
    response.setBufferSize(BUFFER_LENGTH);

    String startGroup = partialHeaders.group("start");
    int start = startGroup.isEmpty() ? 0 : Integer.valueOf(startGroup);
    start = start < 0 ? 0 : start;

    String endGroup = partialHeaders.group("end");
    int end = endGroup.isEmpty() ? endOfFullContent : Integer.valueOf(endGroup);
    end = end > endOfFullContent ? endOfFullContent : end;

    int partContentLength = end - start + 1;
    return new ContentRangeData(start, end, partContentLength,
        String.format("bytes %s-%s/%s", start, end, fullContentLength));
  }

  static class ContentRangeData {
    final int start;
    final int end;
    final int partContentLength;
    final String headerValue;

    ContentRangeData(final int start, final int end, final int partContentLength,
        final String headerValue) {
      this.start = start;
      this.end = end;
      this.partContentLength = partContentLength;
      this.headerValue = headerValue;
    }
  }
}
