/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import java.util.Set;

/**
 * A CMIS File is a CMIS object that is file-able in the CMIS objects tree. This is an abstract
 * class that provides a default implementation of the {@link Fileable} interface.
 * @author mmoquillon
 */
public abstract class CmisFile extends CmisObject implements Fileable {

  private String parentId;

  /**
   * Constructs a new CMIS file with the specified identifier, name and language in which the file
   * is written.
   * @param id a {@link ResourceIdentifier} instance identifying a file-able resource in Silverpeas.
   * @param name the name of the resource in Silverpeas.
   * @param language the language in which the resource is written.
   */
  CmisFile(final ResourceIdentifier id, final String name, final String language) {
    super(id, name, language);
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public String getPath() {
    CmisFilePath myPath = CmisFilePathProvider.get().getPath(this);
    return myPath.toString();
  }

  /**
   * Sets a unique identifier of the parent to this file is any. If null, then this file is
   * orphaned, that is to say it is not filed in the CMIS objects tree. A file has always as parent
   * a CMIS folder.
   * @param parentId the unique identifier of a folder.
   * @return itself.
   */
  public CmisFile setParentId(final String parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Is this file has folding capabilities? A file has such capabilities when it can wrap others
   * file-able objects.
   * @return by default false.
   */
  public boolean isFolding() {
    return false;
  }

  @Override
  public final boolean isFileable() {
    return true;
  }

  protected Set<Action> completeWithFileActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_OBJECT_PARENTS);
    return actions;
  }
}
  