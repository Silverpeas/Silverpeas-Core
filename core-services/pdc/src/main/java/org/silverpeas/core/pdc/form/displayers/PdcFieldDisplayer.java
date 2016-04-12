/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.pdc.form.displayers;

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.displayers.AbstractFieldDisplayer;
import org.silverpeas.core.pdc.form.fieldtype.PdcField;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.UsedAxisPK;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.*;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Displayer class of a PDC field.
 * @author ahedin
 * @see PdcField
 */
public class PdcFieldDisplayer extends AbstractFieldDisplayer<PdcField> {

  // Multilang resource path
  private static String MULTILANG_RESOURCE_PATH =
      "org.silverpeas.pdcPeas.multilang.pdcBundle";
  // Icons resource path
  private static String ICONS_RESOURCE_PATH =
      "org.silverpeas.pdcPeas.settings.pdcPeasIcons";

  // Bean to access PDC axis data
  private PdcManager pdcManager = null;

  @Override
  public void display(PrintWriter out, PdcField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);

    if (!PdcField.TYPE.equals(field.getTypeName())) {

    }

    String axis = parameters.get("pdcAxis");
    String value = field.getValue(language);
    boolean readOnly = (template.isHidden() || template.isDisabled() || template.isReadOnly()
        || !"pdc".equals(template.getDisplayerName()));
    boolean mandatory = (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && pagesContext.useMandatory());

    out.println(getPositionsHtml(fieldName, value, axis, language, readOnly, mandatory));
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    if (!template.getTypeName().equals(PdcField.TYPE)) {

    }

