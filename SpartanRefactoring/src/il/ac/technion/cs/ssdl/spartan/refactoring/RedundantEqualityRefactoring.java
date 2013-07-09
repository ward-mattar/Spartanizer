package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.BasicSpartanization.SpartanizationRange;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin (v2)
 * 
 */
public class RedundantEqualityRefactoring extends BaseRefactoring {
  @Override public String getName() {
    return "Remove Redundant Equality";
  }
  
  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression node) {
        if (!inRange(m, node))
          return true;
        if (node.getOperator() != Operator.EQUALS && node.getOperator() != Operator.NOT_EQUALS)
          return true;
        ASTNode nonliteral = null;
        BooleanLiteral literal = null;
        if (node.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            && node.getLeftOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
          nonliteral = r.createMoveTarget(node.getLeftOperand());
          literal = (BooleanLiteral) node.getRightOperand();
        } else if (node.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            && node.getRightOperand().getNodeType() != ASTNode.BOOLEAN_LITERAL) {
          nonliteral = r.createMoveTarget(node.getRightOperand());
          literal = (BooleanLiteral) node.getLeftOperand();
        } else
          return true;
        ASTNode newnode = null;
        if (literal.booleanValue() && node.getOperator() == Operator.EQUALS || !literal.booleanValue()
            && node.getOperator() == Operator.NOT_EQUALS)
          newnode = nonliteral;
        else {
          final ParenthesizedExpression paren = t.newParenthesizedExpression();
          paren.setExpression((Expression) nonliteral);
          newnode = t.newPrefixExpression();
          ((PrefixExpression) newnode).setOperand(paren);
          ((PrefixExpression) newnode).setOperator(PrefixExpression.Operator.NOT);
        }
        r.replace(node, newnode, null);
        return true;
      }
    });
  }
  
  @Override public Collection<SpartanizationRange> checkForSpartanization(final CompilationUnit cu) {
    final Collection<SpartanizationRange> $ = new ArrayList<SpartanizationRange>();
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression node) {
        if (node.getOperator() != Operator.EQUALS && node.getOperator() != Operator.NOT_EQUALS)
          return true;
        if (node.getRightOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL
            || node.getLeftOperand().getNodeType() == ASTNode.BOOLEAN_LITERAL)
          $.add(new SpartanizationRange(node));
        return true;
      }
    });
    return $;
  }
}
