/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Takes care of registering and de-registering local ImageIO plugins (service providers) for
 * the Silverpeas application context.
 * <p>
 * There is a known issue with ImageIO plug-ins and containers (like a web/servlet container).
 * Deploying plug-ins as part of the web application is not well supported by ImageIO. The ImageIO
 * registry that keeps track of registered plug-ins is in effect JVM global (it is actually a
 * registry per application context, however, there is usually only a single application context).
 * This initializer take care of this issue by explicitly mange the registering and the
 * de-registering of the available ImageIO plugins.
 * </p>
 * <p>
 * Registers all available plugins at the initialization of Silverpeas, using
 * {@code ImageIO.scanForPlugins()}, to make sure they are available to the current application
 * context. De-registers all plugins which have the
 * {@link Thread#getContextClassLoader() current thread's context class loader} as its class loader
 * on {@code release} event, to avoid class/resource leak.
 * </p>
 * <p>
 * This code was fetched from the Twelve Monkeys
 * <a href="https://github.com/haraldk/TwelveMonkeys/blob/master/servlet/src/main/java/com/twelvemonkeys/servlet/image/IIOProviderContextListener.java">IIOProviderContextListener.java></a>
 * code.</p>
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @see javax.imageio.ImageIO#scanForPlugins()
 */
@Technical
@Bean
@Singleton
public class IIOProviderInitializer implements Initialization {

  @Override
  public void init() {
    // Registers all locally available IIO plugins.
    SilverLogger.getLogger(this).info("Registers all available IIO plugins...");
    ImageIO.scanForPlugins();
  }

  @Override
  public void release() {
    // De-register any locally registered IIO plugins. Relies on each app having its own context
    // class loader.
    SilverLogger.getLogger(this).info("Unregisters all available IIO plugins...");
    LocalFilter localFilter = new LocalFilter(Thread.currentThread().getContextClassLoader());
    IIORegistry registry = IIORegistry.getDefaultInstance();
    Iterator<Class<?>> categories = registry.getCategories();
    while (categories.hasNext()) {
      deregisterLocalProvidersForCategory(registry, localFilter, categories.next());
    }
  }

  private static <T> void deregisterLocalProvidersForCategory(IIORegistry registry,
      LocalFilter localFilter, Class<T> category) {
    Iterator<T> providers = registry.getServiceProviders(category, localFilter, false);

    // Copy the providers, as de-registering while iterating over providers will lead to ConcurrentModificationExceptions.
    List<T> providersCopy = new ArrayList<>();
    while (providers.hasNext()) {
      providersCopy.add(providers.next());
    }

    for (T provider : providersCopy) {
      registry.deregisterServiceProvider(provider, category);
    }
  }


  static class LocalFilter implements ServiceRegistry.Filter {
    private final ClassLoader loader;

    public LocalFilter(ClassLoader loader) {
      this.loader = loader;
    }

    public boolean filter(Object provider) {
      return provider.getClass().getClassLoader() == loader;
    }
  }
}
  