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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util.indexEngine.model;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Remove apostrophe from tokens extracted with StandardFilter. Used in french to remove first part
 * of token such as: "l'amour" to "amour" "d'ailleurs" to "ailleurs" "c'est" to "est" "m'avait" to
 * "avait" "n'avez" to "avez" "s'était" to "était" "t'étais" to "étais"
 * @author neysseri
 */
public final class ApostropheFilter extends TokenFilter {
  public ApostropheFilter(TokenStream in) {
    super(in);
  }

  public final Token next() throws IOException {
    Token t = input.next();
    if (t == null) {
      return null;
    }
    String text = t.termText();
    String type = t.type();

    if (text.startsWith("c'") || text.startsWith("d'") || text.startsWith("l'")
        || text.startsWith("m'") || text.startsWith("n'")
        || text.startsWith("s'") || text.startsWith("t'")) {
      return new Token(text.substring(2), t.startOffset(), t.endOffset(), type);
    } else {
      return t;
    }
  }
}