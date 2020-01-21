/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.cmis.model;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.silverpeas.core.admin.service.OrganizationController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An application in Silverpeas. An application is always a child of a given Silverpeas space and
 * with the aim of managing some kinds of contributions of the users.
 * @author mmoquillon
 */
public class Application extends CmisFolder {

  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_APPLICATION;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Collections.emptyList();
  }

  Application(final String id, final String name, final String language) {
    super(id, name, language);
  }

  @Override
  public String getPath() {
    final OrganizationController controller = OrganizationController.get();
    return CmisFolder.PATH_SEPARATOR + controller.getPathToComponent(getId())
        .stream()
        .map(s -> s.getName(getLanguage()))
        .collect(Collectors.joining(PATH_SEPARATOR)) + PATH_SEPARATOR + getName();
  }

  @Override
  public BaseTypeId getBaseCmisType() {
    return CMIS_TYPE.getBaseTypeId();
  }

  @Override
  public TypeId getCmisType() {
    return CMIS_TYPE;
  }

  public boolean isRoot() {
    return false;
  }

  @Override
  public List<TypeId> getAllowedChildrenTypes() {
    return getAllAllowedChildrenTypes();
  }

}
  