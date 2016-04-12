/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.component.GroupOfParametersSorter;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.CollectionUtil;

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
@XmlType(name = "WAComponentType", propOrder = { "name", "behaviors", "label", "description", "suite",
    "visible", "visibleInPersonalSpace", "portlet", "router", "profiles",
    "groupsOfParameters", "parameters" })
public class WAComponent {

  /**
   * Gets the WAComponent instance with the specified name.
   * @param componentName the unique name of the WAComponent to return.
   * @return optionally a WAComponent instance with the given name.
   */
  public static Optional<WAComponent> get(String componentName) {
    return WAComponentRegistry.get().getWAComponent(componentName);
  }

  /**
   * Gets all the available WAComponent instances.
   * @return a collection of WAComponent instance.
   */
  public static Collection<WAComponent> getAll() {
    return WAComponentRegistry.get().getAllWAComponents().values();
  }


  @XmlTransient
  private ParameterSorter sorter = new ParameterSorter();

  @XmlElement(required = true)
  protected String name;
  protected ComponentBehaviors behaviors;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> label;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> description;
  @XmlElement(required = true)
  @XmlJavaTypeAdapter(MultilangHashMapAdapter.class)
  protected HashMap<String, String> suite;
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
  @XmlTransient
  protected Map<String, Parameter> indexedParametersByName = new HashMap<>();

  /**
   * Gets the value of the name property.
   * @return possible object is {@link String }
   */
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
  public HashMap<String, String> getLabel() {
    if (label == null) {
      label = new HashMap<>();
    }
    return label;
  }

  public String getLabel(String lang) {
    if (getLabel().containsKey(lang)) {
      return getLabel().get(lang);
    }
    return getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the label property.
   * @param value allowed object is {@link Multilang }
   */
  public void setLabel(HashMap<String, String> value) {
    this.label = value;
  }

  /**
   * Gets the value of the description property.
   * @return possible object is {@link Multilang }
   */
  public HashMap<String, String> getDescription() {
    if (description == null) {
      description = new HashMap<>();
    }
    return description;
  }

  public String getDescription(String lang) {
    if (getDescription().containsKey(lang)) {
      return getDescription().get(lang);
    }
    return getDescription().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Sets the value of the description property.
   * @param value allowed object is {@link Multilang }
   */
  public void setDescription(HashMap<String, String> value) {
    this.description = value;
  }

  /**
   * Gets the value of the suite property.
   * @return possible object is {@link Multilang }
   */
  public HashMap<String, String> getSuite() {
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
  public void setSuite(HashMap<String, String> value) {
    this.suite = value;
  }

  /**
   * Gets the value of the visible property.
   */
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
  public List<Parameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    return parameters;
  }

  /**
   * Gets defined parameters indexed by their names.
   * @return
   */
  private Map<String, Parameter> getIndexedParametersByName() {
    List<Parameter> definedParameters = getParameters();
    if (CollectionUtil.isNotEmpty(definedParameters) &&
        definedParameters.size() != indexedParametersByName.size()) {
      for (Parameter parameter : definedParameters) {
        indexedParametersByName.put(parameter.getName(), parameter);
      }
    }
    List<GroupOfParameters> groupsOfParameters = getGroupsOfParameters();
    for (GroupOfParameters group : groupsOfParameters) {
      for (Parameter parameter : group.getParameters()) {
        indexedParametersByName.put(parameter.getName(), parameter);
      }
    }
    return indexedParametersByName;
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
  public boolean hasParameterDefined(String parameterName) {
    return getIndexedParametersByName().get(parameterName) != null;
  }

  public List<Parameter> getSortedParameters() {
    Collections.sort(getParameters(), sorter);
    return this.parameters;
  }

  public ParameterList getAllParameters() {
    ParameterList result = new ParameterList();
    for (Parameter param : getParameters()) {
      result.add(param);
    }
    for (GroupOfParameters group : getGroupsOfParameters()) {
      result.addAll(group.getParameters());
    }
    return result;
  }

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

  /**
   * Is this WAComponent is a workflow?
   * @return true if this component satisfies the behavior of a workflow, that is to say if it
   * defines a workflow. False if it is a regular Silverpeas.
   * application.
   */
  public boolean isWorkflow() {
    return getBehaviors() != null &&
        getBehaviors().getBehavior().contains(ComponentBehavior.WORKFLOW);
  }

  /**
   * Is this WAComponent is a topic tracker?
   * @return true if this component satisfies the behavior of a topic tracker.
   */
  public boolean isTopicTracker() {
    return getBehaviors() != null &&
        getBehaviors().getBehavior().contains(ComponentBehavior.TOPIC_TRACKER);
  }

}