package org.silverpeas.core.webapi.twofactor;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import java.util.List;

import static org.silverpeas.core.webapi.twofactor.TwoFactorAuthenticationResourceURIs.BASE_URI;
import static org.silverpeas.core.webapi.twofactor.TwoFactorAuthenticationResourceURIs.RECOVERY_CODES_URI_PART;

/** REST resource for the recovery codes of the currently authenticated user. */
@WebService
@Path(BASE_URI)
@Authenticated
public class TwoFactorAuthenticationResource extends RESTWebService {

  @Inject
  private TwoFactorAuthenticationService service;

  @Override
  protected String getResourceBasePath() {
    return BASE_URI;
  }

  /**
   * Generates a new set of recovery codes for the current authenticated user.
   * The generated codes are returned once; only their hashes are persisted.
   */
  @POST
  @Path(RECOVERY_CODES_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> generateRecoveryCodes() {
    return service.generateRecoveryCodes(getUser().getId());
  }
}
