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
package com.sun.portal.portletcontainer.admin.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A util class which provides the helper methods to copy files which will be
 * used by the implementations of WebAppDeployer interface.
 */
public class WebAppDeployerUtil {

  /**
   * Copies contents from the InputStream to the OutputStream.
   * 
   * @param in
   *          InputStream from which to copy contents.
   * @param out
   *          OutputStream to which contents are written.
   */
  public static void copyInputStream(InputStream in, OutputStream out)
      throws IOException {
    byte[] buffer = new byte[2048];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

  /**
   * Copies contents from one file to another
   * 
   * @param fromFile
   *          File from which to copy contents.
   * @param toFile
   *          File to which contents are written.
   */
  public static void copyFile(String fromFile, String toFile)
      throws IOException {
    File from = new File(fromFile);
    File to = new File(toFile);

    FileInputStream inStream = new FileInputStream(from);

    BufferedOutputStream out = new BufferedOutputStream(
        new FileOutputStream(to));
    copyInputStream(inStream, out);
  }
}
