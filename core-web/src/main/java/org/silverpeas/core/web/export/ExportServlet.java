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

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.html.HtmlCleaner;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportServlet extends HttpServlet {

  private static final long serialVersionUID = 1717365114830659355L;

  private static final String TYPE_ARRAYPANE = "ArrayPane";
  private static final String FIELD_SEPARATOR = ";";
  private static final String EXPORT_ENCODING_DEFAULT = "UTF8";

  private static final String EXPORT_ENCODING = ResourceLocator.getSettingBundle(
      "org.silverpeas.util.viewGenerator.settings.graphicElementFactorySettings")
      .getString("gef.arraypane.export.encoding", EXPORT_ENCODING_DEFAULT);

  private final HtmlCleaner cleaner = new HtmlCleaner();

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Prepare response
    response.setContentType("text/csv");
    response.setCharacterEncoding(EXPORT_ENCODING);

    response.setHeader("Content-Disposition", "attachment; fileName=export_data_" +
        System.currentTimeMillis() + ".csv");
    final PrintWriter out = response.getWriter();

    // Get the session
    final HttpSession session = request.getSession(true);
    final String type = request.getParameter("type");
    final String name = request.getParameter("name");

    if (TYPE_ARRAYPANE.equals(type)) {
      exportArrayPane(name, session, out);
    }
  }

  @SuppressWarnings("unchecked")
  private void exportArrayPane(final String name, final HttpSession session, final PrintWriter out)
      throws IOException {
    if (StringUtil.isDefined(name)) {
      // Retrieve ArrayPane data in session
      final List<ArrayColumn> columns = (List<ArrayColumn>) session.getAttribute(name + "_columns");

      StringBuilder listColumns = new StringBuilder();
      final int lastIndex = columns.size() - 1;
      for (ArrayColumn curCol : columns) {
        listColumns.append("\"").append(formatCSVCell(curCol.getTitle())).append("\"");
        if (columns.indexOf(curCol) != lastIndex) {
          listColumns.append(FIELD_SEPARATOR);
        }
      }
      out.println(listColumns.toString());

      final List<ArrayLine> lines = (List<ArrayLine>) session.getAttribute(name + "_lines");
      for (ArrayLine curLine : lines) {
        listColumns = new StringBuilder();
        for (ArrayColumn curCol : columns) {
          final String fullCell = curLine.getCellAt(curCol.getColumnNumber()).print();
          String cleanCell = cleanHtmlCode(fullCell);
          cleanCell = cleanCell.replaceAll("[\\r\\n]", "");
          listColumns.append("\"").append(formatCSVCell(cleanCell)).append("\"");
          if (columns.indexOf(curCol) != lastIndex) {
            listColumns.append(FIELD_SEPARATOR);
          }
        }
        out.println(listColumns.toString());
      }
    }
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

  /**
   * Escape double quote
   * @param cell
   * @return CSV cell
   */
  private String formatCSVCell(String cell) {
    if (StringUtil.isNotDefined(cell)) {
      return "";
    }
    return cell.replaceAll("\"", "\"\"");
  }
}
