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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
public abstract class WarBuilder<T extends WarBuilder>
    implements Builder<WebArchive>, CommonWebArchive<T> {

  public Collection<String> mavenDependencies = new HashSet<>(Arrays.asList("com.ninja-squad:DbSetup", "org.apache.commons:commons-lang3",
          "commons-codec:commons-codec", "commons-io:commons-io", "org.silverpeas.core:test-core"));

  private WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");

  /**
   * Hidden constructor.
   */
  protected WarBuilder() {
    // Nothing is done in the constructor.
  }

  /**
   * Adds maven dependencies.
   * @param mavenDependencies the canonical maven dependencies to add.
   * @return the instance of the configurator.
   */
  @SuppressWarnings("unchecked")
  public T addMavenDependencies(String... mavenDependencies) {
    Collections.addAll(this.mavenDependencies, mavenDependencies);
    return (T) this;
  }

  @Override
  public boolean contains(final String path) throws IllegalArgumentException {
    return war.contains(path);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addClasses(final Class<?>... classes) throws IllegalArgumentException {
    war.addClasses(classes);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addPackages(final boolean recursive, final String... packages)
      throws IllegalArgumentException {
    war.addPackages(recursive, packages);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addAsResource(final String resourceName) throws IllegalArgumentException {
    war.addAsResource(resourceName);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addAsResource(String resourceName, String target) throws IllegalArgumentException {
    war.addAsResource(resourceName, target);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addAsWebInfResource(final String resourceName, final String target)
      throws IllegalArgumentException {
    war.addAsWebInfResource(resourceName, target);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T addAsWebInfResource(final Asset resource, final String target)
      throws IllegalArgumentException {
    war.addAsWebInfResource(resource, target);
    return (T) this;
  }

  /**
   * Builds the final WAR archive. The following stuffs are automatically added :
   * <ul>
   *   <li>The <b>beans.xml</b> in order to activate CDI</li>
   *   <li>The <b>test-ds.xml</b> resource in order to define a data source for tests</li>
   * </ul>
   * @return the built WAR archive.
   */
  @Override
  public final WebArchive build() {
    File[] libs =
        Maven.resolver().loadPomFromFile("pom.xml").resolve(mavenDependencies).withTransitivity()
            .asFile();
    war.addAsLibraries(libs);
    war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    war.addAsWebInfResource("test-ds.xml", "test-ds.xml");
    return war;
  }

  /**
   * Applies the configuration that the test is focused on.
   * @param testFocus the instance of an anonymous implementation of {@link TestFocus} interface.
   * @return the instance of the war builder.
   */
  @SuppressWarnings("unchecked")
  public final T testFocusedOn(TestFocus testFocus) {
    testFocus.focus(this);
    return (T) this;
  }

  /**
   * Applies a configuration by using directly the {@link WebArchive} instance provided by the
   * ShrinkWrap API.
   * @param onShrinkWrapWar the instance of an anonymous implementation of {@link org.silverpeas
   * .test.WarBuilder.OnShrinkWrapWar}
   * interface.
   * @return the instance of the war builder.
   */
  @SuppressWarnings("unchecked")
  public final T applyManually(OnShrinkWrapWar onShrinkWrapWar) {
    onShrinkWrapWar.applyManually(war);
    return (T) this;
  }

  /**
   * In order to highlight the specification of tested classes, packages or resources.
   */
  public interface TestFocus<B extends CommonArchive<B>> {
    void focus(B warBuilder);
  }
}
