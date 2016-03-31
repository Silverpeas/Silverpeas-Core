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

package org.silverpeas.core.persistence.jcr;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.persistence.jcr.provider.JcrSystemCredentialsProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.Session;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * An initialization service whose aims is to register the Silverpeas specific schema into the
 * JCR repository used by Silverpeas. The schema is registered only the first time the repository
 * is spawned; it is no more loaded if it already exists in the underlying JCR repository.
 * @author mmoquillon
 */
public class SilverpeasJcrSchemaRegistering implements Initialization {

  private static final String SILVERPEAS_JCR_SCHEMA = "/silverpeas-jcr.cnd";

  @Inject
  private Repository repository;

  @Override
  public void init() throws Exception {
    Session session = null;
    try {
      SilverLogger.getLogger(this)
          .info("Silverpeas specific JCR schema loading...");
      session = repository.login(JcrSystemCredentialsProvider.getJcrSystemCredentials());
      InputStreamReader reader =
          new InputStreamReader(getClass().getResourceAsStream(SILVERPEAS_JCR_SCHEMA),
              Charset.forName("UTF-8"));
      CndImporter.registerNodeTypes(reader, session);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
}
