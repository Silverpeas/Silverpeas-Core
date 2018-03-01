package org.silverpeas.core.webapi.variables;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariablePeriod;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestScoped
@Path(VariablesResource.PATH)
@Authenticated
public class VariablesResource extends RESTWebService {

  static final String PATH = "variables";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getAllVariables() {
    List<Variable> allVariables = VariablesWebManager.get().getAllVariables();
    return asWebEntities(allVariables);
  }

  @GET
  @Path("/currents")
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getCurrentVariables() {
    List<Variable> variables = VariablesWebManager.get().getCurrentVariables();
    return asWebEntities(variables);
  }

  @GET
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity getVariable(@PathParam("variableId") String variableId) {
    Variable variable = VariablesWebManager.get().getVariable(variableId);
    return VariableEntity.fromVariable(variable);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity createVariable(final VariableEntity entity) {
    Variable variable = entity.toVariable();
    VariablePeriodEntity periodEntity = entity.getPeriods().get(0);
    Variable newVariable =
        VariablesWebManager.get().createVariable(variable, periodEntity.toVariablePeriod());
    return VariableEntity.fromVariable(newVariable);
  }

  @POST
  @Path("/{variableId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity updateVariable(@PathParam("variableId") String variableId,
      final VariableEntity entity) {
    Variable variable = VariablesWebManager.get().getVariable(variableId);
    entity.merge(variable);
    Variable updatedVariable = VariablesWebManager.get().updateVariable(variable);
    return VariableEntity.fromVariable(updatedVariable);
  }

  @DELETE
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariable(@PathParam("variableId") String variableId) {
    VariablesWebManager.get().deleteVariable(variableId);
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariables() {
    List<String> ids = getHttpRequest().getParameterAsList("selectedIds");
    VariablesWebManager.get().deleteVariables(ids);
  }

  @POST
  @Path("/{variableId}/periods")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariablePeriodEntity createVariablePeriod(@PathParam("variableId") String variableId,
      final VariablePeriodEntity entity) {
    VariablePeriod period = entity.toVariablePeriod();
    Variable value = VariablesWebManager.get().getVariable(variableId);
    period.setVariable(value);
    VariablePeriod createdPeriod = VariablesWebManager.get().createPeriod(period);
    return VariablePeriodEntity.fromVariablePeriod(createdPeriod);
  }

  @POST
  @Path("/{variableId}/periods/{periodId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariablePeriodEntity updateVariablePeriod(@PathParam("variableId") String variableId,
      @PathParam("periodId") String periodId, final VariablePeriodEntity entity) {
    VariablePeriod period = entity.toVariablePeriod();
    period.setId(periodId);

    Variable variable = VariablesWebManager.get().getVariable(variableId);
    period.setVariable(variable);

    VariablePeriod updatedPeriod = VariablesWebManager.get().updatePeriod(period);
    return VariablePeriodEntity.fromVariablePeriod(updatedPeriod);
  }

  @DELETE
  @Path("/{variableId}/periods/{periodId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariablePeriod(@PathParam("variableId") String variableId,
      @PathParam("periodId") String periodId) {
    VariablesWebManager.get().deletePeriod(periodId);
  }

  private List<VariableEntity> asWebEntities(Collection<Variable> variables) {
    return variables.stream().map(this::asWebEntity).collect(Collectors.toList());
  }

  private VariableEntity asWebEntity(Variable value) {
    return VariableEntity.fromVariable(value);
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return null;
  }

}