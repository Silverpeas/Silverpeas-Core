package org.silverpeas.core.webapi.security;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.security.encryption.cipher.CryptoException;
import org.silverpeas.core.security.encryption.CipherKeyUpdateException;
import org.silverpeas.core.security.encryption.ContentEncryptionService;
import org.silverpeas.core.security.encryption.DefaultContentEncryptionService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.silverpeas.core.util.logging.SilverLogger.*;

/**
 * A WEB resource representing the cipher key used in Silverpeas to encrypt and decrypt content.
 */
@Service
@RequestScoped
@Path(CipherKeyResource.WEB_PATH)
@Authorized
public class CipherKeyResource extends RESTWebService {

  /**
   * The path at which this REST-based web service is published, relative to the root URI of all
   * REST-based web services in Silverpeas.
   */
  public static final String WEB_PATH = "security/cipherkey";
  protected static final String INVALID_CIPHER_KEY = "crypto.invalidKey";
  protected static final String CIPHER_KEY_UPDATE_FAILURE = "crypto.keyUpdateFailure";
  protected static final String CIPHER_RENEW_FAILURE = "crypto.cipherRenewFailure";
  protected static final String CIPHER_KEY_IMPORT_SUCCESS = "crypto.importOk";
  @Inject
  private DefaultContentEncryptionService contentEncryptionService;

  @Override
  public String getComponentId() {
    return Administration.ADMIN_COMPONENT_ID;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getToto() {
    return Response.ok("Toto").build();
  }

  /**
   * Sets the specified cipher key to encrypt and decrypt the content in Silverpeas.
   * </p>
   * If a cipher key was already defined, then this new one will replace it and in a such case, any
   * content that was encrypted with the previous key will be encrypted again but with the new key.
   *
   * @param cipherKey the cipher key in hexadecimal.
   * @return the status of the cipher key setting.
   */
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response setCipherKey(final String cipherKey) {
    LocalizationBundle messages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.crypto.multilang.cryptoBundle",
        getUserPreferences().getLanguage());
    Response status;
    try {
      getContentEncryptionService().updateCipherKey(cipherKey);
      status = Response.ok(messages.getString(CIPHER_KEY_IMPORT_SUCCESS)).build();
    } catch (AssertionError e) {
      logError(e);
      String message = formatMessage(messages.getString(INVALID_CIPHER_KEY), e.getMessage());
      status = Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    } catch (CipherKeyUpdateException e) {
      logError(e);
      String message = formatMessage(messages.getString(CIPHER_KEY_UPDATE_FAILURE), e.getMessage());
      status = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    } catch (CryptoException e) {
      logError(e);
      String message = formatMessage(messages.getString(CIPHER_RENEW_FAILURE), e.getMessage());
      status = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }
    return status;
  }

  private ContentEncryptionService getContentEncryptionService() {
    return contentEncryptionService;
  }

  private static String formatMessage(String pattern, String value) {
    String msg = pattern;
    if (!pattern.endsWith("\n")) {
      msg += " ";
    }
    return msg + value;
  }

  private static void logError(final Throwable ex) {
    getLogger(CipherKeyResource.class).error(ex.getMessage(), ex);
  }
}