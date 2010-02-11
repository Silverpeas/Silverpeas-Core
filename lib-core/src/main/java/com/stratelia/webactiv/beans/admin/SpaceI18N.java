/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.webactiv.organization.SpaceI18NRow;

public class SpaceI18N extends Translation {

  private static final long serialVersionUID = 7054435736300537280L;
  private String name = null;
  private String description = null;

  public SpaceI18N() {
  }

  public SpaceI18N(String lang, String name, String description) {
    if (lang != null)
      super.setLanguage(lang);
    this.name = name;
    this.description = description;
  }

  public SpaceI18N(SpaceI18NRow row) {
    super.setId(row.id);
    super.setLanguage(row.lang);
    name = row.name;
    description = row.description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSpaceId() {
    return super.getObjectId();
  }

  public void setSpaceId(String id) {
    super.setObjectId(id);
  }

}