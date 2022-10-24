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
package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.component.GroupOfParametersSorter;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.ui.DisplayI18NHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * WAComponent stands for Web Application Component and it represents an application that is
 * available in Silverpeas and that can be instantiated to a {@code ComponentInst} object.
 * <p>
 * The Web Application components available in Silverpeas are loaded by the
 * {@code org.silverpeas.core.admin.component.WAComponentRegistry} registry. They can be the accessed
 * either by the registry itself or by the WAComponent class (it delegates the access to the
 * registry).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WAComponentType", propOrder = {"name", "behaviors", "label", "description",
    "suite", "inheritSpaceRightsByDefault", "publicByDefault", "visible", "visibleInPersonalSpace",
    "portlet", "router", "profiles", "groupsOfParameters", "parameters"})
public class WAComponent extends AbstractSilverpeasComponent {

  @XmlTransient
  private ParameterSorter sorter = new ParameterSorter();

  @XmlElement(required = true)
  protected String name;
  protected ComponentBehaviors behaviors;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> label;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> description;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected Map<String, String> suite;
  protected boolean inheritSpaceRightsByDefault = true;
  protected boolean publicByDefault = false;
  protected boolean visible;
  protected boolean visibleInPersonalSpace = false;
  protected boolean portlet;
  protected String router;
  @XmlElementWrapper(name = "profiles")
  @XmlElement(name = "profile", required = true)
  protected List<Profile> profiles;
  @XmlElementWrapper(name = "groupsOfParameters")
  @XmlElement(name = "groupOfParameters")
  protected List<GroupOfParameters> groupsOfParameters;
  @XmlElementWrapper(name = "parameters")
  @XmlElement(name = "parameter")
  protected List<Parameter> parameters;

  /**
   * Gets the WAComponent object with the specified name.
   * @param componentName the unique name of the WAComponent to return.
   * @return optionally a WAComponent instance with the given name.
   */
  public static Optional<WAComponent> getByName(String componentName) {
    return WAComponentRegistry.get().getWAComponent(componentName);
  }

  /**
   * Gets the WAComponent object representing the component to which the specified instance is
   * related.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return optionally a WAComponent object related to the component instance.
   */
  public static Optional<WAComponent> getByInstanceId(String componentInstanceId) {
    return getByName(ComponentInst.getComponentName(componentInstanceId));
  }

  /**
   * Gets all the available WAComponent instances.
   * @return a collection of WAComponent instance.
   */
  public static Collection<WAComponent> getAll() {
    return WAComponentRegistry.get().getAllWAComponents().values();
  }

  /**
   * Gets the value of the name property.
   * @return possible object is {@link String }
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * @param value allowed object is {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the different behaviors this component satisfies.
   *
   * @return
   *     possible object is
   *     {@link ComponentBehaviors }
   *
   */
  public ComponentBehaviors getBehaviors() {
    return behaviors;
  }

  /**
   * Sets all the behaviors this component has to satisfy.
   *
   * @param value
   *     allowed object is
   *     {@link ComponentBehaviors }
   *
   */
  public void setBehaviors(ComponentBehaviors value) {
    this.behaviors = value;
  }

