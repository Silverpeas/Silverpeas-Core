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

import org.silverpeas.core.pdc.pdc.model.PdcAxisValue;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import java.util.Set;

/**
 * A decorator of a Pdc axis value by adding to it additional methods required to compute
 * information to render in a view. Theses information are localized according to the language
 * passed in the constructor.
 */
public class LocalizedPdcAxisValue extends PdcAxisValue implements LocalizedValue {

  private static final long serialVersionUID = 3041692821847135712L;
  private final String language;
  private final PdcAxisValue decoratedValue;

  public static LocalizedPdcAxisValue decorate(final PdcAxisValue value, String forLanguage) {
    return new LocalizedPdcAxisValue(value, forLanguage);
  }

  @Override
  public String getLocalizedPath() {
    String path = getMeaningTranslatedIn(getLanguage());
    String[] pathNodes = path.split("/");
    if (pathNodes.length > MAX_NUMBER_OF_RENDERED_PATH_NODE) {
      path = buildTruncatedPath(pathNodes);
    }
    if (path.equals(SEPARATOR_PATH)) {
      path = "";
    }
    return path;
  }

  @Override
  public String getLanguage() {
    return this.language;
  }

  @Override
  public String toString() {
    return decoratedValue.toString();
  }

  @Override
  public ClassifyValue toClassifyValue() throws PdcException {
    return decoratedValue.toClassifyValue();
  }

  @Override
  public boolean isBaseValue() {
    return decoratedValue.isBaseValue();
  }

  @Override
  public String getValuePath() {
    return decoratedValue.getValuePath();
  }

  @Override
  public String getTermTranslatedIn(String language) {
    return decoratedValue.getTermTranslatedIn(language);
  }

  @Override
  public String getTerm() {
    return decoratedValue.getTerm();
  }

  @Override
  public PdcAxisValue getParentValue() {
    return decoratedValue.getParentValue();
  }

  @Override
  public String getMeaningTranslatedIn(String language) {
    return decoratedValue.getMeaningTranslatedIn(language);
  }

  @Override
  public String getMeaning() {
    return decoratedValue.getMeaning();
  }

  @Override
  public String getId() {
    return decoratedValue.getId();
  }

  @Override
  public Set<PdcAxisValue> getChildValues() {
    return decoratedValue.getChildValues();
  }

  @Override
  public String getAxisId() {
    return decoratedValue.getAxisId();
  }

  private LocalizedPdcAxisValue(PdcAxisValue value, String forLanguage) {
    this.decoratedValue = value;
    this.language = forLanguage;
  }

  private String buildTruncatedPath(String[] splitedPath) {
    int nodeCount = splitedPath.length;
    return buildPathBetween(splitedPath, 0, NUMBER_OF_RENDERED_PATH_NODE_IN_TRUNCATION)
        + SEPARATOR_PATH + TRUNCATION_SEPARATOR + SEPARATOR_PATH
        + buildPathBetween(splitedPath, nodeCount - NUMBER_OF_RENDERED_PATH_NODE_IN_TRUNCATION,
        nodeCount);
  }

  private String buildPathBetween(String[] splitedPath, int startIndex, int endIndex) {
    if (startIndex < 0 || startIndex >= splitedPath.length || endIndex < startIndex) {
      throw new IndexOutOfBoundsException("The indexes are out of bounds (startIndex=" + startIndex
          + ", endIndex=" + endIndex + ")");
    }
    StringBuilder path = new StringBuilder();
    for (int i = startIndex; i < endIndex; i++) {
      path.append(splitedPath[i]).append(SEPARATOR_PATH);
    }
    return path.substring(0, path.length() - 2);
  }
}
