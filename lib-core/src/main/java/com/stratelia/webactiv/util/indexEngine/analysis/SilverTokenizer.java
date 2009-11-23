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
package com.stratelia.webactiv.util.indexEngine.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

//import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * A grammar-based tokenizer constructed with JFlex
 * <p>
 * This should be a good tokenizer for most European-language documents:
 * <ul>
 * <li>Splits words at punctuation characters, removing punctuation. However, a dot that's not
 * followed by whitespace is considered part of a token.
 * <li>Splits words at hyphens, unless there's a number in the token, in which case the whole token
 * is interpreted as a product number and is not split.
 * <li>Recognizes email addresses and internet hostnames as one token.
 * </ul>
 * <p>
 * Many applications have specific tokenizer needs. If this tokenizer does not suit your
 * application, please consider copying this source code directory to your project and maintaining
 * your own grammar-based tokenizer.
 */

public class SilverTokenizer extends Tokenizer {
  /** A private instance of the JFlex-constructed scanner */
  private final SilverTokenizerImpl scanner;

  /**
   * Specifies whether deprecated acronyms should be replaced with HOST type. This is false by
   * default to support backward compatibility.
   *<p/>
   * See http://issues.apache.org/jira/browse/LUCENE-1068
   * @deprecated this should be removed in the next release (3.0).
   */
  private boolean replaceInvalidAcronym = false;

  void setInput(Reader reader) {
    this.input = reader;
  }

  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /**
   * Set the max allowed token length. Any token longer than this is skipped.
   */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /** @see #setMaxTokenLength */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * Creates a new instance of the {@link StandardTokenizer}. Attaches the <code>input</code> to a
   * newly created JFlex scanner.
   */
  public SilverTokenizer(Reader input) {
    this.input = input;
    this.scanner = new SilverTokenizerImpl(input);
  }

  /**
   * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.
   * Attaches the <code>input</code> to the newly created JFlex scanner.
   * @param input The input reader
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms with HOST. See
   * http://issues.apache.org/jira/browse/LUCENE-1068
   */
  public SilverTokenizer(Reader input, boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
    this.input = input;
    this.scanner = new SilverTokenizerImpl(input);
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  public Token next(Token result) throws IOException {
    int posIncr = 1;

    while (true) {
      int tokenType = scanner.getNextToken();

      if (tokenType == SilverTokenizerImpl.YYEOF) {
        return null;
      }

      if (scanner.yylength() <= maxTokenLength) {
        result.clear();
        result.setPositionIncrement(posIncr);
        scanner.getText(result);
        final int start = scanner.yychar();
        result.setStartOffset(start);
        result.setEndOffset(start + result.termLength());
        // This 'if' should be removed in the next release. For now, it converts
        // invalid acronyms to HOST. When removed, only the 'else' part should
        // remain.
        if (tokenType == SilverTokenizerImpl.ACRONYM_DEP) {
          if (replaceInvalidAcronym) {
            result
                .setType(SilverTokenizerImpl.TOKEN_TYPES[SilverTokenizerImpl.HOST]);
            result.setTermLength(result.termLength() - 1); // remove extra '.'
          } else {
            result
                .setType(SilverTokenizerImpl.TOKEN_TYPES[SilverTokenizerImpl.ACRONYM]);
          }
        } else {
          result.setType(SilverTokenizerImpl.TOKEN_TYPES[tokenType]);
        }
        return result;
      } else
        // When we skip a too-long term, we still increment the
        // position increment
        posIncr++;
    }
  }

  /*
   * (non-Javadoc)
   * @see org.apache.lucene.analysis.TokenStream#reset()
   */
  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(input);
  }

  public void reset(Reader reader) throws IOException {
    input = reader;
    reset();
  }

  /**
   * Prior to https://issues.apache.org/jira/browse/LUCENE-1068, StandardTokenizer mischaracterized
   * as acronyms tokens like www.abc.com when they should have been labeled as hosts instead.
   * @return true if StandardTokenizer now returns these tokens as Hosts, otherwise false
   * @deprecated Remove in 3.X and make true the only valid value
   */
  public boolean isReplaceInvalidAcronym() {
    return replaceInvalidAcronym;
  }

  /**
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms as HOST.
   * @deprecated Remove in 3.X and make true the only valid value See
   * https://issues.apache.org/jira/browse/LUCENE-1068
   */
  public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
  }
}
