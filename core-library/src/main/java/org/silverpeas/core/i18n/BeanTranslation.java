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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.i18n;

import org.silverpeas.core.SilverpeasRuntimeException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BeanTranslation implements ResourceTranslation, Serializable {

  private static final long serialVersionUID = -3879515108587719162L;
  private String id = "-1";
  private String objectId = null;
  private String language = I18NHelper.DEFAULT_LANGUAGE;
  private String name = "";
  private String description = "";

  /**
   * Constructs an empty translation.
   */
  protected BeanTranslation() {
    // Nothing is done
  }

  /**
   * Constructs a new translation of a bean by copying the specified one. Only the identifier isn't
   * copied as it should be unique.
   * @param translation the bean translation to copy.
   */
  protected BeanTranslation(final BeanTranslation translation) {
    id = translation.id;
    objectId = translation.objectId;
    language = translation.language;
    name = translation.name;
    description = translation.description;
  }

  protected BeanTranslation(String lang, String name, String description) {
    if (lang != null) {
      setLanguage(lang);
    }
    setName(name);
    setDescription(description);
  }

  protected BeanTranslation(String id, String lang, String name, String description) {
    this(lang, name, description);
    setId(id);
  }

  public final String getId() {
    return id;
  }

  public final void setId(String id) {
    this.id = id;
  }

  public final String getLanguage() {
    return language;
  }

  public final void setLanguage(String language) {
    this.language = language;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public final String getName() {
    return this.name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public final String getDescription() {
    return this.description;
  }

  public final void setDescription(String description) {
    this.description = description;
  }

  /**
   * Copies this bean into another one. This method expects the classes extending the
   * {@link BeanTranslation} class having a public or a protected default constructor, otherwise a
   * {@link SilverpeasRuntimeException} exception is thrown. The difference between this copy method
   * with the copy constructor is that it can be used against any child of the
   * {@link BeanTranslation} class without explicitly having a copy constructor or overriding this
   * method.
   * @return a copy of this bean.
   */
  @SuppressWarnings("unchecked")
  protected <T extends BeanTranslation> T copy() {
    try {
      Constructor<? extends BeanTranslation> constructor = getClass().getDeclaredConstructor();
      if (!constructor.canAccess(null)) {
        constructor.trySetAccessible();
      }
      BeanTranslation copy = constructor.newInstance();
      copy.id = id;
      copy.objectId = objectId;
      copy.language = language;
      copy.name = name;
      copy.description = description;
      return (T) copy;
    } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
        IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

}
