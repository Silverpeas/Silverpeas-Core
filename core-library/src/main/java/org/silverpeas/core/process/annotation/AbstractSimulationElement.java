/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.process.annotation;

/**
 * User: Yohann Chastagnier
 * Date: 28/10/13
 */
public abstract class AbstractSimulationElement<E> implements SimulationElement<E> {

  private boolean old = false;
  private final E element;

  /**
   * Default constructor.
   * @param element the source element.
   */
  public AbstractSimulationElement(final E element) {
    this.element = element;
  }

  /**
   * Indicates the element as an old one
   * @return the current instance of the element of the simulation.
   */
  public AbstractSimulationElement<E> setOld() {
    old = true;
    return this;
  }

  @Override
  public boolean isOld() {
    return old;
  }

  @Override
  public E getElement() {
    return element;
  }
}
