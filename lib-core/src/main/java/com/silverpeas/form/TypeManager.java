/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Set;

/**
 * The TypeManager gives all the known field and displayer type
 * @see Field
 * @see FieldDisplayer
 */
public class TypeManager {

  /**
   * Returns all the type names.
   */
  static public String[] getTypeNames() {
    Set<String> keys = implementations.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Returns the class field implementation of the named type.
   * @throws FormException if the type name is unknown.
   */
  static public Class<?> getFieldImplementation(String typeName)
      throws FormException {
    Class<?> implementation = implementations.get(typeName);

    if (implementation == null) {
      throw new FormException("TypeManager", "form.EXP_UNKNOWN_TYPE", typeName);
    }

    return implementation;
  }

  /**
   * Returns the name of the default FieldDisplayer of the named type.
   * @throws FormException if the type name is unknown.
   */
  static public String getDisplayerName(String typeName) throws FormException {
    List<String> displayerNames = typeName2displayerNames.get(typeName);

    if (displayerNames == null || displayerNames.isEmpty()) {
      throw new FormException("TypeManager", "form.EXP_UNKNOWN_TYPE", typeName);
    }

    return displayerNames.get(0);
  }

  /**
   * Returns the names of all the FieldDisplayers which can be used with the named type.
   * @throws FormException if the type name is unknown.
   */
  static public String[] getDisplayerNames(String typeName)
      throws FormException {
    List<String> displayerNames = typeName2displayerNames.get(typeName);

    if (displayerNames == null || displayerNames.isEmpty()) {
      throw new FormException("TypeManager", "form.EXP_UNKNOWN_TYPE", typeName);
    }

    return displayerNames.toArray(new String[displayerNames.size()]);
  }

  /**
   * Returns the named FieldDisplayer of the named field.
   * @throws FormException if the type name is unknown.
   * @throws FormException if the displayer name is unknown.
   * @throws FormException if the displayer and the type are not compatible.
   */
  static public FieldDisplayer getDisplayer(String typeName,
      String displayerName) throws FormException {
    String displayerId = getDisplayerId(typeName, displayerName);
    Class<?> displayerClass = (Class<?>) displayerId2displayerClass.get(displayerId);

    if (displayerClass == null) {
      List<String> displayerNames = typeName2displayerNames.get(typeName);

      if (displayerNames == null || displayerNames.isEmpty()) {
        throw new FormException("TypeManager", "form.EXP_UNKNOWN_TYPE",
            typeName);
      } else {
        throw new FormException("TypeManager", "form.EXP_UNKNOWN_DISPLAYER",
            displayerName);
      }
    }

    return constructDisplayer(displayerClass);
  }

  /**
   * Set the implementation class for typeName.
   */
  static public void setFieldImplementation(String fieldClassName,
      String typeName) throws FormException {
    Class<?> fieldImplementation = getFieldClass(fieldClassName);
    implementations.put(typeName, fieldImplementation);
  }

  /**
   * Set the FieldDisplayer class for typeName, displayerName
   */
  static public void setDisplayer(String displayerClassName, String typeName,
      String displayerName, boolean defaultDisplayer) throws FormException {
    Class<?> displayerClass = getDisplayerClass(displayerClassName);
    String displayerId = getDisplayerId(typeName, displayerName);

    // binds ( typeName -> displayerName )
    List<String> displayerNames = typeName2displayerNames.get(typeName);

    if (displayerNames == null) {
      displayerNames = new ArrayList<String>();
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

  /**
   * Builds a displayer id from a type name and a displayer name.
   */
  static private String getDisplayerId(String typeName, String displayerName) {
    return typeName + "." + displayerName;
  }

  /**
   * Extracts the typeName from a class identifier typeName.implementation typeName.displayer
   * typeName.displayer.displayerName
   */
  static private String extractTypeName(String identifier) {
    int dot = identifier.indexOf(".");
    if (dot == -1)
      return identifier.trim();
    else
      return identifier.substring(0, dot).trim();
  }

  /**
   * Extracts the default extension from a class identifier typeName.implementation
   * typeName.displayer typeName.displayer.displayerName
   */
  static private String extractClassKind(String identifier) {
    int dot = identifier.indexOf(".");
    if (dot == -1 || dot + 1 == identifier.length())
      return "";
    else {
      String afterFirstDot = identifier.substring(dot + 1);
      dot = afterFirstDot.indexOf(".");
      if (dot == -1)
        return afterFirstDot.trim();
      else
        return afterFirstDot.substring(0, dot).trim();
    }
  }

  /**
   * Extracts the displayerName from a class identifier : typeName.implementation typeName.displayer
   * typeName.displayer.displayerName
   */
  static private String extractDisplayerName(String identifier) {
    int dot = identifier.indexOf(".");
    if (dot == -1 || dot + 1 == identifier.length())
      return "";
    else {
      String afterFirstDot = identifier.substring(dot + 1);
      dot = afterFirstDot.indexOf(".");
      if (dot == -1 || dot + 1 == afterFirstDot.length())
        return "default";
      else
        return afterFirstDot.substring(dot + 1).trim();
    }
  }

  /**
   * Builds a field.
   */
  static private Field constructField(Class<?> fieldClass) throws FormException {
    try {
      Class<?>[] noParameterClass = new Class[0];
      Constructor<?> constructor = fieldClass.getConstructor(noParameterClass);
      Object[] noParameter = new Object[0];
      Field field = (Field) constructor.newInstance(noParameter);

      return field;
    } catch (NoSuchMethodException e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_MISSING_EMPTY_CONSTRUCTOR", fieldClass.getName());
    } catch (ClassCastException e) {
      throw new FormFatalException("TypeManager", "form.EXP_NOT_A_FIELD",
          fieldClass.getName());
    } catch (Exception e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_FIELD_CONSTRUCTION_FAILED", fieldClass.getName());
    }
  }

  /**
   * Builds a displayer.
   */
  static private FieldDisplayer constructDisplayer(Class<?> displayerClass)
      throws FormException {
    try {
      Class<?>[] noParameterClass = new Class[0];
      Constructor<?> constructor = displayerClass.getConstructor(noParameterClass);

      Object[] noParameter = new Object[0];
      FieldDisplayer displayer = (FieldDisplayer) constructor
          .newInstance(noParameter);

      return displayer;
    } catch (NoSuchMethodException e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_MISSING_EMPTY_CONSTRUCTOR", displayerClass.getName());
    } catch (ClassCastException e) {
      throw new FormFatalException("TypeManager", "form.EXP_NOT_A_DISPLAYER",
          displayerClass.getName());
    } catch (Exception e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_DISPLAYER_CONSTRUCTION_FAILED", displayerClass.getName());
    }
  }

  /**
   * Get the field class from class name.
   */
  static private Class<?> getFieldClass(String fieldClassName)
      throws FormException {
    try {
      Class<?> fieldClass = Class.forName(fieldClassName);

      // try to built a displayer from this class
      // and discards the constructed object.
      constructField(fieldClass);

      return fieldClass;
    } catch (ClassNotFoundException e) {
      throw new FormFatalException("TypeManager", "form.EXP_UNKNOWN_CLASS",
          fieldClassName);
    }
  }

  /**
   * Get the displayer class from class name.
   */
  static private Class<?> getDisplayerClass(String displayerClassName)
      throws FormException {
    try {
      Class<?> displayerClass = Class.forName(displayerClassName);

      // try to built a displayer from this class
      // and discards the constructed object.
      constructDisplayer(displayerClass);

      return displayerClass;
    } catch (ClassNotFoundException e) {
      throw new FormFatalException("TypeManager", "form.EXP_UNKNOWN_CLASS",
          displayerClassName);
    }
  }

  /**
   * Init the Maps from the com.silverpeas.form.settings.types properties file. (typeName ->
   * List(displayerName)) (the first is the default). (displayerId -> displayerClass).
   */
  static private void init() throws FormException {
    String identifier;
    String className;
    String typeName;
    String classKind;
    String displayerName;
    boolean isDefault;

    try {
      ResourceLocator properties = new ResourceLocator(
          "com.silverpeas.form.settings.types", "");

      Enumeration<String> binds = properties.getKeys();
      while (binds.hasMoreElements()) {
        identifier = binds.nextElement();
        SilverTrace.info("form", "TypeManager.init",
            "root.MSG_GEN_PARAM_VALUE", "identifier=" + identifier);
        className = properties.getString(identifier);
        SilverTrace.info("form", "TypeManager.init",
            "root.MSG_GEN_PARAM_VALUE", "className=" + className);

        typeName = extractTypeName(identifier);
        classKind = extractClassKind(identifier);
        displayerName = extractDisplayerName(identifier);

        if ("implementation".equals(classKind)) {
          setFieldImplementation(className, typeName);
        } else if ("displayer".equals(classKind)) {
          isDefault = "default".equals(displayerName);
          setDisplayer(className, typeName, displayerName, isDefault);
        }
      }
    } catch (MissingResourceException e) {
      throw new FormFatalException("TypeManager",
          "form.EXP_MISSING_DISPLAYER_PROPERTIES",
          "com.silverpeas.form.settings.types");
    }
  }

  /**
   * The Map (typeName -> fieldClass)
   */
  static private Map<String, Class<?>> implementations = new HashMap<String, Class<?>>();

  /**
   * The Map (typeName -> List(displayerName)) (the first is the default).
   */
  static private Map<String, List<String>> typeName2displayerNames =
      new HashMap<String, List<String>>();

  /**
   * The Map (displayerId -> displayerClass).
   */
  static private Map<String, Class<?>> displayerId2displayerClass = new HashMap<String, Class<?>>();

  /**
   * Init the TypeManager class.
   */
  static {
    try {
      init();
    } catch (FormException e) {
      SilverTrace.fatal("form", "TypeManager.initialization",
          "form.EXP_INITIALIZATION_FAILED", e);
    }
  }

}
