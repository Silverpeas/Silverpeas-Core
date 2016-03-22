/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class NavigationListItemTag extends TagSupport {

  private static final long serialVersionUID = -8969022796706694482L;

  private String label;
  private String action;
  private Integer nbElem;
  private String description;
  private String universalLink;

  public void setAction(final String action) {
    this.action = action;
  }

  public void setNbElem(final int nbElem) {
    this.nbElem = nbElem;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setUniversalLink(final String universalLink) {
    this.universalLink = universalLink;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public int doEndTag() throws JspException {
    NavigationListTag navigationList =
        (NavigationListTag) findAncestorWithClass(this, NavigationListTag.class);
    final Item item;
    if (nbElem != null) {
      item = new Item(label, action, nbElem, description);
    } else {
      item = new Item(label, action, description);
    }
    item.setUniversalLink(universalLink);
    navigationList.addItem(item);
    return EVAL_PAGE;
  }
}
