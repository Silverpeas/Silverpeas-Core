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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.calendar;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.time.ZoneId;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractCalendarResource extends RESTWebService {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @QueryParam("zoneid")
  private String zoneId;

  @QueryParam("editionMode")
  private Boolean editionMode;

  @Inject
  private CalendarResourceURIs uri;

  /**
   * Gets the zoneId into which dates must be set.
   * @return a {@link ZoneId} instance if zoneid parameter has been set, null otherwise.
   */
  public ZoneId getZoneId() {
    return StringUtil.isDefined(zoneId) ? ZoneId.of(zoneId) : null;
  }

  @Override
  protected String getResourceBasePath() {
    return CalendarResourceURIs.CALENDAR_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  public CalendarResourceURIs uri() {
    return uri;
  }

  boolean isEditionMode() {
    return editionMode != null ? editionMode : false;
  }
}
