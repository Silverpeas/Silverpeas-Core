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
package org.silverpeas.core.notification.user.builder;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.kernel.test.UnitTest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the contribution the builder of a notification about a resource considers by
 * default as being the one the subscribers are notified about.
 * @author mmoquillon
 */
@UnitTest
class ResourceUserNotificationBuilderTest {

  private static final ContributionIdentifier IDENTIFIER =
      ContributionIdentifier.from("kmelia42", "23", "Publication");

  @Test
  void aContentManagedByTheContentManagerIsTheContributionTheSubscribersAreNotifiedAbout() {
    final SilverpeasContent content = mock(SilverpeasContent.class);
    when(content.getIdentifier()).thenReturn(IDENTIFIER);

    final Optional<ContributionIdentifier> contribution =
        new ResourceNotificationBuilder<>(content).getSubscribedContribution();

    assertThat(contribution, is(Optional.of(IDENTIFIER)));
  }

  /**
   * The identifier of a contribution is unique only among the contributions of the same type
   * whereas the content manager ignores the type of the contents: another contribution could be
   * mistaken for a content classified on the PdC.
   */
  @Test
  void anyOtherContributionIsNotConsideredAsTheOneTheSubscribersAreNotifiedAbout() {
    final Contribution anyContribution = mock(Contribution.class);
    when(anyContribution.getIdentifier()).thenReturn(IDENTIFIER);

    final Optional<ContributionIdentifier> contribution =
        new ResourceNotificationBuilder<>(anyContribution).getSubscribedContribution();

    assertThat(contribution.isPresent(), is(false));
  }

  @Test
  void aResourceThatIsNotAContributionIsNotConsideredAsTheOneTheSubscribersAreNotifiedAbout() {
    final Optional<ContributionIdentifier> contribution =
        new ResourceNotificationBuilder<>("a resource").getSubscribedContribution();

    assertThat(contribution.isPresent(), is(false));
  }

  /**
   * A builder of notifications about a resource that doesn't specify by itself the contribution
   * the subscribers are notified about.
   */
  private static class ResourceNotificationBuilder<T>
      extends AbstractResourceUserNotificationBuilder<T> {

    ResourceNotificationBuilder(final T resource) {
      super(resource);
    }

    @Override
    protected void performBuild(final T resource) {
      // nothing to build: the tests are about the contribution behind the resource
    }

    @Override
    protected void performNotificationResource(final T resource,
        final NotificationResourceData notificationResourceData) {
      // nothing to build: the tests are about the contribution behind the resource
    }

    @Override
    protected NotifAction getAction() {
      return NotifAction.CREATE;
    }

    @Override
    protected String getComponentInstanceId() {
      return IDENTIFIER.getComponentInstanceId();
    }

    @Override
    protected String getSender() {
      return "1";
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return List.of();
    }
  }
}
