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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * This class permits to setup an integration test
 * @author Yohann Chastagnier
 */
public abstract class IntegrationTestConfigurator {

  private JavaArchive jar;
  private WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
  private ServiceProviderContainer current = war;

  private Collection<String> mavenDependencies = new HashSet<>(Arrays
      .asList("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
          "commons-codec:commons-codec", "commons-io:commons-io", "org.silverpeas.core:test-core"));

  /**
   * Hidden constructor.
   */
  protected IntegrationTestConfigurator() {
  }

  /**
   * Sets the current set archive to the JAR one.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public <T extends IntegrationTestConfigurator> T onJar() {
    current = getJar();
    return (T) this;
  }

  /**
   * Sets the current set archive to the WAR one.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public <T extends IntegrationTestConfigurator> T onWar() {
    current = getWar();
    return (T) this;
  }

  /**
   * Gets the current container that has been set by {@link #onJar()} or {@link #onWar()} methods.
   * @return the current archive.
   */
  @SuppressWarnings("unchecked")
  private <T extends Archive<T>> ServiceProviderContainer<T> getCurrentContainer() {
    return current;
  }

  /**
   * Gets the current archive that has been set by {@link #onJar()} or {@link #onWar()} methods.
   * @return the current archive.
   */
  @SuppressWarnings("unchecked")
  private <T extends Archive<T>> Archive<T> getCurrentArchive() {
    return (Archive) current;
  }

  /**
   * Adds maven dependencies.
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public <T extends IntegrationTestConfigurator> T addMavenDependencies(
      String... mavenDependencies) {
    Collections.addAll(this.mavenDependencies, mavenDependencies);
    return (T) this;
  }

  /**
   * Gets the jar archive. If this method is never called, no jar archive is created for the test.
   * @return the jar archive.
   */
  private JavaArchive getJar() {
    if (jar == null) {
      jar = ShrinkWrap.create(JavaArchive.class, "test.jar")
          .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    return jar;
  }

  /**
   * Gets the war archive.
   * @return the war archive.
   */
  private WebArchive getWar() {
    return war;
  }

  /**
   * Builds the final WAR archive.
   * @return the built WAR archive.
   */
  public final WebArchive buildWar() {
    File[] libs =
        Maven.resolver().loadPomFromFile("pom.xml").resolve(mavenDependencies).withTransitivity()
            .asFile();
    war.addAsLibraries(libs);
    if (jar != null) {
      war.addAsLibraries(jar);
    }
    war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    war.addAsWebInfResource("test-ds.xml", "test-ds.xml");
    return war;
  }

  /**
   * Calls configuration on current test library.
   * @param onCurrent the configuration to apply.
   * @param <T> the type of the test configurator.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public final <T extends IntegrationTestConfigurator> T apply(OnCurrent onCurrent) {
    onCurrent.apply(new OnCurrentContext(getCurrentArchive(), getCurrentContainer()));
    return (T) this;
  }

  /**
   * Calls configuration on JAR test library.
   * @param onJar the configuration to apply.
   * @param <T> the type of the test configurator.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends IntegrationTestConfigurator> T applyOnJar(OnJar onJar) {
    onJar.apply(getJar());
    return (T) this;
  }

  /**
   * Calls configuration on WAR test archive.
   * @param onWar the configuration to apply.
   * @param <T> the type of the test configurator.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  protected final <T extends IntegrationTestConfigurator> T applyOnWar(OnWar onWar) {
    onWar.apply(getWar());
    return (T) this;
  }

  /**
   * Context of current configuration.
   */
  public class OnCurrentContext {
    private final Archive<?> archive;
    private final ServiceProviderContainer<?> container;

    private OnCurrentContext(final Archive<?> archive,
        final ServiceProviderContainer<?> container) {
      this.archive = archive;
      this.container = container;
    }

    public Archive<?> getArchive() {
      return archive;
    }

    public ServiceProviderContainer<?> getContainer() {
      return container;
    }
  }

  /**
   * To add configuration on current test container (JAR library or WAR archive : the current
   * container that has been set by {@link #onJar()} or {@link #onWar()} methods).
   */
  public interface OnCurrent {
    public void apply(final OnCurrentContext context);
  }

  /**
   * To add configuration on JAR test library.
   */
  protected interface OnJar {
    public void apply(JavaArchive jar);
  }

  /**
   * To add configuration on WAR test archive.
   */
  protected interface OnWar {
    public void apply(WebArchive war);
  }
}
