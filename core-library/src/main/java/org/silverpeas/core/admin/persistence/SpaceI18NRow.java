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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.space.SpaceI18N;

public class SpaceI18NRow {
  private int id = -1;
  private int spaceId = -1;
  private String lang = null;
  private String name = null;
  private String description = null;

  public SpaceI18NRow() {
  }

  public SpaceI18NRow(SpaceI18N spaceI18N) {
    id = Integer.parseInt(spaceI18N.getId());
    spaceId = Integer.parseInt(spaceI18N.getSpaceId());
    lang = spaceI18N.getLanguage();
    name = spaceI18N.getName();
    description = spaceI18N.getDescription();
  }

  public SpaceI18NRow(SpaceRow space) {
    spaceId = space.id;
    lang = space.lang;
    name = space.name;
    description = space.description;
  }

  public SpaceI18NRow(int spaceId, String lang, String name, String description) {
    this.spaceId = spaceId;
    this.lang = lang;
    this.name = name;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public int getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(final int spaceId) {
    this.spaceId = spaceId;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(final String lang) {
    this.lang = lang;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }
}
