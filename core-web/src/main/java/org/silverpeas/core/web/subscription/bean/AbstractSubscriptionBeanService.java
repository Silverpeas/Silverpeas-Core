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
package org.silverpeas.core.web.subscription.bean;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;

/**
 * @author silveryocha
 */
public abstract class AbstractSubscriptionBeanService implements SubscriptionBeanService,
    Initialization {

  @Override
  public void init() throws Exception {
    SubscriptionBeanProvider.registerSubscriptionBeanService(this);
  }

  /**
   * Gets the list of subscription resource type that the implementation handles.
   * <p>
   * An empty returned list means that the default implementation is aimed.
   * </p>
   * @return a list of {@link SubscriptionResourceType} instances.
   */
  protected abstract List<SubscriptionResourceType> getHandledSubscriptionResourceTypes();

  @Override
  public String getSubscriptionTypeListLabel(final SubscriptionResourceType type,
      final String language) {
    final Optional<String> label;
    if (COMPONENT.equals(type)) {
      label = Optional.of(getBundle(language).getString("application"));
    } else if (NODE.equals(type)) {
      label = Optional.of(getBundle(language).getString("thematique"));
    } else {
      // nothing is done here about other types, explicit component implementation MUST exist.
      label = Optional.empty();
    }
    return label.orElse(StringUtil.EMPTY);
  }

  @Override
  public List<AbstractSubscriptionBean> toSubscriptionBean(
      final Collection<Subscription> subscriptions, final String language) {
    final OrganizationController controller = OrganizationController.get();
    final List<AbstractSubscriptionBean> converted = new ArrayList<>();
    for (final Subscription subscription : subscriptions) {
      // Subscriptions managed at this level are only those of node subscription.
      final SubscriptionResourceType currentType = subscription.getResource().getType();
      if (COMPONENT.equals(currentType)) {
        controller.getComponentInstance(subscription.getResource().getInstanceId())
                  .ifPresent(i -> converted.add(new ComponentSubscriptionBean(subscription, i, language)));
      } else if (NODE.equals(currentType)) {
        controller.getComponentInstance(subscription.getResource().getInstanceId())
                  .ifPresent(i -> {
          final NodePath path = NodeService.get().getPath(subscription.getResource().getPK());
          converted.add(new NodeSubscriptionBean(subscription, path, i, language));
        });
      } else {
        // nothing is done here about other types, explicit component implementation MUST exist.
      }
    }
    return converted;
  }

  protected LocalizationBundle getBundle(final String language) {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.pdcSubscriptionPeas.multilang.pdcSubscriptionBundle",
            language);
  }
}
