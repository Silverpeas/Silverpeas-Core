/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.security;

import org.silverpeas.core.SilverpeasRuntimeException;

import javax.jcr.Credentials;
import javax.security.auth.spi.LoginModule;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Registry of all {@link javax.security.auth.spi.LoginModule} instances to use when authenticating
 * a user accessing the JCR. These {@link LoginModule} must be a bridge in the security system
 * between the JCR implementation and Silverpeas and as such they should extend the
 * {@link SilverpeasJCRLoginModule} abstract class. By default, the following {@link LoginModule}s
 * are registered:
 * <ul>
 *   <li>{@link SilverpeasSimpleJCRLoginModule} to take in charge the authentication of a user
 *   in Silverpeas by a {@link javax.jcr.SimpleCredentials} in which the user login and
 *   the user password are provided.</li>
 *   <li>{@link SilverpeasTokenJCRLoginModule} to take in charge the authentication of a user in
 *   Silverpeas by a
 *   {@link org.apache.jackrabbit.api.security.authentication.token.TokenCredentials} in which
 *   the API token of the user is provided.</li>
 * </ul>
 * @author mmoquillon
 */
public class LoginModuleRegistry {

  private static final LoginModuleRegistry instance = new LoginModuleRegistry();
  private final Map<Class<? extends Credentials>, List<Supplier<SilverpeasJCRLoginModule>>>
      registry = new HashMap<>();

  @SuppressWarnings("unchecked")
  private LoginModuleRegistry() {
    SilverpeasSimpleJCRLoginModule.SUPPORTED_CREDENTIALS
        .forEach(c -> addLoginModule(c, SilverpeasSimpleJCRLoginModule.class));

    SilverpeasTokenJCRLoginModule.SUPPORTED_CREDENTIALS
        .forEach(c -> addLoginModule(c, SilverpeasTokenJCRLoginModule.class));
  }

  /**
   * Gets an instance of the registry.
   * @return a {@link LoginModuleRegistry} object.
   */
  public static LoginModuleRegistry getInstance() {
    return instance;
  }

  /**
   * Adds the specified {@link LoginModule} class as a processor of any instances of the given
   * credentials type.
   * @param credentialsType a concrete type of {@link Credentials}.
   * @param module a {@link SilverpeasJCRLoginModule} class that will be instantiated on demand to
   * perform an authentication operation.
   */
  public void addLoginModule(final Class<? extends Credentials> credentialsType,
      final Class<? extends SilverpeasJCRLoginModule> module) {
    registry.computeIfAbsent(credentialsType, k -> new ArrayList<>())
        .add(() -> spawn(module));
  }

  /**
   * Gets all the {@link LoginModule} objects that support the specified type of credentials. The
   * modules lifecycle is thread-scoped, meaning they are instantiated per thread, then they are
   * disposed once the thread terminated.
   * @param credentialsType a concrete type of {@link Credentials}.
   * @return a set of {@link LoginModule} objects that can process the specified type of
   * credentials or an empty list if no one can take in charge this type of credentials.
   */
  public Set<SilverpeasJCRLoginModule> getLoginModule(
      final Class<? extends Credentials> credentialsType) {
    return registry.getOrDefault(credentialsType, Collections.emptyList())
        .stream()
        .map(Supplier::get)
        .collect(Collectors.toSet());
  }

  private SilverpeasJCRLoginModule spawn(final Class<? extends SilverpeasJCRLoginModule> clazz) {
    PrivilegedExceptionAction<? extends SilverpeasJCRLoginModule> newLoginModule = () -> {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodHandles.Lookup
          privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
      MethodType constructorType = MethodType.methodType(void.class);
      MethodHandle constructor = privateLookup.findConstructor(clazz, constructorType);
      try {
        //noinspection
        return (SilverpeasJCRLoginModule) constructor.invoke();
      } catch (Throwable e) {
        throw new SilverpeasRuntimeException(e);
      }
    };

    try {
      return AccessController.doPrivileged(newLoginModule);
    } catch (PrivilegedActionException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