    if (template.isMandatory() && pagesContext.useMandatory()) {
      String language = pagesContext.getLanguage();
      out.println(" if (document.getElementById(\"" + template.getFieldName() +
          "\").value == \"\") {");
      out.println("   errorMsg += \"  - '" + template.getLabel(language) + "' "
          + Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("   errorNb++;");
      out.println(" }");
    }

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public List<String> update(String value, PdcField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (!PdcField.TYPE.equals(field.getTypeName())) {
      throw new FormException(
          "PdcFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE", PdcField.TYPE);
    }
    if (field.acceptValue(value, pagesContext.getLanguage())) {
      field.setValue(value, pagesContext.getLanguage());
    } else {
      throw new FormException(
          "PdcFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE", PdcField.TYPE);
    }
    return new ArrayList<>();
  }

  /**
   * @param value The description of used axis which are needed, following the pattern :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   * @return The list of used axis corresponding to the description given as parameter.
   */
  public ArrayList<UsedAxis> getUsedAxisList(String value) {
    ArrayList<UsedAxis> usedAxisList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(value, ".");
    String[] axisData;
    AxisHeader axisHeader;
    String axisId;
    String baseValueId;
    String mandatory;
    String variant;
    int usedAxisId = 0;
    while (st.hasMoreTokens()) {
      axisData = st.nextToken().split(",");
      if (axisData.length == 4) {
        axisId = axisData[0];
        baseValueId = axisData[1];
        mandatory = axisData[2];
        variant = axisData[3];
        axisHeader = getAxisHeader(axisId);
        if (axisHeader != null) {
          UsedAxis usedAxis = new UsedAxis();
          usedAxis.setPK(new UsedAxisPK(usedAxisId));
          usedAxis.setAxisId(Integer.parseInt(axisId));
          usedAxis.setMandatory(Integer.parseInt(mandatory));
          usedAxis.setVariant(Integer.parseInt(variant));
          usedAxis.setBaseValue(Integer.parseInt(baseValueId));

          usedAxis._setAxisHeader(axisHeader);
          usedAxis._setAxisName(axisHeader.getName());
          usedAxis._setAxisType(axisHeader.getAxisType());
          usedAxis._setAxisValues(getAxisValues(Integer.parseInt(axisId)));
          Value baseValue = getAxisValue(baseValueId, axisId);
          usedAxis._setBaseValueName(baseValue.getName());

          usedAxisList.add(usedAxis);
          usedAxisId++;
        }
      }
    }
    return usedAxisList;
  }

  /**
   * @param axisId The axis id.
   * @return The header of the axis which id is given as parameter.
   */
  private AxisHeader getAxisHeader(String axisId) {
    Axis axis = getAxisDetail(axisId);
    if (axis != null) {
      return axis.getAxisHeader();
    }
    return null;
  }

  /**
   * @param value The description of used axis which are needed, following the pattern :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   * @param language The language to use to display axis content.
   * @return The HTML content corresponding to the description and language given as parameters.
   */
  public String getAxisHtml(String value, String language) {
    ElementContainer result = new ElementContainer();

    Table table = new Table();
    table.setClass("tableArrayPane");
    table.setWidth("100%");
    table.setBorder(0);
    table.setCellPadding(2);
    table.setCellSpacing(2);

    // Header
    TR headerLine = new TR();
    MultiSilverpeasBundle pdcResource = new MultiSilverpeasBundle(
        ResourceLocator.getLocalizationBundle(MULTILANG_RESOURCE_PATH, language),
        ResourceLocator.getSettingBundle(ICONS_RESOURCE_PATH), language);
    String[] headerLabelsData = {
        "GML.type", "GML.name", "pdcPeas.baseValue", "GML.requiredField", "pdcPeas.variant" };
    for (String headerLabelKey : headerLabelsData) {
      TD headerCell = new TD();
      headerCell.setClass("ArrayColumn");
      headerCell.addElement(pdcResource.getString(headerLabelKey));
      headerLine.addElement(headerCell);
    }

    // Operation column.
    TD headerOperationCell = new TD();
    headerOperationCell.setClass("ArrayColumn");
    headerOperationCell.addElement(pdcResource.getString("pdcPeas.axisOperation"));
    headerLine.addElement(headerOperationCell);
    table.addElement(headerLine);

    // Axis data.
    int usedAxisCount = 0;
    if (value != null && value.length() > 0) {
      ArrayList<UsedAxis> usedAxisList = getUsedAxisList(value);
      usedAxisCount = usedAxisList.size();
      if (!usedAxisList.isEmpty()) {
        // Axis list.
        UsedAxis usedAxis;
        String usedAxisId;
        String usedAxisName;
        String usedAxisType;
        String usedAxisBaseValue;
        int usedAxisMandatory;
        int usedAxisVariant;
        for (int i = 0; i < usedAxisCount; i++) {
          usedAxis = usedAxisList.get(i);
          usedAxisId = usedAxis.getPK().getId();
          usedAxisName = usedAxis._getAxisName();
          usedAxisType = usedAxis._getAxisType();
          usedAxisBaseValue = usedAxis._getBaseValueName();
          usedAxisMandatory = usedAxis.getMandatory();
          usedAxisVariant = usedAxis.getVariant();

          TR axisLine = new TR();

          // Axis type.
          TD axisTypeCell = new TD();
          axisTypeCell.setClass("ArrayCell");
          axisTypeCell.setAlign(AlignType.center);
          IMG axisTypeImg = new IMG();
          if (usedAxisType.equals("P")) {
            axisTypeImg.setSrc(pdcResource.getIcon("pdcPeas.icoPrimaryAxis"));
            axisTypeImg.setAlt(pdcResource.getString("pdcPeas.primaryAxis"));
            axisTypeImg.setTitle(pdcResource.getString("pdcPeas.primaryAxis"));
          } else {
            axisTypeImg.setSrc(pdcResource.getIcon("pdcPeas.icoSecondaryAxis"));
            axisTypeImg.setAlt(pdcResource.getString("pdcPeas.secondaryAxis"));
            axisTypeImg.setTitle(pdcResource.getString("pdcPeas.secondaryAxis"));
          }

          axisTypeCell.addElement(axisTypeImg);
          axisLine.addElement(axisTypeCell);

          // Axis name.
          TD axisNameCell = new TD();
          axisNameCell.setClass("ArrayCell");
          axisNameCell.setAlign(AlignType.center);
          A axisEditLink = new A();
          axisEditLink.setHref("javascript:editAxis('" + usedAxisId + "')");
          axisEditLink.setTitle(pdcResource.getString("pdcPeas.axisUtilizationParameter"));
          axisEditLink.addElement(usedAxisName);
          axisNameCell.addElement(axisEditLink);
          axisLine.addElement(axisNameCell);

          // Base value.
          TD baseValueCell = new TD();
          baseValueCell.setClass("ArrayCell");
          baseValueCell.setAlign(AlignType.center);
          baseValueCell.addElement(usedAxisBaseValue);
          axisLine.addElement(baseValueCell);

          // Mandatory.
          TD mandatoryCell = new TD();
          mandatoryCell.setClass("ArrayCell");
          mandatoryCell.setAlign(AlignType.center);
          if (usedAxisMandatory == 1) {
            IMG mandatoryImg = new IMG();
            mandatoryImg.setSrc(pdcResource.getIcon("pdcPeas.bulet"));
            mandatoryImg.setAlt(pdcResource.getString("GML.requiredField"));
            mandatoryImg.setTitle(pdcResource.getString("GML.requiredField"));
            mandatoryCell.addElement(mandatoryImg);
          } else {
            mandatoryCell.addElement("&nbsp;");
          }
          axisLine.addElement(mandatoryCell);

          // Variant.
          TD variantCell = new TD();
          variantCell.setClass("ArrayCell");
          variantCell.setAlign(AlignType.center);
          if (usedAxisVariant == 1) {
            IMG variantImg = new IMG();
            variantImg.setSrc(pdcResource.getIcon("pdcPeas.bulet"));
            variantImg.setAlt(pdcResource.getString("pdcPeas.variant"));
            variantImg.setTitle(pdcResource.getString("pdcPeas.variant"));
            variantCell.addElement(variantImg);
          } else {
            variantCell.addElement("&nbsp;");
          }
          axisLine.addElement(variantCell);

          // Operation column.
          TD operationCell = new TD();
          operationCell.setClass("ArrayCell");
          operationCell.setAlign(AlignType.center);
          Input operationInput = new Input();
          operationInput.setType(Input.checkbox);
          operationInput.setName("deleteAxis");
          operationInput.setValue(usedAxisId);
          operationCell.addElement(operationInput);
          axisLine.addElement(operationCell);

          table.addElement(axisLine);
        }
      }
    }
    if (usedAxisCount == 0) {
      TR emptyLine = new TR();
      TD emptyCell = new TD();
      emptyCell.setColSpan(headerLabelsData.length + 1);
      emptyCell.addElement("&nbsp;");
      emptyLine.addElement(emptyCell);
      table.addElement(emptyLine);
    }

    Table globalTable = new Table();
    globalTable.setBorder(0);
    globalTable.setCellPadding(0);
    globalTable.setCellSpacing(0);
    TR globalLine = new TR();

    TD globalCell1 = new TD();
    globalCell1.setWidth("100%");
    globalCell1.addElement(table);
    globalLine.addElement(globalCell1);

    TD globalCell2 = new TD();
    globalCell2.setVAlign(AlignType.top);
    IMG mandatoryImg = new IMG();
    mandatoryImg.setSrc(Util.getIcon("mandatoryField"));
    mandatoryImg.setWidth(5);
    mandatoryImg.setHeight(5);
    mandatoryImg.setBorder(0);
    mandatoryImg.setAlt(pdcResource.getString("GML.obligatoire"));
    mandatoryImg.setTitle(pdcResource.getString("GML.obligatoire"));
    mandatoryImg.setStyle("position: relative; margin: 5px");
    globalCell2.addElement(mandatoryImg);
    globalLine.addElement(globalCell2);

    globalTable.addElement(globalLine);
    result.addElement(globalTable);

    Input axisCountInput = new Input();
    axisCountInput.setName("axisCount");
    axisCountInput.setID("axisCount");
    axisCountInput.setType(Input.hidden);
    axisCountInput.setValue(usedAxisCount);
    result.addElement(axisCountInput);

    return result.toString();
  }

  /**
   * @param fieldName The name of the PDC field.
   * @param pattern The description of required positions, following the pattern :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   * @param axis The description of used axis which are needed, following the pattern :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   * @param language The language to use to display positions content.
   * @param readOnly The display mode.
   * @param mandatory Indicates whether a value is required for the PDC field.
   * @return The HTML content corresponding to the positions.
   */
  private String getPositionsHtml(String fieldName, String pattern, String axis, String language,
      boolean readOnly, boolean mandatory) {
    if (pattern == null) {
      pattern = "";
    }
    ElementContainer result = new ElementContainer();

    MultiSilverpeasBundle pdcResource = new MultiSilverpeasBundle(
        ResourceLocator.getLocalizationBundle(MULTILANG_RESOURCE_PATH, language),
        ResourceLocator.getSettingBundle(ICONS_RESOURCE_PATH),
        language);

    Div div = new Div();
    div.setID("pdcPositions_" + fieldName);

    result.addElement(div);
    div.addElement(getPositionsDivContent(fieldName, pattern, readOnly, pdcResource));

    if (!readOnly) {
      A addPositionLink = new A();
      addPositionLink.setHref("javascript:addPositions_" + fieldName + "()");
      addPositionLink.setClass("add_position");
      addPositionLink.setTitle(pdcResource.getString("pdcPeas.addPosition"));
      addPositionLink.addElement(pdcResource.getString("pdcPeas.addPosition"));
      result.addElement(addPositionLink);

      if (mandatory) {
        result.addElement(Util.getMandatorySnippet());
      }

      result.addElement(getPositionsScript(fieldName, axis, language, pdcResource));
    }

    return result.toString();
  }

  /**
   * @param fieldName The name of the PDC field.
   * @param axis The description of used axis which are needed, following the pattern :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   * @param language The language to use to display positions scripts.
   * @param pdcResource The labels resources.
   * @return The javascript content describing the actions required to manage the positions of the
   * PDC field.
   */
  private Script getPositionsScript(String fieldName, String axis, String language,
      MultiSilverpeasBundle pdcResource) {
    Script script = new Script();
    script.setType("text/javascript");
    script.addElement(
        "function addPositions_" + fieldName + "() {"
        + "openPositionsWindow_" + fieldName + "(\"NewPosition\", null);"
        + "}"
        + "function editPositions_" + fieldName + "(id) {"
        + "openPositionsWindow_" + fieldName + "(\"EditPosition\", \"Id=\" + id);"
        + "}"
        + "function deletePositions_" + fieldName + "(id) {"
        + "if (confirm(\"" + pdcResource.getString("pdcPeas.confirmDeletePosition") + "\")) {"
        + "openPositionsWindow_" + fieldName + "(\"DeletePosition\", \"Ids=\" + id);"
        + "}"
        + "}"
        + "function openPositionsWindow_" + fieldName + "(action, params) {"
        + "var context = window.location.pathname;"
        + "context = context.substring(0, context.indexOf(\"/\", 1));"
        + "var url = context + \"/RpdcClassify/jsp/PdcFieldMode"
        + "?pdcFieldName=" + fieldName
        + "&pdcFieldPositions=\" + document.getElementById(\"" + fieldName + "\").value + \""
        + "&pdcAxis=" + axis
        + "&action=\" + action;"
        + "if (params != null && params != \"\") {"
        + "url += \"&\" + params;"
        + "}"
        + "SP_openWindow(url, action, 600, 400, \"\");"
        + "}"
        + "function updatePositions_" + fieldName + "(positions) {"
        + "var context = window.location.pathname;"
        + "context = context.substring(0, context.indexOf(\"/\", 1));"
        + "var url = context + \"/PdcAjaxServlet"
        + "?fieldName=" + fieldName
        + "&positions=\" + positions + \""
        + "&language=" + language + "\";"
        + "$('#pdcPositions_" + fieldName + "').load(url);"
        + "}");
    return script;
  }

  /**
   * @param fieldName The name of the PDC field.
   * @param pattern The description of required positions, following the pattern :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   * @param language The language to use to display the positions content.
   * @return The HTML content of the positions block defined for the PDC field.
   */
  public String getPositionsDivContent(String fieldName, String pattern, String language) {
    MultiSilverpeasBundle pdcResource = new MultiSilverpeasBundle(
        ResourceLocator.getLocalizationBundle(MULTILANG_RESOURCE_PATH, language),
        ResourceLocator.getSettingBundle(ICONS_RESOURCE_PATH),
        language);
    return getPositionsDivContent(fieldName, pattern, false, pdcResource).toString();
  }

  /**
   * @param fieldName The name of the PDC field.
   * @param pattern The description of required positions, following the pattern :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   * @param readOnly The display mode.
   * @param pdcResource The labels resources.
   * @return The HTML content of the positions block defined for the PDC field.
   */
  private ElementContainer getPositionsDivContent(String fieldName, String pattern,
      boolean readOnly, MultiSilverpeasBundle pdcResource) {
    ElementContainer positionsDiv = new ElementContainer();
    ArrayList<ClassifyPosition> positions = getPositions(pattern);
    if (positions.isEmpty()) {
      I noPositionsLabel = new I();
      noPositionsLabel.addElement(pdcResource.getString("pdcPeas.noPosition"));
      positionsDiv.addElement(noPositionsLabel);
    } else {
      ClassifyPosition position;
      ClassifyValue value;
      String language = pdcResource.getLanguage();

      UL positionsUl = new UL();
      positionsUl.setClass("list_pdc_position");
      for (int i = 0; i < positions.size(); i++) {
        position = positions.get(i);
        LI positionLi = new LI();
        Span positionSpan = new Span();
        positionLi.addElement(positionSpan);
        positionSpan.setClass("pdc_position");
        positionSpan.addElement(pdcResource.getString("pdcPeas.position") + "&nbsp;" + (i + 1));

        if (!readOnly) {
          A editPositionLink = new A();
          editPositionLink.setHref("javascript:editPositions_" + fieldName + "(" + i + ")");
          editPositionLink.setTitle(pdcResource.getString("pdcPeas.editPosition"));
          editPositionLink.setClass("edit");
          IMG editPositionImg = new IMG();
          editPositionImg.setSrc(pdcResource.getIcon("pdcPeas.editPosition"));
          editPositionImg.setAlt(pdcResource.getString("pdcPeas.editPosition"));
          editPositionLink.addElement(editPositionImg);
          positionSpan.addElement(editPositionLink);

          A deletePositionLink = new A();
          deletePositionLink.setHref("javascript:deletePositions_" + fieldName + "(" + i + ")");
          deletePositionLink.setTitle(pdcResource.getString("pdcPeas.deletePosition"));
          deletePositionLink.setClass("delete");
          IMG deletePositionImg = new IMG();
          deletePositionImg.setSrc(pdcResource.getIcon("pdcPeas.deletePosition"));
          deletePositionImg.setAlt(pdcResource.getString("pdcPeas.deletePosition"));
          deletePositionLink.addElement(deletePositionImg);
          positionSpan.addElement(deletePositionLink);
        }

        UL valuesUl = new UL();
        List<ClassifyValue> classifyValues = position.getValues();
        for (ClassifyValue classifyValue : classifyValues) {
          value = classifyValue;
          LI valueLi = new LI();
          valueLi.addElement(getValuesPath(value, language));
          valuesUl.addElement(valueLi);
        }
        positionLi.addElement(valuesUl);
        positionsUl.addElement(positionLi);
      }
      positionsDiv.addElement(positionsUl);
    }

    if (!readOnly) {
      Input positionsInput = new Input();
      positionsInput.setID(fieldName);
      positionsInput.setName(fieldName);
      positionsInput.setValue(pattern);
      positionsInput.setType(Input.hidden);
      positionsDiv.addElement(positionsInput);
    }

    return positionsDiv;
  }

  /**
   * @param value The position value.
   * @param language The language to use to display the path of the position value.
   * @return The path corresponding to the position value and the language given as parameters.
   */
  private String getValuesPath(ClassifyValue value, String language) {
    String pathSeparator = " / ";
    List<Value> values = value.getFullPath();
    if (values.isEmpty()) {
      return pathSeparator;
    }
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i != 0) {
        path.append(pathSeparator);
      }
      path.append(values.get(i).getName(language));
    }
    return path.toString();
  }

  /**
   * @param pattern The description of required positions, following the pattern :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   * @return The list of positions corresponding to the description given as parameter.
   */
  public ArrayList<ClassifyPosition> getPositions(String pattern) {
    ArrayList<ClassifyPosition> positions = new ArrayList<>();
    StringTokenizer classifyPositionSt = new StringTokenizer(pattern, ".");
    StringTokenizer classifyValueSt;
    String classifyValueData;
    String axisId;
    String valueId;
    int separatorIndex;
    TreeNode node;
    String nodeId;
    StringBuffer valuesPath;
    int positionId = 0;
    while (classifyPositionSt.hasMoreTokens()) {
      ArrayList<ClassifyValue> classifyValues = new ArrayList<>();
      classifyValueSt = new StringTokenizer(classifyPositionSt.nextToken(), ";");
      while (classifyValueSt.hasMoreTokens()) {
        classifyValueData = classifyValueSt.nextToken();
        separatorIndex = classifyValueData.indexOf(",");
        if (separatorIndex != -1) {
          axisId = classifyValueData.substring(0, separatorIndex);
          valueId = classifyValueData.substring(separatorIndex + 1);
          List<Value> nodes = getFullPath(valueId, axisId);
          if (nodes != null) {
            Iterator<Value> nodesIter = nodes.iterator();
            ArrayList<Value> values = new ArrayList<>();
            valuesPath = new StringBuffer();
            while (nodesIter.hasNext()) {
              node = nodesIter.next();
              nodeId = node.getPK().getId();
              Value value = getAxisValue(nodeId, axisId);
              values.add(value);
              valuesPath.append("/").append(nodeId);
            }
            if (valuesPath.length() > 0) {
              valuesPath.append("/");
            }
            ClassifyValue classifyValue = new ClassifyValue();
            classifyValue.setFullPath(values);
            classifyValue.setAxisId(Integer.parseInt(axisId));
            classifyValue.setValue(valuesPath.toString());
            classifyValues.add(classifyValue);
          }
        }
      }
      ClassifyPosition position = new ClassifyPosition(positionId, classifyValues);
      positionId++;
      positions.add(position);
    }
    return positions;
  }

  /**
   * @return The bean allowing to access to PDC axis data.
   */
  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
    }
    return pdcManager;
  }

  /**
   * @param axisId The axis id.
   * @return The detail of the axis which id is given as parameter.
   */
  private Axis getAxisDetail(String axisId) {
    try {
      return getPdcManager().getAxisDetail(axisId);
    } catch (PdcException e) {

    }
    return null;
  }

  /**
   * @param valueId The value id.
   * @param axisId The axis id.
   * @return The axis value corresponding to the ids given as parameters.
   */
  private Value getAxisValue(String valueId, String axisId) {
    Value value = null;
    try {
      value = getPdcManager().getAxisValue(valueId, axisId);
    } catch (PdcException e) {

    }
    return value;
  }

  /**
   * @param axisId The axis id.
   * @return The list of values contained by the axis corresponding to the id given as parameter.
   */
  private List<Value> getAxisValues(int axisId) {
    List<Value> values = null;
    try {
      values = getPdcManager().getAxisValues(axisId);
    } catch (PdcException e) {

    }
    return values;
  }

  /**
   * @param valueId The value id.
   * @param axisId The axis id.
   * @return The list of nodes which describe the path of the axis value corresponding to the ids
   * given as parameters.
   */
  private List<Value> getFullPath(String valueId, String axisId) {
    List<Value> nodes = null;
    try {
      return getPdcManager().getFullPath(valueId, axisId);
    } catch (PdcException e) {

    }
    return nodes;
  }

}