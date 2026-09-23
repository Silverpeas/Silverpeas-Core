package org.silverpeas.core.webapi.twofactor;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import java.util.List;

import static org.silverpeas.core.webapi.twofactor.TwoFactorAuthenticationResourceURIs.QR_CODE_URI_PART;

import static org.silverpeas.core.webapi.twofactor.TwoFactorAuthenticationResourceURIs.BASE_URI;
import static org.silverpeas.core.webapi.twofactor.TwoFactorAuthenticationResourceURIs.RECOVERY_CODES_URI_PART;

/** REST resource for the recovery codes of the currently authenticated user. */
@WebService
@Path(BASE_URI)
@Authenticated
public class TwoFactorAuthenticationResource extends RESTWebService {

  @Inject
  private TwoFactorAuthenticationService service;

  @Inject
  private TotpService totpService;

  @Inject
  private QrCodeGenerator qrCodeGenerator;

  @Override
  protected String getResourceBasePath() {
    return BASE_URI;
  }

  @Override
  public String getComponentId() {
    return "";
  }

  /**
   * Generates the QR code for the current user's TOTP authenticator enrollment.
   * The secret itself is never exposed by this endpoint; the QR code contains the
   * same otpauth URI that is displayed during enrollment.
   */
  @GET
  @Path(QR_CODE_URI_PART)
  @Produces("image/png")
  public Response getQrCode() {
    final int userId = Integer.parseInt(getUser().getId());
    final TwoFactorAuthentication authentication = service.getAuthentication(userId)
        .orElseThrow(() -> new IllegalStateException("Two-factor authentication is not configured"));
    if (!authentication.isPending()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    final String uri = totpService.buildOtpAuthUri(authentication.getSecret());
    final byte[] png = qrCodeGenerator.generate(uri, 256);
    return Response.ok(png, "image/png")
        .header("Cache-Control", "no-store, no-cache, must-revalidate")
        .header("Pragma", "no-cache")
        .build();
  }

  /**
   * Generates a new set of recovery codes for the current authenticated user.
   * The generated codes are returned once; only their hashes are persisted.
   */
  @POST
  @Path(RECOVERY_CODES_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> generateRecoveryCodes() {
    return service.generateRecoveryCodes(Integer.parseInt(getUser().getId()));
  }
}
