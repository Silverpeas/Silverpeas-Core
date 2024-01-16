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
package org.silverpeas.core.admin.component.notification;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.notification.system.ResourceEventNotifier;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.notification.system.ResourceEvent.Type.CREATION;

/**
 * Integration test validating a notification about a life-cycle event of a component instance is
 * correctly triggered.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ComponentInstanceEventNotificationIT {

  @Inject
  private TestComponentInstanceEventObserver observer;

  @Inject
  private ResourceEventNotifier<ComponentInst, ComponentInstanceEvent> notifier;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ComponentInstanceEventNotificationIT.class)
        .addAdministrationFeatures()
        .addClasses(TestComponentInstanceEventObserver.class)
        .build();
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
    assertThat(true, is(true));
  }

  @Test
  public void aFirstWayToTriggerAnEvent() {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setName("kmelia");
    componentInst.setLocalId(1);

    ComponentInstanceEvent event = new ComponentInstanceEvent(CREATION, componentInst);
    notifier.notify(event);
    assertThat(observer.isAnEventObserved(), is(true));
    assertThat(observer.getObservedEvent(), is(event));
  }

  @Test
  public void aSecondWayToTriggerAnEvent() {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setName("kmelia");
    componentInst.setLocalId(1);

    notifier.notifyEventOn(CREATION, componentInst);
    assertThat(observer.isAnEventObserved(), is(true));
    assertThat(observer.getObservedEvent().getType(), is(CREATION));
    assertThat(observer.getObservedEvent().getTransition().getAfter(), is(componentInst));
  }

}
