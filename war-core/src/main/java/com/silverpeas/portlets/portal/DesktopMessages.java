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

package com.silverpeas.portlets.portal;

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * DesktopMessages is used to get the localized messages from
 * DesktopMessages.properties
 */
public class DesktopMessages {

  public DesktopMessages() {
  }

  private static final String RESOURCE_BASE = "com.silverpeas.portlets.multilang.portletsBundle";

  private static ResourceBundle rb;

  public static void init(HttpServletRequest request) {
    rb = PropertyResourceBundle.getBundle(RESOURCE_BASE, request.getLocale());
  }

  public static String getLocalizedString(String key) {
    return rb.getString(key);
  }

  public static String getLocalizedString(String key, Object[] tokens) {
    String msg = getLocalizedString(key);

    if (tokens != null && tokens.length > 0) {
      MessageFormat mf = new MessageFormat("");
      mf.setLocale(rb.getLocale());
      mf.applyPattern(msg);
      return mf.format(tokens);
    } else {
      return msg;
    }
  }
}
