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
import com.silverpeas.admin.components.PasteDetail;
import com.silverpeas.admin.components.PasteDetailFromToPK;
import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.silvertrace.SilverpeasTrace;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.admin.user.constant.UserState;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.contribution.model.Contribution;
import org.silverpeas.core.IdentifiableResource;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.util.*;
import org.silverpeas.util.exception.FromModule;
import org.silverpeas.util.exception.RelativeFileAccessException;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.exception.UtilException;
import org.silverpeas.util.exception.WithNested;
import org.silverpeas.util.fileFolder.FileFolderManager;
import org.silverpeas.util.pool.ConnectionPool;
import org.silverpeas.util.template.SilverpeasStringTemplate;
import org.silverpeas.util.template.SilverpeasStringTemplateUtil;
import org.silverpeas.util.template.SilverpeasTemplate;
import org.silverpeas.util.template.SilverpeasTemplateFactory;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4LibCore extends WarBuilder<WarBuilder4LibCore> {


  /**
   * Gets an instance of an war archive test builder with the following common stuffs:
   * <ul>
   * <li>{@link ServiceProvider} features.</li>
   * </ul>
   * @return the instance of the war archive builder.
   */
  public static WarBuilder4LibCore onWar() {
    WarBuilder4LibCore warBuilder = new WarBuilder4LibCore();
    warBuilder.addServiceProviderFeatures();
    warBuilder.addSilverTraceFeatures();
    return warBuilder;
  }

  /**
   * Adds cache features:
   * <ul>
   * <li>ehcache maven dependencies</li>
   * <li>org.silverpeas.cache</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCacheFeatures() {
    if (!contains("org.silverpeas.cache")) {
      addMavenDependencies("net.sf.ehcache:ehcache-core");
      addPackages(true, "org.silverpeas.cache");
    }
    return this;
  }

  /**
   * Adds common utilities classes:
   * <ul>
   * <li>{@link StringUtil}</li>
   * <li>{@link CollectionUtil}</li>
   * <li>{@link MapUtil}</li>
   * <li>{@link AssertArgument}</li>
   * <li>{@link ActionType} and classes in {@link org.silverpeas.util.annotation}</li>
   * <li>{@link #addSilverpeasContentFeatures()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonBasicUtilities() {
    if (!contains(StringUtil.class.getName())) {
      addClasses(StringUtil.class);
    }
    if (!contains(MapUtil.class.getName())) {
      addClasses(MapUtil.class);
    }
    if (!contains(CollectionUtil.class.getName())) {
      addClasses(CollectionUtil.class, ExtractionList.class, ExtractionComplexList.class);
    }
    if (!contains(AssertArgument.class.getName())) {
      addClasses(AssertArgument.class);
    }
    if (!contains(ActionType.class.getName())) {
      addSilverpeasContentFeatures();
      addClasses(ActionType.class);
      addPackages(false, "org.silverpeas.util.annotation");
    }
    return this;
  }

  /**
   * Sets common user beans.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonUserBeans() {
    if (!contains(UserDetail.class.getName())) {
      addClasses(UserDetail.class, UserAccessLevel.class, UserState.class);
    }
    if (!contains(Domain.class.getName())) {
      addClasses(Domain.class);
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
    if (!contains(SilverpeasException.class.getName())) {
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
    if (!contains(ResourceLocator.class.getName())) {
      addClasses(ResourceLocator.class, DisplayI18NHelper.class, ConfigurationClassLoader.class,
          ConfigurationControl.class, ResourceBundleWrapper.class, FileUtil.class, MimeTypes.class,
          RelativeFileAccessException.class, GeneralPropertiesManager.class);
      addAsResource("org/silverpeas/general.properties");
      addAsResource("org/silverpeas/multilang/generalMultilang.properties");
      addAsResource("org/silverpeas/lookAndFeel/generalLook.properties");
    }
    return this;
  }

  /**
   * Adds URL features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasUrlFeatures() {
    if (!contains(URLManager.class.getName())) {
      addClasses(URLManager.class);
    }
    return this;
  }

  /**
   * Adds common classes to handle silverpeas content features.
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addSilverpeasContentFeatures() {
    if (!contains(Contribution.class.getName())) {
      addClasses(Contribution.class, IdentifiableResource.class);
    }
    if (!contains(SilverpeasContent.class.getName())) {
      addClasses(SilverpeasContent.class);
    }
    if (!contains(SilverContentInterface.class.getName())) {
      addClasses(SilverContentInterface.class);
    }
    if (!contains(WAPrimaryKey.class.getName())) {
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
    if (!contains(FileRepositoryManager.class.getName())) {
      addClasses(FileRepositoryManager.class, FileFolderManager.class);
    }
    return this;
  }

  /**
   * Sets persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addBundleBaseFeatures()}</li>
   * <li>{@link #addCacheFeatures()}</li>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * <li>{@link #addCommonUserBeans()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addPersistenceFeatures() {
    addBundleBaseFeatures();
    addCacheFeatures();
    addCommonBasicUtilities();
    addCommonUserBeans();
    if (!contains(Transaction.class.getName())) {
      addPackages(true, "org.silverpeas.admin.user.constant");
      addPackages(true, "org.silverpeas.persistence.model");
      addPackages(true, "org.silverpeas.persistence.repository");
      addClasses(DBUtil.class, ConnectionPool.class, Transaction.class, TransactionProvider.class);
      addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
    }
    return this;
  }

  /**
   * Sets manual CDI features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addServiceProviderFeatures() {
    addClasses(ServiceProvider.class, BeanContainer.class, CDIContainer.class)
        .addPackages(true, "org.silverpeas.initialization")
        .addAsResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "META-INF/services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    return this;
  }

  /**
   * Sets empty Silver trace implementation features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addSilverTraceFeatures() {
    addClasses(SilverpeasTrace.class, TestSilverpeasTrace.class, SilverTrace.class);
    return this;
  }

  /**
   * Add quartz scheduler libraries in web archive (war)
   * @return the instance of the war builder with quartz scheduler libraries
   */
  public WarBuilder4LibCore addQuartzSchedulerFeatures() {
    return addMavenDependencies("org.quartz-scheduler:quartz");
  }

  /**
   * Add novell jldap libraries in web archive (war)
   * @return the instance of the war builder with novell jldap
   */
  public WarBuilder4LibCore addLDAPFeatures() {
    return addMavenDependencies("com.novell.ldap:jldap", "org.forgerock.opendj:opendj-server");
  }

  /**
   * @return
   */
  public WarBuilder4LibCore addStringTemplateFeatures() {
    return addMavenDependencies("org.antlr:stringtemplate")
        .addClasses(SilverpeasTemplateFactory.class, SilverpeasTemplate.class,
            SilverpeasStringTemplateUtil.class, SilverpeasStringTemplate.class)
        .addAsResource("com/silverpeas/util/stringtemplate.properties");

  }

}
