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
