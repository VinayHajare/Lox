package JLox.lox;

import JLox.lox.Expr.Assign;
import JLox.lox.Expr.Variable;

/**
 * A pretty printer to print string representation of given AST Node.
 * It doen't print the Lox source program of given AST but its equivalent code
 * in LISP style like
 * (* (-123) (group 90.50))
 * which represent tree in the form :-
 *          [*]
 *          / \
 *       [-]   [()]
 *        |      |
 *      [123]  [90.50]
 */
public class ASTPrinter implements Expr.Visitor<String> {

    // Call the correct visitor method for given AST node 
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }

        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return parenthesize("assign " + expr.name.lexeme, expr.value);
    }

    // Recusrssively convert expr to the LISP-like string
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)
                        )
        );
        System.out.println(new ASTPrinter().print(expression));
    }
}
