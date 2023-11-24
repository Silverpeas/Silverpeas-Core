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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.annotation.Nonnull;

public class ArrayCellLink extends ArrayCell
    implements SimpleGraphicElement, Comparable<SimpleGraphicElement> {

  private String color = null;
  private final String link;
  private String info = null;
  private String target = null;

  public ArrayCellLink(String text, String link, ArrayLine line) {
    super(text, line);
    this.link = link;
  }

  public ArrayCellLink(String text, String link, String info, ArrayLine line) {
    super(text, line);
    this.link = link;
    this.info = info;
  }

  public String getText() {
    return getName();
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getLink() {
    return link;
  }

  public String getInfo() {
    return info;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public int compareTo(@Nonnull final SimpleGraphicElement other) {
    if (other instanceof ArrayEmptyCell) {
      return 1;
    }
    if (!(other instanceof ArrayCellLink)) {
      return 0;
    }
    ArrayCellLink tmp = (ArrayCellLink) other;

    return this.getText().compareToIgnoreCase(tmp.getText());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrayCellLink)) {
      return false;
    }

    final ArrayCellLink that = (ArrayCellLink) o;
    return that.getText().equals(((ArrayCellLink) o).getText());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getText()).hashCode();
  }

  @Override
  public String getSyntax() {
   StringBuilder result = new StringBuilder();

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
    }

    result.append("<a class=\"").append(getStyleSheet()).append("\" ");
    result.append("href=\"").append(getLink()).append("\"");

    if (getTarget() != null)
      result.append(" target=\"").append(getTarget()).append("\"");

    result.append(">");
    result.append(getText());
    result.append("</a>");

    if (getColor() != null) {
      result.append("</font>");
    }
    return result.toString();
  }

}
