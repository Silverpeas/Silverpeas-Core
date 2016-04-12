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

package com.sun.portal.portletcontainer.admin.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A util class which provides the helper methods to copy files which will be used by the
 * implementations of WebAppDeployer interface.
 */
public class WebAppDeployerUtil {

  /**
   * Copies contents from the InputStream to the OutputStream.
   * @param in InputStream from which to copy contents.
   * @param out OutputStream to which contents are written.
   */
  public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[2048];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

  /**
   * Copies contents from one file to another
   * @param fromFile File from which to copy contents.
   * @param toFile File to which contents are written.
   */
  public static void copyFile(String fromFile, String toFile) throws IOException {
    File from = new File(fromFile);
    File to = new File(toFile);

    FileInputStream inStream = new FileInputStream(from);

    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(to));
    copyInputStream(inStream, out);
  }
}