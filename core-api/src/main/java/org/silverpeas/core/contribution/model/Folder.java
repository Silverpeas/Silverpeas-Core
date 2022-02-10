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

package org.silverpeas.core.contribution.model;

/**
 * A folder is a contribution with folding capabilities that can contain others contributions,
 * folders included. The folder is dedicated to be used to categorize others
 * contributions, with each sub-folders being a refinement of the categorization.
 * <p>
 * The links between each folders in a given application shape a tree of folders in which the leaves
 * are the non-folder contributions. Such a tree must be rooted to a single folder, a virtual one,
 * representing in fact the application itself as a container of its contributions.
 * </p>
 * @author mmoquillon
 */
public interface Folder extends Contribution {

  /**
   * Is this folder the root one? A root folder represents the application itself as the container
   * of its contributions.
   * @return true if this folder is a root one. False otherwise.
   */
  boolean isRoot();

  /**
   * Is this folder a child of another folder? If no, then the folder is either an orphan one or
   * a root.
   * @return true if this folder is a child of another one. False otherwise.
   */
  boolean isChild();

  /**
   * Is this folder a dedicated one for unclassified, uncategorized contributions?
   * @return true if this folder is for gathers all the uncategorized contributions. False
   * otherwise.
   */
  boolean isUnclassified();

  /**
   * Is this folder a dedicated one for removed contributions? In some applications supporting the
   * categorization of contributions with folders, the deletion of contributions is just a moving of
   * them into a special folder acting then like a bin of removed contributions.
   * @return true if this folder is a bin of removed contributions. False otherwise.
   */
  boolean isBin();
}
