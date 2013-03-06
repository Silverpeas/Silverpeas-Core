package com.silverpeas.util.security;

import org.silverpeas.util.crypto.CryptoException;

/**
 * Exception that is thrown when an error has occurred while updating a cipher key.
 */
public class CipherKeyUpdateException extends CryptoException {
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
