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
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodePath;

import java.util.Collections;
import java.util.List;

/**
 * A publication in Silverpeas. A publication is a contribution of a user that gathers one or more
 * contents on a similar topic. Such content can be a WYSIWYG text or a form, and one or more
 * attached documents.
 * <p>
 * A publication, in our CMIS implementation, is contained either into a contribution folder (for
 * applications categorizing their resources) or directly at the root level of the application. For
 * applications using the {@link ContributionFolder}s to categorize the publications, usually a root
 * virtual folder is used to represent the application itself; in that case, for publications
 * organized into such a folder, their parent is then considered to be the application itself.
 * </p>
 *
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
   *
   * @param id       the {@link ContributionIdentifier} instance identifying the publication in
   *                 Silverpeas.
   * @param name     the name of the publication.
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
    ContributionIdentifier parentId;
    if (ContributionIdentifier.isValid(getParentId())) {
      // the parent is a contribution folder
      parentId = ContributionIdentifier.decode(getParentId());
    } else if (getApplicationId().equals(getParentId())) {
      // the parent is the application itself
      parentId = ContributionFolder.getRootFolderId(getParentId());
    } else {
      throw new CmisConstraintException(String.format(
          "The parent %s of the publication isn't a contribution folder neither an application",
          getParentId()));
    }
    return PATH_SEPARATOR + NodePath.getPath(parentId).format(getLanguage(), true, PATH_SEPARATOR) +
        PATH_SEPARATOR + getName();
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
  