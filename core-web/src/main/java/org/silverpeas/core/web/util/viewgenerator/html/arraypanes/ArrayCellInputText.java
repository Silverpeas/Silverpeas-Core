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
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

public class ArrayCellInputText extends ActionableArrayCell implements SimpleGraphicElement {

  private String size = null;
  private String maxlength = null;
  private String color = null;
  private String bgcolor = null;
  private String textAlign = null;
  private boolean readOnly = false;

  private final String value;

  private String syntax = "";

  public ArrayCellInputText(String name, String value, ArrayLine line) {
    super(name, line);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getMaxlength() {
    return maxlength;
  }

  public void setMaxlength(String maxlength) {
    this.maxlength = maxlength;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getBgColor() {
    return bgcolor;
  }

  public void setBgColor(String bgcolor) {
    this.bgcolor = bgcolor;
  }

  public String getTextAlign() {
    return textAlign;
  }

  public void setTextAlign(String textAlign) {
    this.textAlign = textAlign;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public String getSyntax() {

    syntax += " <input type=\"text\" name=\"" +
        (StringUtil.isNotDefined(getName()) ? "textfield" : getName()) + "\" value=\"" +
        (StringUtil.isNotDefined(getValue()) ? "" : getValue()) + "\"";

    // param size
    if (getSize() != null) {
      syntax += " size=\"" + getSize() + "\"";
    }

    // param maxlength
    if (getMaxlength() != null) {
      syntax += " maxlength=\"" + getMaxlength() + "\"";
    }

    // set Style
    syntax += " style=\"";

    // param likeText
    if (isReadOnly()) {
      syntax += "border: 1 solid rgb(255,255,255);";
    }

    // param textAlign
    if (getTextAlign() != null) {
      syntax += "text-align:" + getTextAlign() + ";";
    }

    // param color
    if (getColor() != null) {
      syntax += " color:" + getColor() + ";";
    }

    // param background color
    if (getBgColor() != null) {
      syntax += " background-color:" + getBgColor() + ";";
    }

    syntax += "\"";

    // param action JavaScript
    if (getAction() != null) {
      syntax += " " + getAction();
    }

    // readOnly ???
    if (isReadOnly()) {
      syntax += " readOnly";
    }

    syntax += "/>";

    return syntax;
  }

}
