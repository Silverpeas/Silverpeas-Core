package com.stratelia.webactiv.util.indexEngine.model;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Remove apostrophe from tokens extracted with StandardFilter. Used in french
 * to remove first part of token such as: "l'amour" to "amour" "d'ailleurs" to
 * "ailleurs" "c'est" to "est" "m'avait" to "avait" "n'avez" to "avez" "s'était"
 * to "était" "t'étais" to "étais"
 * 
 * @author neysseri
 * 
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