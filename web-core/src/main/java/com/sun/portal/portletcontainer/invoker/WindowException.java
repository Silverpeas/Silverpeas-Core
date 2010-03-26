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
package com.sun.portal.portletcontainer.invoker;

import com.sun.portal.container.ErrorCode;

/**
 * A WindowException is thrown when error message can be displayed on the Portlet Window.
 **/
public class WindowException extends Exception {

  protected ErrorCode errorCode = null;

  /**
   * Constructs a new exception with the specified message, indicating an error in the provider as
   * happened.<br>
   * <br>
   * @param msg The descriptive message.
   */
  public WindowException(ErrorCode code, String msg) {
    super(msg);
    errorCode = code;
  }

  /**
   * Constructs a new exception with the specified message, and the original <code>exception</code>
   * or <code>error</code>, indicating an error in the container as happened.<br>
   * <br>
   * @param msg The descriptive message.
   * @param cause The original <code>exception</code> or <code>error</code>.
   */
  public WindowException(ErrorCode code, String msg, Throwable cause) {
    super(msg, cause);
    errorCode = code;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
