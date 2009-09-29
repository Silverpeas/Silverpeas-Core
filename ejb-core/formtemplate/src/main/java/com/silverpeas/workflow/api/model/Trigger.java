package com.silverpeas.workflow.api.model;

import java.util.Iterator;

/**
 * Interface describing a representation of the &lt;trigger&gt; element of a
 * Process Model.
 */
public interface Trigger {
  /**
   * Get the name of the Trigger
   * 
   * @return parameter's name
   */
  public String getName();

  /**
   * Set the name of the Trigger
   * 
   * @param parameter
   *          's name
   */
  public void setName(String name);

  /**
   * Get the className of the Trigger
   * 
   * @return className
   */
  public String getClassName();

  /**
   * Set the className of the Trigger
   * 
   * @param className
   */
  public void setClassName(String className);

  /**
   * Get the parameter specified by name
   * 
   * @param strName
   *          the parameter name
   * @return the parameters
   */
  public Parameter getParameter(String strName);

  /**
   * Create an object implementing Parameter
   */
  public Parameter createParameter();

  /**
   * Add a Parameter to the collection
   */
  public void addParameter(Parameter parameter);

  /**
   * Return an Iterator over the parameters collection
   */
  public Iterator iterateParameter();
}
