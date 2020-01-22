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
package org.silverpeas.core.web.export;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.web.http.FileResponse;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ExportCSVBuilder {

  private static final String FIELD_SEPARATOR = ";";
  private static final String EXPORT_ENCODING_DEFAULT = Charsets.UTF_8.name();
  private static final String REQUEST_ATTRIBUTE_NAME = "ExportCSVBuidler";

  private CSVRow header;
  private List<CSVRow> rows = new ArrayList<>();

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

  public String setupRequest(HttpRequest request) {
    request.setAttribute(REQUEST_ATTRIBUTE_NAME, this);

    return "/Export";
  }

  public static Optional<ExportCSVBuilder> getFrom(HttpRequest request) {
    return Optional.ofNullable(request.getAttribute(REQUEST_ATTRIBUTE_NAME))
        .map(ExportCSVBuilder.class::cast);
  }

  protected void sendTo(HttpServletResponse response) throws IOException {

    // Prepare response
    response.setContentType("text/csv");
    response.setCharacterEncoding(EXPORT_ENCODING_DEFAULT);

    response.setHeader("Content-Disposition", FileResponse
        .encodeAttachmentFilenameAsUtf8("export_data_" + System.currentTimeMillis() + ".csv"));
    final PrintWriter out = response.getWriter();

    if (header != null && !header.isEmpty()) {
      out.println(buildCSVRow(header));
    }

    for (CSVRow row : rows) {
      out.println(buildCSVRow(row));
    }

  }

  private String buildCSVRow(CSVRow row) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < row.size(); i++) {
      if (i != 0) {
        sb.append(FIELD_SEPARATOR);
      }
      addCSVValue(sb, row.getCell(i));
    }
    return sb.toString();
  }

  /**
   * Escape double quote
   * @param cell
   * @return CSV cell
   */
  private String formatCSVValue(Object cell) {
    if (cell == null) {
      return "";
    }
    String str = "";
    if (cell instanceof String) {
      str = (String) cell;
      str = str.replaceAll("\"", "\"\"");
      str = str.replace("<br/>", "\n");
    } else if (cell instanceof Integer) {
      str = Integer.toString((Integer) cell);
    } else if (cell instanceof Float) {
      str = Float.toString((Float) cell);
    } else if (cell instanceof Date) {
      str = DateUtil.getOutputDate((Date) cell,
          User.getCurrentRequester().getUserPreferences().getLanguage());
    } else if (cell instanceof User) {
      User user = (User) cell;
      str = user.getLastName() + " " + user.getFirstName();
    }

    return str;
  }

  private void addCSVValue(StringBuilder row, Object value) {
    row.append("\"");
    if (value != null) {
      row.append(formatCSVValue(value));
    }
    row.append("\"");
  }

}