/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.admin.space.quota.process.check.exception;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.quota.model.Quota;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class DataStorageQuotaException extends RuntimeException {
  private static final long serialVersionUID = 1663450786546676632L;
  private final Quota quota;
  private final SpaceInst space;
  private final ComponentInstLight fromComponent;
  private String language;

  /**
   * Default constructor
   * @param quota
   * @param space
   * @param fromComponent
   */
  public DataStorageQuotaException(final Quota quota, final SpaceInst space,
      final ComponentInstLight fromComponent) {
    this.quota = quota;
    this.space = space;
    this.fromComponent = (fromComponent != null) ? fromComponent : new ComponentInstLight();
  }

  /**
   * @return the quota
   */
  public Quota getQuota() {
    return quota;
  }

  /**
   * @return the space
   */
  public SpaceInst getSpace() {
    return space;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * @return the fromComponent
   */
  public ComponentInstLight getFromComponent() {
    return fromComponent;
  }

  /**
   * @return the fromComponentURL
   */
  @SuppressWarnings("UnusedDeclaration")
  public String getFromComponentUrl() {
    return (isDefined(fromComponent.getId())) ? URLUtil.getApplicationURL() +
        URLUtil.getURL(fromComponent.getName(), null, fromComponent.getId()) + "Main" : "";
  }
}
