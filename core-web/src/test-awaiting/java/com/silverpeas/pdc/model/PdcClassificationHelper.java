/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.pdc.model;

/**
 * A helper class to access protected methods of the PdcClassification objects for testing purpose.
 */
public class PdcClassificationHelper {

  public static PdcClassification newPdcClassificationWithId(Long id) {
    return new PdcClassification(id);
  }

  public static PdcPosition newPdcPositionWithId(Long id) {
    return new PdcPosition(id);
  }

  public static boolean isPersisted(final PdcClassification classification) {
    return classification.getId() != null;
  }

  public static Long idOf(final PdcClassification classification) {
    return classification.getId();
  }

  public static void setClassificationId(final PdcClassification classification, long id) {
    classification.setId(id);
  }
}
