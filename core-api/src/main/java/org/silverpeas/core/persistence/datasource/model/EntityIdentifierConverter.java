/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.persistence.datasource.model;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author mmoquillon
 */
public class EntityIdentifierConverter {

  private Class<? extends EntityIdentifier> entityIdentifierClass;

  /**
   * Constructs a converter of identities with the specified class.
   * @param entityIdentifierClass the class of the identities to convert.
   */
  public EntityIdentifierConverter(Class<? extends EntityIdentifier> entityIdentifierClass) {
    this.entityIdentifierClass = entityIdentifierClass;
  }

  /**
   * Converts the given String id into the right entity identifier.
   * @param <T> the concrete type of the entity identifier.
   * @param idAsString the String representation of the identifier.
   * @return the entity identifier from its String representation.
   */
  @SuppressWarnings("unchecked")
  public <T extends EntityIdentifier> T convertToEntityIdentifier(String idAsString) {
    try {
      T identifier = (T) getEntityIdentifierClass().newInstance();
      identifier.fromString(idAsString);
      return identifier;
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Converts the given String ids into the right entity identifiers.
   * @param <T> the concrete type of the entity identifier.
   * @param idsAsString one or more identifier as String value(s).
   * @return a collection of entity identifiers from their String representations.
   */
  public <T extends EntityIdentifier> Collection<T> convertToEntityIdentifiers(
      String... idsAsString) {
    return convertToEntityIdentifiers(
        idsAsString == null ? null : CollectionUtil.asList(idsAsString));
  }

  /**
   * Converts the given String ids into the right entity identifiers.
   * @param <T> the concrete type of the entity identifier.
   * @param idsAsString a collection of identifiers as String values.
   * @return a collection of the entity identifiers from their String representations.
   */
  public <T extends EntityIdentifier> Collection<T> convertToEntityIdentifiers(
      Collection<String> idsAsString) {
    int size = (idsAsString == null) ? 0 : idsAsString.size();
    Collection<T> identifiers = new ArrayList<>(size);
    if (size > 0) {
      for (String id : idsAsString) {
        identifiers.add(convertToEntityIdentifier(id));
      }
    }
    return identifiers;
  }

  /**
   * Gets the identifier class of the entity managed by the repository.
   * @param <T> the concrete type of the entity identifier.
   * @return the class of the entity identifier.
   */
  @SuppressWarnings("unchecked")
  private <T extends EntityIdentifier> Class<T> getEntityIdentifierClass() {
    return (Class<T>) entityIdentifierClass;
  }
}
