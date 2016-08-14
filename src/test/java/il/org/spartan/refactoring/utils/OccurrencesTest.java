package il.org.spartan.refactoring.utils;

import static il.org.spartan.azzert.*;
import static il.org.spartan.refactoring.utils.Funcs.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;
import org.junit.runners.*;

import il.org.spartan.refactoring.spartanizations.*;

/**
 * @author Yossi Gil
 * @since 2014-08-25
 */
@SuppressWarnings({ "javadoc" })//
@FixMethodOrder(MethodSorters.NAME_ASCENDING)//
public class OccurrencesTest {
  private final String from = "int a = 2,b; if (a+b) a =3;";
  private final String wrap = Wrap.Statement.on(from);
  private final CompilationUnit u = (CompilationUnit) ast.COMPILIATION_UNIT.from(wrap);
  private final SimpleName a = extract.firstVariableDeclarationFragment(u).getName();
  private final VariableDeclarationStatement ab = (VariableDeclarationStatement) a.getParent().getParent();
  private final SimpleName b = ((VariableDeclarationFragment) ab.fragments().get(1)).getName();
  private final IfStatement s = extract.nextIfStatement(a);
  private final InfixExpression e = (InfixExpression) s.getExpression();

  @Test public void correctSettings() {
    that(ab, iz("int a=2,b;"));
    that(b.toString(), is("b"));
    that(s, is(extract.firstIfStatement(u)));
    that(s, iz("if (a + b) a=3;"));
    that(e, iz("a + b"));
  }
  @Test public void exploreLeftOfE() {
    that(left(e), iz("a"));
  }
  @Test public void lexicalUsesCollector() {
    final List<SimpleName> into = new ArrayList<>();
    final ASTVisitor collector = Collect.lexicalUsesCollector(into, a);
    a.accept(collector);
    that(into.size(), is(1));
  }
  @Test public void occurencesAinAL() {
    that(Collect.BOTH_SEMANTIC.of(a).in(a).size(), is(1));
  }
  @Test public void occurencesAinAsame() {
    that(same(a, a), is(true));
  }
  @Test public void occurencesAinE() {
    that(Collect.BOTH_SEMANTIC.of(a).in(e).size(), is(1));
  }
  @Test public void occurencesAinLeftOfE() {
    that(Collect.BOTH_SEMANTIC.of(a).in(left(e)).size(), is(1));
  }
  @Test public void occurencesAinLeftOfEsame() {
    that(same(left(e), a), is(true));
  }
  @Test public void occurencesAinRightOfE() {
    that(Collect.BOTH_SEMANTIC.of(a).in(right(e)).size(), is(0));
  }
  @Test public void occurencesBinE() {
    that(Collect.BOTH_SEMANTIC.of(b).in(e).size(), is(1));
  }
  @Test public void occurencesBinRightOfE() {
    that(Collect.BOTH_SEMANTIC.of(b).in(right(e)).size(), is(1));
  }
  @Test public void sameAandLeftOfE() {
    that(same(a, left(e)), is(true));
  }
  @Test public void sameTypeAandLeftOfE() {
    that(a, instanceOf(left(e).getClass()));
  }
}