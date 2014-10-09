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
import org.silverpeas.admin.user.constant.UserAccessLevel;
import org.silverpeas.admin.user.constant.UserState;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.TransactionProvider;
import org.silverpeas.util.*;
import org.silverpeas.util.exception.RelativeFileAccessException;
import org.silverpeas.util.pool.ConnectionPool;

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
    warBuilder.withServiceProviderFeatures();
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
  public <T extends Archive<T>> WarBuilder4LibCore addCacheFeatures() {
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
   * <li>{@link AssertArgument}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addCommonBasicUtilities() {
    if (!contains(StringUtil.class.getName())) {
      addClasses(StringUtil.class);
    }
    if (!contains(CollectionUtil.class.getName())) {
      addClasses(CollectionUtil.class);
    }
    if (!contains(AssertArgument.class.getName())) {
      addClasses(AssertArgument.class);
    }
    return this;
  }

  /**
   * Adds bundle features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore addBundleBaseFeatures() {
    addClasses(ResourceLocator.class, DisplayI18NHelper.class, ConfigurationClassLoader.class,
        ConfigurationControl.class, ResourceBundleWrapper.class, FileUtil.class, MimeTypes.class,
        RelativeFileAccessException.class, GeneralPropertiesManager.class);
    addAsResource("org/silverpeas/general.properties");
    addAsResource("org/silverpeas/multilang/generalMultilang.properties");
    addAsResource("org/silverpeas/lookAndFeel/generalLook.properties");
    return this;
  }

  /**
   * Sets persistence features.
   * Calls automatically:
   * <ul>
   * <li>{@link #addBundleBaseFeatures()}</li>
   * <li>{@link #addCacheFeatures()}</li>
   * <li>{@link #addCommonBasicUtilities()}</li>
   * </ul>
   * @return the instance of the war builder.
   */
  public WarBuilder4LibCore addPersistenceFeatures() {
    addBundleBaseFeatures();
    addCacheFeatures();
    addCommonBasicUtilities();
    addPackages(true, "org.silverpeas.admin.user.constant");
    addPackages(true, "org.silverpeas.persistence.model");
    addPackages(true, "org.silverpeas.persistence.repository");
    addClasses(DBUtil.class, ConnectionPool.class, Transaction.class, TransactionProvider.class,
        UserDetail.class, UserAccessLevel.class, UserState.class);
    addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");
    return this;
  }

  /**
   * Sets manual CDI features.
   * @return the instance of the war builder.
   */
  private WarBuilder4LibCore withServiceProviderFeatures() {
    addClasses(ServiceProvider.class, BeanContainer.class, CDIContainer.class).addAsResource("META-INF/services/test-org.silverpeas.util.BeanContainer",
            "META-INF/services/org.silverpeas.util.BeanContainer")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    return this;
  }
}
