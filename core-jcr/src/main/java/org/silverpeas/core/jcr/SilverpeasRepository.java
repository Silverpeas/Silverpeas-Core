/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.jcr;

import javax.jcr.Credentials;
import javax.jcr.GuestCredentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Implementation of the {@link Repository} in Silverpeas. The {@link SilverpeasRepository} objects
 * are created by a {@link SilverpeasRepositoryFactory} instance.
 * <p>
 * It is in fact a wrapper of the actual {@link Repository} object used to access the JCR with the
 * goal to wrap the login/logout operations in order to implement a reentrant JCR session. Indeed,
 * in Silverpeas several accesses to the JCR can be performed within a single flow of treatment (and
 * hence a single thread) and these accesses can be asked by independent services. As these services
 * don't know what it was done out of their own scope, they cannot know whether a session has been
 * already opened with the JCR and hence ask for their own use to open a session. In order to avoid
 * inter-blocking and interleaving session, a mechanism of reentrant session is implemented in
 * Silverpeas with the aim to reuse an opened session within the same single thread.
 * <p>
 * So, when a call to a <code>login</code> method is performed, the first thing the method does is
 * to check a session is already opened. In this case, this session is just returned, otherwise a
 * true login is performed driving to the creation and the opening of a new session to the workspace
 * of the repository (in Silverpeas, there is only one workspace).
 * @author mmoquillon
 */
public class SilverpeasRepository implements Repository {

  private final Repository repository;

  /**
   * Wraps the specified repository to enrich it with a reentrant session mechanism.
   * @param repository the repository created by the JCR implementation used in Silverpeas.
   * @return a {@link SilverpeasRepository} instance.
   */
  static SilverpeasRepository wrap(final Repository repository) {
    return repository instanceof SilverpeasRepository ?
        (SilverpeasRepository) repository :
        new SilverpeasRepository(repository);
  }

  /**
   * Constructor to be used only by {@link SilverpeasRepository#wrap(Repository)} or by any
   * subclasses of {@link SilverpeasRepository}.
   * @param repository the JCR repository to wrap.
   */
  protected SilverpeasRepository(final Repository repository) {
    this.repository = repository;
  }

  @Override
  public String[] getDescriptorKeys() {
    return repository.getDescriptorKeys();
  }

  @Override
  public boolean isStandardDescriptor(final String key) {
    return repository.isStandardDescriptor(key);
  }

  @Override
  public boolean isSingleValueDescriptor(final String key) {
    return repository.isSingleValueDescriptor(key);
  }

  @Override
  public Value getDescriptorValue(final String key) {
    return repository.getDescriptorValue(key);
  }

  @Override
  public Value[] getDescriptorValues(final String key) {
    return repository.getDescriptorValues(key);
  }

  @Override
  public String getDescriptor(final String key) {
    return repository.getDescriptor(key);
  }

  @Override
  public JCRSession login(final Credentials credentials, final String workspaceName)
      throws RepositoryException {
    return JCRSession.open(credentials, c -> repository.login(c, workspaceName));
  }

  @Override
  public JCRSession login(final Credentials credentials) throws RepositoryException {
    return JCRSession.open(credentials, repository::login);
  }

  @Override
  public JCRSession login(final String workspaceName)
      throws RepositoryException {
    return JCRSession.open(new GuestCredentials(), c -> repository.login(workspaceName));
  }

  @Override
  public JCRSession login() throws RepositoryException {
    return JCRSession.open(new GuestCredentials(), c -> repository.login());
  }

  /**
   * Gets the wrapped repository.
   * @return the JCR repository used in Silverpeas.
   */
  protected Repository getRepository() {
    return repository;
  }
}
