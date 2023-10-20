/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.template;

import java.util.Map;

/**
 * A Silverpeas template. A {@link SilverpeasTemplate} instance represents either a template or a
 * set of template files located into a specified directory. This class is to abstract and wrap the
 * actual templating engine used in Silverpeas so that any changes are limited to the implementors
 * of this interface. Nevertheless, a limitation and constrains on this, is that in the case of a
 * templating engine change, the template grammar can also change and by consequence the templates
 * can require to be rewritten in order to satisfy the new grammar.
 * <p>
 * Templates are essentially exemplars of the desired output with "holes" where the programmer may
 * stick untyped values called attributes or other template instances. To enforce model-view
 * separation, templates may not test nor compute with attribute values and, consequently,
 * attributes have no need for type information. Templates may, however, know the data is structured
 * in a particular manner such as a tree structure. Templates can include execution statements to
 * contextualize the rendering of the output according to the available attributes. Such statements
 * include usually conditional instructions, nevertheless the complete set of possible statements is
 * related to the underlying templating engine used by Silverpeas.
 * </p>
 * <p>
 * A template in Silverpeas can be embedded into a String or a file. It can be either a raw one or a
 * declarative one. A raw template is just a plain text containing templating instructions whereas a
 * declarative template is a descriptor of a template with an header and a body (the body is the
 * template itself).
 * </p>
 */
public interface SilverpeasTemplate {

  /**
   * The property key from which the root directory containing all the template files in Silverpeas
   * can be got.
   */
  String TEMPLATE_ROOT_DIR = "template.root.dir";

  /**
   * The property key from which the customer directory containing all the customized template files
   * can be got.
   */
  String TEMPLATE_CUSTOM_DIR = "template.customer.dir";

  /**
   * When the result of a template compilation is built from a combination of several template
   * files, this method MUST be called BEFORE calling {@link #applyFileTemplate(String)}.
   * <p>
   * It is in charge of merging the default template provided by Silverpeas and the one modified by
   * the customer. By this way, the customer modifies only the necessary templates in case of
   * customization.
   * </p>
   *
   * @return itself.
   */
  SilverpeasTemplate mergeRootWithCustom();

  /**
   * Sets the specified attribute to pass to the underlying template for expansion.
   *
   * @param name the name of the attribute
   * @param value the value of the attribute with which the declared variable in the template will
   * be replaced.
   */
  void setAttribute(String name, Object value);

  /**
   * Compiles the text template in the specified file and executes the resulting code by applying
   * the attributes set to generate a full text.
   * <p>
   * All statements in a template are identified by being wrapped into '$' character. Such
   * statements include attribute name (to be expanded by their value), conditional instruction (if,
   * elseif, else, endif), sub-templating application, and other statements specific to the
   * underlying templating engine.
   * </p>
   *
   * @param fileName the path of the template file relative to the root directory of the templates
   * backed by this Silverpeas template.
   * @return the full text generated from the specified template file.
   */
  String applyFileTemplate(String fileName);

  /**
   * Compiles the template defined in the specified descriptor and executes the resulting code by
   * applying the attributes to generate a full text.
   * <p>
   * To create a descriptor of a template, id est to write a definition of a template in a file,
   * please see the javadoc of the implementor of this method.
   * </p>
   * <p>
   * All statements in a template are identified by being wrapped into '$' character. Such
   * statements include attribute name (to be expanded by their value), conditional instruction (if,
   * elseif, else, endif), sub-templating application, and other statements specific to the
   * underlying templating engine.
   * </p>
   *
   * @param descriptor the path of the template descriptor relative to the root directory of the
   * templates backed by this Silverpeas template.
   * @return the full text generated from the specified template descriptor.
   */
  String applyFileTemplateDescriptor(String descriptor);

  /**
   * Compiles the text template in the specified file of the given Silverpeas component and executes
   * the resulting code by applying the attributes to generate a full text.
   * <p>
   * All statements in a template are identified by being wrapped into '$' character. Such
   * statements include attribute name (to be expanded by their value), conditional instruction (if,
   * elseif, else, endif), sub-templating application, and other statements specific to the
   * underlying templating engine.
   * </p>
   *
   * @param componentName the name o the component to which the template belongs.
   * @param fileName the path of the template file relative to the directory of templates of the
   * specified Silverpeas component.
   * @return the full text generated from the specified template file.
   */
  String applyFileTemplateOnComponent(String componentName, String fileName);

  /**
   * Is the specified template exists for the given Silverpeas component?
   *
   * @param componentName the name of the Silverpeas component to which the template belongs.
   * @param fileName the path of the template file relative to the root directory of the component
   * containing its template files.
   * @return true if such template exists for the given Silverpeas component. False otherwise.
   */
  boolean isCustomTemplateExists(String componentName, String fileName);

  /**
   * Compiles the template embedded into the specified {@link String}, executes the resulting code
   * by applying the attributes to generate the full text.
   *
   * @param template an inlined template.
   * @return the full text generated from the specified inlined template.
   */
  String applyStringTemplate(String template);

  /**
   * Gets all the attributes that were set for this Silverpeas template.
   *
   * @return a mapping of attributes name to values.
   */
  Map<String, Object> getAttributes();
}