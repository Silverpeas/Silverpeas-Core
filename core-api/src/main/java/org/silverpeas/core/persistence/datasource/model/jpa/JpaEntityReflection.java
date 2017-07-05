/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.util.logging.SilverLogger;

import java.lang.reflect.Field;

/**
 * @author Yohann Chastagnier
 */
public class JpaEntityReflection {

  private static Field createdBySetManuallyField = null;
  private static Field lastUpdatedBySetManuallyField = null;

  public static boolean isCreatedBySetManually(final SilverpeasJpaEntity entity) {
    try {
      if (createdBySetManuallyField == null) {
        createdBySetManuallyField =
            SilverpeasJpaEntity.class.getDeclaredField("createdBySetManually");
        createdBySetManuallyField.setAccessible(true);
      }
      return (boolean) createdBySetManuallyField.get(entity);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      SilverLogger.getLogger(JpaEntityReflection.class).error(e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }

  public static boolean isLastUpdatedBySetManually(final SilverpeasJpaEntity entity) {
    try {
      if (lastUpdatedBySetManuallyField == null) {
        lastUpdatedBySetManuallyField =
            SilverpeasJpaEntity.class.getDeclaredField("lastUpdatedBySetManually");
        lastUpdatedBySetManuallyField.setAccessible(true);
      }
      return (boolean) lastUpdatedBySetManuallyField.get(entity);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      SilverLogger.getLogger(JpaEntityReflection.class).error(e.getMessage(), e);
      throw new IllegalArgumentException(e);
    }
  }
}
