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

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.silverpeas.core.ResourceIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The abstract representation of a Silverpeas resource as a CMIS folder. In CMIS, a Folder is a
 * file-able CMIS object that can contain others file-ables CMIS objects. In Silverpeas, a space,
 * a component instance (aka application), a topic in an EDM, a gallery in a media library, ... are
 * all a container of other objects and hence they can be represented by a CMIS folder.
 * @author mmoquillon
 */
public abstract class CmisFolder extends CmisFile implements Folding {

  /**
   * Gets the types of the objects this type of folder accept as children. This method returns
   * an empty list by default and requires to be overridden by the children classes in the case
   * the {@link CmisFolder} is a CMIS Folder. Otherwise, nothing is returned.
   * @return a list with all the {@link TypeId} that can be a children of such a folder type. If it
   * isn't a folder, then returns an empty list. By default, this implementation returns an empty
   * list.
   */
  public static List<TypeId> getAllowedChildrenType() {
    return Collections.emptyList();
  }

  /**
   * Constructs a new CMIS folder with the specified identifier, name and language in which the
   * folder name and description is written.
   * @param id the {@link ResourceIdentifier} instance identifying a resource in Silverpeas that
   * can wrap other resources.
   * @param name the name of the resource.
   * @param language the language in which the name and the description of the folder is written.
   */
  CmisFolder(final ResourceIdentifier id, final String name, final String language) {
    super(id, name, language);
  }

  /**
   * Sets the unique identifier of the parent to this folder if any. If null, then this folder is a
   * root one in the CMIS objects tree. Otherwise, the identifier must be the one of another CMIS
   * folder; a folder cannot be orphaned.
   * @param parentId the unique identifier of the parent folder.
   * @return either the unique identifier of a folder, parent of it, or null if this folder is a
   * root one in the CMIS objects tree.
   */
  public CmisFolder setParentId(final String parentId) {
    super.setParentId(parentId);
    return this;
  }

  /**
   * A folder cannot be orphaned.
   * @return false.
   */
  @Override
  public boolean isOrphaned() {
    return false;
  }

  @Override
  public boolean isFolding() {
    return true;
  }

  @Override
  protected Supplier<Set<Action>> getAllowableActionsSupplier() {
    Supplier<Set<Action>> supplier;
    if (isRoot()) {
      supplier = () -> completeWithCommonFolderActions(theCommonActions());
    } else {
      supplier = () -> completeWithFolderActions(theCommonActions());
    }
    return supplier;
  }

  private Set<Action> completeWithCommonFolderActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_DESCENDANTS);
    actions.add(Action.CAN_GET_CHILDREN);
    actions.add(Action.CAN_GET_FOLDER_TREE);

    return actions;
  }

  private Set<Action> completeWithFolderActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_FOLDER_PARENT);
    return completeWithCommonFolderActions(completeWithFileActions(actions));
  }
}
  