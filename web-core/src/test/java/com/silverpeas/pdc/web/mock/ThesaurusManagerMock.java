/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web.mock;

import com.silverpeas.pdc.web.Thesaurus;
import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Jargon;
import java.util.Collection;
import javax.inject.Named;
import static org.mockito.Mockito.*;
import static com.silverpeas.pdc.web.TestConstants.*;

/**
 * A mock of the thesaurus manager. It mocks of its methods.
 */
@Named("thesaurusManager")
public class ThesaurusManagerMock extends ThesaurusManager {
  
  private Thesaurus thesaurus = new Thesaurus();

  @Override
  public Jargon getJargon(String idUser) throws ThesaurusException {
    Jargon jargon = mock(Jargon.class);
    when(jargon.getIdUser()).thenReturn(USER_ID);
    return jargon;
  }

  @Override
  public Collection<String> getSynonyms(long idTree, long idTerm, String idUser) throws ThesaurusException {
    return thesaurus.getSynonyms(String.valueOf(idTerm));
  }
  
}
