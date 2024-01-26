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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.mvc.route;

import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;

import static java.util.Optional.ofNullable;

/**
 * This abstraction is dedicated to administration implementations of
 * {@link ComponentRequestRouter}.
 * <p>
 * It mainly centralizes useful common processes for administration context, as the management of
 * forbidden access.
 * </p>
 * @author silveryocha
 */
public abstract class AdminComponentRequestRouter<T extends AbstractAdminComponentSessionController>
    extends ComponentRequestRouter<T> {
  private static final long serialVersionUID = -1845547120139170876L;

  /**
   * This method has to be implemented by the admin component request Router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp", when accessing
   * "/RjobManager/jsp/Main.jsp")
   * @param componentSC The component Session Controller, build and initialised.
   * @param request The entering request. The request Router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/RjobManager/jsp/Main.jsp?flag=read")
   */
  public abstract String getAdminDestination(String function, T componentSC, HttpRequest request);

  @Override
  public String getDestination(final String function, final T componentSC,
      final HttpRequest request) {
    String destination = StringUtil.EMPTY;
    final Mutable<ForbiddenRuntimeException> forbidden = Mutable.empty();
    try {
      componentSC.checkAccessGranted();
      try {
        destination = getAdminDestination(function, componentSC, request);
      } finally {
        ofNullable(request.getAttribute("javax.servlet.jsp.jspException"))
            .filter(ForbiddenRuntimeException.class::isInstance)
            .map(ForbiddenRuntimeException.class::cast)
            .stream()
            .forEach(forbidden::set);
      }
    } catch (final ForbiddenRuntimeException e) {
      forbidden.set(e);
    }
    if (forbidden.isPresent()) {
      throwHttpForbiddenError(forbidden.get().getMessage());
    }
    return destination;
  }
}
