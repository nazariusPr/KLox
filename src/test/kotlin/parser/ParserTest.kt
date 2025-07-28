package parser

import org.example.ast.Expr
import org.example.parser.Parser
import org.example.scanner.Scanner
import org.example.scanner.Token
import org.example.scanner.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParserTest {
    private fun parseSource(source: String): Expr? {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens)

        return parser.parse()
    }

    @Test
    fun `test literal true`() {
        val expr = parseSource("true")

        assertTrue(expr is Expr.Literal)
        assertEquals(true, expr.value)
    }

    @Test
    fun `test number literal`() {
        val expr = parseSource("42")

        assertTrue(expr is Expr.Literal)
        assertEquals(42.0, expr.value)
    }

    @Test
    fun `test string literal`() {
        val expr = parseSource("\"lox\"")

        assertTrue(expr is Expr.Literal)
        assertEquals("lox", expr.value)
    }

    @Test
    fun `test grouping expression`() {
        val expr = parseSource("(123)")
        assertTrue(expr is Expr.Grouping)

        val inner = expr.expression
        assertTrue(inner is Expr.Literal)
        assertEquals(123.0, inner.value)
    }

    @Test
    fun `test binary precedence - multiplication before addition`() {
        val expr = parseSource("1 + 2 * 3")
        assertTrue(expr is Expr.Binary)

        assertEquals(TokenType.PLUS, expr.operator.type)
        assertTrue(expr.right is Expr.Binary)

        val right = expr.right
        assertEquals(TokenType.STAR, right.operator.type)
    }

    @Test
    fun `test unary minus`() {
        val expr = parseSource("-123")
        assertTrue(expr is Expr.Unary)

        assertEquals(TokenType.MINUS, expr.operator.type)
        assertTrue(expr.right is Expr.Literal)
        assertEquals(123.0, expr.right.value)
    }

    @Test
    fun `test error on invalid expression`() {
        val expr = parseSource("* 42")
        assertNull(expr)
    }
}
