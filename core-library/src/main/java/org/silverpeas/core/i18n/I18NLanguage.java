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

package org.silverpeas.core.i18n;

public class I18NLanguage {

  private int translationId = -1;
  private String code = null;
  private String label = null;

  public I18NLanguage(String code) {
    this.code = code;
  }

  public I18NLanguage(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof I18NLanguage) {
      I18NLanguage other = (I18NLanguage) o;
      return other.getCode().equals(code);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + (this.code != null ? this.code.hashCode() : 0);
    return hash;
  }

  public int getTranslationId() {
    return translationId;
  }

  public void setTranslationId(int translationId) {
    this.translationId = translationId;
  }

}
