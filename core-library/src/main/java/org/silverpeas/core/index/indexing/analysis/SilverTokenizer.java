/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.analysis;

import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import org.silverpeas.core.silvertrace.SilverTrace;

/**
 * A grammar-based tokenizer constructed with JFlex <p> This should be a good tokenizer for most
 * European-language documents: <ul> <li>Splits words at punctuation characters, removing
 * punctuation. However, a dot that's not followed by whitespace is considered part of a token.
 * <li>Splits words at hyphens, unless there's a number in the token, in which case the whole token
 * is interpreted as a product number and is not split. <li>Recognizes email addresses and internet
 * hostnames as one token. </ul> <p> Many applications have specific tokenizer needs. If this
 * tokenizer does not suit your application, please consider copying this source code directory to
 * your project and maintaining your own grammar-based tokenizer.
 */
public class SilverTokenizer extends Tokenizer {

  /**
   * A private instance of the JFlex-constructed scanner
   */
  private SilverTokenizerImpl scanner;
  private boolean replaceInvalidAcronym;
  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /**
   * Set the max allowed token length. Any token longer than this is skipped.
   */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /**
   * @see #setMaxTokenLength
   */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.
   * Attaches the
   * <code>input</code> to the newly created JFlex scanner.
   *
   * @param input The input reader
   *
   * See http://issues.apache.org/jira/browse/LUCENE-1068
   */
  public SilverTokenizer(Reader input) {
    super(input);
    init();
  }

  /**
   * Creates a new StandardTokenizer with a given {@link AttributeSource}.
   */
  public SilverTokenizer(AttributeSource source, Reader input) {
    super(source, input);
    init();
  }

  /**
   * Creates a new StandardTokenizer with a given
   * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}
   */
  public SilverTokenizer(AttributeFactory factory, Reader input) {
    super(factory, input);
    init();
  }

  private void init() {
    this.scanner = new SilverTokenizerImpl(input);
    replaceInvalidAcronym = false;
  }

  // this tokenizer generates three attributes:
  // term offset, positionIncrement and type
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posIncrAtt = addAttribute(
          PositionIncrementAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    int posIncr = 1;

    while (true) {
      int tokenType = -1;

      try {
        tokenType = scanner.getNextToken();
      } catch (Error e) {
        SilverTrace.error("indexing", "SilverTokenizer.incrementToken", "root.MSG_GEN_PARAM_VALUE",
            "Error while tokenizing content : " + e.getMessage());
        return false;
      }

      if (tokenType == StandardTokenizerInterface.YYEOF) {
        return false;
      }

      if (scanner.yylength() <= maxTokenLength) {
        posIncrAtt.setPositionIncrement(posIncr);
        scanner.getText(termAtt);
        final int start = scanner.yychar();
        offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.length()));
        // This 'if' should be removed in the next release. For now, it converts
        // invalid acronyms to HOST. When removed, only the 'else' part should
        // remain.
        if (tokenType == SilverTokenizerImpl.ACRONYM_DEP) {
          if (replaceInvalidAcronym) {
            typeAtt.setType(SilverTokenizerImpl.TOKEN_TYPES[SilverTokenizerImpl.HOST]);
            termAtt.setLength(termAtt.length() - 1); // remove extra '.'
          } else {
            typeAtt.setType(SilverTokenizerImpl.TOKEN_TYPES[SilverTokenizerImpl.ACRONYM]);
          }
        } else {
          typeAtt.setType(SilverTokenizerImpl.TOKEN_TYPES[tokenType]);
        }
        return true;
      } else // When we skip a too-long term, we still increment the
      // position increment
      {
        posIncr++;
      }
    }
  }

  @Override
  public final void end() {
    // set final offset
    int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }

  @Override
  public void reset(Reader reader) throws IOException {
    super.reset(reader);
    scanner.yyreset(reader);
  }

  /**
   * Prior to https://issues.apache.org/jira/browse/LUCENE-1068, StandardTokenizer mischaracterized
   * as acronyms tokens like www.abc.com when they should have been labeled as hosts instead.
   *
   * @return true if StandardTokenizer now returns these tokens as Hosts, otherwise false
   *
   * @deprecated Remove in 3.X and make true the only valid value
   */
  @Deprecated
  public boolean isReplaceInvalidAcronym() {
    return replaceInvalidAcronym;
  }

  /**
   *
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms as HOST.
   * @deprecated Remove in 3.X and make true the only valid value
   *
   * See https://issues.apache.org/jira/browse/LUCENE-1068
   */
  @Deprecated
  public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
  }
}
