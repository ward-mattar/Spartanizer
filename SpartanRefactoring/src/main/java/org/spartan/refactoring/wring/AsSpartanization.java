package org.spartan.refactoring.wring;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.spartan.refactoring.spartanizations.Spartanization;
import org.spartan.utils.Range;

/**
 * An adapter which makes it possible to use a single @{link Wring} as a
 * {@link Spartanization}
 *
 * @author Yossi Gil
 * @since 2015/07/25
 */
public class AsSpartanization extends Spartanization {
  final Wring inner;
  /**
   * Instantiates this class
   *
   * @param inner The wring we wish to convert
   * @param name The title of the refactoring
   * @param description One line description of the refactoring
   */
  public AsSpartanization(final Wring inner, final String name, final String description) {
    super(name, description);
    this.inner = inner;
  }
  @Override protected ASTVisitor collectOpportunities(final List<Range> $) {
    return new ASTVisitor() {
      @Override public boolean visit(final Block e) {
        if (!inner.scopeIncludes(e) || inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final ConditionalExpression e) {
        if (!inner.scopeIncludes(e) || inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final IfStatement e) {
        if (!inner.scopeIncludes(e) || inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final InfixExpression e) {
        if (!inner.scopeIncludes(e) || inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final PrefixExpression e) {
        if (!inner.scopeIncludes(e) || inner.noneligible(e))
          return true;
        $.add(new Range(e));
        return true;
      }
      @Override public boolean visit(final VariableDeclarationFragment f) {
        if (!inner.scopeIncludes(f) || inner.noneligible(f))
          return true;
        $.add(new Range(f));
        return true;
      }
    };
  }
  @Override protected final void fillRewrite(final ASTRewrite r, @SuppressWarnings("unused") final AST t, final CompilationUnit u, final IMarker m) {
    u.accept(new ASTVisitor() {
      @Override public boolean visit(final Block e) {
        return !inRange(m, e) || inner.go(r, e);
      }
      @Override public boolean visit(final ConditionalExpression e) {
        return !inRange(m, e) || inner.go(r, e);
      }
      @Override public boolean visit(final IfStatement e) {
        return !inRange(m, e) || inner.go(r, e);
      }
      @Override public boolean visit(final InfixExpression e) {
        return !inRange(m, e) || inner.go(r, e);
      }
      @Override public boolean visit(final PrefixExpression e) {
        return !inRange(m, e) || inner.go(r, e);
      }
      @Override public boolean visit(final VariableDeclarationFragment f) {
        return !inRange(m, f) || inner.go(r, f);
      }
    });
  }
}