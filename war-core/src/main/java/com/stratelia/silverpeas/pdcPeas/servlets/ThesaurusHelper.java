/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.pdcPeas.servlets;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.thesaurus.model.Jargon;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ThesaurusHelper {

  public static void initializeJargon(PdcSearchSessionController pdcSC)
      throws PdcException {
    pdcSC.initializeJargon();
  }

  public static void setJargonInfoInRequest(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws PdcException {
    try {
      setJargonInfoInRequest(pdcSC, request, pdcSC.getActiveThesaurus());
    } catch (RemoteException e) {
      throw new PdcException("ThesaurusHelper.setJargonInfoInRequest()",
          SilverpeasException.ERROR,
          "pdcPeas.EX_CANT_SET_JARGON_INFO_IN_REQUEST", "", e);
    }
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