/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.*;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class WorkflowIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(WorkflowIT.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        .createMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .createMavenDependencies("org.silverpeas.core.services:silverpeas-core-personalorganizer")
        .testFocusedOn(war -> war.addPackages(true, "org.silverpeas.core.workflow"))
        .build();
  }


  @Test
  public void testGetWorkflowEngine() {
    WorkflowEngine wfEngine = Workflow.getWorkflowEngine();
    assertThat(wfEngine, notNullValue());
  }
}