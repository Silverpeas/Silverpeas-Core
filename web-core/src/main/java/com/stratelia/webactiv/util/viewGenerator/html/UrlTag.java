/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.util.viewGenerator.html;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.silverpeas.web.token.SynchronizerTokenService;
import org.silverpeas.web.token.SynchronizerTokenServiceFactory;

/**
 * A tag to print out a valid URL from the path of a web resource relative to the application
 * context.
 *
 * @author mmoquillon
 */
public class UrlTag extends TagSupport {

  private static final long serialVersionUID = -590355763653543757L;

  private String value;
  private boolean toProtect;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isProtected() {
    return toProtect;
  }

  public void setProtected(boolean toProtect) {
    this.toProtect = toProtect;
  }

  @Override
  public int doStartTag() throws JspException {
    HttpServletRequest request = getRequest();
    String url = request.getContextPath() + "/" + getValue();
    if (isProtected()) {
      SynchronizerTokenService service = getSynchronizerTokenService();
      url = service.stampsResourceURL(url, getRequest());
    }
    try {
      getOut().print(url);
    } catch (IOException ex) {
      Logger.getLogger(UrlTag.class.getName()).log(Level.SEVERE, null, ex);
      throw new JspException(ex.getMessage(), ex);
    }
    return EVAL_PAGE;
  }

  protected JspWriter getOut() {
    return pageContext.getOut();
  }

  protected HttpServletRequest getRequest() {
    return (HttpServletRequest) pageContext.getRequest();
  }

  protected SynchronizerTokenService getSynchronizerTokenService() {
    return SynchronizerTokenServiceFactory.getSynchronizerTokenService();
  }

}
