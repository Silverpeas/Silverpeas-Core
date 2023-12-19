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
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Create a new ArrayPane.
 * @author cdm
 */
public class ArrayPaneTag extends TagSupport {

  public static final String ARRAY_PANE_PAGE_ATT = "pageContextArrayPane";
  private static final long serialVersionUID = 1370094709020971218L;
  private String var;
  private String title;
  private String summary;
  private String routingAddress;
  private boolean export;
  private String lineMoveCallback;
  private boolean movableLines;
  private int numberLinesPerPage;

  public ArrayPaneTag() {
    this.init();
  }

  void init() {
    var = null;
    title = null;
    summary = null;
    routingAddress = null;
    numberLinesPerPage = 10;
  }

  @Override
  public void release() {
    super.release();
    this.init();
  }

  @Override
  public int doStartTag() throws JspException {
    final GraphicElementFactory gef =
        (GraphicElementFactory) pageContext.getSession().getAttribute(
        "SessionGraphicElementFactory");
    ArrayPane arrayPane =
        gef.getArrayPane(var, routingAddress, pageContext.getRequest(), pageContext.getSession());
    if (title != null) {
      arrayPane.setTitle(title);
    }
    if (summary != null) {
      arrayPane.setSummary(summary);
    }
    arrayPane.setExportData(export);
    arrayPane.setVisibleLineNumber(numberLinesPerPage);
    arrayPane.setMovableLines(movableLines);
    arrayPane.setLineMoveCallback(lineMoveCallback);
    pageContext.setAttribute(ARRAY_PANE_PAGE_ATT, arrayPane);
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    final JspWriter out = pageContext.getOut();
    try {
      ArrayPane arrayPane = getArrayPane();
      pageContext.removeAttribute(ARRAY_PANE_PAGE_ATT);
      out.println(arrayPane.print());
    } catch (final IOException e) {
      throw new JspException("ArrayPane Tag", e);
    }
    return EVAL_PAGE;
  }

  public ArrayPane getArrayPane() {
    return (ArrayPane) pageContext.getAttribute(ARRAY_PANE_PAGE_ATT);
  }

  public String getRoutingAddress() {
    return routingAddress;
  }

  public void setRoutingAddress(String routingAddress) {
    this.routingAddress = routingAddress;
  }

  /**
   * The name of the session attribute that contains the ArrayPane.
   * @param name the name of the session attribute
   */
  public void setVar(final String name) {
    this.var = name;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setSummary(final String summary) {
    this.summary = summary;
  }


  public void setExport(boolean export) {
    this.export = export;
  }

  public void setNumberLinesPerPage(int numberLinesPerPage) {
    this.numberLinesPerPage = numberLinesPerPage;
  }

  @SuppressWarnings("unused")
  // the method is used by JSTL and the tag attribute is used in JSPs
  public void setMovableLines(boolean movable) {
    this.movableLines = movable;
  }

  @SuppressWarnings("unused")
  // the method is used by JSTL and the tag attribute is used in JSPs
  public void setOnLineMove(String callback) {
    this.lineMoveCallback = callback;
  }
}
