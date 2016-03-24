/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.test.rule;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * This rule handles field injections. <br/>
 * The mechanism is very simple:
 * <ul>
 * <li>saving the previous value</li>
 * <li>setting the given value</li>
 * <li>at the end of the test, the old value is set again</li>
 * </ul>
 * This prevent from getting an unstable context of tests in case of chained execution.
 * @author Yohann Chastagnier
 */
public class MockByReflectionRule implements TestRule {

  private Map<Object, Map<FieldInjectionDirective, Object>> entitiesOldValues =
      new HashMap<>();

  private Logger logger = Logger.getLogger(MockByReflectionRule.class.getSimpleName());

  @Override
  public Statement apply(final Statement base, final Description description) {

    return new Statement() {
      @SuppressWarnings("unchecked")
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } finally {
          if (!entitiesOldValues.isEmpty()) {
            logger.info("Unset mocked fields...");
          }
          for (Map.Entry<Object, Map<FieldInjectionDirective, Object>> objectOldValue :
              entitiesOldValues
              .entrySet()) {
            for (Map.Entry<FieldInjectionDirective, Object> oldValue : objectOldValue.getValue()
                .entrySet()) {
              FieldInjectionDirective fieldDirective = oldValue.getKey();
              fieldDirective.write(oldValue.getValue());
            }
          }
        }
      }
    };
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
      if (entitiesOldValues.isEmpty()) {
        logger.info("Set mocked fields...");
      }
      return inject(instanceOrClass,
          new FieldInjectionDirective<>(instanceOrClass, fieldNames, logger), value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Performs the new value injection and registers in a cache the previous value.
   * @return the injected value.
   */
  private <T> T inject(Object instanceOrClass, FieldInjectionDirective<T> fieldInjectionDirective,
      T newValue) throws Exception {
    Map<FieldInjectionDirective, Object> entityOldValues = entitiesOldValues.get(instanceOrClass);
    if (entityOldValues == null) {
      entityOldValues = new HashMap<>();
      entitiesOldValues.put(instanceOrClass, entityOldValues);
    }
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
    private final Logger logger;

    private final static String WRITE_MESSAGE =
        "{0} - ''{1}'' field - set ''{2}'' instead of current ''{3}'' value";

    private FieldInjectionDirective(final Object instanceOrClass, final String fieldNames,
        final Logger logger) {
      this.instanceOrClass = instanceOrClass;
      this.fieldNames = fieldNames;
      this.logger = logger;
    }

    /**
     * Gets the final Object / Field to set by analyzing the path.
     * @return the final Object / Field to set by analyzing the path.
     * @throws Exception
     */
    private ObjectField getFinalObjectField() throws Exception {
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
    public T read() throws Exception {
      ObjectField finalObjectField = getFinalObjectField();
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
     * @throws Exception
     */
    public T write(T object) throws Exception {
      T previousValue = read();
      ObjectField finalObjectField = getFinalObjectField();
      if (finalObjectField.getInstanceOrClass() instanceof Class) {
        FieldUtils.writeStaticField((Class) finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), object, true);
        logger.info(format(WRITE_MESSAGE,
                ((Class) finalObjectField.getInstanceOrClass()).getSimpleName() + " class",
                finalObjectField.getFieldName(), object, previousValue));
      } else {
        FieldUtils.writeField(finalObjectField.getInstanceOrClass(),
            finalObjectField.getFieldName(), object,
                true);
        logger.info(format(WRITE_MESSAGE,
            finalObjectField.getInstanceOrClass().getClass().getSimpleName() + " instance",
            finalObjectField.getFieldName(), object, previousValue));
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
