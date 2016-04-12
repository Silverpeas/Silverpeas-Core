/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.accesscontrol;

import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.web.test.WarBuilder4WarCore;
import org.silverpeas.core.util.ServiceProvider;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the access of beans managed by CDI.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SimpleDocumentAccessControllerIntegrationTest {


  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WarCore.onWarForTestClass(SimpleDocumentAccessControllerIntegrationTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.web.silverstatistics");
          warBuilder.addAsResource("org/silverpeas/publication/publicationSettings.properties");
        }).build();
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
  }

  @Test
  public void fetchAManagedAndQualifiedBeanTypeByTheServiceProviderShouldSucceed() {
    AccessController<SimpleDocument> simpleAccessController =
        ServiceProvider.getService(SimpleDocumentAccessControl.class);
    assertThat(simpleAccessController, instanceOf(AccessController.class));

    AccessController<SimpleDocument> accessController = AccessControllerProvider
        .getAccessController(SimpleDocumentAccessControl.class);

    assertThat(accessController, instanceOf(AccessController.class));
  }

}
