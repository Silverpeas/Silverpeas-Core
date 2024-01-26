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

package org.silverpeas.core.web.publication.subscription.bean;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPath;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBeanService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionConstants.PUBLICATION;
import static org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionConstants.PUBLICATION_ALIAS;

/**
 * @author silveryocha
 */
@Service
public class PublicationSubscriptionBeanService extends AbstractSubscriptionBeanService {

  @Override
  protected List<SubscriptionResourceType> getHandledSubscriptionResourceTypes() {
    return Stream.of(PUBLICATION, PUBLICATION_ALIAS).collect(Collectors.toList());
  }

  @Override
  public String getSubscriptionTypeListLabel(final SubscriptionResourceType type,
      final String language) {
    final Optional<String> label;
    if (PUBLICATION.equals(type) || PUBLICATION_ALIAS.equals(type)) {
      label = Optional.of(getBundle(language).getString("GML.publications"));
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
      // Subscriptions managed at this level are only those of publication subscription.
      final SubscriptionResource resource = subscription.getResource();
      final SubscriptionResourceType type = resource.getType();
      if (PUBLICATION.equals(type) || PUBLICATION_ALIAS.equals(type)) {
        final String instanceId = resource.getInstanceId();
        controller.getComponentInstance(instanceId)
            .ifPresent(i -> {
              final ContributionIdentifier id = ContributionIdentifier.from(instanceId,
                  resource.getId(), PublicationDetail.TYPE);
              final SubscriptionSubscriber subscriber = subscription.getSubscriber();
              final PublicationPath path = SubscriberType.GROUP == subscriber.getType() ?
                  PublicationPath.getBestPathForGroup(id, subscriber.getId()) :
                  PublicationPath.getBestPathForUser(id, subscriber.getId());
              converted.add(new PublicationSubscriptionBean(subscription, path, i, language));
            });
      }
    }
    return converted;
  }

  @Override
  protected LocalizationBundle getBundle(final String language) {
    return ResourceLocator.getGeneralLocalizationBundle(language);
  }
}
