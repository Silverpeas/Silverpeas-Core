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

package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.admin.component.model.ComponentI18N;

public class ComponentInstanceI18NRow {
  public int id = -1;
  public int componentId = -1;
  public String lang = null;
  public String name = null;
  public String description = null;

  public ComponentInstanceI18NRow() {
  }

  public ComponentInstanceI18NRow(ComponentI18N componentI18N) {
    id = componentI18N.getId();
    componentId = Integer.parseInt(componentI18N.getObjectId());
    lang = componentI18N.getLanguage();
    name = componentI18N.getName();
    description = componentI18N.getDescription();
  }

  public ComponentInstanceI18NRow(ComponentInstanceRow component) {
    componentId = component.id;
    lang = component.lang;
    name = component.name;
    description = component.description;
  }

  public ComponentInstanceI18NRow(int componentId, String lang, String name, String description) {
    this.componentId = componentId;
    this.lang = lang;
    this.name = name;
    this.description = description;
  }
}
