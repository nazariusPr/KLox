package ast

import org.example.ast.AstPrinter
import org.example.ast.Expr
import org.example.scanner.Token
import org.example.scanner.TokenType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AstPrinterTest {
    @Test
    fun `test expressions printer`() {
        val expression =
            Expr.Binary(
                Expr.Unary(
                    Token(TokenType.MINUS, "-", null, 1),
                    Expr.Literal(123),
                ),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Grouping(
                    Expr.Literal(45.67),
                ),
            )

        val astPrinter = AstPrinter()
        val result = astPrinter.print(expression)

        assertEquals("(* (- 123) (group 45.67))", result)
    }
}
