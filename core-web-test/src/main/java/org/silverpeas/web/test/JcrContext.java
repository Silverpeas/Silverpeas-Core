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
package org.silverpeas.web.test;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.persistence.jcr.JcrRepositoryProvider;
import org.silverpeas.core.persistence.jcr.SilverpeasJcrSchemaRegistering;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.jcr.Repository;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * This class handle a JCR test.<br/>
 * The repository is created before and deleted after each test.<br/>
 * The registered physical files are also deleted.
 * @author Yohann Chastagnier
 */
public class JcrContext implements TestRule {

  private static final String TEST_REPOSITORY_LOCATION = "test-jcr_";
  public static final String REPOSITORY_IN_MEMORY_XML = "/test-repository-in-memory.xml";

  private JcrTestContext context = null;

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        final Path repositoryPath = Files.createTempDirectory(TEST_REPOSITORY_LOCATION);
        try (final InputStream configStream = JcrContext.class.getResourceAsStream(
            REPOSITORY_IN_MEMORY_XML);) {
          context = new JcrTestContext(configStream, repositoryPath);
          beforeEvaluate(context);
          base.evaluate();
        } finally {
          try {
            afterEvaluate(context);
          } finally {
            FileUtils.deleteQuietly(repositoryPath.toFile());
          }
        }
      }
    };
  }

  protected void beforeEvaluate(JcrTestContext context) throws Exception {
    clearFileSystem();
    StubbedJcrRepositoryProvider jcrRepositoryProvider =
        (StubbedJcrRepositoryProvider) ServiceProvider.getService(JcrRepositoryProvider.class);
    jcrRepositoryProvider.setRepository(context.getRepository());
    ServiceProvider.getService(SilverpeasJcrSchemaRegistering.class).init();
  }

  protected void afterEvaluate(JcrTestContext context) {
    try {
      if (context != null) {
        context.getRepository().shutdown();
      }
    } finally {
      clearFileSystem();
    }
  }

  private void clearFileSystem() {
    File file = new File(FileRepositoryManager.getAbsolutePath(""));
    FileUtils.deleteQuietly(file);
    File index = new File(IndexFileManager.getIndexUpLoadPath());
    FileUtils.deleteQuietly(index);
  }

  protected class JcrTestContext {
    private final InputStream configStream;
    private final Path repositoryLocation;
    private final RepositoryConfig config;
    private final JackrabbitRepository repository;

    public JcrTestContext(final InputStream configStream, final Path repositoryPath)
        throws Exception {
      this.configStream = configStream;
      repositoryLocation = repositoryPath.toAbsolutePath();
      config = RepositoryConfig.create(configStream, repositoryLocation.toString());
      repository = RepositoryImpl.create(config);
    }

    public InputStream getConfigStream() {
      return configStream;
    }

    public Path getRepositoryLocation() {
      return repositoryLocation;
    }

    public RepositoryConfig getConfig() {
      return config;
    }

    public JackrabbitRepository getRepository() {
      return repository;
    }
  }

  public JcrTestContext getContext() {
    return context;
  }

  /*
  REPOSITORY PROVIDER
   */

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedJcrRepositoryProvider implements JcrRepositoryProvider {

    private Repository repository;

    @Produces
    @Override
    public Repository getRepository() {
      return repository;
    }

    public void setRepository(final Repository repository) {
      this.repository = repository;
    }
  }

}
