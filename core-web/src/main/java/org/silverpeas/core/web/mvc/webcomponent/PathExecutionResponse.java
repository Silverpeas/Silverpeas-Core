/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import javax.ws.rs.Produces;
import java.util.Optional;

/**
 * This class represents the response of a {@link Path} processing.
 * @author Yohann Chastagnier
 */
class PathExecutionResponse {
  private String produces = null;
  private Navigation navigation = null;

  /**
   * Initialize an instance from a content production.
   * @param produces the production specification.
   * @return the initialized instance.
   */
  static PathExecutionResponse hasProduced(Produces produces) {
    PathExecutionResponse instance = new PathExecutionResponse();
    instance.produces = "PRODUCES_" + produces.value()[0];
    return instance;
  }

  /**
   * Initialize an instance from a navigation directive.
   * @param navigation the navigation directive.
   * @return the initialized instance.
   */
  static PathExecutionResponse navigateTo(Navigation navigation) {
    PathExecutionResponse instance = new PathExecutionResponse();
    instance.navigation = navigation;
    return instance;
  }

  /**
   * Hidden constructor.
   */
  private PathExecutionResponse() {
  }

  public Optional<String> produces() {
    return Optional.ofNullable(produces);
  }

  public Optional<Navigation> navigation() {
    return Optional.ofNullable(navigation);
  }
}
