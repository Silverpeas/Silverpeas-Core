package com.silverpeas.social.service;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.social.mock.OrganizationControllerMock;

import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.socialnetwork.service.SocialNetworkService;
import com.stratelia.webactiv.beans.admin.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.test.WarBuilder4LibCore;
import org.silverpeas.util.ListSlice;
import org.silverpeas.util.ServiceProvider;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;

@RunWith(Arquillian.class)
public class SocialNetworkServiceTest {

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  @Inject
  OrganizationControllerMock organisationController;

  SocialNetworkService service;

  public static final Operation TABLES_CREATION =
      Operations.sql("CREATE TABLE IF NOT EXISTS sb_sn_externalaccount (" +
          "profileId varchar(100) NOT NULL ," +
          "networkId varchar(10) NOT NULL," +
          "silverpeasUserId varchar(50) NULL)");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("sb_sn_externalaccount");

  public static final Operation INSERT_DATA = Operations.insertInto("sb_sn_externalaccount")
      .columns("profileId", "networkId", "silverpeasUserId").values("1233", "LINKEDIN", "10")
      .values("1234", "LINKEDIN", "11").values("1235", "LINKEDIN", "12").build();
  ;

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(TABLES_CREATION, CLEAN_UP, INSERT_DATA);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);

    service = ServiceProvider.getService(SocialNetworkService.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWar().addPersistenceFeatures()
        .addMavenDependencies("org.springframework.social:spring-social-linkedin",
            "org.springframework.social:spring-social-facebook").testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.util.exception");
          warBuilder.addPackages(true, "com.silverpeas.socialnetwork");
          //warBuilder.addPackages(true, "org.silverpeas.core.admin");
          warBuilder.addClasses(OrganizationController.class, Group.class, ComponentInst.class,
              SpaceInst.class, UserFull.class, ComponentInstLight.class, SpaceInstLight.class,
              UserDetailsSearchCriteria.class, GroupsSearchCriteria.class, ProfileInst.class,
              ObjectType.class, ComponentSearchCriteria.class, SearchCriteria.class, Domain.class,
              CompoSpace.class, ListSlice.class, WAComponent.class,
              OrganizationControllerMock.class, OrganizationControllerProvider.class);
          warBuilder.addPackages(true, "org.silverpeas.util.i18n");
          warBuilder
              .addAsResource("org/silverpeas/social/settings/socialNetworkSettings.properties");
        }).build();
  }


  @Test
  public void testReadByPrimaryKeyUnexistingUser() throws Exception {
    Transaction.performInOne(() -> {
      ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1233");
      assertThat(account, nullValue());
      return null;
    });
  }

  @Test
  @Transactional
  public void testReadByPrimaryKeyValidUser() throws Exception {
    ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1234");
    assertThat(account.getSilverpeasUserId(), is("11"));
    assertThat(account.getNetworkId(), is(SocialNetworkID.LINKEDIN));
  }

  @Test
  @Transactional
  public void testReadByPrimaryKeyDeletedUser() throws Exception {
    Transaction.performInOne(() -> {
      ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1235");
      assertThat(account, nullValue());
      return null;
    });
  }

  @Test
  @Transactional
  public void testCreateExternalAccount() throws Exception {
    Transaction.performInOne(() -> {
      service.createExternalAccount(SocialNetworkID.FACEBOOK, "13", "1345");
      return null;
    });
    ExternalAccount account = service.getExternalAccount(SocialNetworkID.FACEBOOK, "1345");
    assertThat(account, is(notNullValue()));
    assertThat(account.getSilverpeasUserId(), is("13"));
  }

}