/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.web.jobdomain;

import jakarta.ws.rs.FormParam;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.http.UnescapeHtml;

/**
 * The data of a group of users as they are sent by the form of creation, of modification or of
 * import of a group. The values are taken as typed by the user: they aren't HTML encoded, as it is
 * done on their rendering, and the synchronization rule keeps its operators. To get a loaded
 * container, use {@link RequestParameterDecoder#decode(HttpRequest, Class)}.
 *
 * @author mmoquillon
 */
public class GroupRequestData {

  @FormParam("groupName")
  @UnescapeHtml
  private String name;

  @FormParam("groupDescription")
  @UnescapeHtml
  private String description;

  @FormParam("groupRule")
  @UnescapeHtml
  private String rule;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getRule() {
    return rule;
  }
}
