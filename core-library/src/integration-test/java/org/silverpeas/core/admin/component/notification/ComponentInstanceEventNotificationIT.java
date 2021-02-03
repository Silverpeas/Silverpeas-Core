/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.admin.component.notification;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentI18N;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasSharedComponentInstance;
import org.silverpeas.core.admin.persistence.ComponentInstanceI18NRow;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.i18n.BeanTranslation;
import org.silverpeas.core.i18n.Translation;
import org.silverpeas.core.notification.system.ResourceEventNotifier;
import org.silverpeas.core.util.BeanContainer;
import org.silverpeas.core.util.CDIContainer;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.notification.system.StateTransition;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NBean;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    return ShrinkWrap.create(JavaArchive.class, "test.jar")
        .addClasses(ServiceProvider.class, BeanContainer.class, CDIContainer.class,
            Translation.class, SilverpeasRuntimeException.class,
            BeanTranslation.class, ComponentInstanceI18NRow.class, StateTransition.class,
            ComponentI18N.class, AbstractI18NBean.class, I18NBean.class,
            SilverpeasComponentInstance.class, SilverpeasSharedComponentInstance.class,
            ComponentInst.class, ComponentInstanceEvent.class, ComponentInstanceEventNotifier.class,
            TestComponentInstanceEventObserver.class)
        .addPackages(true, "org.silverpeas.core.cache")
        .addClass(SilverpeasRuntimeException.class)
        .addPackage("org.silverpeas.core.notification.system")
        .addPackages(true, "org.silverpeas.core.cache")
        .addAsManifestResource("META-INF/services/test-org.silverpeas.core.util.BeanContainer",
            "services/org.silverpeas.core.util.BeanContainer")
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @Test
  public void emptyTest() {
    // just to test the deployment into wildfly works fine.
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
