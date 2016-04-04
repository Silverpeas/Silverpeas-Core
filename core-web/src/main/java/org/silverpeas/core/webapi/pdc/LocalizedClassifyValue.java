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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.pdc.classification.ClassifyEngineException;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.Value;
import java.util.List;

/**
 * A decorator of a classify value by adding to it additional methods required to compute
 * information to render in a view. Theses information are localized according to the language
 * passed in the constructor.
 */
public class LocalizedClassifyValue extends ClassifyValue implements LocalizedValue {

  private static final long serialVersionUID = -8345717889242896633L;
  private final String language;
  private final ClassifyValue decoratedValue;

  public static LocalizedClassifyValue decorate(final ClassifyValue value, String forLanguage) {
    return new LocalizedClassifyValue(value, forLanguage);
  }

  /**
   * Gets the path of this value, on the PdC's axis, in which each path node is localized according
   * to the language.
   * @return the localized path of this value on the PdC's axis.
   */
  @Override
  public String getLocalizedPath() {
    String path;
    if (getFullPath().size() > MAX_NUMBER_OF_RENDERED_PATH_NODE) {
      path = buildTruncatedPath();
    } else {
      path = buildPathBetween(0, getFullPath().size());
    }

    if (path.equals(SEPARATOR_PATH)) {
      path = "";
    }
    return path;
  }

  /**
   * Gets the language used in the localization.
   * @return the language used in the localization.
   */
  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public String getAxisName() {
    return getDecoratedValue().getAxisName();
  }

  @Override
  public List<Value> getFullPath() {
    return getDecoratedValue().getFullPath();
  }

  @Override
  public void setAxisName(String axisName) {
    getDecoratedValue().setAxisName(axisName);
  }

  @Override
  public void setFullPath(List<Value> fullPath) {
    getDecoratedValue().setFullPath(fullPath);
  }

  @Override
  public String toString() {
    return getDecoratedValue().toString();
  }

  @Override
  public void checkValue() throws ClassifyEngineException {
    getDecoratedValue().checkValue();
  }

  @Override
  public int getAxisId() {
    return getDecoratedValue().getAxisId();
  }

  @Override
  public int getPhysicalAxisId() {
    return getDecoratedValue().getPhysicalAxisId();
  }

  @Override
  public String getValue() {
    return getDecoratedValue().getValue();
  }

  @Override
  public void setAxisId(int nGivenAxisId) {
    getDecoratedValue().setAxisId(nGivenAxisId);
  }

  @Override
  public void setPhysicalAxisId(int id) {
    getDecoratedValue().setPhysicalAxisId(id);
  }

  @Override
  public void setValue(String sGivenValue) {
    getDecoratedValue().setValue(sGivenValue);
  }

  @Override
  public boolean equals(Object o) {
    return getClass() == o.getClass() && getDecoratedValue().equals(o);
  }

  @Override
  public int hashCode() {
    return getDecoratedValue().hashCode();
  }

  private LocalizedClassifyValue(final ClassifyValue value, String language) {
    this.decoratedValue = value;
    this.language = language;
  }

  private ClassifyValue getDecoratedValue() {
    return decoratedValue;
  }

  private String buildTruncatedPath() {
    int nodeCount = getFullPath().size();
    return buildPathBetween(0, NUMBER_OF_RENDERED_PATH_NODE_IN_TRUNCATION)
        + SEPARATOR_PATH + TRUNCATION_SEPARATOR + SEPARATOR_PATH
        + buildPathBetween(nodeCount - NUMBER_OF_RENDERED_PATH_NODE_IN_TRUNCATION, nodeCount);
  }

  private String buildPathBetween(int startIndex, int endIndex) {
    if (startIndex < 0 || startIndex >= getFullPath().size() || endIndex < startIndex) {
      throw new IndexOutOfBoundsException("The indexes are out of bounds (startIndex=" + startIndex
          + ", endIndex=" + endIndex + ")");
    }
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < endIndex; i++) {
      Value value = getFullPath().get(i);
      path.append(value.getName(getLanguage())).append(SEPARATOR_PATH);
    }
    return path.substring(0, path.length() - 2);
  }
}
