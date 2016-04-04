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

package org.silverpeas.web.jobsearch.servlets;

import org.silverpeas.web.jobsearch.SearchResult;
import org.silverpeas.web.jobsearch.JobSearchPeasSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.List;

/**
 * Class declaration
 * @author Cécile Bonin
 */
public class JobSearchPeasRequestRouter extends
    ComponentRequestRouter<JobSearchPeasSessionController> {

  /**
   *
   */
  private static final long serialVersionUID = 9185878202301815494L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public JobSearchPeasSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new JobSearchPeasSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "jobSearchPeas";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param jobSearchPeasSC The component Session Control, build and initialised.
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, JobSearchPeasSessionController jobSearchPeasSC,
      HttpRequest request) {
    String destination = "";
    SilverTrace
        .info("jobSearchPeas", "JobSearchPeasRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + jobSearchPeasSC.getUserId() + " Function=" +
        function);

    try {
      if (function.equals("Main")) {
        request.setAttribute("IdOrName", jobSearchPeasSC.getSearchField());
        request.setAttribute("Category", jobSearchPeasSC.getCategory());
        request.setAttribute("ListResult", jobSearchPeasSC.getListResult());
        destination = "/jobSearchPeas/jsp/jobSearchResult.jsp";
      } else if ("SearchResult".equals(function)) {
        String searchField = request.getParameter("SearchField");
        searchField = searchField.trim();//supprime les espaces avant et après la chaine
        while(searchField.length()>=1 && (searchField.charAt(0) == '*' || searchField.charAt(0) == '?')) {
          //supprime les * et ? en début de chaine : non supportés par Lucène
          if(searchField.length() == 1) {
            searchField = "";
          } else {
            searchField = searchField.substring(1);
          }
        }
        jobSearchPeasSC.setSearchField(searchField);

        String category = request.getParameter("Category");
        jobSearchPeasSC.setCategory(category);

        List<SearchResult> listResult = jobSearchPeasSC.searchResult(searchField, category);
        jobSearchPeasSC.setListResult(listResult);

        request.setAttribute("IdOrName", jobSearchPeasSC.getSearchField());
        request.setAttribute("Category", jobSearchPeasSC.getCategory());
        request.setAttribute("ListResult", jobSearchPeasSC.getListResult());
        destination = "/jobSearchPeas/jsp/jobSearchResult.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

}
