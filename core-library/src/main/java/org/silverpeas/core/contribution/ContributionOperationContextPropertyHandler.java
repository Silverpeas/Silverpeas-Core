/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution;

import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;

/**
 * Handler of a property of the context of an operation implying a contribution.
 * <p>
 * The contributions are the core of the information handled by the users. As such, they are implied
 * in a lot of operations like a document saving or a user notification. Each operation in
 * Silverpeas can carry a context that can give the operator some data on how to perform this
 * operation. Properties can be defined within the scope of such a context in order to direct the
 * operation processing in different ways: it is the goal of the {@link
 * ContributionOperationContextPropertyHandler} to set such properties and to give access to them to
 * the operator that performs some of the operations implying the contributions.
 * </p>
 * @author mmoquillon
 */
public interface ContributionOperationContextPropertyHandler {

  /**
   * Parses the specified incoming HTTP request for a given property of the context of the operation
   * triggered or asked by the specified request.
   * @param request an HTTP request.
   */
  void parseForProperty(final HttpServletRequest request);

  /**
   * Parses the specified HTTP request for properties additional to the context of the operation
   * triggered or asked by the specified request. It delegates the parsing to all the handlers, each
   * of them about a given and different context property.
   * @param request an HTTP request from which an operation implied a contribution is going to be
   * triggered.
   */
  static void parseRequest(final HttpServletRequest request) {
    ServiceProvider.getAllServices(ContributionOperationContextPropertyHandler.class)
        .forEach(h -> h.parseForProperty(request));
  }
}
