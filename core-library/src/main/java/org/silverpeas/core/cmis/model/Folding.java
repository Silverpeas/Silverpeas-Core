
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.cmis.model;

import java.util.List;

/**
 * A file-able CMIS object having the capability to organize others file-able CMIS objects into a
 * subtree of the main CMIS trees object. A folding object can then contains others file-able CMIS
 * object as children.
 * @author mmoquillon
 */
public interface Folding extends Fileable {

  /**
   * Gets the unique identifier of the folder parent of this one
   * @return the unique identifier of the parent of this folding instance or null if it is a root
   * folder.
   */
  @Override
  String getParentId();

  /**
   * Is this object is the root folder in the CMIS objects tree?
   * @return true if this folder is the root of the CMIS objects tree. False otherwise.
   */
  boolean isRoot();

  /**
   * Gets all the types this object accepts as children.
   * @return a list of {@link TypeId} instances, each of them identifying the type of CMIS objects
   * this folding object accepts as children.
   */
  List<TypeId> getAllowedChildrenTypes();
}
