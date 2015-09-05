package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.elze;
import static org.spartan.refactoring.utils.Funcs.same;
import static org.spartan.refactoring.utils.Funcs.then;
import static org.spartan.refactoring.wring.Wrings.insertAfter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>if (a) {
i++;
f();
} else {
j++;
f();
}</code> into <code>if (a) {
i++;
} else {
j++;
}
f();</code>
 *
 * @author Yossi Gil
 * @since 2015-09-05
 */
public final class IfBarFooElseBazFoo extends Wring<IfStatement> {
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Consolidate commmon suffix of then and else branches to just after if statement";
  }
  @Override boolean eligible(@SuppressWarnings("unused") final IfStatement _) {
    return true;
  }
  @Override Rewrite make(final IfStatement s) {
    final List<Statement> then = Extract.statements(then(s));
    if (then.isEmpty())
      return null;
    final List<Statement> elze = Extract.statements(elze(s));
    if (elze.isEmpty())
      return null;
    final List<Statement> commmonSuffix = commmonSuffix(then, elze);
    return then.isEmpty() && elze.isEmpty() || commmonSuffix.isEmpty() ? null : new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        final IfStatement newIf = replacement();
        if (Is.block(s.getParent())) {
          final ListRewrite lr = insertAfter(s, commmonSuffix, r, g);
          lr.insertAfter(newIf, s, g);
          lr.remove(s, g);
        } else {
          if (newIf != null)
            commmonSuffix.add(0, newIf);
          final Block b = Subject.ss(commmonSuffix).toBlock();
          r.replace(s, b, g);
        }
      }
      private IfStatement replacement() {
        return replacement(s.getExpression(), Subject.ss(then).toOneStatementOrNull(), Subject.ss(elze).toOneStatementOrNull());
      }
      private IfStatement replacement(final Expression condition, final Statement trimmedThen, final Statement trimmedElse) {
        return trimmedThen == null && trimmedElse == null ? null
            : trimmedThen == null ? Subject.pair(trimmedElse, null).toNot(condition) : Subject.pair(trimmedThen, trimmedElse).toIf(condition);
      }
    };
  }
  private static List<Statement> commmonSuffix(final List<Statement> ss1, final List<Statement> ss2) {
    final List<Statement> $ = new ArrayList<>();
    while (!ss1.isEmpty() && !ss2.isEmpty()) {
      final Statement s1 = ss1.get(ss1.size() - 1);
      final Statement s2 = ss2.get(ss2.size() - 1);
      if (!same(s1, s2))
        break;
      $.add(s1);
      ss1.remove(ss1.size() - 1);
      ss2.remove(ss2.size() - 1);
    }
    return $;
  }
  @Override Rewrite make(final IfStatement n, final ExclusionManager exclude) {
    return super.make(n, exclude);
  }
  @Override boolean scopeIncludes(final IfStatement n) {
    return make(n) != null;
  }
}