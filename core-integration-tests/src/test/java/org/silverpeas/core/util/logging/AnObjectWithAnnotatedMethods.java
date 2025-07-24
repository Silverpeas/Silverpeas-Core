/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
package org.silverpeas.core.util.logging;

import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.core.annotation.Bean;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

@Bean
public class AnObjectWithAnnotatedMethods {

  @Log(dualRecord = true)
  public void doSomething() {
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
  }

  @Log(message = "I love to do anything for you")
  public void doAnotherThing() {
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
  }

  @Log(dualRecord = true)
  public void doSomething(String param1, double param2, Date param3) {
    assert param2 > 0.0;
    Objects.requireNonNull(param1);
    Objects.requireNonNull(param3);
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
  }

  @Log(message = "I''d like to do {0} {1} times for you at {2}", dualRecord = true)
  public void doAnotherThing(String param1, double param2, Date param3) {
    assert param2 > 0.0;
    Objects.requireNonNull(param1);
    Objects.requireNonNull(param3);
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
  }

  @Error
  public void raiseAnError() throws SilverpeasException {
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
    throw new SilverpeasException("A failure!");
  }

  @Error
  public void raiseAnError(String param1, double param2, Date param3) throws SilverpeasException {
    assert param2 > 0.0;
    Objects.requireNonNull(param1);
    Objects.requireNonNull(param3);
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
    throw new SilverpeasException("A failure!");
  }

  @Error(message = "Oops, an error occurred!")
  public void raiseAnotherError() throws SilverpeasException {
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
    throw new SilverpeasException("A failure!");
  }

  @Error(message = "Oops, an error occurred for {0}: {m}. {e}")
  public void raiseAnotherError(String param1, double param2, Date param3)
      throws SilverpeasException {
    assert param2 > 0.0;
    Objects.requireNonNull(param1);
    Objects.requireNonNull(param3);
    await().pollDelay(100, TimeUnit.MICROSECONDS).untilTrue(new AtomicBoolean(true));
    throw new SilverpeasException("A failure!");
  }
}
