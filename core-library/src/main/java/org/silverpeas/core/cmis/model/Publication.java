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
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodePath;

import java.util.Collections;
import java.util.List;

/**
 * A publication in an application in Silverpeas. A publication is a contribution of a user that
 * gathers one or more contents on a similar topic. Such content can be a WYSIWYG text or a form,
 * and one or more attached documents. A publication, in our CMIS implementation, is always
 * contained into a contribution folder that can be, for some applications that don't organize their
 * publications in folder, a virtual one for convenience of this constrain (in that case, the
 * virtual folder is the root one of the application).
 * @author mmoquillon
 */
public class Publication extends CmisFolder {

  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_PUBLICATION;

  private final ContributionIdentifier id;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Collections.singletonList(TypeId.SILVERPEAS_DOCUMENT);
  }

  /**
   * Constructs a new publication with the specified identifier, name and language.
   * @param id the {@link ContributionIdentifier} instance identifying the publication in
   * Silverpeas.
   * @param name the name of the publication.
   * @param language the language in which is written the publication.
   */
  Publication(final ContributionIdentifier id, final String name, final String language) {
    super(id, name, language);
    this.id = id;
  }

  public String getApplicationId() {
    return id.getComponentInstanceId();
  }

  @Override
  public String getPath() {
    // the parent of a publication must be always a folder in our implementation of the CMIS objects
    // tree
    ContributionIdentifier folderId = ContributionIdentifier.decode(getParentId());
    String folderPath =
        NodePath.getPath(folderId).format(getLanguage(), true, PATH_SEPARATOR);
    return PATH_SEPARATOR + folderPath + PATH_SEPARATOR + getName();
  }

  @Override
  public boolean isRoot() {
    return false;
  }

  @Override
  public List<TypeId> getAllowedChildrenTypes() {
    return getAllAllowedChildrenTypes();
  }

  @Override
  public BaseTypeId getBaseTypeId() {
    return BaseTypeId.CMIS_FOLDER;
  }

  @Override
  public TypeId getTypeId() {
    return CMIS_TYPE;
  }
}
  