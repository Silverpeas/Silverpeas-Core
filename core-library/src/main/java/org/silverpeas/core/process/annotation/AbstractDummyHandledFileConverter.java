/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.process.annotation;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public abstract class AbstractDummyHandledFileConverter<E extends SimulationElement<?>>
    implements DummyHandledFileConverter<E> {

  /**
   * Just after Silverpeas server start, this method is called.
   * The content of this method consists to register the class instance into
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  @Override
  public void init() {
    DummyHandledFileConverterRegistration.register(this);
  }

  /**
   * Just before Silverpeas server stop, this method is called.
   * The content of this method consists to unregister the class instance from
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  @Override
  public void release() {
    DummyHandledFileConverterRegistration.unregister(this);
  }
}
