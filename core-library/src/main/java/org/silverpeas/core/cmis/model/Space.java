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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A collaborative space in Silverpeas. A collaborative space provides a way to organize the
 * applications and others spaces in Silverpeas as an hierarchical tree, starting with the root
 * space that is predefined in Silverpeas. The tree of spaces is often a representation of the inner
 * or outer structural organization of an enterprise, of an association or of any organisation.
 * A Space can be made up of others spaces as well as of applications.
 * @author mmoquillon
 */
public class Space  extends CmisFolder {

  public static final String ROOT_ID = "WA0";
  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_SPACE;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Arrays.asList(TypeId.SILVERPEAS_SPACE, TypeId.SILVERPEAS_APPLICATION);
  }

  public static boolean isSpace(final String folderId) {
    return folderId.startsWith("WA");
  }

  Space(final String id, final String name, final String language) {
    super(id, name, language);
  }

  @Override
  public String getPath() {
    final String path;
    if (getId().equals(ROOT_ID)) {
      path = PATH_SEPARATOR;
    } else {
      final OrganizationController controller = OrganizationController.get();
      path = PATH_SEPARATOR + controller.getPathToSpace(getId())
          .stream()
          .map(s -> s.getName(getLanguage()))
          .collect(Collectors.joining(PATH_SEPARATOR));
    }
    return path;
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
    return ROOT_ID.equals(getId());
  }

  @Override
  public List<TypeId> getAllowedChildrenTypes() {
    return isRoot() ? Collections.singletonList(TypeId.SILVERPEAS_SPACE) :
        getAllAllowedChildrenTypes();
  }

}
  