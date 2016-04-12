/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import java.util.function.Function;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * Class declaration
 * @author
 */
public class ArrayCellText extends ArrayCell implements SimpleGraphicElement, Comparable {

  private String text;
  private Object lazyInstance;
  private Function<Object, String> lazyText;
  private String alignement = null;
  private String color = null;
  private String valignement = null;
  private boolean noWrap = false;

  private Comparable compareOn = null;

  /**
   * Constructor declaration
   * @param text
   * @param line
   * @see
   */
  public ArrayCellText(String text, ArrayLine line) {
    super(line);
    this.text = text;
  }

  /**
   * The text of the cell is computed from a {@link Function<T,String>} applied to the given
   * instance parameter.<br/>
   * The function takes in input the given instance and the result must be a {@link String}.<br/>
   * The advantage of this way of use is that the text is computed only when the line is displayed.
   * So that can be see as a lazy computation.<br/>
   * Once the text computation is done, it is cached so that the computation is performed at most
   * one time.
   * @param instance the instance in input of the function.
   * @param lazyText the function to apply to the instance.
   * @param line the line of the array.
   */
  @SuppressWarnings("unchecked")
  public <T> ArrayCellText(T instance, Function<T, String> lazyText, ArrayLine line) {
    super(line);
    this.lazyInstance = instance;
    this.lazyText = (Function) lazyText;
  }

  /**
   * @return
   */
  public String getText() {
    if (text == null && lazyInstance != null && lazyText != null) {
      text = defaultStringIfNotDefined(
          EncodeHelper.javaStringToHtmlString(lazyText.apply(lazyInstance)));
    }
    return text;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * @param alignement
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  /**
   * @return
   */
  public boolean getNoWrap() {
    return noWrap;
  }

  /**
   * @param noWrap
   */
  public void setNoWrap(boolean noWrap) {
    this.noWrap = noWrap;
  }

  /**
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @param color
   */
  public void setColor(String color) {
    this.color = color;
  }

  public String getValignement() {
    return valignement;
  }

  /**
   * @param valignement
   */
  public void setValignement(String valignement) {
    this.valignement = valignement;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuilder result = new StringBuilder();

    result.append("<td ");

    if (getAlignement() != null) {
      if (getAlignement().equalsIgnoreCase("center")
          || getAlignement().equalsIgnoreCase("right")) {
        result.append(" align=\"").append(getAlignement()).append("\"");
      }
    }

    if (getValignement() != null) {
      if (getValignement().equalsIgnoreCase("bottom")
          || getValignement().equalsIgnoreCase("top")
          || getValignement().equalsIgnoreCase("baseline")) {
        result.append(" valign=\"").append(getValignement()).append("\"");
      }
    }

    if (getNoWrap()) {
      result.append(" nowrap=\"nowrap\"");
    }

    result.append(" class=\"").append(getStyleSheet()).append("\">");

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
      result.append(getText());
      result.append("</font>");
    } else {
      result.append(getText());
    }

    result.append("</td>\n");
    return result.toString();
  }

  /**
   * Method declaration
   * @param object
   * @see
   */
  public void setCompareOn(Comparable object) {
    this.compareOn = object;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public Comparable getCompareOn() {
    return this.compareOn;
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public int compareTo(final java.lang.Object other) {
    if (other instanceof ArrayEmptyCell) {
      return 1;
    }
    if (!(other instanceof ArrayCellText)) {
      return 0;
    }
    ArrayCellText tmp = (ArrayCellText) other;

    if (getCompareOn() != null && tmp.getCompareOn() != null && getCompareOn().getClass().equals(
        tmp.getCompareOn().getClass())) {
      return getCompareOn().compareTo(tmp.getCompareOn());
    }

    if (this.getText() != null && tmp.getText() != null) {
      return this.getText().compareToIgnoreCase(tmp.getText());
    }

    if (this.getText() == null && tmp.getText() == null) {
      return 0;
    } else if (this.getText() == null) {
      return -1;
    } else {
      return +1;
    }
  }
}
