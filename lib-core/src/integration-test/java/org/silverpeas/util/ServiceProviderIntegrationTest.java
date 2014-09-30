/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.util;

import org.hamcrest.CoreMatchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.junit.Arquillian;

import javax.enterprise.inject.spi.CDI;
import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test on the access of beans managed by CDI.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ServiceProviderIntegrationTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    return ShrinkWrap.create(JavaArchive.class, "test.jar")
        .addClass(ServiceProvider.class)
        .addClass(BeanContainer.class)
        .addClass(CDIContainer.class)
        .addClass(TestManagedBean.class)
        .addAsManifestResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "services/org.silverpeas.util.BeanContainer")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
  }

  @Test
  public void fetchAManagedBeanByTheServiceProviderShouldSucceed() {
    TestManagedBean bean = ServiceProvider.getService(TestManagedBean.class);
    assertThat(bean, notNullValue());
  }
}
