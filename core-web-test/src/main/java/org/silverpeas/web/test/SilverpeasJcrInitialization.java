package org.silverpeas.web.test;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.initialization.SilverpeasServiceInitialization;
import org.silverpeas.core.jcr.util.SilverpeasJCRIndexation;
import org.silverpeas.core.jcr.util.SilverpeasJCRSchemaRegister;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.function.Predicate;

/**
 * In order to initialize the JCR that can be used in integration tests, this listener MUST be
 * defined into the web.xml of the archive built of an integration test with arquillian + Wildfly.
 * <p>
 * At server starting, when the context of the integration is initialized, all the tasks required to initialize
 * the JCR for the integration tests are performed. The requirement is the dependencies on the JCR have to be
 * satisfied.
 * </p>
 * <p>
 * By default, this listener is added onto the war archive used in the integration tests by the
 * {@link WarBuilder4Web} object.
 * </p>
 * @author silveryocha
 */
public class SilverpeasJcrInitialization implements ServletContextListener {

  private static final String JCR_HOME = "target/jcr";
  private static final String JCR_CONFIG = "classpath:/silverpeas-oak.properties";

  private static final Predicate<Initialization> FILTER =
      i -> i.getClass().getSimpleName().contains("SilverpeasJCRSchemaRegister");

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    SystemWrapper systemWrapper = SystemWrapper.getInstance();
    systemWrapper.setProperty("jcr.home", JCR_HOME);
    systemWrapper.setProperty("jcr.conf", JCR_CONFIG);

    SilverpeasJCRSchemaRegister register = new SilverpeasJCRSchemaRegister();
    register.register();
    SilverpeasJCRIndexation indexation = SilverpeasJCRIndexation.get();
    indexation.initialize();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    SilverpeasServiceInitialization.stop(FILTER);
  }
}
