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

package org.silverpeas.web.pdc.vo;

import java.util.List;

/**
 * SearchTypeConfigurationVO is a search type configuration value object representation
 */
public class SearchTypeConfigurationVO {

  private int configId = -1;
  private String name = "";
  private List<String> components = null;
  private List<String> types = null;

  /**
   * Constructor
   * @param configId configuration identifier
   * @param name the search type configuration name
   * @param components : list of component name
   * @param types : list of object type name
   */
  public SearchTypeConfigurationVO(int configId, String name, List<String> components,
      List<String> types) {
    super();
    this.configId = configId;
    this.name = name;
    this.components = components;
    this.types = types;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the components
   */
  public List<String> getComponents() {
    return components;
  }

  /**
   * @param components the components to set
   */
  public void setComponents(List<String> components) {
    this.components = components;
  }

  /**
   * @return the types
   */
  public List<String> getTypes() {
    return types;
  }

  /**
   * @param types the types to set
   */
  public void setTypes(List<String> types) {
    this.types = types;
  }

  /**
   * @return the configId
   */
  public int getConfigId() {
    return configId;
  }

}
