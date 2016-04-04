/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.pdc.servlets;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;

public class ThesaurusHelper {

  public static void initializeJargon(PdcSearchSessionController pdcSC)
      throws PdcException {
    pdcSC.initializeJargon();
  }

  public static void setJargonInfoInRequest(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws PdcException {
    setJargonInfoInRequest(pdcSC, request, pdcSC.getActiveThesaurus());

  }

  public static void setJargonInfoInRequest(PdcSearchSessionController pdcSC,
      HttpServletRequest request, boolean isThesaurusActive) {
    Jargon jargon = pdcSC.getJargon();
    request.setAttribute("Jargon", jargon);
    request.setAttribute("ActiveThesaurus", Boolean.valueOf(isThesaurusActive));
  }

  private ThesaurusHelper() {
  }

}