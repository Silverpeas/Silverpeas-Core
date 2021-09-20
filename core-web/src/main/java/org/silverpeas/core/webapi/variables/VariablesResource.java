package org.silverpeas.core.webapi.variables;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariableScheduledValue;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
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

@WebService
@Path(VariablesResource.PATH)
@Authenticated
public class VariablesResource extends RESTWebService {

  static final String PATH = "variables";

  @Inject
  private VariablesWebManager webManager;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getAllVariables() {
    List<Variable> allVariables = webManager.getAllVariables();
    return asWebEntities(allVariables);
  }

  @GET
  @Path("/currents")
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getCurrentVariables() {
    List<Variable> variables = webManager.getCurrentVariables();
    return asWebEntities(variables);
  }

  @GET
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity getVariable(@PathParam("variableId") String variableId) {
    Variable variable = webManager.getVariable(variableId);
    return VariableEntity.fromVariable(variable);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity createVariable(final VariableEntity entity) {
    Variable variable = entity.toVariable();
    Variable newVariable =
        webManager.createVariable(variable);
    return VariableEntity.fromVariable(newVariable);
  }

  @POST
  @Path("/{variableId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity updateVariable(@PathParam("variableId") String variableId,
      final VariableEntity entity) {
    Variable updatedVariable = webManager.updateVariable(variableId, entity.toVariable());
    return VariableEntity.fromVariable(updatedVariable);
  }

  @DELETE
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariable(@PathParam("variableId") String variableId) {
    webManager.deleteVariable(variableId);
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariables() {
    List<String> ids = getHttpRequest().getParameterAsList("selectedIds");
    webManager.deleteVariables(ids);
  }

  @POST
  @Path("/{variableId}/values")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableScheduledValueEntity createVariableValue(@PathParam("variableId") String variableId,
      final VariableScheduledValueEntity entity) {
    VariableScheduledValue value = entity.toVariableScheduledValue();
    VariableScheduledValue createdValue = webManager.scheduleValue(value, variableId);
    return VariableScheduledValueEntity.fromVariableScheduledValue(createdValue);
  }

  @POST
  @Path("/{variableId}/values/{valueId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableScheduledValueEntity updateVariableValue(@PathParam("variableId") String variableId,
      @PathParam("valueId") String valueId, final VariableScheduledValueEntity entity) {
    VariableScheduledValue value = entity.toVariableScheduledValue();
    VariableScheduledValue updatedValue = webManager.updateValue(valueId, variableId, value);
    return VariableScheduledValueEntity.fromVariableScheduledValue(updatedValue);
  }

  @DELETE
  @Path("/{variableId}/values/{valueId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariableValues(@PathParam("variableId") String variableId,
      @PathParam("valueId") String valueId) {
    webManager.deleteValue(valueId, variableId);
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