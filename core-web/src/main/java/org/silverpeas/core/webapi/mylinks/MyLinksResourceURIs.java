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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.SilverpeasWebResource;

import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.lang.String.valueOf;

/**
 * Base URIs from which the REST-based ressources representing MyLinks entities are defined.
 * @author silveryocha
 */
@Bean
@Singleton
public class MyLinksResourceURIs {

  public static final String MYLINKS_BASE_URI = "mylinks";
  public static final String MYLINKS_CATEGORY_URI_PART = "categories";

  public static MyLinksResourceURIs get() {
    return ServiceProvider.getSingleton(MyLinksResourceURIs.class);
  }

  /**
   * Centralizes the build of a category URI.
   * @param category a category.
   * @return the computed URI.
   */
  public URI ofCategory(final CategoryDetail category) {
    if (category == null || category.getId() == -1) {
      return null;
    }
    return getBase().path(MYLINKS_CATEGORY_URI_PART).path(valueOf(category.getId())).build();
  }

  /**
   * Centralizes the build of a link URI.
   * @param link a link.
   * @return the computed URI.
   */
  public URI ofLink(final LinkDetail link) {
    if (link == null || link.getLinkId() == -1) {
      return null;
    }
    return getBase().path(valueOf(link.getLinkId())).build();
  }

  private UriBuilder getBase() {
    return SilverpeasWebResource.getBasePathBuilder().path(MYLINKS_BASE_URI);
  }

  private MyLinksResourceURIs() {
  }
}
