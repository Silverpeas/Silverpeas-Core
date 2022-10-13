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

package org.silverpeas.core.test.extention;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.silverpeas.core.SilverpeasRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Provides mocking facilities on the fields of beans used in unit tests.
 * @author mmoquillon
 */
public class FieldMocker implements AfterEachCallback {

  private Map<Object, Map<FieldInjectionDirective<?>, Object>> entitiesOldValues = new HashMap<>();

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    unsetMockedFields();
  }

  protected void unsetMockedFields() throws IllegalAccessException {
    for (Map.Entry<Object, Map<FieldInjectionDirective<?>, Object>> objectOldValue :
        entitiesOldValues.entrySet()) {
      for (Map.Entry<FieldInjectionDirective<?>, Object> oldValue : objectOldValue.getValue()
          .entrySet()) {
        FieldInjectionDirective<?> fieldDirective = oldValue.getKey();
        fieldDirective.write(oldValue.getValue());
      }
    }
  }

  /**
   * Mocks a field specified by the given field name of the given instance with a new mock instance
   * of the specified class.
   * @param <T> the type of the mocked instance.
   * @param instanceOrClass the instance or class into which the field will be mocked.
   * @param classToMock the class to get a new mock instance.
   * @param fieldNames the aimed field name. If several, then it represents a path to access to
   * the field. If the fieldName path part starts with '.' character, it sets that the
   * field is static.
   * @return the new mocked instance.
   */
  public <T> T mockField(Object instanceOrClass, Class<T> classToMock, String fieldNames) {
    return setField(instanceOrClass, Mockito.mock(classToMock), fieldNames);
  }

  /**
   * Spies a field specified by the given field name of the given instance with a new mock instance
   * of the specified class.
   * @param <T> the type of the mocked instance.
   * @param instanceOrClass the instance or class into which the field will be mocked.
   * @param classToMock the class to get a new mock instance.
   * @param fieldNames the aimed field name. If several, then it represents a path to access to
   * the field. If the fieldName path part starts with '.' character, it sets that the
   * field is static.
   * @return the new mocked instance.
   */
  public <T> T spyField(Object instanceOrClass, Class<T> classToMock, String fieldNames) {
    return setField(instanceOrClass, Mockito.spy(classToMock), fieldNames);
  }

  /**
   * Spies a field specified by the given field name of the given instance with a new mock instance
   * of the specified class.
   * @param <T> the type of the mocked instance.
   * @param instanceOrClass the instance or class into which the field will be mocked.
   * @param value the value to spy.
   * @param fieldNames the aimed field name. If several, then it represents a path to access to
   * the field. If the fieldName path part starts with '.' character, it sets that the
   * field is static.
   * @return the new mocked instance.
   */
  public <T> T spyField(Object instanceOrClass, T value, String fieldNames) {
    return setField(instanceOrClass, Mockito.spy(value), fieldNames);
  }

  /**
   * Sets a field specified by the given field name of the given instance with a given value
   * of the specified class.
   * @param <T> the type of the mocked instance.
   * @param instanceOrClass the instance or class into which the field will be mocked.
   * @param value the value to set.
   * @param fieldNames the aimed field name. If several, then it represents a path to access to
   * the field. If the fieldName path part starts with '.' character, it sets that the
   * field is static.
   * @return the new mocked instance.
   */
  public <T> T setField(Object instanceOrClass, T value, String fieldNames) {
    try {
      return inject(instanceOrClass, new FieldInjectionDirective<>(instanceOrClass, fieldNames),
          value);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Performs the new value injection and registers in a cache the previous value.
   * @return the injected value.
   */
  private <T> T inject(Object instanceOrClass, FieldInjectionDirective<T> fieldInjectionDirective,
      T newValue) throws IllegalAccessException {
    Map<FieldInjectionDirective<?>, Object> entityOldValues =
        entitiesOldValues.computeIfAbsent(instanceOrClass, k -> new HashMap<>());
    entityOldValues.put(fieldInjectionDirective, fieldInjectionDirective.write(newValue));
    return newValue;
  }

  /**
   * Handle the injection directive associated to a field.
   * @param <T>
   */
  private static class FieldInjectionDirective<T> {
    private final Object instanceOrClass;
    private final String fieldNames;

    private FieldInjectionDirective(final Object instanceOrClass, final String fieldNames) {
      this.instanceOrClass = instanceOrClass;
      this.fieldNames = fieldNames;
    }

    /**
     * Gets the final Object / Field to set by analyzing the path.
     * @return the final Object / Field to set by analyzing the path.
     * @throws Exception
     */
    private ObjectField getFinalObjectField() throws IllegalAccessException {
      Object currentInstanceOrClass = instanceOrClass;
      StringTokenizer fieldNameTokenizer = new StringTokenizer(fieldNames, ".");
      while (fieldNameTokenizer.hasMoreTokens()) {
        String fieldName = fieldNameTokenizer.nextToken();
        if (!fieldNameTokenizer.hasMoreTokens()) {
          return ObjectField.of(currentInstanceOrClass, fieldName);
        }
        if (currentInstanceOrClass instanceof Class) {
          currentInstanceOrClass =
              FieldUtils.readStaticField((Class) instanceOrClass, fieldName, true);
        } else {
          currentInstanceOrClass = FieldUtils.readField(instanceOrClass, fieldName, true);
        }
      }
      return null;
    }

    /**
     * Reads from the field the value.
     * @return the value of the field.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public T read() throws IllegalAccessException {
      ObjectField finalObjectField = getFinalObjectField();
      Objects.requireNonNull(finalObjectField);
      if (finalObjectField.getInstanceOrClass() instanceof Class) {
        return (T) FieldUtils.readStaticField((Class) finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), true);
      } else {
        return (T) FieldUtils.readField(finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), true);
      }
    }

    /**
     * Writes into the field the given value.
     * @param object the given value to write.
     * @return the previous value.
     * @throws IllegalAccessException
     */
    public T write(Object object) throws IllegalAccessException {
      T previousValue = read();
      ObjectField finalObjectField = getFinalObjectField();
      Objects.requireNonNull(finalObjectField);
      if (finalObjectField.getInstanceOrClass() instanceof Class) {
        FieldUtils.writeStaticField((Class) finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), object, true);
      } else {
        FieldUtils.writeField(finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), object, true);
      }
      return previousValue;
    }
  }

  private static class ObjectField {
    private final Object instanceOrClass;
    private final String fieldName;

    public static ObjectField of(final Object instanceOrClass, final String fieldName) {
      return new ObjectField(instanceOrClass, fieldName);
    }

    private ObjectField(final Object instanceOrClass, final String fieldName) {
      this.instanceOrClass = instanceOrClass;
      this.fieldName = fieldName;
    }

    public String getFieldName() {
      return fieldName;
    }

    public Object getInstanceOrClass() {
      return instanceOrClass;
    }
  }
}
  