/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

import org.silverpeas.core.workflow.api.WorkflowException;

/**
 * Interface describing an object holding a collection of <code>ContextualDesignation</code>
 * objects. Does not correspond to any Process Model schema elements.
 */
public interface ContextualDesignations {

  /**
   * Iterate through the ContextualDesignation objects
   * @return an iterator
   */
  public Iterator<ContextualDesignation> iterateContextualDesignation();

  /**
   * Create a ContextualDesignation
   * @return an object implementing ContextualDesignation
   */
  public ContextualDesignation createContextualDesignation();

  /**
   * Add a contextualDesignation to the collection
   * @param contextualDesignation to be added
   */
  public void addContextualDesignation(ContextualDesignation contextualDesignation);

  /**
   * Remove a matching contextualDesignation from the collection. The collection shall be searched
   * for a Designation with the same language and role.
   * @param contextualDesignation a model of the contextualDesignation to be removed.
   * @throws WorkflowException when a matching contextualDescription could not be found.
   */
  public void removeContextualDesignation(ContextualDesignation contextualDesignation) throws
      WorkflowException;

  /**
   * Get the designation for the given role and language; make an exact match, do not fall-back to
   * the default values.
   * @param role the name of the role
   * @param language the code of the language
   * @return an object implementing ContextualDesignation or <code>null</code>
   */
  public ContextualDesignation getSpecificLabel(String role, String language);

  /**
   * Get the designation for the given role and language; make the best match if the required
   * language and/ or role are not supported fall-back to the default values to the default values.
   * @param role the name of the role
   * @param language the code of the language
   * @return the label or an empty string if nothing found.
   */
  public String getLabel(String role, String language);
}
