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
package com.sun.portal.portletcontainer.context;

import javax.servlet.ServletContext;

/**
 * This class provides access to the ServletContext using ThreadLocal class It is required by WSRP
 * caching implementaion.
 */
public class ServletContextThreadLocalizer {

  private static ThreadLocal servletContextThreadLocal = new ThreadLocal();

  private ServletContextThreadLocalizer() {
    // nothing, cannot be called
  }

  /**
   * Returns ServletContext
   * @return
   */
  public static ServletContext get() {
    ServletContext sc = (ServletContext) servletContextThreadLocal.get();
    if (sc == null) {
      throw new Error(
          "ServletContextThreadLocalizer.get(): no thread localResourceLocator set for this thread");
    }

    return sc;
  }

  /**
   * Sets ServletContext in the ThreadLocal variable
   * @param sc ServletContextContext
   */
  public static void set(ServletContext sc) {
    servletContextThreadLocal.set(sc);
  }

  /**
   * Checks whether ServletContext is set in the ThreadLocal variable
   */
  public static synchronized boolean exists() {
    ServletContext sc = (ServletContext) servletContextThreadLocal.get();
    return sc != null;
  }
}
