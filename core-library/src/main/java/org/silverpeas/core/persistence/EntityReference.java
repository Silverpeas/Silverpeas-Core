/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A reference to an entity in Silverpeas.
 *
 * An entity is a business object in Silverpeas that is persisted in a data source. Some times,
 * instead of referring a peculiar entity, an object can refer an entity whatever its type; this is
 * why it refers such objects with an <code>EntityReference</code> instance.
 *
 * The type of the entity referred by a such reference is defined by the type of the reference
 * itself; An entity reference must be concrete and its type carries the type of the entity it is
 * upon. For example, you can implement a reference to a user by naming it
 * <code>UserReference<UserDetail></code>.
 *
 * @param <T> the type of the entity on which this reference is about.
 * @author mmoquillon
 */
public abstract class EntityReference<T> {

  private static final String[] TechnicalTerms = {"Detail", "Full", "Complete", "Silver", "Content",
    "Silverpeas"};
  private final String id;

  public static final String UNKNOWN_TYPE = "UNKNOWN";

  /**
   * Constructs a reference targeting the entity identified by the specified unique identifier.
   *
   * @param id the unique identifier of the entity to refer.
   */
  public EntityReference(String id) {
    this.id = id;
  }

  /**
   * Gets the unique identifier of the entity referred by this reference.
   *
   * @return the unique identifier of the entity as a String.
   */
  public final String getId() {
    return id;
  }

  /**
   * Gets the name of the entity type referred by this reference. The name is returned in upper
   * case.
   *
   * The name is different from the class name of both of the reference itself and of the referred
   * entity, so any class name change shouldn't impact the type name here. The type name is in fact
   * derived from the class name of the entity but by removing all extra technical terms in order to
   * keep only the meaningful name of the business object. For example, for the entity
   * <code>UserDetail</code>, only the term <code>User</code> is kept and then set in upper case.
   * Not all technical terms are detected; for instance, only the following technical terms are
   * taken into account: Silverpeas, Detail, Interface, Silver, Content, Full, and Complete.
   *
   * @return the meaningful name in upper case of the type of the referred entity.
   */
  public final String getType() {
    String type = "";
    Type genericSuperclass = getClass().getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) genericSuperclass;
      Type paramType = pt.getActualTypeArguments()[0];
      type = paramType.toString();
      type = type.substring(type.lastIndexOf(".") + 1);
    }
    return getType(type);
  }

  /**
   * Extracts automatically the type from a {@link Class}.
   *
   * @return the meaningful name in upper case of the type of the referred entity.
   */
  public static String getType(Class aClass) {
    return getType(aClass.getSimpleName());
  }

  /**
   * Extracts automatically the type from a simple name of {@link Class}.
   *
   * @return the meaningful name in upper case of the type of the referred entity.
   */
  public static String getType(String classSimpleName) {
    for (String term : TechnicalTerms) {
      if (classSimpleName.contains(term)) {
        classSimpleName = classSimpleName.replace(term, "");
      }
    }
    if (classSimpleName.isEmpty()) {
      classSimpleName = UNKNOWN_TYPE;
    }
    return classSimpleName.toUpperCase();
  }

  /**
   * Gets the instance of the entity targeted by this reference.
   *
   * @return the entity identified by this reference.
   */
  public abstract T getEntity();

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 67 * hash + (this.getType() != null ? this.getType().hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object anotherReference) {
    if (anotherReference == null) {
      return false;
    }
    if (getClass() != anotherReference.getClass()) {
      return false;
    }
    return this.hashCode() == anotherReference.hashCode();
  }

}
