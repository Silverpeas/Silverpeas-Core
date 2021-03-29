/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.space.model.SpacePath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A collaborative space in Silverpeas. A collaborative space provides a way to organize the
 * applications and others spaces in Silverpeas as an hierarchical tree, starting with the root
 * space that is predefined in Silverpeas. The tree of spaces is often a representation of the inner
 * or outer structural organization of an enterprise, of an association or of any organisation.
 * A Space can be made up of others spaces as well as of applications.
 * @author mmoquillon
 */
public class Space  extends CmisFolder {

  public static final BasicIdentifier ROOT_ID = new BasicIdentifier(0,"WA0");
  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_SPACE;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Arrays.asList(TypeId.SILVERPEAS_SPACE, TypeId.SILVERPEAS_APPLICATION);
  }

  public static boolean isSpace(final String folderId) {
    return folderId.startsWith("WA");
  }

  /**
   * Constructs a new space with the specified identifier, name and language.
   * @param id the {@link ResourceIdentifier} instance identifying a collaborative space in
   * Silverpeas.
   * @param name the name of the collaborative space.
   * @param language the language in which are written the properties of the space.
   */
  Space(final ResourceIdentifier id, final String name, final String language) {
    super(id, name, language);
  }

  @Override
  public String getPath() {
    final String path;
    if (ROOT_ID.asString().equals(getId())) {
      path = PATH_SEPARATOR;
    } else {
      path =
          PATH_SEPARATOR + SpacePath.getPath(getId()).format(getLanguage(), true, PATH_SEPARATOR);
    }
    return path;
  }

  @Override
  public BaseTypeId getBaseTypeId() {
    return CMIS_TYPE.getBaseTypeId();
  }

  @Override
  public TypeId getTypeId() {
    return CMIS_TYPE;
  }

  public boolean isRoot() {
    return ROOT_ID.asString().equals(getId());
  }

  @Override
  public List<TypeId> getAllowedChildrenTypes() {
    return isRoot() ? Collections.singletonList(TypeId.SILVERPEAS_SPACE) :
        getAllAllowedChildrenTypes();
  }

}
  