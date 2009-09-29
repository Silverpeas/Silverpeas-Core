/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.beans.admin.instance.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SPParameters implements Serializable {
  private Hashtable parameters = new Hashtable();

  public SPParameters() {
  }

  public SPParameters(Hashtable parameters) {
    this.parameters = parameters;
  }

  public void setParameterValue(String name, String value) {
    if (parameters != null) {
      SPParameter parameter = (SPParameter) parameters.get(name.toLowerCase());
      if (parameter != null) {
        SilverTrace.info("admin", "SPParameters.setParameterValue",
            "root.MSG_GEN_PARAM_VALUE", name + " = " + value);
        parameter.setValue(value);
      } else {
        SilverTrace.info("admin", "SPParameters.setParameterValue",
            "root.MSG_GEN_PARAM_VALUE", "parameter " + name + " not found !");
      }
    }
  }

  public List getParameters() {
    return (List) new ArrayList(parameters.values());
  }

  public List getSortedParameters() {
    List parametersToReturn = getParameters();
    Collections.sort(parametersToReturn);
    return parametersToReturn;
  }

  public void setParameters(List newParameters) {
    parameters.clear();

    SPParameter parameter = null;
    for (int p = 0; p < newParameters.size(); p++) {
      parameter = (SPParameter) newParameters.get(p);
      addParameter(parameter);
    }
  }

  public SPParameter getParameter(String parameterName) {
    return (SPParameter) parameters.get(parameterName.toLowerCase());
  }

  public String getParameterValue(String parameterName) {
    SPParameter parameter = getParameter(parameterName);
    if (parameter != null) {
      return parameter.getValue();
    } else {
      return "";
    }
  }

  public void addParameter(SPParameter parameter) {
    if (parameter != null) {
      parameters.put(parameter.getName().toLowerCase(), parameter);
    }
  }

  public SPParameters(Document document) {
    // Parameters
    Node nParametersList = WADOMUtil.findNode(document, "Parameters");
    if (nParametersList != null) {
      NodeList listParameters = nParametersList.getChildNodes();

      String sParameterName = "";
      String sParameterLabel = "";
      String sParameterValue = "";
      String sParameterMandatory = "";
      String sParameterUpdatable = "";
      String sParameterType = "";
      String sParameterSize = "";
      String sParameterOrder = "";
      Hashtable sParameterHelp = null; // New parameter to display help on each
      // parameter (key = language, value =
      // help string according to the language)
      ArrayList sParameterOptions = null; // New parameter to display Select
      // object

      Node nParameterName = null;
      Node nParameterLabel = null;
      Node nParameterValue = null;
      Node nParameterMandatory = null;
      Node nParameterUpdatable = null;
      Node nParameterType = null;
      Node nParameterSize = null;
      Node nParameterOrder = null;
      Node nParameterHelp = null;
      Node nParameterOptions = null;

      SPParameter parameter = null;

      for (int nI = 0; nI < listParameters.getLength(); nI++)
        if (listParameters.item(nI).getNodeType() == Node.ELEMENT_NODE) {
          sParameterName = "";
          sParameterLabel = "";
          sParameterValue = "";
          sParameterMandatory = "";
          sParameterUpdatable = "";
          sParameterType = "";
          sParameterSize = "";
          sParameterOrder = "";
          sParameterHelp = new Hashtable();
          sParameterOptions = new ArrayList();

          nParameterName = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterName");
          nParameterLabel = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterLabel");
          nParameterValue = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterValue");
          nParameterMandatory = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterMandatory");
          nParameterUpdatable = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterUpdatable");
          nParameterType = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterType");
          nParameterSize = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterSize");
          nParameterOrder = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterOrder");
          nParameterHelp = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterHelp");
          nParameterOptions = WADOMUtil.findNode(listParameters.item(nI),
              "ParameterOptions");

          if (nParameterName != null && nParameterName.getFirstChild() != null)
            sParameterName = nParameterName.getFirstChild().getNodeValue();

          if (nParameterLabel != null
              && nParameterLabel.getFirstChild() != null)
            sParameterLabel = nParameterLabel.getFirstChild().getNodeValue();

          if (nParameterValue != null
              && nParameterValue.getFirstChild() != null)
            sParameterValue = nParameterValue.getFirstChild().getNodeValue();

          if (nParameterMandatory != null
              && nParameterMandatory.getFirstChild() != null)
            sParameterMandatory = nParameterMandatory.getFirstChild()
                .getNodeValue();

          if (nParameterUpdatable != null
              && nParameterUpdatable.getFirstChild() != null)
            sParameterUpdatable = nParameterUpdatable.getFirstChild()
                .getNodeValue();

          if (nParameterType != null && nParameterType.getFirstChild() != null)
            sParameterType = nParameterType.getFirstChild().getNodeValue();

          if (nParameterSize != null && nParameterSize.getFirstChild() != null)
            sParameterSize = nParameterSize.getFirstChild().getNodeValue();

          if (nParameterOrder != null
              && nParameterOrder.getFirstChild() != null)
            sParameterOrder = nParameterOrder.getFirstChild().getNodeValue();

          SilverTrace.info("admin", "SPParameters.SPParameters",
              "root.MSG_GEN_PARAM_VALUE", "sParameterType = " + sParameterType);

          // get helps on current parameter
          if (nParameterHelp != null) {
            NodeList helps = nParameterHelp.getChildNodes();
            if (helps != null) {
              for (int nH = 0; nH < helps.getLength(); nH++) {
                Node nParameterHelpFr = WADOMUtil.findNode(helps.item(nH),
                    "ParameterHelpFr");
                if (nParameterHelpFr != null
                    && nParameterHelpFr.getFirstChild() != null) {
                  sParameterHelp.put("fr", nParameterHelpFr.getFirstChild()
                      .getNodeValue());
                }

                Node nParameterHelpEn = WADOMUtil.findNode(helps.item(nH),
                    "ParameterHelpEn");
                if (nParameterHelpEn != null
                    && nParameterHelpEn.getFirstChild() != null) {
                  sParameterHelp.put("en", nParameterHelpEn.getFirstChild()
                      .getNodeValue());
                }
              }
            }
          }

          // Type parameter "<select>" to choose a value between several
          // options.
          if (sParameterType.equals("select") || sParameterType.equals("radio")) {
            NodeList options = nParameterOptions.getChildNodes();
            if (options != null) {
              for (int nH = 0; nH < options.getLength(); nH++) {
                Node nParameterOption = WADOMUtil.findNode(options.item(nH),
                    "ParameterOption");
                SilverTrace.info("admin", "SPParameters.SPParameters",
                    "root.MSG_GEN_PARAM_VALUE", "nParameterOption="
                        + nParameterOption);
                if (nParameterOption != null) {
                  Node nParameterOptionName = WADOMUtil.findNode(options
                      .item(nH), "Name");
                  Node nParameterOptionValue = WADOMUtil.findNode(options
                      .item(nH), "Value");
                  if (nParameterOptionValue != null
                      && nParameterOptionValue.getFirstChild() != null) {
                    ArrayList sParameterOption = new ArrayList();
                    sParameterOption.add(nParameterOptionName.getFirstChild()
                        .getNodeValue());
                    sParameterOption.add(nParameterOptionValue.getFirstChild()
                        .getNodeValue());
                    SilverTrace.info("admin", "SPParameters.SPParameters",
                        "root.MSG_GEN_PARAM_VALUE", "sParameterName="
                            + sParameterOption.get(0));
                    SilverTrace.info("admin", "SPParameters.SPParameters",
                        "root.MSG_GEN_PARAM_VALUE", "sParameterValue="
                            + sParameterOption.get(1));
                    sParameterOptions.add(sParameterOption);
                  }
                }
              }
            }
          }

          parameter = new SPParameter(sParameterName, sParameterLabel,
              sParameterValue, sParameterMandatory, sParameterUpdatable,
              sParameterType, sParameterHelp, sParameterSize, sParameterValue,
              sParameterOrder, sParameterOptions);
          addParameter(parameter);
        }
    }
  }

  public void mergeWith(List parametersToMerge) {
    SPParameter parameterToMerge = null;
    SPParameter parameter = null;
    for (int p = 0; p < parametersToMerge.size(); p++) {
      parameterToMerge = (SPParameter) parametersToMerge.get(p);
      parameter = getParameter(parameterToMerge.getName().toLowerCase());
      if (parameter == null) {
        // Le paramètre existe en base mais plus dans le xmlComponent
        SilverTrace.info("admin", "SPParameters.mergeWith",
            "root.MSG_GEN_PARAM_VALUE", "dbParameter '"
                + parameterToMerge.getName() + "' is no more use !");
      } else {
        parameter.setValue(parameterToMerge.getValue());
      }
    }
  }

  public Object clone() {
    SPParameters clonedParameters = new SPParameters();
    SPParameter parameter = null;
    List params = (List) getParameters();
    for (int i = 0; i < params.size(); i++) {
      parameter = (SPParameter) params.get(i);
      clonedParameters.addParameter((SPParameter) parameter.clone());
    }
    return clonedParameters;
  }
}