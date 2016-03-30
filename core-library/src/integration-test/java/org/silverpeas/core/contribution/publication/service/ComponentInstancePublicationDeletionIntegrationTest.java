package org.silverpeas.core.contribution.publication.service;

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
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstancePublicationDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "create-database.sql";
  private static final String DATASET_SCRIPT =
      "publication-component-instance-deletion-dataset.sql";

  private ComponentInstanceDeletion publicationService;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Publication.onWarForTestClass(
        ComponentInstancePublicationDeletionIntegrationTest.class).build();
  }

  @Before
  public void setup() {
    publicationService = ServiceProvider.getService(DefaultPublicationService.class);
    assertThat(publicationService, notNullValue());
  }

  @Test
  public void verifyingSqlTestData() throws Exception {
    assertThat("Publications", getPublications(),
        contains("1153-kmelia382-Valid", "1154-kmelia382-Draft", "1155-kmelia382-Valid",
            "1156-kmelia382-Draft", "1157-kmelia382-ToValidate", "1158-kmelia383-Valid",
            "1159-kmelia383-ToValidate", "1160-kmelia383-Draft", "1161-kmelia384-Valid"));

    assertThat("Publication translations", getPublicationTranslations(),
        contains("1-1153-en", "2-1154-fr", "3-1155-en", "4-1156-fr", "5-1157-en", "6-1158-fr",
            "7-1159-en", "8-1160-fr", "9-1161-en"));

    assertThat("Publication locations", getPublicationLocations(),
        contains("1153-11306-kmelia382", "1153-11309-kmelia383", "1153-0-kmelia384",
            "1154-1-kmelia382", "1155-0-kmelia382", "1155-11306-kmelia382", "1155-11308-kmelia383",
            "1155-0-kmelia384", "1156-11306-kmelia382", "1157-11306-kmelia382",
            "1158-11306-kmelia382", "1158-0-kmelia383", "1158-11309-kmelia383", "1158-0-kmelia384",
            "1159-11308-kmelia383", "1160-11309-kmelia383", "1161-11306-kmelia382",
            "1161-0-kmelia383", "1161-11308-kmelia383", "1161-11309-kmelia383",
            "1161-0-kmelia384"));

    assertThat("Publication validations", getPublicationValidations(),
        contains("1-1157-kmelia382-2", "2-1157-kmelia382-3", "3-1159-kmelia383-2"));

    assertThat("Publication see also", getPublicationSeeAlso(),
        contains("1-1153-kmelia382-1155-kmelia382", "2-1155-kmelia382-1153-kmelia382",
            "3-1157-kmelia382-1155-kmelia382", "4-1157-kmelia382-1158-kmelia383",
            "5-1158-kmelia383-1153-kmelia382", "6-1158-kmelia383-1155-kmelia382",
            "7-1160-kmelia383-1155-kmelia382", "8-1160-kmelia383-1158-kmelia383",
            "9-1161-kmelia384-1155-kmelia382", "10-1161-kmelia384-1158-kmelia383"));
  }

  @Test
  public void deleteKmelia382() throws Exception {
    publicationService.delete("kmelia382");

    assertThat("Publications", getPublications(),
        contains("1158-kmelia383-Valid", "1159-kmelia383-ToValidate", "1160-kmelia383-Draft",
            "1161-kmelia384-Valid"));

    assertThat("Publication translations", getPublicationTranslations(),
        contains("6-1158-fr", "7-1159-en", "8-1160-fr", "9-1161-en"));

    assertThat("Publication locations", getPublicationLocations(),
        contains("1158-0-kmelia383", "1158-11309-kmelia383", "1158-0-kmelia384",
            "1159-11308-kmelia383", "1160-11309-kmelia383", "1161-0-kmelia383",
            "1161-11308-kmelia383", "1161-11309-kmelia383", "1161-0-kmelia384"));

    assertThat("Publication validations", getPublicationValidations(),
        contains("3-1159-kmelia383-2"));

    assertThat("Publication see also", getPublicationSeeAlso(),
        contains("8-1160-kmelia383-1158-kmelia383", "10-1161-kmelia384-1158-kmelia383"));
  }

  @Test
  public void deleteKmelia382ButSimulatingRollback() throws Exception {
    boolean rollback = false;
    try {
      Transaction.performInOne(() -> {
        publicationService.delete("kmelia382");
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
    publicationService.delete("kmelia383");

    assertThat("Publications", getPublications(),
        contains("1153-kmelia382-Valid", "1154-kmelia382-Draft", "1155-kmelia382-Valid",
            "1156-kmelia382-Draft", "1157-kmelia382-ToValidate", "1161-kmelia384-Valid"));

    assertThat("Publication translations", getPublicationTranslations(),
        contains("1-1153-en", "2-1154-fr", "3-1155-en", "4-1156-fr", "5-1157-en", "9-1161-en"));

    assertThat("Publication locations", getPublicationLocations(),
        contains("1153-11306-kmelia382", "1153-0-kmelia384", "1154-1-kmelia382", "1155-0-kmelia382",
            "1155-11306-kmelia382", "1155-0-kmelia384", "1156-11306-kmelia382",
            "1157-11306-kmelia382", "1161-11306-kmelia382", "1161-0-kmelia384"));

    assertThat("Publication validations", getPublicationValidations(),
        contains("1-1157-kmelia382-2", "2-1157-kmelia382-3"));

    assertThat("Publication see also", getPublicationSeeAlso(),
        contains("1-1153-kmelia382-1155-kmelia382", "2-1155-kmelia382-1153-kmelia382",
            "3-1157-kmelia382-1155-kmelia382", "9-1161-kmelia384-1155-kmelia382"));
  }

  @Test
  public void deleteKmelia384() throws Exception {
    publicationService.delete("kmelia384");

    assertThat("Publications", getPublications(),
        contains("1153-kmelia382-Valid", "1154-kmelia382-Draft", "1155-kmelia382-Valid",
            "1156-kmelia382-Draft", "1157-kmelia382-ToValidate", "1158-kmelia383-Valid",
            "1159-kmelia383-ToValidate", "1160-kmelia383-Draft"));

    assertThat("Publication translations", getPublicationTranslations(),
        contains("1-1153-en", "2-1154-fr", "3-1155-en", "4-1156-fr", "5-1157-en", "6-1158-fr",
            "7-1159-en", "8-1160-fr"));

    assertThat("Publication locations", getPublicationLocations(),
        contains("1153-11306-kmelia382", "1153-11309-kmelia383", "1154-1-kmelia382",
            "1155-0-kmelia382", "1155-11306-kmelia382", "1155-11308-kmelia383",
            "1156-11306-kmelia382", "1157-11306-kmelia382", "1158-11306-kmelia382",
            "1158-0-kmelia383", "1158-11309-kmelia383", "1159-11308-kmelia383",
            "1160-11309-kmelia383"));

    assertThat("Publication validations", getPublicationValidations(),
        contains("1-1157-kmelia382-2", "2-1157-kmelia382-3", "3-1159-kmelia383-2"));

    assertThat("Publication see also", getPublicationSeeAlso(),
        contains("1-1153-kmelia382-1155-kmelia382", "2-1155-kmelia382-1153-kmelia382",
            "3-1157-kmelia382-1155-kmelia382", "4-1157-kmelia382-1158-kmelia383",
            "5-1158-kmelia383-1153-kmelia382", "6-1158-kmelia383-1155-kmelia382",
            "7-1160-kmelia383-1155-kmelia382", "8-1160-kmelia383-1158-kmelia383"));
  }

  /**
   * Returns the list of publications (sb_publication_publi table).
   * @return list of strings which the schema is: [pubid]-[instanceid]-[pubstatus]
   * @throws Exception
   */
  private List<String> getPublications() throws Exception {
    return JdbcSqlQuery.createSelect("pubid, instanceid, pubstatus from sb_publication_publi")
        .addSqlPart("order by instanceid, pubid")
        .execute(row -> row.getInt(1) + "-" + row.getString(2) + "-" + row.getString(3));
  }

  /**
   * Returns the list of publication translations (sb_publication_publii18n table).
   * @return list of strings which the schema is: [id]-[pubid]-[lang]
   * @throws Exception
   */
  private List<String> getPublicationTranslations() throws Exception {
    return JdbcSqlQuery.createSelect("id, pubid, lang from sb_publication_publii18n")
        .addSqlPart("order by pubid")
        .execute(row -> row.getInt(1) + "-" + row.getInt(2) + "-" + row.getString(3));
  }

  /**
   * Returns the list of publication locations (sb_publication_publifather table).
   * @return list of strings which the schema is: [pubid]-[nodeid]-[instanceid]
   * @throws Exception
   */
  private List<String> getPublicationLocations() throws Exception {
    return JdbcSqlQuery.createSelect("pubid, nodeid, instanceid from sb_publication_publifather")
        .addSqlPart("order by pubid, instanceId, nodeid")
        .execute(row -> row.getInt(1) + "-" + row.getString(2) + "-" + row.getString(3));
  }

  /**
   * Returns the list of publication validations (sb_publication_validation table).
   * @return list of strings which the schema is: [id]-[pubid]-[instanceid]-[userid]
   * @throws Exception
   */
  private List<String> getPublicationValidations() throws Exception {
    return JdbcSqlQuery.createSelect("id, pubid, instanceid, userid from sb_publication_validation")
        .addSqlPart("order by pubid, instanceId, userid")
        .execute(row -> row.getInt(1) + "-" + row.getInt(2) + "-" + row.getString(3) + "-" +
            row.getInt(4));
  }

  /**
   * Returns the list of publication see also (sb_seealso_link table).
   * @return list of strings which the schema is:
   * [id]-[objectid]-[objectinstanceid]-[targetid]-[targetinstanceid]
   * @throws Exception
   */
  private List<String> getPublicationSeeAlso() throws Exception {
    return JdbcSqlQuery.createSelect(
        "id, objectid, objectinstanceid, targetid, targetinstanceid from sb_seealso_link")
        .addSqlPart("order by objectid, targetid")
        .execute(row -> row.getInt(1) + "-" + row.getInt(2) + "-" + row.getString(3) + "-" +
            row.getInt(4) + "-" + row.getString(5));
  }
}