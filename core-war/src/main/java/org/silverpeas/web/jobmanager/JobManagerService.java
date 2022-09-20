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
package org.silverpeas.web.jobmanager;

import java.io.Serializable;

public class JobManagerService implements Serializable {

  private final String id;
  private final String label;
  private final int level;
  private final String url;
  private final String[] idSubServices;
  private boolean isActive;
  public static final int LEVEL_SERVICE = 0;
  public static final int LEVEL_OPERATION = 1;

 public JobManagerService(String id, String label, int level, String url,
      String[] idSubServices, boolean isActive) {
    this.id = id;
    this.label = label;
    this.level = level;
    this.url = url;
    this.idSubServices = (idSubServices != null ? idSubServices.clone() : null);
    this.isActive = isActive;
  }

  public void setActive(boolean a) {
    isActive = a;
  }

  public String getLabel() {
    return label;
  }

  public boolean isActive() {
    return isActive;
  }

  public int getLevel() {
    return level;
  }

  public String[] getIdSubServices() {
    return idSubServices;
  }

  public String getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getDefaultIdSubService() {
    return idSubServices[0];
  }
}
