/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.jstl.constant.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Extension of
 * <code>java.util.HashMap</code> that enforces a "strict" get() method in that it will throw an
 * exception if the specified key has no corresponding Map entry.
 *
 */
public class ClassConstantsMap extends HashMap<String, Object> {

  private static final long serialVersionUID = 1L;
  private String className;

  public ClassConstantsMap(String className) throws ClassNotFoundException, IllegalArgumentException,
      IllegalAccessException {
    super();
    this.className = className;
    init();
  }

  protected final void init() throws ClassNotFoundException, IllegalArgumentException,
      IllegalAccessException {
    Class declaringClass = Class.forName(this.className);
    Field[] fields = declaringClass.getFields();
    for (Field field : fields) {
      if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
          && Modifier.isFinal(field.getModifiers())) {
        put(field.getName(), field.get(null));
      }
    }
  }

  @Override
  public Object get(Object key) {
    if (super.get(key) == null) {
      throw new IllegalArgumentException("Key " + key
          + " could not be found in class constant map for " + this.className);
    }
    return super.get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object put(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("null keys are not permitted");
    }
    if (value == null) {
      throw new IllegalArgumentException("null values are not permitted");
    }
    return super.put(key, value);
  }
}
