package interpreter

import org.example.ast.Expr
import org.example.interpreter.Interpreter
import org.example.scanner.Token
import org.example.scanner.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InterpreterTest {
    private val interpreter = Interpreter()

    private fun numberLiteral(value: Double): Expr.Literal = Expr.Literal(value)

    private fun stringLiteral(value: String): Expr.Literal = Expr.Literal(value)

    private fun token(type: TokenType): Token = Token(type, type.name, null, 1)

    @Test
    fun `test addition of two numbers`() {
        val expr =
            Expr.Binary(
                numberLiteral(2.0),
                token(TokenType.PLUS),
                numberLiteral(3.0),
            )
        val result = interpreter.interpret(expr)
        assertEquals("5", result)
    }

    @Test
    fun `test addition of two strings`() {
        val expr =
            Expr.Binary(
                stringLiteral("foo"),
                token(TokenType.PLUS),
                stringLiteral("bar"),
            )
        val result = interpreter.interpret(expr)
        assertEquals("foobar", result)
    }

    @Test
    fun `test greater than comparison`() {
        val expr =
            Expr.Binary(
                numberLiteral(5.0),
                token(TokenType.GREATER),
                numberLiteral(3.0),
            )
        val result = interpreter.interpret(expr)
        assertEquals("true", result)
    }

    @Test
    fun `test unary minus expression`() {
        val expr =
            Expr.Unary(
                token(TokenType.MINUS),
                numberLiteral(5.0),
            )
        val result = interpreter.interpret(expr)
        assertEquals("-5", result)
    }

    @Test
    fun `test unary bang expression on null`() {
        val expr =
            Expr.Unary(
                token(TokenType.BANG),
                Expr.Literal(null),
            )
        val result = interpreter.interpret(expr)
        assertEquals("true", result)
    }

    @Test
    fun `test runtime error on invalid operand types`() {
        val expr =
            Expr.Binary(
                numberLiteral(2.0),
                token(TokenType.PLUS),
                stringLiteral("bad"),
            )

        val result = interpreter.interpret(expr)
        assertNull(result)
    }
}
