package org.silverpeas.security.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.util.security.CipherKeyUpdateException;
import com.silverpeas.util.security.DefaultContentEncryptionService;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.util.crypto.CryptoException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

/**
 * A WEB resource representing the cipher key used in Silverpeas to encrypt and decrypt content.
 */
@Service
@Scope("request")
@Path(CipherKeyResource.WEB_PATH)
@Authorized
public class CipherKeyResource extends RESTWebService {

  /**
   * The path at which this REST-based web service is published, relative to the root URI of all
   * REST-based web services in Silverpeas.
   */
  public static final String WEB_PATH                   = "security/cipherkey";

  protected static final String INVALID_CIPHER_KEY        = "crypto.invalidKey";
  protected static final String CIPHER_KEY_UPDATE_FAILURE = "crypto.keyUpdateFailure";
  protected static final String CIPHER_RENEW_FAILURE      = "crypto.cipherRenewFailure";
  protected static final String CIPHER_KEY_IMPORT_SUCCESS = "crypto.importOk";

  @Inject
  private DefaultContentEncryptionService contentEncryptionService;

  @Override
  public String getComponentId() {
    return Admin.ADMIN_COMPONENT_ID;
  }

  /**
   * Sets the specified cipher key to encrypt and decrypt the content in Silverpeas.
   * </p>
   * If a cipher key was already defined, then this new one will replace it and in a such case, any
   * content that was encrypted with the previous key will be encrypted again but with the new key.
   * @param cipherKey the cipher key in hexadecimal.
   * @return the status of the cipher key setting.
   */
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  public Response setCipherKey(final String cipherKey) {
    ResourceLocator messages = new ResourceLocator("org.silverpeas.crypto.multilang.cryptoBundle",
        getUserPreferences().getLanguage());
    Response status;
    try {
      getContentEncryptionService().updateCipherKey(cipherKey);
      status = Response.status(Response.Status.OK).entity(CIPHER_KEY_IMPORT_SUCCESS).build();
    } catch (AssertionError e) {
      String message = formatMessage(messages.getString(INVALID_CIPHER_KEY), e.getMessage());
      status = Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    } catch (CipherKeyUpdateException e) {
      String message = formatMessage(messages.getString(CIPHER_KEY_UPDATE_FAILURE), e.getMessage());
      status = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    } catch (CryptoException e) {
      String message = formatMessage(messages.getString(CIPHER_RENEW_FAILURE), e.getMessage());
      status = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }
    return status;
  }

  private DefaultContentEncryptionService getContentEncryptionService() {
    return contentEncryptionService;
  }

  private static String formatMessage(String pattern, String value) {
    return MessageFormat.format(pattern, value);
  }
}