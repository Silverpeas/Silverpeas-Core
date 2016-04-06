package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstancePublicationTemplateDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "create-database.sql";
  private static final String DATASET_SCRIPT =
      "test-publicationTemplate-componentInstanceDeletion.sql";

  private ComponentInstanceDeletion manager;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore
        .onWarForTestClass(ComponentInstancePublicationTemplateDeletionIntegrationTest.class)
        .addDatabaseToolFeatures()
        .addSilverpeasExceptionBases()
        .addOrganisationFeatures()
        .addPublicationTemplateFeatures()
        .addComponentInstanceDeletionFeatures()
        .addIndexEngineFeatures()
        .testFocusedOn(
            war -> war.addAsResource("org/silverpeas/core/contribution/template/publication"))
        .build();
  }

  @Before
  public void setup() {
    manager = ServiceProvider.getService(PublicationTemplateManager.class);
    assertThat(manager, sameInstance(PublicationTemplateManager.getInstance()));
  }

  @Test
  public void verifyingSqlTestData() throws Exception {
    assertThat("Templates", getTemplates(), contains(
        "1 - demandeCongesSimple8:folder - null",
        "5 - demandeCongesSimple8:form:acceptation - null",
        "6 - demandeCongesSimple8:form:refus - null",
        "9 - classifieds18:classifieds - classifieds.xml",
        "14 - gallery65:classifieds - classifieds.xml",
        "15 - gallery65:model1 - model1.xml"));
    assertThat("Records", getRecords(), is(63L));
    assertThat("TemplateFields", getTemplateFields(), is(11L));
    assertThat("TextField", getTextFields(), is(240L));
  }

  @Test
  public void nothingShouldBeDeleted() throws Exception {
    manager.delete("demandeCongesSimple26");
    verifyingSqlTestData();
  }

  @Test
  public void allDataAboutDemandeCongesSimple8ShouldBeDeleted() throws Exception {
    manager.delete("demandeCongesSimple8");

    assertThat("Templates", getTemplates(), contains(
        "9 - classifieds18:classifieds - classifieds.xml",
        "14 - gallery65:classifieds - classifieds.xml",
        "15 - gallery65:model1 - model1.xml"));
    assertThat("Records", getRecords(), is(61L));
    assertThat("TemplateFields", getTemplateFields(), is(0L));
    assertThat("TextField", getTextFields(), is(230L));
  }

  @Test
  public void allDataAboutClassifieds18ShouldBeDeleted() throws Exception {
    manager.delete("classifieds18");

    assertThat("Templates", getTemplates(), contains(
        "1 - demandeCongesSimple8:folder - null",
        "5 - demandeCongesSimple8:form:acceptation - null",
        "6 - demandeCongesSimple8:form:refus - null",
        "14 - gallery65:classifieds - classifieds.xml",
        "15 - gallery65:model1 - model1.xml"));
    assertThat("Records", getRecords(), is(56L));
    assertThat("TemplateFields", getTemplateFields(), is(11L));
    assertThat("TextField", getTextFields(), is(226L));
  }

  @Test
  public void allDataAboutGallery65ShouldBeDeleted() throws Exception {
    manager.delete("gallery65");

    assertThat("Templates", getTemplates(), contains(
        "1 - demandeCongesSimple8:folder - null",
        "5 - demandeCongesSimple8:form:acceptation - null",
        "6 - demandeCongesSimple8:form:refus - null",
        "9 - classifieds18:classifieds - classifieds.xml"));
    assertThat("Records", getRecords(), is(9L));
    assertThat("TemplateFields", getTemplateFields(), is(11L));
    assertThat("TextField", getTextFields(), is(24L));
  }

  private List<String> getTemplates() throws Exception {
    return JdbcSqlQuery
        .createSelect("templateid,externalid,templatename from sb_formtemplate_template")
        .addSqlPart("order by templateid")
        .execute(row -> row.getInt(1) + " - " + row.getString(2) + " - " + row.getString(3));
  }

  private long getRecords() throws Exception {
    return JdbcSqlQuery.createCountFor("sb_formtemplate_record").execute();
  }

  private long getTemplateFields() throws Exception {
    return JdbcSqlQuery.createCountFor("sb_formtemplate_templatefield").execute();
  }

  private long getTextFields() throws Exception {
    return JdbcSqlQuery.createCountFor("sb_formtemplate_textfield").execute();
  }
}