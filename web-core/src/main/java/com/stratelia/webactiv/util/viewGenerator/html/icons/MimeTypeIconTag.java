/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.icons;

import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Properties;

public class MimeTypeIconTag extends TagSupport {

  private static final long serialVersionUID = 4375920954086066884L;
  private static final String DEFAULT_STYLE = "unknown";
  private static final Properties STYLES = new Properties();
  static {
    try {
      FileUtil.loadProperties(STYLES, "org/silverpeas/view/generator/mime_types_styles.properties");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  private String fileExtension = "";
  private String divId = "";

  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().println("</div>");
    } catch (IOException ioex) {
      throw new JspException(ioex);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    try {
      boolean styleFound = true;
      String style = STYLES.getProperty(this.fileExtension);
      if (style == null) {
        styleFound = false;
        style = STYLES.getProperty(DEFAULT_STYLE);
      }
      pageContext.getOut().print("<div class=\"");
      pageContext.getOut().print(style);
      if (this.divId != null) {
        styleFound = true;
        pageContext.getOut().print("\" id=\"");
        pageContext.getOut().print(divId);
      }
      pageContext.getOut().print("\">");
      if (styleFound) {
        pageContext.getOut().print("&nbsp;");
        return SKIP_BODY;
      }
      return EVAL_BODY_INCLUDE;
    } catch (IOException ioex) {
      throw new JspException(ioex);
    }
  }

  /**
   * @param fileName the fileName to set for the wanted icon.
   */
  public void setFileName(String fileName) {
    this.fileExtension = FileRepositoryManager.getFileExtension(fileName)
        .toLowerCase();
  }

  /**
   * @param fileExtension the file extension to set
   */
  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  /**
   * @param divId the id attribute for the cerated div.
   */
  public void setDivId(String divId) {
    this.divId = divId;
  }
}
