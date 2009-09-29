package com.stratelia.webactiv.util.indexEngine.model;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Extends lucene TokenFilter : replaces from each token all the special
 * characters like é or ç.
 */
public final class CharFilter extends TokenFilter {
  /**
   * Constructs a filter which uses the given CharReplacer to change the token
   * of the stream.
   */
  /*
   * public CharFilter(TokenStream stream, CharReplacer replacer) { this.input =
   * stream; this.replacer = replacer; }
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
