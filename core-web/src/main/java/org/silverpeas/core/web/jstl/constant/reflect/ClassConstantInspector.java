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
import java.util.Map;

/**
 * Inspector class that analyses a class and extract its constant filed value.
 */
public class ClassConstantInspector {

  private Field field;

  /**
   * Constructs a class constant inspector for the passed field. The field is checked to ensure that
   * it is a public static final field.
   *
   * @param constantPath the fully qualified name of the class class constant
   *
   * @throws ClassNotFoundException if the named class does not exist
   * @throws NoSuchFieldException if the named field does not exists
   * @throws IllegalArgumentException if the class or field fails to meet the constrictions
   * described above
   */
  public ClassConstantInspector(String constantPath) throws ClassNotFoundException,
      NoSuchFieldException {
    FieldPathParser parser = new FieldPathParser(constantPath);
    Class currentClass = Class.forName(parser.getDeclaringClassName());
    Map<String, Class> innerClasses = null;
    for (String fieldOrClassName : parser.getFieldOrClassNames()) {
      if (innerClasses == null) {
        innerClasses = new HashMap<String, Class>();
        for (Class innerClass : currentClass.getClasses()) {
          innerClasses.put(innerClass.getSimpleName(), innerClass);
        }
      }
      if (innerClasses.containsKey(fieldOrClassName)) {
        currentClass = innerClasses.get(fieldOrClassName);
        innerClasses = null;
      } else {
        this.field = currentClass.getField(fieldOrClassName);
      }
    }
    if (this.field == null || !Modifier.isPublic(this.field.getModifiers()) ||
        !Modifier.isStatic(this.field.getModifiers()) ||
        !Modifier.isFinal(this.field.getModifiers())) {
      throw new IllegalArgumentException("Field " + constantPath
          + " is not a public static final field");
    }
  }

  /**
   * Returns the value of the inspected class constant.
   *
   * @return the value of the inspected constant
   * @throws IllegalAccessException if the field cannot be accessed
   * @throws InstantiationException if the class cannot be instantiated
   */
  public Object getValue() throws IllegalAccessException, InstantiationException {
    return this.field.get(null);
  }

  /**
   * Static convenience method to obtain the value of a constant. Suitable for use as an EL
   * function.
   *
   * @param constantName the fully qualified name of the class class constant
   *
   * @return the value of the inspected constant
   * @throws NoSuchFieldException if the field does not exist
   * @throws ClassNotFoundException if the class does not exist
   * @throws IllegalAccessException if the field cannot be accessed
   * @throws InstantiationException if the class cannot be instantiated
   */
  public static Object getValue(String constantName) throws NoSuchFieldException,
      ClassNotFoundException, IllegalAccessException, InstantiationException {
    return new ClassConstantInspector(constantName).getValue();
  }
}
