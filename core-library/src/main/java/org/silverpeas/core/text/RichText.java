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
package org.silverpeas.core.text;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Text;
import org.commonmark.node.Node;
import org.commonmark.node.CustomNode;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.parser.Parser;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.renderer.text.TextContentRenderer;

/** Markdown interchange utilities; HTML is never used as a storage format. */
public final class RichText {
  private static final Parser PARSER = Parser.builder().extensions(List.of(StrikethroughExtension.create())).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();
  private static final TextContentRenderer TEXT = TextContentRenderer.builder().build();
  private RichText() { }

  public static String plainText(String markdown) {
    var document = PARSER.parse(markdown == null ? "" : markdown);
    document.accept(new AbstractVisitor() {
      @Override
      public void visit(Link link) {
        visitChildren(link);
        unwrap(link);
      }
      @Override
      public void visit(CustomNode node) {
        visitChildren(node);
        if (node instanceof Strikethrough) { unwrap(node); }
      }
    });
    return TEXT.render(document).strip();
  }

  private static void unwrap(Node node) {
    while (node.getFirstChild() != null) { node.insertBefore(node.getFirstChild()); }
    node.unlink();
  }

  private static boolean canonicalAt(String source, Link link) {
    if (link.getSourceSpans().isEmpty()) { return false; }
    int at = link.getSourceSpans().getFirst().getInputIndex() - 1;
    if (at < 0 || source.charAt(at) != '@') { return false; }
    int slashes = 0;
    for (int index = at - 1; index >= 0 && source.charAt(index) == '\\'; index--) { slashes++; }
    return slashes % 2 == 0;
  }

  /** Only actual mention links are considered, excluding code and escaped syntax. */
  public static Set<String> mentionIds(String markdown) {
    var ids = new LinkedHashSet<String>();
    String source = markdown == null ? "" : markdown;
    PARSER.parse(source).accept(new AbstractVisitor() {
      @Override
      public void visit(Link link) {
        if (link.getDestination().matches("[0-9]{1,40}")
            && link.getPrevious() instanceof Text text && text.getLiteral().endsWith("@")
            && canonicalAt(source, link)) {
          ids.add(link.getDestination());
        }
        visitChildren(link);
      }
    });
    return Set.copyOf(ids);
  }
}
