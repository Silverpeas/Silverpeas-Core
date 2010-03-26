/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Author : Norbert CHAIX
// Date : 2002/02/18

package com.stratelia.silverpeas.classifyEngine;

class ClassifyManager {
  public static void main(String[] args) {
    /*
     * try {
     */
    /*
     * ClassifyEngine ce = new ClassifyEngine(); ArrayList alValues = new ArrayList(); // Register
     * /unregister Axis for(int nI=0; nI < 20; nI++) ce.registerAxis(nI); ce.unregisterAxis(9);
     * ce.unregisterAxis(15); ce.registerAxis(21); ce.registerAxis(22); ce.unregisterAxis(3);
     * ce.registerAxis(23);
     */

    // Classify
    /*
     * alValues.add(new Value(0, "/0")); alValues.add(new Value(1, "/1")); alValues.add(new Value(2,
     * "/2")); Position p = new Position(alValues); ce.classifySilverObject(null, 0, p);
     */

    // Update
    /*
     * alValues = new ArrayList(); alValues.add(new Value(0, "/0/1/")); alValues.add(new Value(1,
     * "/0/12/")); p.setValues(alValues); p.setPositionId(10); ce.updateSilverObjectPosition(null,
     * p);
     */

    // Unclassify
    /*
     * alValues = new ArrayList(); alValues.add(new Value(0, "/0/1/")); alValues.add(new Value(1,
     * "/0/1/")); Position p = new Position(alValues); ce.unclassifySilverObjectByPosition(null, 5,
     * p);
     */

    // UnclassifyBis
    // ce.unclassifySilverObjectByPositionId(null, 12);

    // Search
    /*
     * ArrayList alCriterias = new ArrayList(); alCriterias.add(new Criteria(0, "/0/1/2/"));
     * alCriterias.add(new Criteria(1, "/0/1/")); List alSilverObject =
     * ce.findSilverOjectByCriterias(alCriterias); for(int nI=0; nI < alSilverObject.size(); nI++)
     * System.out.println("SilverObject: " + ((Integer) alSilverObject.get(nI)).intValue());
     */

    // Search bis
    /*
     * List alPositions = ce.findPositionsBySilverOjectId(1); for(int nI=0; nI < alPositions.size();
     * nI++) System.out.println("Position PositionId: " +
     * ((Position)alPositions.get(nI)).getPositionId());
     */

    // Remove all axis Values
    // ce.removeAllPositionValuesOnAxis(0);

    // Replace values
    /*
     * Value oldValue = new Value(0, "/146/"); Value newValue = new Value(0, null);
     * ce.replaceValuesOnAxis(oldValue, newValue);
     */
    /*
     * } catch (Exception e) { System.out.println(e); }
     */
  }

  public ClassifyManager() {
  }
}