  /**
   * Gets the value of the label property.
   * @return possible object is {@link Multilang }
   */
  @Override
  public Map<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<>();
    }
    return label;
  }

  /**
   * Sets the value of the label property.
   * @param value allowed object is {@link Multilang }
   */
  public void setLabel(Map<String, String> value) {
    this.label = value;
  }

  /**
   * Gets the value of the description property.
   * @return possible object is {@link Multilang }
   */
  @Override
  public Map<String, String> getDescription() {
    if (description == null) {
      description = new HashMap<>();
    }
    return description;
  }

  /**
   * Sets the value of the description property.
   * @param value allowed object is {@link Multilang }
   */
  public void setDescription(Map<String, String> value) {
    this.description = value;
  }

  /**
   * Gets the value of the suite property.
   * @return possible object is {@link Multilang }
   */
  public Map<String, String> getSuite() {
    if (suite == null) {
      suite = new HashMap<>();
    }
    return suite;
  }

  public String getSuite(String lang) {
    if (getSuite().containsKey(lang)) {
      return getSuite().get(lang);
    }
    return getSuite().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the suite property.
   * @param value allowed object is {@link Multilang }
   */
  public void setSuite(Map<String, String> value) {
    this.suite = value;
  }

  /**
   * Gets the value of the inheritSpaceRights property.
   */
  @Override
  public boolean isInheritSpaceRightsByDefault() {
    return inheritSpaceRightsByDefault;
  }

  /**
   * Sets the value of the inheritSpaceRights property.
   */
  public void setInheritSpaceRightsByDefault(final boolean inheritSpaceRightsByDefault) {
    this.inheritSpaceRightsByDefault = inheritSpaceRightsByDefault;
  }

  /**
   * Gets the value of the publicByDefault property.
   */
  @Override
  public boolean isPublicByDefault() {
    return publicByDefault;
  }

  /**
   * Sets the value of the publicByDefault property.
   */
  public void setPublicByDefault(final boolean publicByDefault) {
    this.publicByDefault = publicByDefault;
  }

  /**
   * Gets the value of the visible property.
   */
  @Override
  public boolean isVisible() {
    return visible;
  }

  /**
   * Sets the value of the visible property.
   */
  public void setVisible(boolean value) {
    this.visible = value;
  }

  /**
   * Gets the value of the visibleInPersonalSpace property.
   * @return possible object is {@link Boolean }
   */
  public boolean isVisibleInPersonalSpace() {
    return visibleInPersonalSpace;
  }

  /**
   * Sets the value of the visibleInPersonalSpace property.
   * @param value allowed object is {@link Boolean }
   */
  public void setVisibleInPersonalSpace(boolean value) {
    this.visibleInPersonalSpace = value;
  }

  /**
   * Gets the value of the portlet property.
   */
  public boolean isPortlet() {
    return portlet;
  }

  /**
   * Sets the value of the portlet property.
   */
  public void setPortlet(boolean value) {
    this.portlet = value;
  }

  /**
   * Gets the value of the router property.
   * @return possible object is {@link String }
   */
  public String getRouter() {
    return router;
  }

  /**
   * Sets the value of the router property.
   * @param value allowed object is {@link String }
   */
  public void setRouter(String value) {
    this.router = value;
  }

  /**
   * Gets the value of the profiles property.
   * @return list of {@link Profile }
   */
  public List<Profile> getProfiles() {
    if (profiles == null) {
      profiles = new ArrayList<>();
    }
    return profiles;
  }

  public Profile getProfile(String name) {
    for (Profile profile : getProfiles()) {
      if (profile.getName().equals(name)) {
        return profile;
      }
    }
    return null;
  }

  /**
   * Sets the value of the profiles property.
   * @param profiles list of {@link Profile}
   */
  public void setProfiles(List<Profile> profiles) {
    this.profiles = profiles;
  }

  /**
   * Gets the value of the parameters property.
   * @return list of {@link Parameter}
   */
  @Override
  public List<Parameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    return parameters;
  }

  /**
   * Sets the value of the parameters property.
   * @param parameters list of {@link Parameter}
   */
  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
    indexedParametersByName.clear();
  }

  /**
   * Indicates if a parameter is defined which name is equal to the given method parameter.
   * @param parameterName the parameter name to perform.
   * @return true if a parameter is defined behind the specified method parameter, false otherwise.
   */
  @Override
  public boolean hasParameterDefined(String parameterName) {
    return getIndexedParametersByName().get(parameterName) != null;
  }

  @Override
  public List<Parameter> getSortedParameters() {
    Collections.sort(getParameters(), sorter);
    return this.parameters;
  }

  @Override
  public List<GroupOfParameters> getGroupsOfParameters() {
    if (groupsOfParameters == null) {
      groupsOfParameters = new ArrayList<>();
    }
    return groupsOfParameters;
  }

  public List<GroupOfParameters> getSortedGroupsOfParameters() {
    Collections.sort(getGroupsOfParameters(), new GroupOfParametersSorter());
    return this.groupsOfParameters;
  }

  @Override
  public boolean isWorkflow() {
    return getBehaviors() != null &&
        getBehaviors().getBehavior().contains(ComponentBehavior.WORKFLOW);
  }

  @Override
  public boolean isTopicTracker() {
    return getBehaviors() != null &&
        getBehaviors().getBehavior().contains(ComponentBehavior.TOPIC_TRACKER);
  }
}