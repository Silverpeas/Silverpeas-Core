package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.mylinks.test.WarBuilder4MyLinks;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstanceMyLinksDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String DATASET_SCRIPT = "test-mylinks-component-instance-deletion-data.sql";

  @Inject
  private MyLinksService linkServiceByInjection;

  private ComponentInstanceDeletion linkService;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4MyLinks
        .onWarForTestClass(ComponentInstanceMyLinksDeletionIntegrationTest.class).build();
  }

  @Before
  public void setup() {
    linkService = ServiceProvider.getService(DefaultMyLinksService.class);
    assertThat(linkService, sameInstance(linkServiceByInjection));
  }

  @Test
  public void verifyingSqlTestData() throws Exception {
    assertThat("Links", getLinks(), contains(
         "1 | /Component/kmelia4 | null",
         "3 | /Publication/19 | null",
         "4 | /Topic/10514?ComponentId=gallery89 | null",
         "6 | /Publication/106 | null",
         "7 | /Publication/97 | null",
        "21 | /Component/suggestionBox269 | null",
        "24 | /Component/suggestionBox269 | null",
        "32 | /Component/kmelia188 | null",
        "46 | /Publication/381 | null",
        "51 | /Media/9e941fd2-40d3-42b8-8a83-90586b2d87f6 | null",
        "69 | /Component/suggestionBox279 | null",
        "78 | /Topic/10784?ComponentId=kmelia4 | null",
        "79 | /Publication/26 | kmelia4"));
  }

  @Test
  public void dataAboutKmelia4ShouldBeDeleted() throws Exception {
    linkService.delete("kmelia4");

    assertThat("Links", getLinks(), contains(
         "3 | /Publication/19 | null",
         "4 | /Topic/10514?ComponentId=gallery89 | null",
         "6 | /Publication/106 | null",
         "7 | /Publication/97 | null",
        "21 | /Component/suggestionBox269 | null",
        "24 | /Component/suggestionBox269 | null",
        "32 | /Component/kmelia188 | null",
        "46 | /Publication/381 | null",
        "51 | /Media/9e941fd2-40d3-42b8-8a83-90586b2d87f6 | null",
        "69 | /Component/suggestionBox279 | null"));
  }

  @Test
  public void nothingShouldBeDeletedOnDeletionOfUnknownComponentInstanceId() throws Exception {
    linkService.delete("kmeliaUnknown");
    verifyingSqlTestData();
  }

  /**
   * Returns the list of links (sb_mylinks_link table).
   * @return list of strings which the schema is: [linkid]-[url]-[instanceid]
   * @throws Exception
   */
  private List<String> getLinks() throws Exception {
    return JdbcSqlQuery.createSelect("linkid, url, instanceid from sb_mylinks_link")
        .addSqlPart("order by linkId")
        .execute(row -> row.getInt(1) + " | " + row.getString(2) + " | " + row.getString(3));
  }
}