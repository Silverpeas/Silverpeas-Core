package org.silverpeas.core.webapi.variables;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.variables.VariableScheduledValue;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Variables",
    description = "The variables of Silverpeas and the values that are associated with them. Any " +
    "change of a value is immediately visible in all the contents using the variable. A value " +
    "can be defined over a given period of time; by default it is active from its creation date " +
    "and it can have no end date.")
@WebService
@Path(VariablesResource.PATH)
@Authenticated
public class VariablesResource extends RESTWebService {

  static final String PATH = "variables";

  @Inject
  private VariablesWebManager webManager;

  @Operation(summary = "Gets all the variables.")
  @ApiResponse(responseCode = "200", description = "All the variables defined in Silverpeas.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = VariableEntity.class))))
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getAllVariables() {
    List<Variable> allVariables = webManager.getAllVariables();
    return asWebEntities(allVariables);
  }

  @Operation(summary = "Gets the variables having currently an active value.")
  @ApiResponse(responseCode = "200",
      description = "The variables for which a value is active at the current date.",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = VariableEntity.class))))
  @GET
  @Path("/currents")
  @Produces(MediaType.APPLICATION_JSON)
  public List<VariableEntity> getCurrentVariables() {
    List<Variable> variables = webManager.getCurrentVariables();
    return asWebEntities(variables);
  }

  @Operation(summary = "Gets the variable with the given identifier.")
  @ApiResponse(responseCode = "200", description = "The asked variable.",
      content = @Content(schema = @Schema(implementation = VariableEntity.class)))
  @GET
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity getVariable(@PathParam("variableId") String variableId) {
    Variable variable = webManager.getVariable(variableId);
    return VariableEntity.fromVariable(variable);
  }

  @Operation(summary = "Creates a new variable.")
  @ApiResponse(responseCode = "200", description = "The created variable.",
      content = @Content(schema = @Schema(implementation = VariableEntity.class)))
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity createVariable(final VariableEntity entity) {
    Variable variable = entity.toVariable();
    Variable newVariable =
        webManager.createVariable(variable);
    return VariableEntity.fromVariable(newVariable);
  }

  @Operation(summary = "Updates the variable with the given identifier.")
  @ApiResponse(responseCode = "200", description = "The updated variable.",
      content = @Content(schema = @Schema(implementation = VariableEntity.class)))
  @POST
  @Path("/{variableId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public VariableEntity updateVariable(@PathParam("variableId") String variableId,
      final VariableEntity entity) {
    Variable updatedVariable = webManager.updateVariable(variableId, entity.toVariable());
    return VariableEntity.fromVariable(updatedVariable);
  }

  @Operation(summary = "Deletes the variable with the given identifier.")
  @ApiResponse(responseCode = "204", description = "The variable has been deleted.")
  @DELETE
  @Path("/{variableId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariable(@PathParam("variableId") String variableId) {
    webManager.deleteVariable(variableId);
  }

  @Operation(summary = "Deletes all the variables.")
  @ApiResponse(responseCode = "204", description = "The variables have been deleted.")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteVariables() {
    List<String> ids = getHttpRequest().getParameterAsList("selectedIds");
    webManager.deleteVariables(ids);
  }

  @Operation(summary = "Adds to the given variable a value active over a period of time.")
  @ApiResponse(responseCode = "200", description = "The added value.",
      content = @Content(schema = @Schema(implementation = VariableScheduledValueEntity.class)))
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

  @Operation(summary = "Updates the given value of the given variable.")
  @ApiResponse(responseCode = "200", description = "The updated value.",
      content = @Content(schema = @Schema(implementation = VariableScheduledValueEntity.class)))
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

  @Operation(summary = "Deletes the given value of the given variable.")
  @ApiResponse(responseCode = "204", description = "The value of the variable has been deleted.")
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
