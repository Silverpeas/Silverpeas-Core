package com.silverpeas.workflow.engine.instance;

/**
 * One implementation of WorkflowEngine The workflow engine main services.
 * 
 * @table SB_Workflow_Undo_Step
 * @key-generator MAX
 */
public class UndoHistoryStep {
  /**
   * the unique id
   * 
   * @primary-key
   * @field-name id
   * @sql-type integer
   */
  private String id;

  /**
   * the step id that provokes the atomic operation
   * 
   * @field-name stepId
   * @sql-type integer
   */
  private String stepId;

  /**
   * the instance id on which the atomic operation has been made
   * 
   * @field-name instanceId
   * @sql-type integer
   */
  private String instanceId;

  /**
   * the atomic operation stored in this step
   * 
   * @field-name action
   */
  private String action;

  /**
   * parameters of atomic operation concatenated as "param1##param2...paramN"
   * 
   * @field-name parameters
   */
  private String parameters;

  /**
   * default constructor
   */
  public UndoHistoryStep() {
  }

  /**
   * Get the unique id
   * 
   * @return the unique id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the unique id
   * 
   * @param id
   *          the unique id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the step id that provokes the atomic operation
   * 
   * @return the step id that provokes the atomic operation
   */
  public String getStepId() {
    return stepId;
  }

  /**
   * Set the step id that provokes the atomic operation
   * 
   * @param stepId
   *          the step id that provokes the atomic operation
   */
  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  /**
   * Get the instance id on which the atomic operation has been made
   * 
   * @return the instance id
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set the instance id on which the atomic operation has been made
   * 
   * @param instanceId
   *          the instance id
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Get the atomic operation stored in this step
   * 
   * @return the atomic operation stored in this step
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the atomic operation stored in this step
   * 
   * @param action
   *          the atomic operation stored in this step
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get parameters of atomic operation
   * 
   * @return parameters concatenated as "param1##param2...paramN"
   */
  public String getParameters() {
    return parameters;
  }

  /**
   * Set parameters of atomic operation
   * 
   * @param parameters
   *          parameters concatenated as "param1##param2...paramN"
   */
  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}