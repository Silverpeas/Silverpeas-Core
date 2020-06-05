/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.persistence.datasource.model.jpa;

import org.silverpeas.core.admin.user.model.User;

import java.util.Date;

/**
 * This class permits to manipulate hidden attributes of a Silverpeas JPA entity.<br>
 * It is useful in particular case of technical treatments.
 * @author Yohann Chastagnier
 */
public class JpaEntityReflection {

  /**
   * Hidden constructor.
   */
  private JpaEntityReflection() {
  }

  @SuppressWarnings("unchecked")
  public static <T extends SilverpeasJpaEntity> T setUpdateData(final T entity, User updater,
      Date updateDate) {
    return (T) entity.setLastUpdater(updater).setLastUpdateDate(updateDate);
  }

  @SuppressWarnings("unchecked")
  public static <T extends SilverpeasJpaEntity> T setCreationData(final T entity, User creator,
      Date creationDate) {
    return (T) entity.setCreator(creator).setCreationDate(creationDate);
  }
}
