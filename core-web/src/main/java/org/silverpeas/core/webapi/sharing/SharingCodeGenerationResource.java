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
package org.silverpeas.core.webapi.sharing;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.SecretGenerator;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;
import org.silverpeas.core.webapi.password.PasswordEntity;

/** Generates sharing codes without creating or changing sharing tickets. */
@WebService
@Authenticated
@Path("secrets/sharing-code")
public class SharingCodeGenerationResource extends RESTWebService {
  @Override
  protected String getResourceBasePath() {
    return "secrets/sharing-code";
  }

  @Override
  public String getComponentId() {
    return "";
  }

  /**
   * Generates a code with the shared server-side engine.
   * @return a non-cacheable response containing a fresh sharing code.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Generates a security code for a sharing link.")
  public Response generate() {
    return Response.ok(PasswordEntity.createFrom(SecretGenerator.sharingCode()))
        .header("Cache-Control", "no-store")
        .header("Pragma", "no-cache")
        .build();
  }
}
