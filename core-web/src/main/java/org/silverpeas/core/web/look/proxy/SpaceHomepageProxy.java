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

import org.silverpeas.core.admin.space.SpaceInst;

import static java.util.Optional.ofNullable;

/**
 * The space homepage proxy allows resources different from a space to override the space home
 * page displaying.
 * <p>
 *   The class provides same space homepage methods than {@link SpaceInst} class to manage the
 *   space homepage:
 *   <ul>
 *     <li>{@link SpaceInst#getFirstPageType()}</li>
 *     <li>{@link SpaceInst#getFirstPageExtraParam()} ()}</li>
 *   </ul>
 * </p>
 * @author silveryocha
 */
public class SpaceHomepageProxy {

  private final SpaceInst space;
  private Integer firstPageType;
  private String firstPageExtraParam;
  private Widget thinWidget;

  protected SpaceHomepageProxy(final SpaceInst space) {
    this.space = space;
  }

  /**
   * Gets the space aimed by the proxy.
   * @return a {@link SpaceInst} instance.
   */
  public SpaceInst getSpace() {
    return space;
  }

  /**
   * Indicates if the proxy is enabled or if default space homepage behavior is applied.
   * @return true if enabled, false otherwise (default space homepage behavior).
   */
  public boolean isEffective() {
    return firstPageType != null;
  }

  /**
   * Gets the space first page type of the proxy.
   * @return primitive integer representing the first page type.
   */
  public int getFirstPageType() {
    return ofNullable(firstPageType).orElse(space.getFirstPageType());
  }

  /**
   * Sets the space first page type for proxy.
   * @param firstPageType type of the homepage of the space.
   */
  public void setFirstPageType(final Integer firstPageType) {
    this.firstPageType = firstPageType;
  }

  /**
   * Gets the space first page extra parameter of the proxy.
   * @return a string containing parameters associated to the type given by method
   * {@link #getFirstPageType()}.
   */
  public String getFirstPageExtraParam() {
    return ofNullable(firstPageType)
        .map(t -> this.firstPageExtraParam)
        .orElse(space.getFirstPageExtraParam());
  }

  /**
   * Sets the space first page extra parameter for proxy.
   * @param firstPageExtraParam parameters to pass to the homepage of the space.
   */
  public void setFirstPageExtraParam(final String firstPageExtraParam) {
    this.firstPageExtraParam = firstPageExtraParam;
  }

  /**
   * Gets a thin widget to display into space homepage.
   * @return a {@link Widget} instance or null if none.
   */
  public Widget getThinWidget() {
    return thinWidget;
  }

  /**
   * Sets a thin widget to display into space homepage.
   * @param thinWidget a {@link Widget} instance.
   */
  public void setThinWidget(final Widget thinWidget) {
    this.thinWidget = thinWidget;
  }

  public static class Widget {

    private String title;
    private String content;

    public String getTitle() {
      return title;
    }

    public void setTitle(final String title) {
      this.title = title;
    }

    public String getContent() {
      return content;
    }

    public void setContent(final String content) {
      this.content = content;
    }
  }
}
