/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.web.pdc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the search parameters of a search by form are converted into a query restricted to the
 * form.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class QueryParametersTest {

  private static final String USER_ID = "2005";
  private static final String LANGUAGE = "*";

  private DataRecord data;
  private PublicationTemplate template;
  private QueryParameters parameters;

  @BeforeEach
  void setUp() throws Exception {
    data = mock(DataRecord.class);
    when(data.getFieldNames()).thenReturn(new String[0]);
    RecordTemplate searchTemplate = mock(RecordTemplate.class);
    when(searchTemplate.getEmptyRecord()).thenReturn(data);
    template = mock(PublicationTemplate.class);
    when(template.getSearchTemplate()).thenReturn(searchTemplate);
    when(template.getSearchForm()).thenReturn(mock(XmlSearchForm.class));
    when(template.getFileName()).thenReturn("classifieds.xml");
    parameters = new QueryParameters();
    parameters.setKeywords("toyota");
  }

  @Test
  void aSearchWithoutAnyFormIsNotRestrictedToAForm() {
    assertThat(requestedFormName(), is(Optional.empty()));
  }

  @Test
  void aSearchByFormWithoutAnyFilledFieldIsRestrictedToTheForm() throws Exception {
    parameters.setByXmlSearchForm(template, "classifieds.xml", List.of(), new PagesContext());

    assertThat(requestedFormName(), is(Optional.of("classifieds")));
  }

  @Test
  void aSearchByFormIsRestrictedToTheTemplateFormWhenNoNamespaceIsGiven() throws Exception {
    parameters.setByXmlSearchForm(template, null, List.of(), new PagesContext());

    assertThat(requestedFormName(), is(Optional.of("classifieds")));
  }

  @Test
  void aDirectSearchByFormIsRestrictedToTheForm() {
    parameters.setFormName("prospects");

    assertThat(requestedFormName(), is(Optional.of("prospects")));
  }

  @Test
  void clearingTheParametersRemovesTheRestrictionToAForm() throws Exception {
    parameters.setByXmlSearchForm(template, "classifieds.xml", List.of(), new PagesContext());

    parameters.clear();

    assertThat(requestedFormName(), is(Optional.empty()));
  }

  @Test
  void clearingTheFormQueryRemovesTheRestrictionToAForm() throws Exception {
    parameters.setByXmlSearchForm(template, "classifieds.xml", List.of(), new PagesContext());

    parameters.clearXmlQuery();

    assertThat(requestedFormName(), is(Optional.empty()));
  }

  @Test
  void aFilledFieldOfTheFormIsQueriedWithItsTrimmedValue() throws Exception {
    fillField("type", " offre ");

    parameters.setByXmlSearchForm(template, "classifieds.xml", List.of(), new PagesContext());

    assertThat(fieldQueries(), contains("classifieds$$type=offre"));
  }

  @Test
  void theValuesOfAFieldAreCombinedWithTheOperatorChosenByTheUser() throws Exception {
    fillField("type", "offre##demande");
    FileItem operator = mock(FileItem.class);
    when(operator.isFormField()).thenReturn(true);
    when(operator.getFieldName()).thenReturn("typeOperator");
    when(operator.getContent(any(Charset.class))).thenReturn("OR");
    PagesContext context = new PagesContext();

    parameters.setByXmlSearchForm(template, "classifieds.xml", List.of(operator), context);

    assertThat(fieldQueries(), contains("classifieds$$type=offre OR demande"));
    assertThat(context.getSearchOperator("type", "AND"), is("OR"));
  }

  private void fillField(String name, String value) throws Exception {
    Field field = mock(Field.class);
    when(field.getStringValue()).thenReturn(value);
    when(data.getFieldNames()).thenReturn(new String[]{name});
    when(data.getField(name)).thenReturn(field);
  }

  private List<String> fieldQueries() {
    return parameters.getQueryDescription(USER_ID, LANGUAGE).getMultiFieldQuery().stream()
        .map(f -> f.getFieldName() + "=" + f.getContent())
        .toList();
  }

  private Optional<String> requestedFormName() {
    return parameters.getQueryDescription(USER_ID, LANGUAGE).getRequestedFormName();
  }
}
