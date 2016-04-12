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
package org.silverpeas.core.web.look;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

public class TickerItem {

  private PublicationDetail publication;

  public TickerItem(PublicationDetail pub) {
    publication = pub;
  }

  public String getName(String lang) {
    return publication.getName(lang);
  }

  public String getDescription(String lang) {
    return publication.getDescription(lang);
  }

  public String getHour() {
    if (!"00:00".equals(publication.getBeginHour())) {
      return publication.getBeginHour();
    }
    return "";
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getId());
  }

}
