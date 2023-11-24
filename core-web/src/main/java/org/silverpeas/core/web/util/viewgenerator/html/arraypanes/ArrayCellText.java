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
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class ArrayCellText extends ArrayCell implements SimpleGraphicElement,
    Comparable<Object> {

  private String text;
  private Object lazyInstance;
  private Function<Object, String> lazyText;
  private String color = null;

  private Comparable<?> compareOn = null;

  public ArrayCellText(String text, ArrayLine line) {
    super(text, line);
    this.text = text;
  }

  /**
   * The text of the cell is computed from a {@link Function} applied to the given
   * instance parameter.<br>
   * The function takes in input the given instance and the result must be a {@link String}.<br>
   * The advantage of this way of use is that the text is computed only when the line is displayed.
   * So that can be see as a lazy computation.<br>
   * Once the text computation is done, it is cached so that the computation is performed at most
   * one time.
   * @param instance the instance in input of the function.
   * @param lazyText the function to apply to the instance.
   * @param line the line of the array.
   */
  @SuppressWarnings("unchecked")
  public <T> ArrayCellText(T instance, Function<T, String> lazyText, ArrayLine line) {
    super(null, line);
    this.lazyInstance = instance;
    this.lazyText = (Function<Object, String>) lazyText;
  }

  public String getText() {
    if (text == null && lazyInstance != null && lazyText != null) {
      text = defaultStringIfNotDefined(
          WebEncodeHelper.javaStringToHtmlString(lazyText.apply(lazyInstance)));
    }
    return text;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getSyntax() {
    StringBuilder result = new StringBuilder();

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
      result.append(getText());
      result.append("</font>");
    } else {
      result.append(getText());
    }

    return result.toString();
  }

  public void setCompareOn(Comparable<?> comparable) {
    this.compareOn = comparable;
  }

  @SuppressWarnings("unchecked")
  public Comparable<Object> getCompareOn() {
    return (Comparable<Object>) this.compareOn;
  }

  public int compareTo(@Nonnull final Object other) {
    if (other instanceof ArrayEmptyCell) {
      return 1;
    }
    if (!(other instanceof ArrayCellText)) {
      return 0;
    }
    ArrayCellText tmp = (ArrayCellText) other;

    if (getCompareOn() != null && tmp.getCompareOn() != null && getCompareOn().getClass().equals(
        tmp.getCompareOn().getClass())) {
      Comparable<Object> otherComparable = tmp.getCompareOn();
      Comparable<Object> thisComparable = getCompareOn();
      return thisComparable.compareTo(otherComparable);
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrayCellText)) {
      return false;
    }

    final ArrayCellText that = (ArrayCellText) o;

    return text.equals(that.text);

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getText()).toHashCode();
  }
}
