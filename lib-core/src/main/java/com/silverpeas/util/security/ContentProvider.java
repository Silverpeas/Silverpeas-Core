package com.silverpeas.util.security;

import org.silverpeas.util.crypto.CryptoException;

import java.util.Map;

/**
 * A provider of contents to be encrypted or decrypted.
 */
public interface ContentProvider {

  /**
   * Gets the next content to update either by encrypting it or be decrypting it. The result of the
   * update will be passed to the {@link #setUpdatedContent(java.util.Map)} method.
   * @return the next content to update in the form of a Map instance in which each entry represents
   * a field or a property of the content.
   */
  Map<String, String> getContent();

  /**
   * Is there another content to update? This method is called by the encryption service each time
   * before asking the next content to encrypt it with the {@link #getContent()} method.
   * @return true if there is another content to update. If true, the encryption service will can
   * again the {@link #getContent()} method.
   */
  boolean hasNextContent();

  /**
   * Sets the content, that was get by the {@link #getContent()} method call, updated by the
   * encryption service.
   * @param updatedContent the content updated by the encryption service in the form of a Map
   * instance. Each entry in the Map represents a field or a property of the content as it was
   * passed by the {@link #getContent()} method.
   */
  void setUpdatedContent(Map<String, String> updatedContent);

  /**
   * An error occurred while updating a content. This method is invoked by the encryption service
   * when an error occurs.
   * @param content the content with which the error occurred.
   * @param ex the exception raised at the error.
   */
  void onError(Map<String, String> content, CryptoException ex);
}
