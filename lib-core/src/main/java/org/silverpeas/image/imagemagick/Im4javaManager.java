/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.image.imagemagick;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import com.silverpeas.util.StringUtil;

/**
 * @author Yohann Chastagnier
 */
@Named("im4javaManager")
@Singleton
public class Im4javaManager {

  @PostConstruct
  public void initialize() throws Exception {

    // Im4java settings
    if (!verify(ProcessStarter.getGlobalSearchPath())) {
      for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
        if ("path".equals(entry.getKey().toLowerCase())) {
          verify(entry.getValue());
          break;
        }
      }
    }
  }

  /**
   * Verify the ImageMagick existence from a given path
   * @param path
   * @return
   */
  private boolean verify(final String path) {
    boolean verified = true;
    try {
      final ConvertCmd cmd = new ConvertCmd();
      cmd.setSearchPath(path);
      cmd.run(new IMOperation().version());
      ProcessStarter.setGlobalSearchPath(path);
    } catch (final Exception e) {
      // ImageMagick is not installed
      ProcessStarter.setGlobalSearchPath(null);
      verified = false;
    }
    return verified;
  }

  /**
   * Indicates if im4java is activated
   * @return
   */
  public static boolean isActivated() {
    return StringUtil.isDefined(ProcessStarter.getGlobalSearchPath());
  }
}
