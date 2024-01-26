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
package org.silverpeas.core.webapi.variables;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariableScheduledValue;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
public class VariablesWebManager {

  protected VariablesWebManager() {

  }

  public static VariablesWebManager get() {
    return ServiceProvider.getService(VariablesWebManager.class);
  }

  public List<Variable> getAllVariables() {
    return Variable.getAll();
  }

  public List<Variable> getCurrentVariables() {
    List<VariableScheduledValue> currentValues = VariableScheduledValue.getCurrentOnes();
    List<Variable> variables = new ArrayList<>();
    for (VariableScheduledValue currentValue : currentValues) {
      variables.add(currentValue.getVariable());
    }
    return variables;
  }

  public Variable getVariable(String id) {
    Variable variable = Variable.getById(id);
    if (variable == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return variable;
  }

  @Transactional
  public Variable createVariable(Variable variable) {
    checkAdminAccess();

    Variable createdVariable = process(variable::save, "variables.variable.create.failure");

    addSuccess("variables.variable.create.success");

    return createdVariable;
  }

  @Transactional
  public Variable updateVariable(final String variableId, final Variable variableToUpdate) {
    checkAdminAccess();

    Variable updatedVariable = process(() -> {
      Variable variable = getVariable(variableId);
      variable.merge(variableToUpdate);
      return variable.save();
    }, "variables.variable.update.failure");

    addSuccess("variables.variable.update.success");

    return updatedVariable;
  }

  @Transactional
  public void deleteVariable(String id) {
    checkAdminAccess();
    process(() -> {
      Variable variable = getVariable(id);
      if (variable != null) {
        variable.delete();
      }
      return null;
    }, "variables.variable.delete.failure");

    addSuccess("variables.variable.delete.success");
  }

  @Transactional
  public void deleteVariables(List<String> ids) {
    checkAdminAccess();
    process(() -> {
      for (String id : ids) {
        Variable variable = getVariable(id);
        if (variable != null) {
          variable.delete();
        }
      }
      return null;
    }, "variables.variable.delete.failure.many");

    addSuccess("variables.variable.delete.success.many", ids.size());
  }

  @Transactional
  public VariableScheduledValue scheduleValue(VariableScheduledValue value, String variableId) {
    checkAdminAccess();
    VariableScheduledValue createdPeriod = process(() -> {
      Variable variable = getVariable(variableId);
      return variable.getVariableValues().addAndSave(value);
    }, "variables.variable.value.create.failure");
    addSuccess("variables.variable.value.create.success");
    return createdPeriod;
  }

  @Transactional
  public VariableScheduledValue updateValue(String valueId, String variableId,
      VariableScheduledValue newValueState) {
    checkAdminAccess();

    VariableScheduledValue updatedValue = process(() -> {
      Variable variable = getVariable(variableId);
      VariableScheduledValue value = variable.getVariableValues()
          .get(valueId)
          .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
      return value.updateFrom(newValueState);
    }, "variables.variable.value.update.failure");

    addSuccess("variables.variable.value.update.success");
    return updatedValue;
  }

  @Transactional
  public void deleteValue(String valueId, String variableId) {
    checkAdminAccess();
    process(() -> {
      Variable variable = getVariable(variableId);
      if (!variable.getVariableValues().remove(valueId)) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      return variable.save();
    }, "variables.variable.value.delete.failure");
    addSuccess("variables.variable.value.delete.success");
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

  private void addFailure(String key) {
    getMessager().addError(getLocalizationBundle().getString(key));
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

  private <T> T process(final Supplier<T> processor, final String keyIfFailure) {
    try {
      return processor.get();
    } catch (PersistenceException e) {
      addFailure(keyIfFailure);
      throw e;
    }
  }
}