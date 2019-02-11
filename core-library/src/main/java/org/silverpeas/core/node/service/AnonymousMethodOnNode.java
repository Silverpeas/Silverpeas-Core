/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.node.service;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.node.model.NodePK;

/**
 * Representation of an anonymous method to invoke in some circumstances. This is class is for
 * passing a method invocation within another method call (second-order function).
 */
@FunctionalInterface
public interface AnonymousMethodOnNode {

  /**
   * Invokes the method with the specified node.
   * @param pk the primary key of the node.
   * @throws SilverpeasException if an error occurs during the execution of the anonymous method.
   */
  void invoke(final NodePK pk) throws SilverpeasException;
}
