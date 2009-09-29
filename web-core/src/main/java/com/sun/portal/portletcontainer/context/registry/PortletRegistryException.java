/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.context.registry;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A <code>PortletRegistryException</code> is thrown when there are errors
 * related to registry read/write.
 */

public class PortletRegistryException extends Exception {

  public static final String RESOURCE_BASE = "com.silverpeas.portlets.multilang.PortletRegistry";

  static Locale locale = Locale.getDefault();

  protected Throwable originalException = null;
  protected String key = null;
  protected Object[] tokens = null;

  /**
   * Constructs an instance of the <code>PortletRegistryException</code> class.
   * 
   * @param key
   *          key string to index into resource bundle to retieve localized
   *          message
   */
  public PortletRegistryException(String key) {
    super(key);
    this.key = key;
  }

  /**
   * Constructs an instance of the <code>PortletRegistryException</code> class.
   * 
   * @param key
   *          key string to index into resource bundle to retieve localized
   *          message
   * @param tokens
   *          array of tokens to be used by the exception message
   */
  public PortletRegistryException(String key, Object[] tokens) {
    super(key);
    this.key = key;
    this.tokens = tokens;
  }

  /**
   * Constructs an instance of the <code>PortletRegistryException</code> class.
   * 
   * @param key
   *          key string to index into resource bundle to retieve localized
   *          message
   * @param t
   *          Throwable object provided by the object which is throwing
   */
  public PortletRegistryException(String key, Throwable t) {
    super(key);
    originalException = t;
    this.key = key;
  }

  /**
   * Constructs an instance of the <code>PortletRegistryException</code> class.
   * 
   * @param key
   *          key string to index into resource bundle to retieve localized
   *          message
   * @param t
   *          Throwable object provided by the object which is throwing
   * @param tokens
   *          array of tokens to be used by the exception message
   */
  public PortletRegistryException(String key, Throwable t, Object[] tokens) {
    super(key);
    originalException = t;
    this.key = key;
    this.tokens = tokens;
  }

  /**
   * Constructs an instance of the <code>PortletRegistryException</code> class.
   * 
   * @param t
   *          Throwable object provided by the object which is throwing the
   *          exception
   */
  public PortletRegistryException(Throwable t) {
    super(t);
    this.key = "";
    originalException = t;
  }

  public static void setLocale(Locale loc) {
    locale = loc;
  }

  public String getMessage() {
    // non-localized resource bundle
    ResourceBundle rb = PropertyResourceBundle.getBundle(RESOURCE_BASE, Locale
        .getDefault());
    return getMessageFromRB(rb, key, tokens);
  }

  public Throwable getWrapped() {
    return originalException;
  }

  public String getWrappedMessage() {
    String msg = null;
    if (originalException != null) {
      msg = originalException.getMessage();
    } else {
      msg = null;
    }
    return msg;
  }

  public String getLocalizedMessage() {
    // localized resource bundle
    ResourceBundle rb = PropertyResourceBundle.getBundle(RESOURCE_BASE, locale);
    String msg = null;
    try {
      msg = getMessageFromRB(rb, key, tokens);
    } catch (MissingResourceException mrex) {
      msg = key;
    }
    return msg;
  }

  private String getMessageFromRB(ResourceBundle rb, String key, Object[] tokens)
      throws MissingResourceException {

    String message = null;
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

  public void printStackTrace() {
    if (originalException != null) {
      originalException.printStackTrace();
    } else {
      super.printStackTrace();
    }
  }

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
