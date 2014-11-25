/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.jcr;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.silverpeas.initialization.Initialization;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the JCR repository with the schema defined in a silverpeas-jcr.txt file accessible
 * in the classpath. If the JCR is already initialized, then nothing is done.
 * @author mmoquillon
 */
@Singleton
public class SilverpeasJcrSchemaSetup implements Initialization {

  @Override
  public void init() {
    Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "LOAD CUSTOM SCHEMA IN JCR...");
    try (JcrSession session = JcrRepositoryConnector.openSystemSession()) {
      InputStreamReader reader =
          new InputStreamReader(getClass().getResourceAsStream("/silverpeas-jcr.txt"),
              Charset.forName("UTF-8"));
      CndImporter.registerNodeTypes(reader, session);
      Logger.getLogger(getClass().getSimpleName()).log(Level.INFO, "CUSTOM SCHEMA LOADED IN JCR.");
    } catch (RepositoryException | ParseException | IOException e) {
      Logger.getLogger(getClass().getSimpleName())
          .log(Level.SEVERE, "CUSTOM SCHEMA LOADING FAILED!");
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
