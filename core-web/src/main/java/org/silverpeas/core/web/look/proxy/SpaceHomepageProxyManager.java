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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.look.proxy;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.ServiceProvider;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;
import java.util.UUID;

import static org.silverpeas.core.cache.service.CacheAccessorProvider.getSessionCacheAccessor;

/**
 * The space homepage proxy allows to set a {@link SpaceHomepageProxy} into context of a request.
 * <p>
 *   The method {@link #getProxyOf(SpaceInst)} allows to get the instance of
 *   {@link SpaceHomepageProxy} which allows to override the space homepage displaying.
 * </p>
 * <p>
 *   If nothing has been specifically set into provided {@link SpaceHomepageProxy}, then default
 *   space homepage behavior is performed.
 * </p>
 * <p>
 *   This manager is request scoped, but it is able to retrieve proxy specifications on a request
 *   redirecting by using {@link #setParameterForUrlRedirect(String)} before perform the redirection.
 * </p>
 * @author silveryocha
 */
@RequestScoped
public class SpaceHomepageProxyManager {

  @Inject
  private HttpServletRequest request;

  private static final String URL_PARAM_NAME = "spaceHomepageProxy";
  private SpaceHomepageProxy spaceHomePageProxy;

  public static SpaceHomepageProxyManager get() {
    return ServiceProvider.getSingleton(SpaceHomepageProxyManager.class);
  }

  /**
   * Sets on the given URL a parameter that permits to retrieve the proxy specification on
   * request redirect.
   * @param url a string url to handle.
   * @return the completed url if proxy is effective.
   */
  public String setParameterForUrlRedirect(final String url) {
    if (spaceHomePageProxy != null && spaceHomePageProxy.isEffective()) {
      final String cacheKey = UUID.randomUUID().toString();
      getSessionCacheAccessor().getCache().put(cacheKey, spaceHomePageProxy);
      return UriBuilder.fromUri(url).queryParam(URL_PARAM_NAME, cacheKey).build().toString();
    }
    return url;
  }

  /**
   * Gets a {@link SpaceHomepageProxy} instance linked to given space.
   * @param spaceId a string representing the identifier of the aimed space.
   * @return a {@link SpaceHomepageProxy} instance.
   */
  public SpaceHomepageProxy getProxyBySpaceId(final String spaceId) {
    return getProxyOf(OrganizationController.get().getSpaceInstById(spaceId));
  }

  /**
   * Gets a {@link SpaceHomepageProxy} instance linked to given space.
   * @param space the {@link SpaceInst} of aimed space.
   * @return a {@link SpaceHomepageProxy} instance.
   */
  public SpaceHomepageProxy getProxyOf(final SpaceInst space) {
    if (spaceHomePageProxy == null) {
      spaceHomePageProxy = Optional.ofNullable(request.getParameter(URL_PARAM_NAME))
          .map(k -> getSessionCacheAccessor().getCache().remove(k, SpaceHomepageProxy.class))
          .orElse(null);
    }
    if (spaceHomePageProxy == null || !spaceHomePageProxy.getSpace().equals(space)) {
      spaceHomePageProxy = new SpaceHomepageProxy(space);
    }
    return spaceHomePageProxy;
  }
}
