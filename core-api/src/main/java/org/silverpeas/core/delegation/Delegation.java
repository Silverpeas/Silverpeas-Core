/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.delegation;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * A delegation of roles in a given component instance from a user to another user.
 * A user that delegates whole of his responsibilities is named a <em>delegator</em> whereas the
 * user that receives such responsibilities are named the <em>delegate</em>.
 * @author mmoquillon
 */
@Entity
@Table(name = "sb_delegations")
@NamedQueries({@NamedQuery(name = "Delegation.findByDelegator",
    query = "select d from Delegation d where d.delegatorId = :delegator"),
    @NamedQuery(name = "Delegation.findByDelegate",
        query = "select d from Delegation d where d.delegateId = :delegate"),
    @NamedQuery(name = "Delegation.findByDelegatorByComponentId",
        query = "select d from Delegation d where d.delegatorId = :delegator and " +
            "d.instanceId = :componentId"),
    @NamedQuery(name = "Delegation.findByDelegateByComponentId",
        query = "select d from Delegation d where d.delegateId = :delegate and " +
            "d.instanceId = :componentId")})
public class Delegation extends SilverpeasJpaEntity<Delegation, UuidIdentifier> {

  @Column(nullable = false)
  @NotNull
  private String delegatorId;
  @Column(nullable = false)
  @NotNull
  private String delegateId;
  @Column(nullable = false)
  @NotNull
  private String instanceId;

  protected Delegation() {
    // for JPA
  }

  /**
   * Constructs a new delegation of the whole roles the delegator have in the given component
   * instance between him and a delegate.
   * @param instanceId the unique identifier of a component instance in Silverpeas.
   * @param delegator the user that delegates his whole responsibilities in the component instnace.
   * @param delegate the user that receives the delegation.
   */
  public Delegation(final String instanceId, final User delegator,
      final User delegate) {
    setComponentInstanceId(instanceId);
    setDelegator(delegator);
    setDelegate(delegate);
  }

  /**
   * Prepares the construction of a new delegation of roles in the specified
   * component instance.
   * @param instanceId the unique identifier of a component instance.
   * @return a {@link Constructor} of delegation.
   */
  public static Constructor ofRolesIn(final String instanceId) {
    return new Constructor(instanceId);
  }

  /**
   * Gets all the delegations of roles coming from the specified user.
   * @param aUser a user in Silverpeas.
   * @return a list of {@link Delegation} instances. If no delegations were defined by the
   * specified user, then an empty list is returned.
   */
  public static List<Delegation> getAllFrom(final User aUser) {
    Objects.requireNonNull(aUser, "The delegator of the asked delegations must not be null");
    return DelegationRepository.get().getByDelegator(aUser);
  }

  /**
   * Gets all the delegations of roles in the specified component instance coming from the
   * specified user.
   * @param aUser a user in Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return a list of {@link Delegation} instances. If no delegations were defined by the
   * specified user for his roles in the given component instance then an empty list is returned.
   */
  public static List<Delegation> getAllFrom(final User aUser, final String componentId) {
    Objects.requireNonNull(aUser, "The delegator of the asked delegations must not be null");
    StringUtil.requireDefined(componentId, "The component instance id must be defined");
    return DelegationRepository.get().getByDelegatorAndComponentId(aUser, componentId);
  }

  /**
   * Gets all the delegations of roles that were attributed to the specified user.
   * @param aUser a user in Silverpeas.
   * @return a list of {@link Delegation} instances. If no delegations of roles were attributed to
   * the specified user then an empty list is returned.
   */
  public static List<Delegation> getAllTo(final User aUser) {
    Objects.requireNonNull(aUser, "The delegate of the asked delegations must not be null");
    return DelegationRepository.get().getByDelegate(aUser);
  }

  /**
   * Gets all the delegations of roles in the specified component instance that were attributed
   * to the specified user.
   * @param aUser a user in Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return a list of {@link Delegation} instances. If no delegations of roles in the given
   * component instance were attributed to the specified user then an empty list is returned.
   */
  public static List<Delegation> getAllTo(final User aUser, final String componentId) {
    Objects.requireNonNull(aUser, "The delegate of the asked delegations must not be null");
    StringUtil.requireDefined(componentId);
    return DelegationRepository.get().getByDelegateAndComponentId(aUser, componentId);
  }

  /**
   * Sets a new delegate to this delegation.
   * @param delegate a new user receiving this delegation.
   */
  public void setDelegate(final User delegate) {
    Objects.requireNonNull(delegate, "The delegate to set must not be null");
    requireDifferentUsers(this.delegatorId, delegate.getId());
    this.delegateId = delegate.getId();
  }

  /**
   * Sets the delegator behind this delegation.
   * @param delegator the delegator in this delegation.
   */
  protected final void setDelegator(final User delegator) {
    Objects.requireNonNull(delegator, "The delegator to set must not be null");
    this.delegatorId = delegator.getId();
  }

  /**
   * Sets the component instance that is concerned by this delegation of roles.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  protected final void setComponentInstanceId(final String componentInstanceId) {
    StringUtil.requireDefined(componentInstanceId, "The component instance to set must be defined");
    this.instanceId = componentInstanceId;
  }

  /**
   * Gets the user behind this delegation of roles.
   * @return a {@link User} instance.
   */
  public User getDelegator() {
    return User.getById(delegatorId);
  }

  /**
   * Gets the user to whom this delegation has been done.
   * @return a {@link User} instance
   */
  public User getDelegate() {
    return User.getById(delegateId);
  }

  /**
   * Gets the component instance concerned by this delegation.
   * @return the unique identifier of the component instance in which this delegation was performed.
   */
  public String getComponentInstanceId() {
    return instanceId;
  }

  /**
   * Saves this delegation into the persistence context. Once the current transaction is closed,
   * the delegation will be persisted into the data source of Silverpeas to be retrieved later.
   * @return the saved delegation.
   */
  public Delegation save() {
    return Transaction.performInOne(() -> DelegationRepository.get().save(this));
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  private void requireDifferentUsers(final String delegatorId, final String delegateId) {
    if (delegatorId.equals(delegateId)) {
      throw new IllegalArgumentException("A user cannot delegate his roles to himself!");
    }
  }

  /**
   * Constructor of a {@link Delegation} objects dedicated to construct such instances by using
   * English sentences. The constructor is always related to construct a delegation of roles in
   * a given component instance.
   */
  public static class Constructor {

    private String instanceId;
    private User delegator;

    private Constructor(final String instanceId) {
      this.instanceId = instanceId;
    }

    /**
     * Constructs a new delegation of roles between the two specified users.
     * @param delegator the user that delegates his roles in the underlying component instance.
     * @param delegate the user that receives the delegated roles in the underlying component
     * instance.
     * @return the {@link Delegation} of roles in the underlying component instance between the two
     * specified users.
     */
    public Delegation between(final User delegator, final User delegate) {
      return new Delegation(this.instanceId, delegator, delegate);
    }

    /**
     * From whom the delegation comes.
     * @param delegator the user that delegates his roles in the underlying component instance.
     * @return itself.
     */
    public Constructor from(final User delegator) {
      this.delegator = delegator;
      return this;
    }

    /**
     * To whom the delegation is attributed.
     * @param delegate the user that receives the delegation of roles in the underlying component
     * instance.
     * @return the delegation of roles in the underlying component instance
     * from the underlying delegator to the specified user.
     */
    public Delegation to(final User delegate) {
      return new Delegation(instanceId, delegator, delegate);
    }
  }
}
  