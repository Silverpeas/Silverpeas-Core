/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.WAPrimaryKey;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This interface permits the treatment callers to indicate the complete list of elements
 * behind a primary key. Indeed, common services are not capable to find all elements behind a
 * primary key associated to a complex structure of documents only known by the component caller
 * itself.
 * <p>
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public abstract class SimulationElementLister {

  /**
   * Type of action aimed by the simulation
   */
  private ActionType actionType = null;

  /**
   * Element indexed by
   */
  private Map<Class<SimulationElement>, List<SimulationElement>> elements = null;

  /**
   * Constructor called with Class.newInstance
   */
  public SimulationElementLister() {
    this(null);
  }

  /**
   * Constrictor called from an other element lister
   * @param parentElementLister
   */
  public SimulationElementLister(SimulationElementLister parentElementLister) {
    if (parentElementLister != null) {
      this.actionType = parentElementLister.actionType;
      this.elements = parentElementLister.elements;
    }
  }

  /**
   * Hidden method to pass the type of action aimed by the simulation
   * @param actionType
   */
  void setActionType(final ActionType actionType) {
    this.actionType = actionType;
  }

  /**
   * Gets the type of the action aimed by the simulation
   * @return
   */
  protected ActionType getActionType() {
    return actionType;
  }

  /**
   * Hidden method to pass the element container
   * @param elements
   */
  void setElements(final Map<Class<SimulationElement>, List<SimulationElement>> elements) {
    this.elements = elements;
  }

  /**
   * @param element
   */
  @SuppressWarnings("unchecked")
  protected void addElement(SimulationElement element) {
    if (element != null && element.getElement() != null) {
      MapUtil.putAddList(LinkedList.class, elements, (Class<SimulationElement>) element.getClass(),
          element);
    }
  }

  /**
   * This method contains the treatment that lists all elements which have to be converted into
   * dummy handled file.
   * To register an element, please use
   * {@link SimulationElementLister#addElement(SimulationElement)} ]
   * @param sourcePK the parameter represents a primary key
   */
  public abstract void listElements(final WAPrimaryKey sourcePK, final String language);

  /**
   * This method contains the treatment that lists all elements which have to be converted into
   * dummy handled file.
   * To register an element, please use
   * {@link SimulationElementLister#addElement(SimulationElement)} ]
   * @param source could be anything
   */
  public abstract void listElements(final Object source, final String language,
      final WAPrimaryKey targetPK);
}
