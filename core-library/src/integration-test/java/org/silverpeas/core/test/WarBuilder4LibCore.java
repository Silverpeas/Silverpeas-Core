/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.test;

import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasResource;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.BaseRightProfile;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectIds;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.RightProfile;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.component.model.GlobalContext;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.admin.component.model.PasteDetailFromToPK;
import org.silverpeas.core.admin.domain.driver.DriverSettings;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.quota.QuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaRuntimeException;
import org.silverpeas.core.admin.quota.service.QuotaService;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.service.RightRecover;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceImpl;
import org.silverpeas.core.admin.space.UserFavoriteSpaceServiceProvider;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceBean;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.admin.user.DefaultUserProvider;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.admin.user.model.UserLog;
import org.silverpeas.core.admin.user.model.UserReference;
import org.silverpeas.core.cache.VolatileResourceCleaner;
import org.silverpeas.core.calendar.ical4j.ICal4JCalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JExporter;
import org.silverpeas.core.calendar.ical4j.ICal4JImporter;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.calendar.repository.DefaultCalendarEventOccurrenceRepository;
import org.silverpeas.core.calendar.repository.DefaultCalendarEventRepository;
import org.silverpeas.core.calendar.repository.DefaultCalendarRepository;
import org.silverpeas.core.contribution.ContributionVisibility;
import org.silverpeas.core.contribution.ContributionWithVisibility;
import org.silverpeas.core.contribution.DefaultContributionVisibility;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.repository.JcrContext;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentPostUpdate;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateUserEventListener;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.EncodingException;
import org.silverpeas.core.exception.FromModule;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.exception.WithNested;
import org.silverpeas.core.html.PermalinkRegistry;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.mail.extractor.Mail;
import org.silverpeas.core.notification.user.AttachmentLink;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.persistence.jdbc.AbstractTable;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.reminder.DefaultReminderRepository;
import org.silverpeas.core.security.html.DefaultHtmlSanitizer;
import org.silverpeas.core.security.html.HtmlSanitizer;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.test.jcr.SilverpeasJcrInitialization;
import org.silverpeas.core.test.office.OfficeServiceInitializationListener;
import org.silverpeas.core.test.stub.StubbedOrganizationController;
import org.silverpeas.core.util.*;
import org.silverpeas.core.util.comparator.AbstractComparator;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.ErrorAnnotationProcessor;
import org.silverpeas.core.util.logging.LogAnnotationProcessor;
import org.silverpeas.core.util.logging.LogsAccessor;
import org.silverpeas.core.wbe.StubbedWbeHostManager;

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
    addClasses(DefaultReminderRepository.class);
    addServiceProviderFeatures();
    addBundleBaseFeatures();
    addClasses(EntityReference.class);
    addCalendarBaseFeatures();
    addPackages(true, "org.silverpeas.core.util.logging.sys");
    addMavenDependencies("org.apache.lucene:lucene-core");
    addMavenDependencies("org.apache.lucene:lucene-queryparser");
    addMavenDependencies("org.apache.lucene:lucene-queries");
    addMavenDependencies("org.apache.lucene:lucene-suggest");
    addMavenDependencies("org.apache.lucene:lucene-analysis-common");
    addMavenDependencies("org.apache.tika:tika-core");
    addMavenDependencies("org.apache.tika:tika-parsers-standard-package");
    addMavenDependencies("com.drewnoakes:metadata-extractor");
    addClasses(ErrorAnnotationProcessor.class, LogAnnotationProcessor.class, LogsAccessor.class);
    addAsResource("maven.properties");
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the
   * following common stuffs:
   * <ul>
   * <li>the resources located in the same package of the specified test class,</li>
   * <li>{@link ServiceProvider} features.</li>
   * <li>the base i18n bundle loaded.</li>
   * </ul>
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4LibCore onWarForTestClass(Class<T> test) {
    return new WarBuilder4LibCore(test);
  }

  /**
   * Adds common utilities classes:
   * <ul>
   * <li>{@link ArrayUtil}</li>
   * <li>{@link CollectionUtil}</li>
   * <li>{@link MapUtil}</li>
   * <li>{@link ArgumentAssertion}</li>
   * <li>{@link WebEncodeHelper}</li>
   * <li>{@link ActionType} and classes in {@link org.silverpeas.core.annotation}</li>
   * <li>{@link #addSilverpeasContentFeatures()}</li>
   * <li>{@link AbstractComplexComparator}</li>
   * <li>{@link AbstractComparator}</li>
   * <li>{@link ListSlice}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonBasicUtilities() {
    if (!contains(ArrayUtil.class)) {
      addClasses(ArrayUtil.class);
    }
    if (!contains(MapUtil.class)) {
      addClasses(MapUtil.class);
    }
    if (!contains(WebEncodeHelper.class)) {
      addClasses(WebEncodeHelper.class);
    }
    if (!contains(Charsets.class)) {
      addClasses(Charsets.class);
    }
    if (!contains(CollectionUtil.class)) {
      addClasses(CollectionUtil.class);
    }
    if (!contains(ArgumentAssertion.class)) {
      addClasses(ArgumentAssertion.class);
    }
    if (!contains(ActionType.class)) {
      addSilverpeasContentFeatures();
      addClasses(ActionType.class);
      addPackages(false, "org.silverpeas.core.util.annotation");
    }
    if (!contains(AbstractComplexComparator.class)) {
      addClasses(AbstractComplexComparator.class, AbstractComparator.class);
    }
    if (!contains(ListSlice.class)) {
      addClasses(ListSlice.class);
    }
    if (!contains(StringDataExtractor.class)) {
      addClasses(StringDataExtractor.class);
    }
    if (!contains(GlobalContext.class)) {
      addClasses(GlobalContext.class);
    }
    if (!contains(SilverpeasList.class)) {
      addClasses(SilverpeasList.class, SilverpeasListWrapper.class, SilverpeasArrayList.class);
    }
    return this;
  }

  /**
   * Sets common user beans.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonUserBeans() {
    if (!contains(UserDetail.class)) {
      addClasses(UserDetail.class, UserAccessLevel.class, UserState.class,
          DefaultUserProvider.class);
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
    addPackages(true, "org.silverpeas.core.security.token");
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
    if (!contains(MimeTypes.class)) {
      addMavenDependencies("commons-fileupload:commons-fileupload");
      addClasses(FileUtil.class, Mail.class, MimeTypes.class, MetaData.class, Definition.class,
          RelativeFileAccessException.class, MetadataExtractor.class, MultiSilverpeasBundle.class);
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
    if (!contains(URLUtil.class)) {
      addClasses(URLUtil.class);
    }
    if (!contains(URLEncoder.class)) {
      addClasses(URLEncoder.class);
    }
    if (!contains(PermalinkRegistry.class)) {
      addClasses(PermalinkRegistry.class);
    }
    return this;
  }

  /**
   * Adds common classes to handle silverpeas content features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasContentFeatures() {
    if (!contains(Contribution.class)) {
      addClasses(Contribution.class, SilverpeasResource.class);
    }
    if (!contains(SilverpeasContent.class)) {
      addClasses(SilverpeasContent.class);
    }
    if (!contains(SilverContentInterface.class)) {
      addClasses(SilverContentInterface.class);
    }
    if (!contains(ContentManagerException.class)) {
      addClasses(ContentManagerException.class);
    }
    if (!contains(WAPrimaryKey.class)) {
      addClasses(WAPrimaryKey.class, ResourceReference.class, SimpleDocumentPK.class, PasteDetail.class,
          PasteDetailFromToPK.class);
    }
    addClasses(ContentManagementEngine.class, SilverContentPostUpdate.class);
    addClasses(JoinStatement.class);
    addClasses(ContentManagementEngineProvider.class);
    addClasses(ContentPeas.class);
    addClasses(SilverContentVisibility.class);
    return this;
  }

  /**
   * Add office features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addOfficeFeatures() {
    addWebListener(OfficeServiceInitializationListener.class);
    addMavenDependencies("org.apache.commons:commons-exec", "org.jodconverter:jodconverter-local");
    addPackages(true, "org.silverpeas.core.contribution.converter");
    addAsResource("org/silverpeas/converter");
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
   * Sets Database tool features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addDatabaseToolFeatures() {
    addCommonBasicUtilities();
    if (!contains(DBUtil.class)) {
      addClasses(DBUtil.class);
    }
    return this;
  }

  /**
   * Sets JPA persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * <li>{@link #addBundleBaseFeatures()}</li>
   * <li>{@link #addCommonUserBeans()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addJpaPersistenceFeatures() {
    addCommonBasicUtilities();
    addBundleBaseFeatures();
    addCommonUserBeans();
    addClasses(PaginationPage.class);
    addClasses(Pagination.class);
    return this;
  }

  /**
   * Sets JCR features.<br>
   * This method must be used with {@link JcrContext} junit rule.<br>
   * Calls automatically:
   * <ul>
   * <li>{@link #addJpaPersistenceFeatures()}</li>
   * <li>{@link #addProcessFeatures()}</li>
   * <li>{@link #addSilverpeasExceptionBases()}</li>
   * <li>{@link #addNotificationFeatures()}</li>
   * <li>{@link #addOrganisationFeatures()}</li>
   * <li>{@link #addIndexEngineFeatures()}</li>
   * <li>{@link #addFileRepositoryFeatures()}</li>
   * <li>{@link #addSynchAndAsynchResourceEventFeatures()}</li>
   * <li>{@link #addPublicationTemplateFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addJcrFeatures() {
    initJcr();
    addJpaPersistenceFeatures();
    addProcessFeatures();
    addSilverpeasExceptionBases();
    addNotificationFeatures();
    addOrganisationFeatures();
    addIndexEngineFeatures();
    addFileRepositoryFeatures();
    addSynchAndAsynchResourceEventFeatures();
    addPublicationTemplateFeatures();
    addImageToolFeatures();
    addWbeManagementFeatures();
    addMavenDependencies("commons-beanutils:commons-beanutils");
    if (!contains(JcrIntegrationIT.class)) {
      addClasses(FormException.class, JcrIntegrationIT.class, JcrContext.class,
          FileServerUtils.class);
      addPackages(true, "org.silverpeas.core.persistence.jcr");
      addPackages(true, "org.silverpeas.core.contribution.attachment");
      addClasses(VolatileResourceCleaner.class);
      addAsResource("silverpeas-oak.properties");
      addAsResource("org/silverpeas/util/attachment/Attachment.properties");
      applyManually(war -> war.deletePackages(true, "org.silverpeas.core.contribution.attachment.mock"));
    }

    return this;
  }

  /**
   * The test implied code that uses the JCR. So initialize the JCR schema of Silverpeas and adds the required
   * dependencies.
   * @return itself.
   */
  public WarBuilder4LibCore initJcr() {
    addMavenDependencies("org.silverpeas.core:silverpeas-core-jcr");
    addWebListener(SilverpeasJcrInitialization.class);
    return this;
  }

  /**
   * Sets Index Engine features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addIndexEngineFeatures() {
    if (!contains(FullIndexEntry.class)) {
      addPackages(true, "org.silverpeas.core.index.indexing");
      addAsResource("org/silverpeas/index/indexing");
    }
    return this;
  }

  /**
   * Sets Wysiwyg features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addWysiwygFeatures() {
    if (!contains(WysiwygManager.class)) {
      addClasses(ContributionIdentifier.class, ContributionContent.class);
      addPackages(true, "org.silverpeas.core.contribution.content.wysiwyg");
      addPackages(true, "org.silverpeas.core.contribution.content.renderer");
      addMavenDependencies("net.htmlparser.jericho:jericho-html");
    }
    return this;
  }

  /**
   * Sets Publication Template features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addSecurityFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addPublicationTemplateFeatures() {
    addSecurityFeatures();
    addIndexEngineFeatures();
    addApacheFileUploadFeatures();
    addPackages(true, "org.silverpeas.core.contribution.template.publication");
    addPackages(false, "org.silverpeas.core.contribution.content.form");
    addPackages(false, "org.silverpeas.core.contribution.content.form.record");
    addPackages(false, "org.silverpeas.core.contribution.content.form.form");
    addAsResource("org/silverpeas/publicationTemplate/settings");
    applyManually(war -> war.deleteClass(PublicationTemplateUserEventListener.class));
    return this;
  }

  /**
   * Sets Security features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSecurityFeatures() {
    if (!contains(ContributionWithVisibility.class)) {
      addClasses(ContributionWithVisibility.class, ContributionVisibility.class,
          DefaultContributionVisibility.class);
    }
    if (!contains(ContributionPath.class)) {
      addClasses(ContributionPath.class);
    }
    addPackages(true, "org.silverpeas.core.node");
    addPackages(true, "org.silverpeas.core.contribution.publication.service");
    addPackages(true, "org.silverpeas.core.contribution.publication.model");
    addPackages(true, "org.silverpeas.core.contribution.publication.notification");
    addPackages(true, "org.silverpeas.core.contribution.publication.dao");
    addPackages(true, "org.silverpeas.core.contribution.rating");
    addPackages(true, "org.silverpeas.core.socialnetwork.model");
    addPackages(true, "org.silverpeas.core.security");
    addClasses(SocialInformationPublication.class);
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
      addMavenDependencies("org.antlr:ST4");
      addPackages(true, "org.silverpeas.core.template");
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
      addPackages(true, "org.silverpeas.core.admin.quota");
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
      addPackages(true, "org.silverpeas.core.notification.user.client");
      addPackages(true, "org.silverpeas.core.notification.user.server");
      addPackages(true, "org.silverpeas.core.notification.user.builder");
      addAsResource("org/silverpeas/notificationManager");
      addClasses(AbstractTable.class, UserSubscriptionNotificationSendingHandler.class,
          AttachmentLink.class);
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
    addClasses(NotificationManagerSettings.class);
    addAsResource(
        "org/silverpeas/notificationManager/settings/notificationManagerSettings.properties");
    return this;
  }

  /**
   * Sets synchronous/asynchronous resource event features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSynchAndAsynchResourceEventFeatures() {
    addClasses(DecodingException.class, EncodingException.class);
    addClasses(NotificationManagerSettings.class);
    addAsResource(
        "org/silverpeas/notificationManager/settings/notificationManagerSettings.properties");
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
      addClasses(Administration.class, PublicationTemplateException.class);
      addClasses(RightRecover.class);
      addClasses(DriverSettings.class);
      addClasses(ProfiledObjectType.class);
      addClasses(ProfiledObjectId.class);
      addClasses(ProfiledObjectIds.class);
      addClasses(BaseRightProfile.class);
      addClasses(Pagination.class);
      addClasses(PaginationPage.class);
      addClasses(RightProfile.class);
      addPackages(true, "org.silverpeas.core.i18n");
      addPackages(true, "org.silverpeas.core.admin.component");
      addPackages(true, "org.silverpeas.core.admin.space");
      addPackages(true, "org.silverpeas.core.admin.service");
      addPackages(true, "org.silverpeas.core.admin.user");
      addPackages(true, "org.silverpeas.core.admin.domain");
      addPackages(true, "org.silverpeas.core.admin.domain.driver.sqldriver");
      addPackages(true, "org.silverpeas.core.admin.component.notification");
      addPackages(true, "org.silverpeas.core.clipboard");
      addAsResource("xmlcomponents");
      addAsResource("org/silverpeas/admin");

      // Centralized features
      addProcessFeatures();
      addDatabaseToolFeatures();
      addJpaPersistenceFeatures();
      addQuotaBasesFeatures();
      addStringTemplateFeatures();
      addAdministrationUtilities();
      addCommonUserBeans();
      addOrganisationFeatures();
      addSchedulerFeatures();
      addSilverpeasContentFeatures();
      addIndexEngineFeatures();
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
    addClasses(OrganizationController.class, OrganizationControllerProvider.class,
        UserFavoriteSpaceBean.class, UserFavoriteSpaceVO.class, UserFavoriteSpaceService.class,
        UserFavoriteSpaceServiceImpl.class, UserFavoriteSpaceServiceProvider.class);
    addPackages(true, "org.silverpeas.core.admin.persistence");
    addPackages(true, "com.stratelia.webactiv.persistence");
    // Centralized features
    addAdministrationFeatures();
    return this;
  }

  /**
   * Sets manual CDI features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addServiceProviderFeatures() {
    addClasses(CDIContainer.class).addPackages(true, "org.silverpeas.core.initialization");
    return this;
  }

  /**
   * Sets component instance deletion features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addComponentInstanceDeletionFeatures() {
    addClasses(ComponentInstanceDeletion.class);
    return this;
  }

  /**
   * Add scheduler features in web archive (war) with quartz libraries.
   * @return the instance of the war builder with scheduler features.
   */
  public WarBuilder4LibCore addSchedulerFeatures() {
    if (!contains("org.silverpeas.core.scheduler")) {
      addMavenDependencies("org.quartz-scheduler:quartz");
      addPackages(true, "org.silverpeas.core.scheduler");
      addAsResource("org/silverpeas/scheduler/settings/persistent-scheduler.properties");
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
    addMavenDependencies("com.novell.ldap:jldap", "org.forgerock.ce.opendj:opendj-server");
    return this;
  }

  /**
   * Add process feature in web archive (war)
   * @return the instance of the war builder with process features
   */
  public WarBuilder4LibCore addProcessFeatures() {
    addPackages(true, "org.silverpeas.core.process");
    return this;
  }

  /**
   * Add calendar feature in web archive (war)
   * @return the instance of the war builder with calendar features
   */
  public WarBuilder4LibCore addCalendarBaseFeatures() {
    addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-api");
    addClasses(
        ICal4JCalendarEventOccurrenceGenerator.class,
        DefaultCalendarEventOccurrenceRepository.class,
        DefaultCalendarRepository.class,
        DefaultCalendarEventRepository.class,
        ICal4JImporter.class,
        ICal4JExporter.class,
        ICal4JDateCodec.class,
        ICal4JRecurrenceCodec.class);
    return this;
  }

  /**
   * Add calendar feature in web archive (war)
   * @return the instance of the war builder with calendar features
   */
  public WarBuilder4LibCore addCalendarSynchronizationFeatures() {
    addMavenDependencies("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer");
    addWysiwygFeatures();
    addClasses(HtmlSanitizer.class, DefaultHtmlSanitizer.class);
    return this;
  }

  /**
   * Add image tool features. ImageMagick must be installed on machine.
   * @return the instance of the war with image tool feature.
   */
  public WarBuilder4LibCore addImageToolFeatures() {
    addMavenDependencies("org.im4java:im4java");
    addPackages(true, "org.silverpeas.core.io.media.image");
    return this;
  }

  /**
   * Add apache file upload libraries in web archive (war)
   * @return the instance of the war builder with apache file upload
   */
  public WarBuilder4LibCore addApacheFileUploadFeatures() {
    addMavenDependencies("commons-fileupload:commons-fileupload");
    return this;
  }

  /**
   * Add Web Browser Edition management services in web archive (war)
   * @return the instance of the war builder with WBE management
   */
  public WarBuilder4LibCore addWbeManagementFeatures() {
    addClasses(StubbedWbeHostManager.class);
    return this;
  }

  /**
   * Add stubbed organization controller which is dealing behind with a mocked instance.
   * @return the instance of the war builder with the stub.
   */
  public WarBuilder4LibCore addStubbedOrganizationController() {
    addOrganisationFeatures();
    addClasses(StubbedOrganizationController.class);
    return this;
  }

}
