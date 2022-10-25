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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.component.PersonalComponentRegistry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * UserComponent stands for Web Application Component associated to users and it represents an
 * application that is available in Silverpeas personal space.
 * <p>
 * The Web Application user components available in Silverpeas are loaded by the
 * {@code org.silverpeas.core.admin.component.PersonalComponentRegistry} registry. They can be the
 * accessed
 * either by the registry itself or by the PersonalComponent class (it delegates the access to the
 * registry).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PersonalComponentType", propOrder = {"name", "label", "description", "visible",
    "groupsOfParameters", "parameters"})
public class PersonalComponent extends AbstractSilverpeasComponent {

  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> label;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> description;
  @XmlElement(required = true)
  protected boolean visible;
  @XmlElementWrapper(name = "groupsOfParameters")
  @XmlElement(name = "groupOfParameters")
  protected List<GroupOfParameters> groupsOfParameters;
  @XmlElementWrapper(name = "parameters")
  @XmlElement(name = "parameter")
  protected List<Parameter> parameters;

  /**
   * Gets the PersonalComponent instance with the specified name.
   * @param componentName the unique name of the PersonalComponent to return.
   * @return optionally a PersonalComponent instance with the given name.
   */
  public static Optional<PersonalComponent> getByName(String componentName) {
    return PersonalComponentRegistry.get().getPersonalComponent(componentName);
  }

  /**
   * Gets the PersonalComponent object representing the component to which the specified instance is
   * related.
   * @param componentInstanceId the unique identifier of a personal component instance.
   * @return optionally a PersonalComponent object related to the personal component instance.
   */
  public static Optional<PersonalComponent> getByInstanceId(String componentInstanceId) {
    return getByName(PersonalComponentInstance.getComponentName(componentInstanceId));
  }

  /**
   * Gets all the available PersonalComponent instances.
   * @return a collection of PersonalComponent instance.
   */
  public static Collection<PersonalComponent> getAll() {
    return PersonalComponentRegistry.get().getAllPersonalComponents().values();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<>();
    }
    return label;
  }

  /**
   * Sets the value of the name property.
   * @param value allowed object is {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Sets the value of the label property.
   * @param value allowed object is {@link Multilang }
   */
  public void setLabel(Map<String, String> value) {
    this.label = value;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public boolean isPersonal() {
    return true;
  }

  /**
   * Sets the value of the description property.
   * @param value allowed object is {@link Multilang }
   */
  public void setDescription(Map<String, String> value) {
    this.description = value;
  }

  @Override
  public Map<String, String> getDescription() {
    if (description == null) {
      description = new HashMap<>();
    }
    return description;
  }


  /**
   * Sets the value of the visible property.
   */
  public void setVisible(boolean value) {
    this.visible = value;
  }

  @Override
  public List<Parameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    return parameters;
  }

  /**
   * Sets explicitly the instance parameters to this personal component.
   * @param parameters list of {@link Parameter}
   */
  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
    indexedParametersByName.clear();
  }

  @Override
  public boolean hasParameterDefined(String parameterName) {
    return getIndexedParametersByName().get(parameterName) != null;
  }

  @Override
  public List<GroupOfParameters> getGroupsOfParameters() {
    if (groupsOfParameters == null) {
      groupsOfParameters = new ArrayList<>();
    }
    return groupsOfParameters;
  }
}