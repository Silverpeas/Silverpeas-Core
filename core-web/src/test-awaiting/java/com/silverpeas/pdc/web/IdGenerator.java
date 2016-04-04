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

package com.silverpeas.pdc.web;

/**
 * A generator of identifiers to use by DAO mocks and by tests.
 */
public class IdGenerator {
  private static IdGenerator instance = new IdGenerator();
  private static long classificationIdCounter = 0L;
  private static long positionIdCounter = 0L;

  public long nextPositionId() {
    return positionIdCounter++;
  }

  public long nextClassificationId() {
    return classificationIdCounter++;
  }

  public String nextPositionIdAsString() {
    return String.valueOf(positionIdCounter++);
  }

  public String nextClassificationIdAsString() {
    return String.valueOf(classificationIdCounter++);
  }

  public long lastUsedPositionId() {
    return positionIdCounter - 1;
  }

  public long lastUsedClassificationId() {
    return classificationIdCounter - 1;
  }

  public String lastUsedPositionIdAsString() {
    return String.valueOf(positionIdCounter - 1);
  }

  public String lastUsedClassificationIdAsString() {
    return String.valueOf(classificationIdCounter - 1);
  }

  public static IdGenerator getGenerator() {
    return instance;
  }

  private IdGenerator() {

  }
}
