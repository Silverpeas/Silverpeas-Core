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

package org.silverpeas.core.jcr.impl.oak.configuration;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * <p>
 * Configuration of the {@link javax.jcr.Repository}. It indicates what storage backend to use for
 * the repository in Oak and, for the storage chosen, what are the parameters to initialize the
 * {@link javax.jcr.Repository} instance.
 * </p>
 * <p>
 * In Oak, a repository can be either created programmatically or automatically through OSGi. Latter
 * is the more flexible and simpler way but it requires a load of a full of bloatware dependencies
 * on both OSGi and Felix (an implementation of OSGi). Using the OSGi way doesn't require the
 * application to be an OSGi service; the examples of using Oak in the Oak project itself illustrate
 * this by using the OSGi configuration of Oak to create a repository.
 * </p>
 * <p>
 * The solution chosen here is to take in charge itself the creation of the
 * {@link javax.jcr.Repository} through our own implementation of the
 * {@link javax.jcr.RepositoryFactory} but, as it is with the OSGi way, by using a custom
 * configuration file to parameterize the repository. This requires some additional code and this
 * doesn't profit of the flexibility in the parameterizing brought by OSGi configuration.
 * Nevertheless it avoids to add both useless complexity in our case and dependencies which are not
 * needed elsewhere in Silverpeas. Some of the configuration parameters are taken or inspired from
 * the
 * <a href="https://jackrabbit.apache.org/oak/docs/osgi_config.html">Repository OSGi
 * Configuration</a>.
 * </p>
 * @author mmoquillon
 */
public class OakRepositoryConfiguration {

  private final StorageType storage;

  private final SegmentNodeStoreConfiguration segmentNodeStore;

  private final DocumentNodeStoreConfiguration documentNodeStore;

  /**
   * Loads the configuration file located at the specified absolute path.
   * @param path the absolute path of the properties file to load.
   * @return an instance of {@link OakRepositoryConfiguration} with all the configuration properties
   * loaded from the given properties file.
   * @throws IOException if an error occurs while accessing the file or loading the properties from
   * the file.
   */
  public static OakRepositoryConfiguration load(final String path) throws IOException {
    try (InputStream input = openConfigFileAt(path)) {
      Properties properties = new Properties();
      properties.load(input);
      return new OakRepositoryConfiguration(properties);
    }
  }

  private OakRepositoryConfiguration(@Nonnull Properties props) {
    Objects.requireNonNull(props, "The configuration of the Oak repository shouldn't be null");
    this.storage = StorageType.fromValue(props.getProperty("storage"));
    this.segmentNodeStore = new SegmentNodeStoreConfiguration(props);
    this.documentNodeStore = new DocumentNodeStoreConfiguration(props);
  }

  /**
   * Gets the type of storage to use for the JCR.
   * @return a {@link StorageType} value.
   */
  public StorageType getStorageType() {
    return storage;
  }

  /**
   * Gets the configuration to create a repository with a Segment Node Store as backend.
   * @return a {@link SegmentNodeStoreConfiguration} instance with the properties to initialize the
   * {@link javax.jcr.Repository} instance.
   */
  public SegmentNodeStoreConfiguration getSegmentNodeStoreConfiguration() {
    return segmentNodeStore;
  }

  /**
   * Gets the configuration to create a repository with a Document Node Store as backend.
   * @return a {@link DocumentNodeStoreConfiguration} instance with the properties to initialize the
   * {@link javax.jcr.Repository} instance.
   */
  public DocumentNodeStoreConfiguration getDocumentNodeStoreConfiguration() {
    return documentNodeStore;
  }

  private static InputStream openConfigFileAt(final String path) throws IOException {
    if (path.startsWith("classpath:")) {
      return openInClassPath(path.substring(10));
    }
    Path configFilePath = Path.of(path);
    if (Files.exists(configFilePath)) {
      return Files.newInputStream(configFilePath);
    } else {
      return openInClassPath(path);
    }
  }

  private static InputStream openInClassPath(final String path) throws IOException {
    InputStream inputStream = OakRepositoryConfiguration.class.getResourceAsStream(path);
    if (inputStream == null) {
      throw new FileNotFoundException("The configuration file at " + path +
          " isn't found neither in the filesystem nor in the classpath");
    }
    return inputStream;
  }

}
