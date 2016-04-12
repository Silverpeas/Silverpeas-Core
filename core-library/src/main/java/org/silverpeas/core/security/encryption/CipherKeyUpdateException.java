package org.silverpeas.core.security.encryption;

import org.silverpeas.core.security.encryption.cipher.CryptoException;

/**
 * Exception that is thrown when an error has occurred while updating a cipher key.
 */
public class CipherKeyUpdateException extends CryptoException {

  private static final long serialVersionUID = -6970908432032192938L;

  public CipherKeyUpdateException(final Throwable cause) {
    super(cause);
  }

  public CipherKeyUpdateException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public CipherKeyUpdateException(final String message) {
    super(message);
  }
}
