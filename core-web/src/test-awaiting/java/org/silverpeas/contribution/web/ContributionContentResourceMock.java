/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.contribution.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;

import javax.ws.rs.Path;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Yohann Chastagnier
 * Date: 21/05/13
 */
@Service
@RequestScoped
@Path("contribution/{componentInstanceId}/{contributionId}/content")
@Authorized
public class ContributionContentResourceMock extends ContributionContentResource {
  private PublicationTemplate publicationTemplateMock = null;

  @Override
  protected String getDefaultFormId() {
    if (!"3".equals(getContributionId())) {
      return null;
    }
    return "testFormId";
  }

  @Override
  protected PublicationTemplate getPublicationTemplate(final String formId) {
    try {
      if (publicationTemplateMock == null) {
        publicationTemplateMock = mock(PublicationTemplate.class);
        RecordSet recordSetMock = mock(RecordSet.class);
        RecordTemplate recordTemplateMock = mock(RecordTemplate.class);
        DataRecord dataRecordMock = mock(DataRecord.class);
        when(recordTemplateMock.getFieldTemplates()).thenReturn(new FieldTemplate[0]);
        when(recordSetMock.getRecord(eq("3"), anyString())).thenReturn(dataRecordMock);
        when(publicationTemplateMock.getRecordTemplate()).thenReturn(recordTemplateMock);
        when(publicationTemplateMock.getRecordSet()).thenReturn(recordSetMock);
      }
      return publicationTemplateMock;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
