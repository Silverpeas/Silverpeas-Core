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
package org.silverpeas.core.web.export;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.util.html.HtmlCleaner;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ExportServlet extends HttpServlet {

  private static final long serialVersionUID = 1717365114830659355L;

  private static final String TYPE_ARRAYPANE = "ArrayPane";

  private final HtmlCleaner cleaner = new HtmlCleaner();

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Optional<ExportCSVBuilder> csvBuilder = ExportCSVBuilder.getFrom(HttpRequest.decorate(request));
    if (!csvBuilder.isPresent()) {
      // Get the session
      final HttpSession session = request.getSession(true);
      final String type = request.getParameter("type");
      final String name = request.getParameter("name");

      if (TYPE_ARRAYPANE.equals(type)) {
        csvBuilder = exportArrayPane(name, session);
      }
    }
    if (csvBuilder.isPresent()) {
      csvBuilder.get().sendTo(response);
    }
  }

  @SuppressWarnings("unchecked")
  private Optional<ExportCSVBuilder> exportArrayPane(final String name, final HttpSession session)
      throws IOException {
    ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
    if (StringUtil.isDefined(name)) {
      // Retrieve ArrayPane data in session
      final List<ArrayColumn> columns = (List<ArrayColumn>) session.getAttribute(name + "_columns");

      CSVRow header = new CSVRow();
      for (ArrayColumn curCol : columns) {
        header.addCell(curCol.getTitle());
      }
      csvBuilder.setHeader(header);

      final List<ArrayLine> lines = (List<ArrayLine>) session.getAttribute(name + "_lines");
      for (ArrayLine curLine : lines) {
        CSVRow row = new CSVRow();
        for (ArrayColumn curCol : columns) {
          final String fullCell = curLine.getCellAt(curCol.getColumnNumber()).print();
          String cleanCell = cleanHtmlCode(fullCell);
          cleanCell = cleanCell.replaceAll("[\\r\\n]", "");
          row.addCell(cleanCell);
        }
        csvBuilder.addLine(row);
      }
    }
    return Optional.of(csvBuilder);
  }

  /**
   * Clean HTML code
   * @param html the string to clean (removing HTML node and attributes)
   * @return text node
   * @throws IOException
   */
  private String cleanHtmlCode(String html) throws IOException {
    return cleaner.cleanHtmlFragment(html);
  }

}
