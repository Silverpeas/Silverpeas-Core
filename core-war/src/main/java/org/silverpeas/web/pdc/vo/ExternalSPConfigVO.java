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
package org.silverpeas.web.pdc.vo;

import java.util.List;

/**
 * ExternalSilverpeasConfigVO represents an external silverpeas server value object.
 * Example:
 * <pre>{@code
 * external.search.server.1.name=INPI
 * external.search.server.1.data.path=D:\\silverpeas\\silverpeas_INPI_prod\\data
 * external.search.server.1.component.filters=kmelia
 * external.search.server.1.url=http://pegase.na.inpi/silverpeas
 * }</pre>
 */
public class ExternalSPConfigVO {

  private final String name;
  private final int configOrder;
  private final String dataPath;
  private final List<String> components;
  private final String url;

  public ExternalSPConfigVO(String name, int configOrder, String dataPath, List<String> components,
      String url) {
    super();
    this.name = name;
    this.configOrder = configOrder;
    this.dataPath = dataPath;
    this.components = components;
    this.url = url;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the dataPath
   */
  public String getDataPath() {
    return dataPath;
  }

  /**
   * @return the components
   */
  public List<String> getComponents() {
    return components;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @return the configOrder
   */
  public int getConfigOrder() {
    return configOrder;
  }

}
