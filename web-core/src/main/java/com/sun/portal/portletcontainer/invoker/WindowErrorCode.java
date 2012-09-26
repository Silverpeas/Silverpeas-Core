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
 * WindowErrorCode that must have localized message strings corresponding to each.
 **/
public class WindowErrorCode extends ErrorCode {

  // 1
  public static final WindowErrorCode GENERIC_ERROR = new WindowErrorCode(
      "GENERIC_ERROR");

  // 2
  public static final WindowErrorCode CONTENT_EXCEPTION = new WindowErrorCode(
      "CONTENT_EXCEPTION");

  // 3
  public static final WindowErrorCode INVALID_WINDOW_STATE_CHANGE_REQUEST = new WindowErrorCode(
      "INVALID_WINDOW_STATE_CHANGE_REQUEST");

  // 4
  public static final WindowErrorCode INVALID_MODE_CHANGE_REQUEST = new WindowErrorCode(
      "INVALID_MODE_CHANGE_REQUEST");

  // 5
  public static final WindowErrorCode CONTAINER_EXCEPTION = new WindowErrorCode(
      "CONTAINER_EXCEPTION");

  public WindowErrorCode(String errorCodeKey) {
    super(errorCodeKey);
  }

}
