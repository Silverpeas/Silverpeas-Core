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

import java.io.Serializable;

public class Translation implements Serializable, Cloneable {

  private static final long serialVersionUID = -3879515108587719162L;
  private int id = -1;
  private String objectId = null;
  private String language = I18NHelper.defaultLanguage;
  private String name = "";
  private String description = "";

  protected Translation() {
    // Nothing is done
  }

  protected Translation(String lang, String name, String description) {
    if (lang != null) {
      setLanguage(lang);
    }
    setName(name);
    setDescription(description);
  }

  protected Translation(int id, String lang, String name, String description) {
    this(lang, name, description);
    setId(id);
  }

  public Translation(Translation otherTranslation) {
    setId(otherTranslation.getId());
    setObjectId(otherTranslation.getObjectId());
    setLanguage(otherTranslation.getLanguage());
    setName(otherTranslation.getName());
    setDescription(otherTranslation.getDescription());
  }

  public final int getId() {
    return id;
  }

  public final void setId(int id) {
    this.id = id;
  }

  public final String getLanguage() {
    return language;
  }

  public final void setLanguage(String language) {
    this.language = language;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public final String getName() {
    return this.name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public final String getDescription() {
    return this.description;
  }

  public final void setDescription(String description) {
    this.description = description;
  }

  @Override
  protected Translation clone() {
    try {
      return (Translation) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
