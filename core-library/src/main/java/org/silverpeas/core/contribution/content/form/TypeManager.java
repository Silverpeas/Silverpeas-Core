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
package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The TypeManager gives all the known field and displayer type
 * @see Field
 * @see FieldDisplayer
 */
public class TypeManager {

  private static final TypeManager instance = new TypeManager();
  private static final String TYPE_MANAGER = "TypeManager";
  private static final String FORM_EXP_UNKNOWN_TYPE = "form.EXP_UNKNOWN_TYPE";

  private TypeManager() {
    try {
      init();
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  public static TypeManager getInstance() {
    return instance;
  }

  /**
   * Returns all the type names.
   */
  public String[] getTypeNames() {
    Set<String> keys = implementations.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Returns the class field implementation of the named type.
   *
   * @throws FormException if the type name is unknown.
   */
  public Class<?> getFieldImplementation(String typeName)
      throws FormException {
    if (!implementations.containsKey(typeName)) {
      throw new FormException(TYPE_MANAGER, FORM_EXP_UNKNOWN_TYPE, typeName);
    }
    return implementations.get(typeName);
  }

  /**
   * Returns the name of the default FieldDisplayer of the named type.
   *
   * @throws FormException if the type name is unknown.
   */
  public String getDisplayerName(String typeName) throws FormException {
    List<String> displayerNames = typeName2displayerNames.get(typeName);
    if (displayerNames == null || displayerNames.isEmpty()) {
      throw new FormException(TYPE_MANAGER, FORM_EXP_UNKNOWN_TYPE, typeName);
    }
    return displayerNames.get(0);
  }

  /**
   * Returns the names of all the FieldDisplayers which can be used with the named type.
   *
   * @throws FormException if the type name is unknown.
   */
  public String[] getDisplayerNames(String typeName)
      throws FormException {
    List<String> displayerNames = typeName2displayerNames.get(typeName);
    if (displayerNames == null || displayerNames.isEmpty()) {
      throw new FormException(TYPE_MANAGER, FORM_EXP_UNKNOWN_TYPE, typeName);
    }
    return displayerNames.toArray(new String[displayerNames.size()]);
  }

  /**
   * Returns the named FieldDisplayer of the named field.
   *
   * @throws FormException if the type name is unknown, if the displayer name is unknown or if the
   * displayer and the type are not compatible.
   */
  @SuppressWarnings("unchecked")
  public <T extends Field> FieldDisplayer<T> getDisplayer(String typeName, String displayerName)
      throws FormException {
    String displayerId = getDisplayerId(typeName, displayerName);
    Class<FieldDisplayer<? extends Field>> displayerClass =
        displayerId2displayerClass.get(displayerId);

    if (displayerClass == null) {
      List<String> displayerNames = typeName2displayerNames.get(typeName);
      if (displayerNames == null || displayerNames.isEmpty()) {
        throw new FormException(TYPE_MANAGER, FORM_EXP_UNKNOWN_TYPE, typeName);
      } else {
        throw new FormException(TYPE_MANAGER, "form.EXP_UNKNOWN_DISPLAYER", displayerName);
      }
    }
    return (FieldDisplayer<T>) constructDisplayer(displayerClass);
  }

  /**
   * Set the implementation class for typeName.
   */
  public void setFieldImplementation(String fieldClassName,
      String typeName) throws FormException {
    Class<?> fieldImplementation = getFieldClass(fieldClassName);
    implementations.put(typeName, fieldImplementation);
  }

  /**
   * Set the FieldDisplayer class for typeName, displayerName
   */
  public void setDisplayer(String displayerClassName, String typeName,
      String displayerName, boolean defaultDisplayer) throws FormException {
    Class<FieldDisplayer<? extends Field>> displayerClass = getDisplayerClass(displayerClassName);
    String displayerId = getDisplayerId(typeName, displayerName);

    // binds ( typeName -> displayerName )
    List<String> displayerNames = typeName2displayerNames.get(typeName);
    if (displayerNames == null) {
      displayerNames = new ArrayList<>();
      displayerNames.add(displayerName);
      typeName2displayerNames.put(typeName, displayerNames);
    } else {
      if (defaultDisplayer) {
        displayerNames.add(0, displayerName);
      } else {
        displayerNames.add(displayerName);
      }
    }
    // binds ( displayerId -> displayerClass )
    displayerId2displayerClass.put(displayerId, displayerClass);
  }

  public Map<String, List<String>> getTypesAndDisplayers() {
    return typeName2displayerNames;
  }
  /**
   * Builds a displayer id from a type name and a displayer name.
   */
  private String getDisplayerId(String typeName, String displayerName) {
    return typeName + '.' + displayerName;
  }

  /**
   * Extracts the typeName from a class identifier typeName.implementation typeName.displayer
   * typeName.displayer.displayerName
   */
  private String extractTypeName(String identifier) {
    int dot = identifier.indexOf('.');
    if (dot <= 0) {
      return identifier.trim();
    }
    return identifier.substring(0, dot).trim();
  }

  /**
   * Extracts the default extension from a class identifier typeName.implementation
   * typeName.displayer typeName.displayer.displayerName
   */
  private String extractClassKind(String identifier) {
    int dot = identifier.indexOf('.');
    if (dot == -1 || dot + 1 == identifier.length()) {
      return "";
    }
    String afterFirstDot = identifier.substring(dot + 1);
    dot = afterFirstDot.indexOf('.');
    if (dot == -1) {
      return afterFirstDot.trim();
    }
    return afterFirstDot.substring(0, dot).trim();
  }

  /**
   * Extracts the displayerName from a class identifier : typeName.implementation typeName.displayer
   * typeName.displayer.displayerName
   */
  private String extractDisplayerName(String identifier) {
    int dot = identifier.indexOf('.');
    if (dot == -1 || dot + 1 == identifier.length()) {
      return "";
    }

    String afterFirstDot = identifier.substring(dot + 1);
    dot = afterFirstDot.indexOf('.');
    if (dot == -1 || dot + 1 == afterFirstDot.length()) {
      return "default";
    }
    return afterFirstDot.substring(dot + 1).trim();
  }

  /**
   * Builds a field.
   */
  private Field constructField(Class<?> fieldClass) throws FormException {
    try {
      Constructor<?> constructor = fieldClass.getConstructor();
      return (Field) constructor.newInstance();
    } catch (NoSuchMethodException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_MISSING_EMPTY_CONSTRUCTOR",
          fieldClass.getName(), e);
    } catch (ClassCastException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_NOT_A_FIELD", fieldClass.getName(), e);
    } catch (Exception e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_FIELD_CONSTRUCTION_FAILED",
          fieldClass.getName(), e);
    }
  }

  /**
   * Builds a displayer.
   * @param displayerClass
   */
  private FieldDisplayer<? extends Field> constructDisplayer(
      Class<FieldDisplayer<? extends Field>> displayerClass) throws FormException {
    try {
      Constructor<FieldDisplayer<? extends Field>> constructor = displayerClass.getConstructor();
      return constructor.newInstance();
    } catch (NoSuchMethodException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_MISSING_EMPTY_CONSTRUCTOR",
          displayerClass.getName(), e);
    } catch (ClassCastException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_NOT_A_DISPLAYER",
          displayerClass.getName(), e);
    } catch (Exception e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_DISPLAYER_CONSTRUCTION_FAILED",
          displayerClass.getName(), e);
    }
  }

  /**
   * Get the field class from class name.
   */
  private Class<?> getFieldClass(String fieldClassName)
      throws FormException {
    try {
      Class<?> fieldClass = Class.forName(fieldClassName);
      // try to built a displayer from this class
      // and discards the constructed object.
      constructField(fieldClass);
      return fieldClass;
    } catch (ClassNotFoundException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_UNKNOWN_CLASS", fieldClassName, e);
    }
  }

  /**
   * Get the displayer class from class name.
   */
  @SuppressWarnings("unchecked")
  private Class<FieldDisplayer<? extends Field>> getDisplayerClass(String displayerClassName)
      throws FormException {
    try {
      Class<FieldDisplayer<? extends Field>> displayerClass =
          (Class<FieldDisplayer<? extends Field>>) Class.forName(displayerClassName);
      // try to built a displayer from this class
      // and discards the constructed object.
      constructDisplayer(displayerClass);
      return displayerClass;
    } catch (ClassNotFoundException e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_UNKNOWN_CLASS", displayerClassName, e);
    }
  }

  /**
   * Init the Maps from the org.silverpeas.core.contribution.content.form.settings.types properties file. (typeName ->
   * List(displayerName)) (the first is the default). (displayerId -> displayerClass).
   */
  private void init() throws FormException {
    try {
      SettingBundle properties = ResourceLocator.getSettingBundle("org.silverpeas.form.settings.types");
      Set<String> binds = properties.keySet();
      for(String identifier: binds) {

        String className = properties.getString(identifier);

        String typeName = extractTypeName(identifier);
        String classKind = extractClassKind(identifier);
        String displayerName = extractDisplayerName(identifier);
        if ("implementation".equals(classKind)) {
          setFieldImplementation(className, typeName);
        } else if ("displayer".equals(classKind)) {
          setDisplayer(className, typeName, displayerName, "default".equals(displayerName));
        }
      }
    } catch (Exception e) {
      throw new FormFatalException(TYPE_MANAGER, "form.EXP_MISSING_DISPLAYER_PROPERTIES",
          "org.silverpeas.form.settings.types", e);
    }
  }
  /**
   * The Map (typeName -> fieldClass)
   */
  private final Map<String, Class<?>> implementations = new HashMap<>();
  /**
   * The Map (typeName -> List(displayerName)) (the first is the default).
   */
  private final Map<String, List<String>> typeName2displayerNames =
      new HashMap<>();
  /**
   * The Map (displayerId -> displayerClass).
   */
  private final Map<String, Class<FieldDisplayer<? extends Field>>> displayerId2displayerClass = new HashMap<>();
}
