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
package org.silverpeas.core.web.export;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.date.TemporalFormatter;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.PreparedDownload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.web.http.FileResponse.encodeAttachmentFilenameAsUtf8;
import static org.silverpeas.core.web.http.PreparedDownload.getPreparedDownloadToPerform;

public class ExportCSVBuilder {

  private static final String LINE_SEPARATOR = "\n";
  private static final String FIELD_SEPARATOR = ";";
  private static final String EXPORT_ENCODING_DEFAULT = Charsets.UTF_8.name();
  private static final String REQUEST_ATTRIBUTE_NAME = "ExportCSVBuilder";

  private CSVRow header;
  private List<CSVRow> rows = new ArrayList<>();

  public static Optional<ExportCSVBuilder> getFrom(HttpRequest request) {
    return Optional.ofNullable(request.getAttribute(REQUEST_ATTRIBUTE_NAME))
        .map(ExportCSVBuilder.class::cast);
  }

  public String setupRequest(HttpRequest request) {
    request.setAttribute(REQUEST_ATTRIBUTE_NAME, this);
    return "/Export";
  }

  public void setHeader(CSVRow header) {
    this.header = header;
  }

  public void addLine(CSVRow row) {
    if (row != null && !row.isEmpty()) {
      rows.add(row);
    }
  }

  public void addLines(List<CSVRow> rows) {
    for (CSVRow row : rows) {
      addLine(row);
    }
  }

  protected void sendTo(final HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String filename = "export_data_" + System.currentTimeMillis() + ".csv";
    final String contentType = "text/csv";
    final Optional<PreparedDownload> preparedDownload = getPreparedDownloadToPerform(request, filename, contentType);
    if (preparedDownload.isPresent()) {
      try (final Writer out = preparedDownload.get().getWriter(EXPORT_ENCODING_DEFAULT)) {
        writeTo(out);
      }
      preparedDownload.get().sendDetails(response);
    } else {
      response.setContentType(contentType);
      response.setCharacterEncoding(EXPORT_ENCODING_DEFAULT);
      response.setHeader("Content-Disposition", encodeAttachmentFilenameAsUtf8(filename));
      writeTo(response.getWriter());
    }
  }

  private void writeTo(final Writer out) throws IOException {
    final boolean isHeader = header != null && !header.isEmpty();
    if (isHeader || !rows.isEmpty()) {
      out.write("\uFEFF");
    }
    if (isHeader) {
      out.append(buildCSVRow(header)).append(LINE_SEPARATOR);
    }
    for (final CSVRow row : rows) {
      out.append(buildCSVRow(row)).append(LINE_SEPARATOR);
    }
  }

  /**
   * Builds a line of CSV formatted values.
   * @param row data of a line.
   * @return the formatted CSV line.
   */
  private String buildCSVRow(CSVRow row) {
    return row.stream().map(this::formatCSVValue).collect(Collectors.joining(FIELD_SEPARATOR));
  }

  /**
   * Formats the given value.
   * <p>
   * Escaping double quote for string values.
   * </p>
   * @param value a value of a cell
   * @return the CSV formatted value
   */
  private String formatCSVValue(Object value) {
    if (value == null) {
      return EMPTY;
    }
    final MemoizedSupplier<String> userLanguage = new MemoizedSupplier<>(
        () -> User.getCurrentRequester().getUserPreferences().getLanguage());
    final MemoizedSupplier<ZoneId> userZoneId = new MemoizedSupplier<>(
        () -> User.getCurrentRequester().getUserPreferences().getZoneId());
    final String str;
    if (value instanceof String) {
      str = stringValue((String) value);
    } else if (value instanceof Integer) {
      str = Integer.toString((Integer) value);
    } else if (value instanceof Float) {
      str = Float.toString((Float) value);
    } else if (value instanceof Date) {
      str = DateUtil.getOutputDate((Date) value, userLanguage.get());
    } else if (value instanceof User) {
      final User user = (User) value;
      str = stringValue(user.getLastName() + " " + user.getFirstName());
    } else if (value instanceof Temporal) {
      str = TemporalFormatter.toLocalized((Temporal) value, userZoneId.get(), userLanguage.get());
    } else  {
      str = stringValue(value.toString());
    }
    return str;
  }

  /**
   * Formats a CSV string value by adding enclosing double quotes and escaping those contained
   * into value.
   * @param value a string value.
   * @return formatted string value.
   */
  private String stringValue(String value) {
    return "\"" + defaultStringIfNotDefined(value)
        .replace("\"", "\"\"").replaceAll("(?i)<[ ]*br[ ]*/[ ]*>", LINE_SEPARATOR) + "\"";
  }
}