/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.test;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.admin.components.Parameter;
import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.PasteDetailFromToPK;
import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.admin.spaces.SpaceTemplate;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.domains.DriverSettings;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.*;
import com.stratelia.webactiv.organization.ScheduledDBReset;
import org.apache.commons.collections.list.UnmodifiableList;
import org.silverpeas.EntityReference;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.admin.user.constant.UserState;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.contribution.model.Contribution;
import org.silverpeas.core.IdentifiableResource;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.persistence.TransactionRuntimeException;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;
import org.silverpeas.profile.UserReference;
import org.silverpeas.quota.QuotaKey;
import org.silverpeas.quota.exception.QuotaException;
import org.silverpeas.quota.exception.QuotaRuntimeException;
import org.silverpeas.quota.service.QuotaService;
import org.silverpeas.util.*;
import org.silverpeas.util.comparator.AbstractComparator;
import org.silverpeas.util.comparator.AbstractComplexComparator;
import org.silverpeas.util.exception.DecodingException;
import org.silverpeas.util.exception.EncodingException;
import org.silverpeas.util.exception.FromModule;
import org.silverpeas.util.exception.RelativeFileAccessException;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilException;
import org.silverpeas.util.exception.WithNested;
import org.silverpeas.util.fileFolder.FileFolderManager;
import org.silverpeas.util.pool.ConnectionPool;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.time.TimeConversionBoardKey;
import org.silverpeas.util.time.TimeData;
import org.silverpeas.util.time.TimeUnit;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4LibCore extends WarBuilder<WarBuilder4LibCore> {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4LibCore(final Class<T> test) {
    super(test);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the
   * following common stuffs:
   * <ul>
   * <li>the resources located in the same package of the specified test class,</li>
   * <li>{@link ServiceProvider} features.</li>
   * <li>the SilverTrace subsystem subbed,</li>
   * <li>the base i18n bundle loaded.</li>
   * </ul>
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4LibCore onWarForTestClass(Class<T> test) {
    WarBuilder4LibCore warBuilder = new WarBuilder4LibCore(test);
    warBuilder.addClasses(SilverTrace.class);
    warBuilder.addServiceProviderFeatures();
    warBuilder.addBundleBaseFeatures();
    warBuilder.addClasses(EntityReference.class);
    warBuilder.addAsResource("maven.properties");
    return warBuilder;
  }

  /**
   * Adds cache features:
   * <ul>
   * <li>ehcache maven dependencies</li>
   * <li>org.silverpeas.cache</li>
   * </ul>
   * Calls automatically:
   * <ul>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCacheFeatures() {
    if (!contains("org.silverpeas.cache")) {
      addMavenDependencies("net.sf.ehcache:ehcache-core");
      addPackages(true, "org.silverpeas.cache");
      addClasses(SessionInfo.class);
      addCommonBasicUtilities();
    }
    return this;
  }

  /**
   * Adds common utilities classes:
   * <ul>
   * <li>{@link ArrayUtil}</li>
   * <li>{@link StringUtil}</li>
   * <li>{@link CollectionUtil}</li>
   * <li>{@link MapUtil}</li>
   * <li>{@link DateUtil}</li>
   * <li>{@link AssertArgument}</li>
   * <li>{@link EncodeHelper}</li>
   * <li>{@link ActionType} and classes in {@link org.silverpeas.util.annotation}</li>
   * <li>{@link #addSilverpeasContentFeatures()}</li>
   * <li>{@link AbstractComplexComparator}</li>
   * <li>{@link AbstractComparator}</li>
   * <li>{@link ListSlice}</li>
   * <li>{@link UnitUtil}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonBasicUtilities() {
    if (!contains(ArrayUtil.class)) {
      addClasses(ArrayUtil.class);
    }
    if (!contains(StringUtil.class)) {
      addClasses(StringUtil.class);
    }
    if (!contains(MapUtil.class)) {
      addClasses(MapUtil.class);
    }
    if (!contains(ArrayUtil.class)) {
      addClasses(ArrayUtil.class);
    }
    if (!contains(EncodeHelper.class)) {
      addClasses(EncodeHelper.class);
    }
    if (!contains(DateUtil.class)) {
      addClasses(DateUtil.class);
      addPackages(true, "com.silverpeas.calendar");
    }
    if (!contains(Charsets.class)) {
      addClasses(Charsets.class);
    }
    if (!contains(CollectionUtil.class)) {
      addClasses(CollectionUtil.class, ExtractionList.class, ExtractionComplexList.class);
    }
    if (!contains(AssertArgument.class)) {
      addClasses(AssertArgument.class);
    }
    if (!contains(ActionType.class)) {
      addSilverpeasContentFeatures();
      addClasses(ActionType.class);
      addPackages(false, "org.silverpeas.util.annotation");
    }
    if (!contains(AbstractComplexComparator.class)) {
      addClasses(AbstractComplexComparator.class, AbstractComparator.class);
    }
    if (!contains(ListSlice.class)) {
      addClasses(ListSlice.class);
    }
    if (!contains(UnitUtil.class)) {
      addClasses(UnitUtil.class);
      addClasses(TimeData.class);
      addClasses(TimeUnit.class);
      addClasses(TimeConversionBoardKey.class);
    }
    return this;
  }

  /**
   * Sets common user beans.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonUserBeans() {
    if (!contains(UserDetail.class)) {
      addClasses(UserDetail.class, UserAccessLevel.class, UserState.class);
    }
    if (!contains(UserFull.class)) {
      addClasses(UserFull.class);
    }
    if (!contains(UserLog.class)) {
      addClasses(UserLog.class);
    }
    if (!contains(Domain.class)) {
      addClasses(Domain.class);
    }
    if (!contains(UserReference.class)) {
      addClasses(UserReference.class);
    }
    return this;
  }

  /**
   * Adds bases of Silverpeas exception classes:
   * <ul>
   * <li>{@link WithNested}</li>
   * <li>{@link FromModule}</li>
   * <li>{@link SilverpeasException}</li>
   * <li>{@link SilverpeasRuntimeException}</li>
   * <li>{@link UtilException}</li>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasExceptionBases() {
    if (!contains(SilverpeasException.class)) {
      addClasses(WithNested.class);
      addClasses(FromModule.class);
      addClasses(SilverpeasException.class);
      addClasses(SilverpeasRuntimeException.class);
      addClasses(UtilException.class);
    }
    return this;
  }

  /**
   * Adds bundle features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addBundleBaseFeatures() {
    if (!contains(ResourceLocator.class)) {
      addClasses(ResourceLocator.class, DisplayI18NHelper.class, ConfigurationClassLoader.class,
          ConfigurationControl.class, VariableResolver.class, PropertiesWrapper.class,
          ResourceBundleWrapper.class, FileUtil.class, MimeTypes.class,
          RelativeFileAccessException.class, GeneralPropertiesManager.class);
      addAsResource("org/silverpeas/general.properties");
      addAsResource("org/silverpeas/multilang/generalMultilang.properties");
      addAsResource("org/silverpeas/lookAndFeel/generalLook.properties");
      addAsResource("org/silverpeas/util/i18n.properties");
      addAsResource("org/silverpeas/util/multilang/i18n_fr.properties");
      addAsResource("org/silverpeas/util/multilang/i18n_en.properties");
      addAsResource("org/silverpeas/util/multilang/util.properties");
      addAsResource("org/silverpeas/util/multilang/util_fr.properties");
      addAsResource("org/silverpeas/util/multilang/util_en.properties");
    }
    return this;
  }

  /**
   * Adds URL features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasUrlFeatures() {
    if (!contains(URLManager.class)) {
      addClasses(URLManager.class);
    }
    return this;
  }

  /**
   * Adds common classes to handle silverpeas content features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasContentFeatures() {
    if (!contains(Contribution.class)) {
      addClasses(Contribution.class, IdentifiableResource.class);
    }
    if (!contains(SilverpeasContent.class)) {
      addClasses(SilverpeasContent.class);
    }
    if (!contains(SilverContentInterface.class)) {
      addClasses(SilverContentInterface.class);
    }
    if (!contains(WAPrimaryKey.class)) {
      addClasses(WAPrimaryKey.class, ForeignPK.class, SimpleDocumentPK.class, PasteDetail.class,
          PasteDetailFromToPK.class);
    }
    return this;
  }

  /**
   * Adds file repository features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addBundleBaseFeatures()}</li>
   * <li>{@link #addSilverpeasUrlFeatures()}</li>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addFileRepositoryFeatures() {
    addBundleBaseFeatures();
    addSilverpeasUrlFeatures();
    addCommonBasicUtilities();
    if (!contains(FileRepositoryManager.class)) {
      addClasses(FileRepositoryManager.class, FileFolderManager.class);
    }
    return this;
  }

  /**
   * Sets JDBC persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addJdbcPersistenceFeatures() {
    addCommonBasicUtilities();
    if (!contains(DBUtil.class)) {
      addClasses(DBUtil.class, ConnectionPool.class, Transaction.class, TransactionProvider.class,
          TransactionRuntimeException.class);
      addPackages(false, "org.silverpeas.persistence.jdbc");
    }
    return this;
  }

  /**
   * Sets JPA persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addJdbcPersistenceFeatures()}</li>
   * <li>{@link #addBundleBaseFeatures()}</li>
   * <li>{@link #addCacheFeatures()}</li>
   * <li>{@link #addCommonUserBeans()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addJpaPersistenceFeatures() {
    addJdbcPersistenceFeatures();
    addBundleBaseFeatures();
    addCacheFeatures();
    addCommonUserBeans();
    if (!contains(AbstractJpaEntity.class)) {
      addClasses(ResourceIdentifier.class);
      addPackages(true, "org.silverpeas.admin.user.constant");
      addPackages(true, "org.silverpeas.persistence.model");
      addPackages(true, "org.silverpeas.persistence.repository");
      addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
    }
    return this;
  }

  /**
   * Sets stubbed administration features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addStubbedAdministrationFeatures() {
    if (!contains(StubbedAdministration.class)) {
      addClasses(StubbedAdministration.class);
      addCommonUserBeans();
      addClasses(AdministrationServiceProvider.class, Administration.class, Parameter.class,
          PasteDetail.class, WAComponent.class, SpaceTemplate.class, ComponentInst.class,
          ComponentInstLight.class, SpaceInst.class, SpaceInstLight.class, CompoSpace.class,
          QuotaException.class, ProfileInst.class, SpaceAndChildren.class, SpaceProfileInst.class,
          Group.class, GroupProfileInst.class, AdminGroupInst.class, SearchCriteria.class,
          UserDetailsSearchCriteria.class, GroupsSearchCriteria.class, DomainProperty.class);
      addClasses(Recover.class, AdminException.class);
      addPackages(true, "org.silverpeas.util.i18n");
      // Exclusions
      applyManually(war -> war.deleteClass("com.stratelia.webactiv.beans.admin.Admin"));
    }
    return this;
  }

  /**
   * Adds common administration utilities.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addAdministrationUtilities() {
    if (!contains(SilverpeasRole.class)) {
      addClasses(SilverpeasRole.class);
    }
    if (!contains(AdminException.class)) {
      addClasses(AdminException.class);
    }
    if (!contains(QuotaException.class)) {
      addClasses(QuotaException.class, QuotaKey.class, QuotaRuntimeException.class);
    }
    return this;
  }

  /**
   * Sets string template features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addStringTemplateFeatures() {
    if (!contains(SilverpeasTemplate.class)) {
      addMavenDependencies("org.antlr:stringtemplate");
      addPackages(true, "org.silverpeas.util.template");
      addAsResource("org/silverpeas/util/stringtemplate.properties");
    }
    return this;
  }

  /**
   * Sets Quota Bases features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addJpaPersistenceFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addQuotaBasesFeatures() {
    if (!contains(QuotaService.class)) {
      addClasses(QuotaService.class);
      addPackages(true, "org.silverpeas.quota");
      // Centralized features
      addJpaPersistenceFeatures();
    }
    return this;
  }

  /**
   * Sets notification features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addSilverpeasUrlFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addNotificationFeatures() {
    if (!contains(NotifChannel.class)) {
      addPackages(true, "com.stratelia.silverpeas.notificationManager");
      addPackages(true, "com.stratelia.silverpeas.notificationserver");
      addAsResource("org/silverpeas/notificationManager");
      addClasses(JNDINames.class, AbstractTable.class);
      addAsResource("org/silverpeas/util/jndi.properties");
      // Centralized features
      addSilverpeasUrlFeatures();
    }
    return this;
  }

  /**
   * Sets subscription features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSubscriptionFeatures() {
    addPackages(true, "org.silverpeas.subscription");
    addAsResource("org/silverpeas/subscription/settings/subscriptionSettings.properties");
    return this;
  }

  /**
   * Sets synchronous/asynchronous resource event features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSynchAndAsynchResourceEventFeatures() {
    addClasses(DecodingException.class, EncodingException.class, StateTransition.class);
    addPackages(false, "org.silverpeas.notification");
    applyManually(war -> war.addAsManifestResource("META-INF/test-MANIFEST.MF", "MANIFEST.MF"));
    return this;
  }

  /**
   * Sets administration features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addJpaPersistenceFeatures()}</li>
   * <li>{@link #addQuotaBasesFeatures()}</li>
   * <li>{@link #addStringTemplateFeatures()}</li>
   * <li>{@link #addAdministrationUtilities()}</li>
   * <li>{@link #addCommonUserBeans()}</li>
   * <li>{@link #addOrganisationFeatures()}</li>
   * <li>{@link #addSchedulerFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addAdministrationFeatures() {
    if (!contains(Administration.class)) {
      addClasses(Administration.class, ScheduledDBReset.class);
      addClasses(Recover.class);
      addClasses(DriverSettings.class);
      addPackages(true, "org.silverpeas.util.i18n");
      addPackages(true, "com.silverpeas.admin.components");
      addPackages(true, "com.silverpeas.admin.spaces");
      addPackages(true, "com.silverpeas.domains");
      addPackages(true, "com.stratelia.silverpeas.domains.sqldriver");
      addPackages(true, "com.stratelia.webactiv.beans.admin");
      addPackages(false, "org.silverpeas.notification");
      addPackages(true, "org.silverpeas.admin.component.notification");
      addPackages(true, "org.silverpeas.admin.space.notification");
      addPackages(true, "org.silverpeas.admin.user.constant");
      addPackages(true, "org.silverpeas.admin.user.notification");
      addPackages(true, "org.silverpeas.util.clipboard");
      addAsResource("org/silverpeas/admin/roleMapping.properties");
      addAsResource("org/silverpeas/beans/admin/admin.properties");
      addAsResource("org/silverpeas/beans/admin/instance/control/instanciator.properties");
      // Exclusions
      applyManually(war -> war.deleteClass(StubbedAdministration.class));
      // Centralized features
      addJpaPersistenceFeatures();
      addQuotaBasesFeatures();
      addStringTemplateFeatures();
      addAdministrationUtilities();
      addCommonUserBeans();
      addOrganisationFeatures();
      addSchedulerFeatures();
    }
    return this;
  }

  /**
   * Sets organisation features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addAdministrationFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addOrganisationFeatures() {
    if (!contains(OrganizationController.class)) {
      addClasses(OrganizationController.class, OrganizationControllerProvider.class);
      addPackages(true, "com.stratelia.webactiv.organization");
      addPackages(true, "com.stratelia.webactiv.persistence");
      addClasses(Schema.class, SchemaPool.class);
      // Centralized features
      addAdministrationFeatures();
    }
    return this;
  }

  /**
   * Sets manual CDI features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addServiceProviderFeatures() {
    addClasses(CDIContainer.class).addPackages(true, "org.silverpeas.initialization");
    return this;
  }

  /**
   * Add scheduler features in web archive (war) with quartz libraries.
   * @return the instance of the war builder with scheduler features.
   */
  public WarBuilder4LibCore addSchedulerFeatures() {
    if (!contains("com.silverpeas.scheduler")) {
      addMavenDependencies("org.quartz-scheduler:quartz");
      addPackages(true, "com.silverpeas.scheduler");
    }
    return this;
  }

  /**
   * Add benchmark test features in web archive (war).
   * @return the instance of the war builder with benchmark test features.
   */
  public WarBuilder4LibCore addBenchmarkTestFeatures() {
    addMavenDependencies("com.carrotsearch:junit-benchmarks");
    return this;
  }

  /**
   * Add novell jldap libraries in web archive (war)
   * @return the instance of the war builder with novell jldap
   */
  public WarBuilder4LibCore addLDAPFeatures() {
    addMavenDependencies("com.novell.ldap:jldap", "org.forgerock.opendj:opendj-server");
    return this;
  }

  /**
   * Add image tool features. ImageMagick must be installed on machine.
   * @return the instance of the war with image tool feature.
   */
  public WarBuilder4LibCore addImageToolFeatures() {
    addMavenDependencies("org.im4java:im4java", "org.apache.tika:tika-core",
        "org.apache.tika:tika-parsers");
    addPackages(true, "org.silverpeas.image");
    return this;
  }
}