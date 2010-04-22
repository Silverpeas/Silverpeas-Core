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
 * Extends lucene TokenFilter : replaces from each token all the special characters like é or ç.
 */
public final class CharFilter extends TokenFilter {
  /**
   * Constructs a filter which uses the given CharReplacer to change the token of the stream.
   */
  /*
   * public CharFilter(TokenStream stream, CharReplacer replacer) { this.input = stream;
   * this.replacer = replacer; }
   */

  public CharFilter(TokenStream stream) {
    super(stream);
    // this.input = stream;
  }

  public CharFilter(TokenStream stream, CharReplacer replacer) {
    super(stream);
    this.replacer = replacer;
  }

  /**
   * Returns the next input token which is termText
   */
  @Override
  public Token next() throws IOException {
    Token token = input.next();
    if (token != null) {
      String newText = replacer.replace(token.termText());
      return new Token(newText, token.startOffset(), token.endOffset(), token
          .type());
    } else
      return null;
  }

  /**
   * The replacer which will be used to processed each token.
   */
  private CharReplacer replacer = null;
}