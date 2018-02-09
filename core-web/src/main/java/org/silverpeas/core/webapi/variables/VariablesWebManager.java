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
package org.silverpeas.core.webapi.variables;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariablePeriod;
import org.silverpeas.core.variables.VariablePeriodsRepository;
import org.silverpeas.core.variables.VariablesRepository;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.persistence.Transaction.performInOne;

public class VariablesWebManager {

  protected VariablesWebManager() {

  }

  public static VariablesWebManager get() {
    return ServiceProvider.getService(VariablesWebManager.class);
  }

  public List<Variable> getAllVariables() {
    return VariablesRepository.get().getAllVariables();
  }

  public List<Variable> getCurrentVariables() {
    List<VariablePeriod> periods = VariablePeriodsRepository.get().getCurrentPeriods();
    List<Variable> variables = new ArrayList<>();
    for (VariablePeriod period : periods) {
      variables.add(period.getVariable());
    }
    return variables;
  }

  public Variable getVariable(String id) {
    return VariablesRepository.get().getById(id);
  }

  public Variable createVariable(Variable variable, VariablePeriod period) {
    checkAdminAccess();
    return performInOne(() -> {
      // create variable
      Variable createdValue = VariablesRepository.get().save(variable);

      // create its first period
      period.setVariable(createdValue);
      createdValue.getPeriods().add(period);

      createdValue = VariablesRepository.get().save(createdValue);

      addSuccess("variables.variable.create.success");

      return createdValue;
    });
  }

  public Variable updateVariable(Variable variableToUpdate) {
    checkAdminAccess();
    return performInOne(() -> {
      VariablesRepository vr = getVariablesRepository();
      Variable value = vr.getById(variableToUpdate.getId());
      value.merge(variableToUpdate);
      value = vr.save(value);

      addSuccess("variables.variable.update.success");

      return value;
    });
  }

  public void deleteVariable(String id) {
    checkAdminAccess();
    performInOne(() -> {
      VariablesRepository vr = getVariablesRepository();
      Variable variable = vr.getById(id);
      vr.delete(variable);

      addSuccess("variables.variable.delete.success");

      return null;
    });
  }

  public void deleteVariables(List<String> ids) {
    checkAdminAccess();
    performInOne(() -> {
      VariablesRepository vr = getVariablesRepository();
      for (String id : ids) {
        Variable variable = vr.getById(id);
        vr.delete(variable);
      }

      addSuccess("variables.variable.delete.success.many", ids.size());

      return null;
    });
  }

  public VariablePeriod createPeriod(VariablePeriod period) {
    checkAdminAccess();
    return performInOne(() -> {
      VariablePeriod createdPeriod = VariablePeriodsRepository.get().save(period);
      addSuccess("variables.variable.period.create.success");
      return createdPeriod;
    });
  }

  public VariablePeriod updatePeriod(VariablePeriod period) {
    checkAdminAccess();
    return performInOne(() -> {
      VariablePeriod updatedPeriod = VariablePeriodsRepository.get().save(period);
      addSuccess("variables.variable.period.update.success");
      return updatedPeriod;
    });
  }

  public void deletePeriod(String id) {
    checkAdminAccess();
    performInOne(() -> {
      VariablePeriodsRepository.get().deleteById(id);
      addSuccess("variables.variable.period.delete.success");
      return null;
    });
  }

  private VariablesRepository getVariablesRepository() {
    return VariablesRepository.get();
  }

  private void checkAdminAccess() {
    if (!User.getCurrentRequester().isAccessAdmin()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  private void addSuccess(String key) {
    getMessager().addSuccess(getLocalizationBundle().getString(key));
  }

  private void addSuccess(String key, Object... params) {
    getMessager().addSuccess(getLocalizationBundle().getStringWithParams(key, params));
  }

  private LocalizationBundle getLocalizationBundle() {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.variables.multilang.variables", getUserLanguage());
  }

  private String getUserLanguage() {
    return User.getCurrentRequester().getUserPreferences().getLanguage();
  }


  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }

}