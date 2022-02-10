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

package org.silverpeas.core.workflow.engine;


/**
 * The abstract class for all workflow objects referable
 */
public abstract class AbstractReferrableObject implements ReferrableObjectIntf {

  /**
   * Tests equality with another referable object
   * @param theOther the other instance to verify
   * @return true if both object's keys are equals
   */
  @Override
  public boolean equals(Object theOther) {
    if (theOther == null) {
      return false;
    }
    if (theOther instanceof String) {
      return getKey().equals(theOther);
    } else {
      return getKey().equals(((ReferrableObjectIntf) theOther).getKey());
    }
  }

  /**
   * Calculate the hashcode for this referrable object
   * @return hashcode
   */
  @Override
  public int hashCode() {
    return getKey().hashCode();
  }
}