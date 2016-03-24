/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.context.registry;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A
 * <code>PortletRegistryException</code> is thrown when there are errors related to registry
 * read/write.
 */
public class PortletRegistryException extends Exception {

  public static final String RESOURCE_BASE = "org.silverpeas.portlets.multilang.PortletRegistry";
  static Locale locale = Locale.getDefault();
  private static final long serialVersionUID = 7738268731991950804L;
  protected Throwable originalException = null;
  protected String key = null;
  protected Object[] tokens = null;

  /**
   * Constructs an instance of the
   * <code>PortletRegistryException</code> class.
   *
   * @param key key string to index into resource bundle to retieve localized message
   */
  public PortletRegistryException(String key) {
    super(key);
    this.key = key;
  }

  /**
   * Constructs an instance of the
   * <code>PortletRegistryException</code> class.
   *
   * @param key key string to index into resource bundle to retieve localized message
   * @param tokens array of tokens to be used by the exception message
   */
  public PortletRegistryException(String key, Object[] tokens) {
    super(key);
    this.key = key;
    this.tokens = (tokens != null ? tokens.clone() : null);
  }

  /**
   * Constructs an instance of the
   * <code>PortletRegistryException</code> class.
   *
   * @param key key string to index into resource bundle to retieve localized message
   * @param t Throwable object provided by the object which is throwing
   */
  public PortletRegistryException(String key, Throwable t) {
    super(key);
    originalException = t;
    this.key = key;
  }

  /**
   * Constructs an instance of the
   * <code>PortletRegistryException</code> class.
   *
   * @param key key string to index into resource bundle to retieve localized message
   * @param t Throwable object provided by the object which is throwing
   * @param tokens array of tokens to be used by the exception message
   */
  public PortletRegistryException(String key, Throwable t, Object[] tokens) {
    super(key);
    originalException = t;
    this.key = key;
    this.tokens = (tokens != null ? tokens.clone() : null);
  }

  /**
   * Constructs an instance of the
   * <code>PortletRegistryException</code> class.
   *
   * @param t Throwable object provided by the object which is throwing the exception
   */
  public PortletRegistryException(Throwable t) {
    super(t);
    this.key = "";
    originalException = t;
  }

  public static void setLocale(Locale loc) {
    locale = loc;
  }

  @Override
  public String getMessage() {
    // non-localized resource bundle
    LocalizationBundle rb = ResourceLocator.getLocalizationBundle(RESOURCE_BASE);
    return getMessageFromRB(rb, key, tokens);
  }

  public Throwable getWrapped() {
    return originalException;
  }

  public String getWrappedMessage() {
    String msg;
    if (originalException != null) {
      msg = originalException.getMessage();
    } else {
      msg = null;
    }
    return msg;
  }

  @Override
  public String getLocalizedMessage() {
    // localized resource bundle
    LocalizationBundle rb =
        ResourceLocator.getLocalizationBundle(RESOURCE_BASE, locale.getLanguage());
    String msg;
    try {
      msg = getMessageFromRB(rb, key, tokens);
    } catch (MissingResourceException mrex) {
      msg = key;
    }
    return msg;
  }

  private String getMessageFromRB(ResourceBundle rb, String key, Object[] tokens)
      throws MissingResourceException {

    String message;
    try {
      String msg = rb.getString(key);
      if (tokens != null && tokens.length > 0) {
        java.text.MessageFormat mf = new java.text.MessageFormat("");
        mf.setLocale(rb.getLocale());
        mf.applyPattern(msg);
        message = mf.format(tokens);
      } else {
        message = msg;
      }
    } catch (MissingResourceException mrex) {
      message = key;
    }
    return message;
  }

  @Override
  public void printStackTrace() {
    if (originalException != null) {
      originalException.printStackTrace();
    } else {
      super.printStackTrace();
    }
  }

  @Override
  public void printStackTrace(PrintWriter pw) {
    if (originalException != null) {
      originalException.printStackTrace(pw);
    } else {
      super.printStackTrace(pw);
    }
  }

  public String getKey() {
    return key;
  }

  public Object[] getTokens() {
    return tokens;
  }
}
