package org.silverpeas.core.test.jcr;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.silverpeas.core.contribution.attachment.process.SimpleDocumentDummyHandledFileConverter;
import org.silverpeas.core.contribution.attachment.repository.JcrContext;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.ServiceProvider;

/**
 * @author Yohann Chastagnier
 */
public abstract class JcrIntegrationTest {

  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);
  private ExpectedException expectedException = ExpectedException.none();
  private JcrContext jcrContext = new JcrContext();

  @Rule
  public MavenTargetDirectoryRule getMavenTargetDirectory() {
    return mavenTargetDirectory;
  }

  @Rule
  public ExpectedException getExpectedException() {
    return expectedException;
  }

  @Rule
  public JcrContext getJcr() {
    return jcrContext;
  }

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create_table_favorit_space.sql");

  @Before
  public void setup() throws Exception {
    ServiceProvider.getService(SimpleDocumentDummyHandledFileConverter.class).init();
  }
}
