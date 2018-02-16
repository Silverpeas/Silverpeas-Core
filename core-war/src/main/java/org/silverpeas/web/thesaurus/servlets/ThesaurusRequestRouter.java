/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.web.thesaurus.servlets;

import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.web.thesaurus.control.ThesaurusSessionController;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThesaurusRequestRouter extends ComponentRequestRouter<ThesaurusSessionController> {

  private static final long serialVersionUID = -4111516203248522174L;

  public ThesaurusSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ThesaurusSessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.thesaurusPeas.multilang.thesaurusPeasBundle",
        "org.silverpeas.thesaurusPeas.settings.thesaurusPeasIcons");
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  public String getSessionControlBeanName() {
    return "thesaurusPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  public String getDestination(String function, ThesaurusSessionController scc,
      HttpRequest request) {

    String destination = "";
    try {
      if (function.startsWith("Main")) {
        scc.resetCriterias();

        request.setAttribute("listVoca", scc.getListVoca());
        request.setAttribute("listAxis", scc.getListAxis());
        request.setAttribute("listTerms", new ArrayList<Value>());
        destination = "/thesaurusPeas/jsp/thesaurus.jsp";
      } else if (function.equals("SetVoca")) {
        long idVoca = new Long(request.getParameter("idVoca")).longValue();
        scc.getVocabulary(idVoca);
        request.setAttribute("listVoca", scc.getListVoca());
        request.setAttribute("listAxis", scc.getListAxis());
        request.setAttribute("listTerms", scc.getAxisTerms());
        request.setAttribute("idVoca", new Long(idVoca).toString());
        if (scc.getCurrentAxis() != null) {
          request.setAttribute("idAxis", scc.getCurrentAxis().getAxisHeader()
              .getPK().getId());
          loadSynonyms(request, scc);
        }
        destination = "/thesaurusPeas/jsp/thesaurus.jsp";

      } else if (function.equals("SetAxis")) {

        String idAxis = request.getParameter("idAxis");
        scc.getAxis(idAxis);
        request.setAttribute("listVoca", scc.getListVoca());
        request.setAttribute("listAxis", scc.getListAxis());
        request.setAttribute("listTerms", scc.getAxisTerms());
        if (scc.getCurrentVoca() != null) {
          request.setAttribute("idVoca", scc.getCurrentVoca().getPK().getId());
          loadSynonyms(request, scc);
        }
        request.setAttribute("idAxis", idAxis);

        destination = "/thesaurusPeas/jsp/thesaurus.jsp";

      } else if (function.equals("Back")) {

        request.setAttribute("listVoca", scc.getListVoca());
        request.setAttribute("listAxis", scc.getListAxis());
        request.setAttribute("listTerms", scc.getAxisTerms());
        if (scc.getCurrentVoca() != null) {
          request.setAttribute("idVoca", scc.getCurrentVoca().getPK().getId());
        }
        if (scc.getCurrentAxis() != null) {
          request.setAttribute("idAxis", scc.getCurrentAxis().getAxisHeader()
              .getPK().getId());
          loadSynonyms(request, scc);
        }
        destination = "/thesaurusPeas/jsp/thesaurus.jsp";

      } else if (function.equals("CreateVocaQuery")) {

        destination = "/thesaurusPeas/jsp/createVocabulary.jsp";

      } else if (function.equals("CreateVoca")) {

        String nom = request.getParameter("nom");
        String desc = request.getParameter("description");
        long id = scc.createVocabulary(nom, desc);
        if (id == -1) // pb : le vocabulaire existe déjà
        {
          scc.setCurrentUpdVoca(nom, desc);
          destination = "/thesaurusPeas/jsp/errorCreateVocabulary.jsp";
        } else {
          destination = getDestination("Back", scc, request);
        }

      } else if (function.equals("UpdateVocaQuery")) {

        request.setAttribute("Vocabulary", scc.getCurrentVoca());
        destination = "/thesaurusPeas/jsp/editVocabulary.jsp";

      } else if (function.equals("UpdateVocaQueryBack")) {
        request.setAttribute("Vocabulary", scc.getCurrentUpdVoca());
        destination = "/thesaurusPeas/jsp/editVocabulary.jsp";
      } else if (function.equals("CreateVocaQueryBack")) {
        request.setAttribute("Vocabulary", scc.getCurrentUpdVoca());
        destination = "/thesaurusPeas/jsp/createVocabulary.jsp";
      } else if (function.equals("UpdateVoca")) {

        String nom = request.getParameter("nom");
        String desc = request.getParameter("description");
        long id = scc.updateVocabulary(nom, desc);
        if (id == -1) // pb : un vocabulaire de même nom existe déjà
        {
          scc.setCurrentUpdVoca(nom, desc);
          destination = "/thesaurusPeas/jsp/errorUpdateVocabulary.jsp";
        } else {
          destination = getDestination("Back", scc, request);
        }

      } else if (function.equals("DeleteVoca")) {
        scc.deleteVocabulary();
        destination = getDestination("Back", scc, request);

      } else if (function.equals("EditAssignments")) {

        request.setAttribute("Vocabulary", scc.getCurrentVoca());
        request.setAttribute("ListUser", scc.getJargons());
        destination = "/thesaurusPeas/jsp/editAssignments.jsp";

      } else if (function.equals("ManageAssignments")) {

        destination = scc.initUserPanel();

      } else if (function.equals("UserAssignments")) {
        scc.setUserPanelJargons();
        Collection<Jargon> listJargons = new ArrayList<Jargon>();
        listJargons.addAll(scc.getUserSelectedJargons());
        listJargons.addAll(scc.getUserSelectedNewJargons());
        request.setAttribute("listJargons", listJargons);
        destination = "/thesaurusPeas/jsp/manageAssignments.jsp";

      } else if (function.equals("UserAssignmentsBack")) {
        scc.setJargons();
        Collection<Jargon> listJargons = new ArrayList<Jargon>();
        listJargons.addAll(scc.getUserSelectedJargons());
        listJargons.addAll(scc.getUserSelectedNewJargons());
        request.setAttribute("listJargons", listJargons);
        destination = "/thesaurusPeas/jsp/manageAssignments.jsp";

      } else if (function.equals("UserPanel")) {

        destination = scc.initSelectedUserPanel();

      } else if (function.equals("SelectVocabulary")) {

        request.setAttribute("listVoca", scc.getListVoca());
        destination = "/thesaurusPeas/jsp/selectVocabulary.jsp";

      } else if (function.equals("SaveAssignVoca")) {

        long idVoca = new Long(request.getParameter("idVoca")).longValue();
        int nbConflict = scc.assignVocabulary(idVoca);
        if (nbConflict > 0) {
          request.setAttribute("idVoca", new Long(idVoca).toString());

          if (nbConflict == 1) {
            destination = "/thesaurusPeas/jsp/validateAssignVoca.jsp";
          } else {
            destination = "/thesaurusPeas/jsp/choiceAssignVoca.jsp";
          }
        } else {
          request.setAttribute("urlToReload", "UserAssignmentsBack");
          destination = "/thesaurusPeas/jsp/closeWindow.jsp";
        }

      } else if (function.equals("SaveAssignUser")) {

        scc.setUserPanelJargons();
        int nbConflict = scc.assignVocabulary();
        if (nbConflict > 0) {
          request.setAttribute("Vocabulary", scc.getCurrentVoca());
          if (nbConflict == 1) {
            destination = "/thesaurusPeas/jsp/validateAssignUser.jsp";
          } else {
            destination = "/thesaurusPeas/jsp/choiceAssignUser.jsp";
          }
        } else {
          destination = getDestination("EditAssignments", scc, request);
        }
      } else if (function.equals("CreateNewJargonsVoca")) {

        long idVoca = new Long(request.getParameter("idVoca")).longValue();
        scc.createNewJargons(idVoca);
        request.setAttribute("urlToReload", "UserAssignmentsBack");
        destination = "/thesaurusPeas/jsp/closeWindow.jsp";

      } else if (function.equals("CreateJargonsVoca")) {

        long idVoca = new Long(request.getParameter("idVoca")).longValue();
        scc.createJargons(idVoca);
        request.setAttribute("urlToReload", "UserAssignmentsBack");
        destination = "/thesaurusPeas/jsp/closeWindow.jsp";

      } else if (function.equals("CreateNewJargonsUser")) {

        scc.createNewJargons();
        destination = getDestination("EditAssignments", scc, request);
      } else if (function.equals("CreateJargonsUser")) {

        scc.createJargons();
        destination = getDestination("EditAssignments", scc, request);
      }
    } catch (Exception exce_all) {
      request.setAttribute("javax.servlet.jsp.jspException", exce_all);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private void loadSynonyms(HttpServletRequest request,
      ThesaurusSessionController scc) throws ThesaurusException {
    Collection<Value> terms = scc.getAxisTerms();
    Map<String, List<String>> termsSynonyms = new HashMap<>();
    for (Value term : terms) {
      Collection<Synonym> synonyms = scc.getSynonyms(term.getValuePK().getId());
      List<String> names = new ArrayList<>();
      for (Synonym synonym : synonyms) {
        names.add(synonym.getName());
      }
      termsSynonyms.put(term.getValuePK().getId(), names);
    }

    request.setAttribute("synonyms", termsSynonyms);
  }
}