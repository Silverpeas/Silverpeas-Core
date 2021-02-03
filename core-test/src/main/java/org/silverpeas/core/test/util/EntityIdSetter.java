/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.test.util;

import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A convenient object to generate a unique identifier for an entity and to set it. It is dedicated
 * to unit tests
 * @author mmoquillon
 */
public class EntityIdSetter {

  private final EntityIdentifier generator;

  public EntityIdSetter(final Class<? extends EntityIdentifier> idType) {
    try {
      Constructor<? extends EntityIdentifier> constructor = idType.getDeclaredConstructor();
      constructor.setAccessible(true);
      this.generator = constructor.newInstance();
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
        IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static <T extends AbstractJpaEntity> T setIdTo(final T entity,
      final Class<? extends EntityIdentifier> idType) {
    EntityIdSetter setter = new EntityIdSetter(idType);
    return setter.setIdTo(entity);
  }

  public <T extends AbstractJpaEntity> T setIdTo(final T entity) {
    try {
      Method setter = AbstractJpaEntity.class.getDeclaredMethod("setId", String.class);
      setter.setAccessible(true);
      setter.invoke(entity, this.generator.generateNewId().asString());
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
    return entity;
  }
}
  