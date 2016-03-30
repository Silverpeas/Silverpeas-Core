package org.silverpeas.core.node.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstanceNodeDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "create-database.sql";
  private static final String DATASET_SCRIPT = "node-component-instance-deletion-dataset.sql";

  @Inject
  private NodeService nodeServiceByInjection;

  private ComponentInstanceDeletion nodeService;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ComponentInstanceNodeDeletionIntegrationTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addIndexEngineFeatures()
        .addWysiwygFeatures()
        .testFocusedOn(
            war -> war.addPackages(true, "org.silverpeas.core.node")
                .addAsResource("org/silverpeas/node")
                .addAsResource("org/silverpeas/core/node"))
        .build();
  }

  @Before
  public void setup() {
    nodeService = ServiceProvider.getService(DefaultNodeService.class);
    assertThat(nodeService, sameInstance(nodeServiceByInjection));
  }

  @Test
  public void verifyingSqlTestData() throws Exception {
    assertThat("Nodes", getNodes(),
        contains("kmelia382 | 0 | -1 | /", "kmelia382 | 1 | 0 | /0/", "kmelia382 | 2 | 0 | /0/",
            "kmelia382 | 11306 | 0 | /0/", "kmelia383 | 0 | -1 | /", "kmelia383 | 1 | 0 | /0/",
            "kmelia383 | 2 | 0 | /0/", "kmelia383 | 11308 | 0 | /0/",
            "kmelia383 | 11309 | 11308 | /0/11308/", "kmelia384 | 0 | -1 | /",
            "kmelia384 | 1 | 0 | /0/", "kmelia384 | 2 | 0 | /0/"));

    assertThat("Node translations", getNodeTranslations(),
        contains("11 | 11306 | en", "12 | 11306 | de", "13 | 11308 | en", "14 | 11309 | en"));
  }

  @Test
  public void deleteKmelia382() throws Exception {
    nodeService.delete("kmelia382");

    assertThat("Nodes", getNodes(),
        contains("kmelia383 | 0 | -1 | /", "kmelia383 | 1 | 0 | /0/", "kmelia383 | 2 | 0 | /0/",
            "kmelia383 | 11308 | 0 | /0/", "kmelia383 | 11309 | 11308 | /0/11308/",
            "kmelia384 | 0 | -1 | /", "kmelia384 | 1 | 0 | /0/", "kmelia384 | 2 | 0 | /0/"));

    assertThat("Node translations", getNodeTranslations(),
        contains("13 | 11308 | en", "14 | 11309 | en"));
  }

  @Test
  public void deleteKmelia382ButSimulatingRollback() throws Exception {
    boolean rollback = false;
    try {
      Transaction.performInOne(() -> {
        nodeService.delete("kmelia382");
        throw new NullPointerException("Rollback simulation");
      });
    } catch (Exception e) {
      if (e.getCause().getMessage().equals("Rollback simulation")) {
        rollback = true;
      }
    }
    assertThat(rollback, is(true));
    verifyingSqlTestData();
  }

  @Test
  public void deleteKmelia383() throws Exception {
    nodeService.delete("kmelia383");

    assertThat("Nodes", getNodes(),
        contains("kmelia382 | 0 | -1 | /", "kmelia382 | 1 | 0 | /0/", "kmelia382 | 2 | 0 | /0/",
            "kmelia382 | 11306 | 0 | /0/", "kmelia384 | 0 | -1 | /", "kmelia384 | 1 | 0 | /0/",
            "kmelia384 | 2 | 0 | /0/"));

    assertThat("Node translations", getNodeTranslations(),
        contains("11 | 11306 | en", "12 | 11306 | de"));
  }

  @Test
  public void deleteKmelia384() throws Exception {
    nodeService.delete("kmelia384");

    assertThat("Nodes", getNodes(),
        contains("kmelia382 | 0 | -1 | /", "kmelia382 | 1 | 0 | /0/", "kmelia382 | 2 | 0 | /0/",
            "kmelia382 | 11306 | 0 | /0/", "kmelia383 | 0 | -1 | /", "kmelia383 | 1 | 0 | /0/",
            "kmelia383 | 2 | 0 | /0/", "kmelia383 | 11308 | 0 | /0/",
            "kmelia383 | 11309 | 11308 | /0/11308/"));

    assertThat("Node translations", getNodeTranslations(),
        contains("11 | 11306 | en", "12 | 11306 | de", "13 | 11308 | en", "14 | 11309 | en"));
  }

  /**
   * Returns the list of nodes (sb_node_node table).
   * @return list of strings which the schema is:
   * [instanceid]-[nodeid]-[nodefatherid]-[nodepath]
   * @throws Exception
   */
  private List<String> getNodes() throws Exception {
    return JdbcSqlQuery.createSelect("instanceid,nodeid,nodefatherid,nodepath from sb_node_node")
        .addSqlPart("order by instanceid, nodeid")
        .execute(row -> row.getString(1) + " | " + row.getInt(2) + " | " + row.getInt(3) + " | " +
            row.getString(4));
  }

  /**
   * Returns the list of node translations (sb_node_nodei18n table).
   * @return list of strings which the schema is: [id]-[pubid]-[lang]
   * @throws Exception
   */
  private List<String> getNodeTranslations() throws Exception {
    return JdbcSqlQuery.createSelect("id, nodeid, lang from sb_node_nodei18n")
        .addSqlPart("order by nodeid")
        .execute(row -> row.getInt(1) + " | " + row.getInt(2) + " | " + row.getString(3));
  }
}