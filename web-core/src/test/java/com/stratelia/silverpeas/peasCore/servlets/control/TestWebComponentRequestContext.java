/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.peasCore.servlets.control;

import com.stratelia.silverpeas.peasCore.servlets.WebComponentRequestContext;

/**
 * @author: Yohann Chastagnier
 */
public class TestWebComponentRequestContext
    extends WebComponentRequestContext<TestWebComponentController> {

  private int nbBeforeRequestInitializeCalls = 0;
  private int nbInvokationsBeforeCall = 0;
  private int nbInvokationsAfterCall = 0;

  @Override
  public void beforeRequestProcessing() {
    nbBeforeRequestInitializeCalls++;
  }

  @Override
  public String getComponentUriBase() {
    return "/componentName";
  }

  public int getNbBeforeRequestInitializeCalls() {
    return nbBeforeRequestInitializeCalls;
  }

  public void addInvokationBeforeCall() {
    nbInvokationsBeforeCall++;
  }

  public int getNbInvokationsBeforeCall() {
    return nbInvokationsBeforeCall;
  }

  public void addInvokationAfterCall() {
    nbInvokationsAfterCall++;
  }

  public int getNbInvokationsAfterCall() {
    return nbInvokationsAfterCall;
  }
}
