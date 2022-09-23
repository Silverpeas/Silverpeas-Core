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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.test.util;

import org.silverpeas.core.persistence.datasource.model.EntityIdentifier;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.persistence.datasource.model.jpa.AbstractJpaEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A convenient object to generate a unique identifier for an entity and to set it. It is dedicated
 * to unit tests.
 * @author mmoquillon
 */
public class EntityIdSetter {

  private final EntityIdentifier generator;

  /**
   * Constructs a new setter of entity identifiers whose type is the specified one.
   * @param idType the type of the entity identifier.
   */
  public EntityIdSetter(final Class<? extends EntityIdentifier> idType) {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodHandles.Lookup
          privateLookup = MethodHandles.privateLookupIn(idType, lookup);
      MethodType constructorType = MethodType.methodType(void.class);
      MethodHandle constructor = privateLookup.findConstructor(idType, constructorType);
      this.generator = (EntityIdentifier) constructor.invoke();
    } catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Sets the identifier of the specified entity with a random value of given entity identifier
   * type.
   * @param entity the entity whose identifier has to be set.
   * @param idType the type of the entity identifier.
   * @param <T> the concrete type of the entity.
   * @return the entity itself with its identifier set.
   */
  public static <T extends AbstractJpaEntity<?, ?>> T setIdTo(final T entity,
      final Class<? extends EntityIdentifier> idType) {
    EntityIdSetter setter = new EntityIdSetter(idType);
    return setter.setIdTo(entity);
  }

  /**
   * Sets the identifier of the specified entity with a random value of the underlying entity
   * identifier type.
   * @param entity the entity whose identifier has to be set.
   * @param <T> the concrete type of the entity.
   * @return the entity itself with its identifier set.
   */
  public <T extends AbstractJpaEntity<?, ?>> T setIdTo(final T entity) {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodHandles.Lookup
          privateLookup = MethodHandles.privateLookupIn(AbstractJpaEntity.class, lookup);
      MethodType methodType = MethodType.methodType(IdentifiableEntity.class, String.class);
      MethodHandle setter =
          privateLookup.findVirtual(AbstractJpaEntity.class, "setId", methodType);
      setter.invoke(entity, this.generator.generateNewId().asString());
    } catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
    return entity;
  }

  /**
   * Sets the identifier of the specified entity with the specified value which has to be of the
   * underlying entity identifier type. The serialized value passed as argument isn't of the
   * expected identifier type, then an exception is thrown.
   * @param entity the entity whose identifier has to be set.
   * @param id the value of the identifier as a {@link String}
   * @param <T> the concrete type of the entity.
   * @return the entity itself with its identifier set.
   */
  public <T extends AbstractJpaEntity<?, ?>> T setIdTo(final T entity, final String id) {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodHandles.Lookup
          privateLookup = MethodHandles.privateLookupIn(AbstractJpaEntity.class, lookup);
      MethodType methodType = MethodType.methodType(IdentifiableEntity.class, String.class);
      MethodHandle setter =
          privateLookup.findVirtual(AbstractJpaEntity.class, "setId", methodType);
      setter.invoke(entity, this.generator.fromString(id).asString());
    } catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
    return entity;
  }
}
  