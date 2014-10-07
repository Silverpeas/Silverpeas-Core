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

import com.silverpeas.ui.DisplayI18NHelper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.admin.user.constant.UserState;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.util.*;
import org.silverpeas.util.exception.RelativeFileAccessException;
import org.silverpeas.util.pool.ConnectionPool;

/**
 * @author Yohann Chastagnier
 */
public class LibCoreTestArchiveDeploymentBuilder extends TestArchiveDeploymentBuilder {


  /**
   * Gets an initialized instance of a integration test configurator.
   * @return the initialized instance of the configurator.
   */
  public static LibCoreTestArchiveDeploymentBuilder initialize() {
    return new LibCoreTestArchiveDeploymentBuilder();
  }

  /**
   * Hidden constructor.
   */
  private LibCoreTestArchiveDeploymentBuilder() {
    super();
    withManualCDIFeatures();
  }

  @SuppressWarnings("unchecked")
  @Override
  public LibCoreTestArchiveDeploymentBuilder onJar() {
    return super.onJar();
  }

  @SuppressWarnings("unchecked")
  @Override
  public LibCoreTestArchiveDeploymentBuilder onWar() {
    return super.onWar();
  }

  /**
   * Sets cache features:
   * <ul>
   * <li>ehcache maven dependencies</li>
   * <li>org.silverpeas.cache</li>
   * </ul>
   * @return the instance of the configurator.
   */
  public <T extends Archive<T>> LibCoreTestArchiveDeploymentBuilder addCacheFeatures() {
    apply((context) -> {
      if (!context.getArchive().contains("org.silverpeas.cache")) {
        addMavenDependencies("net.sf.ehcache:ehcache-core");
        context.getContainer().addPackages(true, "org.silverpeas.cache");
      }
    });
    return this;
  }

  /**
   * Sets util classes.
   * @return the instance of the configurator.
   */
  public LibCoreTestArchiveDeploymentBuilder addUtilClasses() {
    apply((context) -> {
      if (!context.getArchive().contains(StringUtil.class.getName())) {
        context.getContainer().addClasses(StringUtil.class);
      }
      if (!context.getArchive().contains(CollectionUtil.class.getName())) {
        context.getContainer().addClasses(CollectionUtil.class);
      }
      if (!context.getArchive().contains(AssertArgument.class.getName())) {
        context.getContainer().addClasses(AssertArgument.class);
      }
    });
//    getCurrentContainer().addClasses(StringUtil.class, CollectionUtil.class,
// AssertArgument.class);
    return this;
  }

  /**
   * Sets bundle features.
   * @return the instance of the configurator.
   */
  private LibCoreTestArchiveDeploymentBuilder addBundleFeatures() {
    apply((context) -> {
      context.getContainer().addClasses(ResourceLocator.class, DisplayI18NHelper.class,
          ConfigurationClassLoader.class, ConfigurationControl.class, ResourceBundleWrapper.class,
          FileUtil.class, MimeTypes.class, RelativeFileAccessException.class,
          GeneralPropertiesManager.class);
      context.getContainer().addAsResource("org/silverpeas/general.properties");
      context.getContainer().addAsResource("org/silverpeas/multilang/generalMultilang.properties");
      context.getContainer().addAsResource("org/silverpeas/lookAndFeel/generalLook.properties");
    });
    return this;
  }

  /**
   * Sets persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addBundleFeatures()}</li>
   * <li>{@link #addCacheFeatures()}</li>
   * <li>{@link #addUtilClasses()}</li>
   * </ul>
   * @return the instance of the configurator.
   */
  public LibCoreTestArchiveDeploymentBuilder addPersistenceFeatures() {
    addBundleFeatures();
    addCacheFeatures();
    addUtilClasses();
    apply((context) -> {
      context.getContainer().addPackages(true, "org.silverpeas.admin.user.constant");
      context.getContainer().addPackages(true, "org.silverpeas.persistence.model");
      context.getContainer().addPackages(true, "org.silverpeas.persistence.repository");
      context.getContainer().addClasses(DBUtil.class, ConnectionPool.class, Transaction.class,
          TransactionProvider.class, UserDetail.class, UserAccessLevel.class, UserState.class);
      if (context.getContainer() instanceof JavaArchive) {
        context.getContainer()
            .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml");
      } else {
        context.getContainer()
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
      }
    });
    return this;
  }

  /**
   * Sets manual CDI features.
   * @return the instance of the configurator.
   */
  private LibCoreTestArchiveDeploymentBuilder withManualCDIFeatures() {
    applyOnWar((war) -> war.addClass(ServiceProvider.class).addClass(BeanContainer.class)
        .addClass(CDIContainer.class)
        .addAsResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "META-INF/services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));
    return this;
  }
}
