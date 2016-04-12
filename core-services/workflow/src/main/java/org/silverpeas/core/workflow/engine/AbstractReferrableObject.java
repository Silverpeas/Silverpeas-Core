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

/**
 * @author ludovic Bertin
 * @version 1.0
 */

/**
 * The abstract class for all workflow objects refferable
 */

package org.silverpeas.core.workflow.engine;

public abstract class AbstractReferrableObject implements ReferrableObjectIntf {

  /**
   * This method has to be implemented by the referrable object it has to compute the unique key
   * @return The unique key.
   * @see equals
   * @see hashCode
   */
  @Override
  public abstract String getKey();

  /**
   * Tests equality with another referrable object
   * @param theOther
   * @return true if both object's keys are equals
   */
  @Override
  public boolean equals(Object theOther) {
    if (theOther instanceof String) {
      return (getKey().equals(theOther));
    } else {
      return (getKey().equals(((ReferrableObjectIntf) theOther).getKey()));
    }
  }

  /**
   * Calculate the hashcode for this referrable object
   * @return hashcode
   */
  @Override
  public int hashCode() {
    return (getKey().hashCode());
  }
}