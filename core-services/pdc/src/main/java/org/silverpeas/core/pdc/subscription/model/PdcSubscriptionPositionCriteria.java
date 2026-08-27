/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.pdc.subscription.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.service.PdcSubscriptionService;
import org.silverpeas.core.subscription.service.AbstractSubscriptionResource;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.silverpeas.core.pdc.subscription.model.PdcSubscriptionConstants.NO_COMPONENT_INSTANCE;
import static org.silverpeas.core.pdc.subscription.model.PdcSubscriptionConstants.PDC;

/**
 * The named criteria defining a position on the axis of the PdC on which users or groups of users
 * can subscribe in order to be notified about the contributions that are classified on such a
 * position.
 * <p>
 * As any position on the PdC, it is made up of one or more values of axis, each of them being
 * expressed here as an {@link AxisValueCriterion}: the axis and, within that axis, the path of the
 * value from the root value. Unlike the concrete position of a classified contribution, a value is
 * matched here by any of the values below it in the axis; a contribution is therefore notified
 * about when one of the positions of its classification satisfies all the criteria.
 * </p>
 * <p>
 * Unlike the resources of the other types of subscription, such criteria aren't handled by a
 * component instance: the PdC is a classification plan that is transverse to all the applications
 * in Silverpeas. Their instance identifier is therefore always
 * {@link PdcSubscriptionConstants#NO_COMPONENT_INSTANCE}.
 * </p>
 * <p>
 * The name and the criteria are lazily loaded from the data source when they are referred only by
 * their identifier, as it is the case when the subscriptions are read from the data source. Use
 * {@link #PdcSubscriptionPositionCriteria(String, String, List)} when those data are already known
 * in order to avoid any access to the data source.
 * </p>
 *
 * @author mmoquillon
 */
public final class PdcSubscriptionPositionCriteria
    extends AbstractSubscriptionResource<ResourceReference> {

  private String name;
  private List<AxisValueCriterion> criteria;
  private boolean loaded;

  /**
   * Gets a reference to the position criteria on the PdC with the specified identifier. Their name
   * and their criteria will be lazily loaded from the data source at their first access.
   *
   * @param id the unique identifier of position criteria on the PdC.
   * @return a {@link PdcSubscriptionPositionCriteria} instance.
   */
  public static PdcSubscriptionPositionCriteria from(final String id) {
    return new PdcSubscriptionPositionCriteria(id);
  }

  /**
   * Gets a reference to the position criteria on the PdC with the specified identifier. Their name
   * and their criteria will be lazily loaded from the data source at their first access.
   *
   * @param id the unique identifier of position criteria on the PdC.
   * @return a {@link PdcSubscriptionPositionCriteria} instance.
   */
  public static PdcSubscriptionPositionCriteria from(final int id) {
    return new PdcSubscriptionPositionCriteria(String.valueOf(id));
  }

  private PdcSubscriptionPositionCriteria(final String id) {
    super(id, PDC, new ResourceReference(id, NO_COMPONENT_INSTANCE));
    this.loaded = false;
  }

  /**
   * Constructs fully valued position criteria on the PdC. No access to the data source will be done
   * to get their name and their criteria.
   *
   * @param id the unique identifier of the position criteria.
   * @param name the name of the position criteria.
   * @param criteria the criteria on the values of the axis of the PdC.
   */
  public PdcSubscriptionPositionCriteria(final String id, final String name,
      final List<AxisValueCriterion> criteria) {
    super(id, PDC, new ResourceReference(id, NO_COMPONENT_INSTANCE));
    this.name = name;
    this.criteria = criteria == null ? List.of() : List.copyOf(criteria);
    this.loaded = true;
  }

  /**
   * Gets the name of these position criteria on the PdC. It is the name that has been given by the
   * user or by the manager of the PdC that has created them.
   *
   * @return the name of the position criteria or an empty string if they don't exist anymore in the
   * data source.
   */
  public String getName() {
    load();
    return name == null ? "" : name;
  }

  /**
   * Gets the criteria on the values of the axis that make up this position on the PdC.
   *
   * @return a list of {@link AxisValueCriterion}, each of them being a criterion on the value of an
   * axis. The list is empty if the position criteria don't exist anymore in the data source.
   */
  public List<AxisValueCriterion> getCriteria() {
    load();
    return criteria == null ? List.of() : criteria;
  }

  private synchronized void load() {
    if (!loaded) {
      loaded = true;
      ServiceProvider.getService(PdcSubscriptionService.class)
          .getPositionCriteria(getId())
          .ifPresent(r -> {
            this.name = r.name;
            this.criteria = r.criteria;
          });
    }
  }

  @Override
  public String toString() {
    return "PdcSubscriptionPositionCriteria{id=" + getId() + ", name=" + name + ", criteria=" +
        criteria + "}";
  }
}
