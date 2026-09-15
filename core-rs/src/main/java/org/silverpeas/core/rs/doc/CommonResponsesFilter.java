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
package org.silverpeas.core.rs.doc;

import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Completes the generated documentation of the REST API with the responses that any endpoint can
 * answer, whatever the web resource it belongs to and whether its access is protected or not.
 * <p>
 * Such a response cannot be brought by a meta-annotation of the web resources. At class level,
 * Swagger stops at the first meta-annotation declaring responses instead of merging them all, so a
 * second one would silently discard the responses of
 * {@link org.silverpeas.core.web.rs.annotation.Authenticated} and of
 * {@link org.silverpeas.core.web.rs.annotation.Authorized}. Hence this filter, applied once the
 * whole specification has been figured out.
 * </p>
 * <p>
 * The filter takes part in nothing but the generation of the documentation; it plays no role at
 * runtime. It is declared by the filterClass parameter of the Swagger Maven plugin.
 * </p>
 * @author Miguel Moquillon
 */
public class CommonResponsesFilter extends AbstractSpecFilter {

  private static final String SERVICE_UNAVAILABLE = "503";
  private static final String SERVICE_UNAVAILABLE_DESCRIPTION =
      "The request cannot be processed for now: the service is unavailable.";

  @Override
  public Optional<Operation> filterOperation(final Operation operation,
      final ApiDescription api, final Map<String, List<String>> params,
      final Map<String, String> cookies, final Map<String, List<String>> headers) {
    // an endpoint declaring its own 503 keeps its wording
    operation.getResponses().computeIfAbsent(SERVICE_UNAVAILABLE,
        code -> new ApiResponse().description(SERVICE_UNAVAILABLE_DESCRIPTION));
    return Optional.of(operation);
  }

  @Override
  public boolean isOpenAPI31Filter() {
    return true;
  }
}
