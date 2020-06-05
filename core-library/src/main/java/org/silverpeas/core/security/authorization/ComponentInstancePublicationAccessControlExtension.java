/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Base;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;

import javax.enterprise.util.AnnotationLiteral;

/**
 * This interface extends access controller extension for a Publication resource.
 * <p>
 * This interface defines an extension for {@link PublicationAccessControl} which is specific to
 * a {@link SilverpeasComponentInstance}.
 * </p>
 * <p>
 * If a component has specific rules about {@link PublicationAccessControl} mechanism, it should
 * implement this interface in order to apply them.
 * </p>
 * <p>
 * Any application that requires to implement this interface MUST qualify with the
 * {@link javax.inject.Named} annotation by a name satisfying the following convention
 * <code>[COMPONENT NAME]InstancePublicationAccessControlExtension</code>. For example, for an
 * application Kmelia, the implementation must be qualified with <code>@Named
 * ("kmeliaInstancePublicationAccessControlExtension")
 * </code>
 * <p>
 * @author silveryocha
 */
public interface ComponentInstancePublicationAccessControlExtension {

  /**
   * Constants are predefined value used by a contribution manager to work with and that carries a
   * semantic that has to be shared by all the implementations of this interface.
   */
  class Constants {

    private Constants() {}

    /**
     * The predefined suffix that must compound the name of each implementation of this interface.
     * An implementation of this interface by a Silverpeas application named Kmelia must be named
     * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
     */
    public static final String NAME_SUFFIX = "InstancePublicationAccessControlExtension";
  }

  /**
   * Gets the {@link ComponentInstancePublicationAccessControlExtension} according to the given
   * identifier of component instance.
   * <p>
   * Instances of {@link ComponentInstancePublicationAccessControlExtension} are request scoped
   * (or thread scoped on backend treatments).
   * </p>
   * @param instanceId the identifier of a component instance from which the qualified name of the
   * implementation will be extracted.
   * @return a {@link ComponentInstancePublicationAccessControlExtension} implementation.
   */
  @SuppressWarnings("serial")
  static ComponentInstancePublicationAccessControlExtension getByInstanceId(final String instanceId) {
    final SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    final String cacheKey = ComponentInstancePublicationAccessControlExtension.class.getName() + "###" + instanceId;

    final Mutable<ComponentInstancePublicationAccessControlExtension> accessControlExtension =
        Mutable.ofNullable(cache.get(cacheKey, ComponentInstancePublicationAccessControlExtension.class));
    if (accessControlExtension.isPresent()) {
      return accessControlExtension.get();
    }

    try {
      accessControlExtension.set(ServiceProvider
          .getServiceByComponentInstanceAndNameSuffix(instanceId, Constants.NAME_SUFFIX));
    } catch (IllegalStateException e) {
      // Default implementation if none existing for the component
      accessControlExtension.set(ServiceProvider.getService(
          ComponentInstancePublicationAccessControlExtension.class, new AnnotationLiteral<Base>() {}));
    }
    cache.put(cacheKey, accessControlExtension.get());
    return accessControlExtension.get();
  }

  boolean canPublicationBePersistedOrDeletedBy(final PublicationDetail publication,
      final String instanceId, final String userId, final SilverpeasRole userRole,
      final AccessControlContext context);
}
