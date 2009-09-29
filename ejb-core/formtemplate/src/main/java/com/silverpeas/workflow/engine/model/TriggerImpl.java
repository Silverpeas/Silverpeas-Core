package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.workflow.api.model.Parameter;
import com.silverpeas.workflow.api.model.Trigger;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;trigger&gt; element of a
 * Process Model.
 **/
public class TriggerImpl extends AbstractReferrableObject implements Trigger,
    Serializable {
  private String name;
  private String className;
  private Vector parameters;

  /**
   * Constructor
   */
  public TriggerImpl() {
    super();
    reset();
  }

  /**
   * reset attributes
   */
  private void reset() {
    name = "";
    className = "";
    parameters = new Vector();
  }

  /**
   * Get the name of the Parameter
   * 
   * @return parameter's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set the name of the Parameter
   * 
   * @param parameter
   *          's name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the value of the Parameter
   * 
   * @return parameter's value
   */
  public String getClassName() {
    return this.className;
  }

  /**
   * Set the value of the Parameter
   * 
   * @param parameter
   *          's value
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.engine.AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (name == null)
      return "";
    else
      return name;
  }

  public Parameter getParameter(String strName) {
    Parameter reference = new ParameterImpl();
    int idx;

    reference.setName(strName);
    idx = parameters.indexOf(reference);

    if (idx >= 0)
      return (Parameter) parameters.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Item#createParameter()
   */
  public Parameter createParameter() {
    return new ParameterImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.silverpeas.workflow.api.model.Item#addParameter(com.silverpeas.workflow
   * .api.model.Parameter)
   */
  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.api.model.Item#iterateParameters()
   */
  public Iterator iterateParameter() {
    return parameters.iterator();
  }
}