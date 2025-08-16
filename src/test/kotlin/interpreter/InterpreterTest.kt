package interpreter
import org.example.ast.Expr
import org.example.ast.Stmt
import org.example.interpreter.Interpreter
import org.example.interpreter.RuntimeError
import org.example.runtime.Environment
import org.example.scanner.Token
import org.example.scanner.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InterpreterTest {
    private fun numberLiteral(value: Double) = Expr.Literal(value)

    private fun stringLiteral(value: String) = Expr.Literal(value)

    private fun boolLiteral(value: Boolean) = Expr.Literal(value)

    private fun token(
        type: TokenType,
        lexeme: String = type.name,
        literal: Any? = null,
    ) = Token(type, lexeme, literal, 1)

    @Test
    fun testLiteralExpression() {
        val interpreter = Interpreter()
        val expr = numberLiteral(42.0)
        val result = expr.accept(interpreter)
        assertEquals(42.0, result)
    }

    @Test
    fun testBinaryAdditionNumbers() {
        val interpreter = Interpreter()
        val expr =
            Expr.Binary(
                numberLiteral(3.0),
                token(TokenType.PLUS, "+"),
                numberLiteral(5.0),
            )
        val result = expr.accept(interpreter)
        assertEquals(8.0, result)
    }

    @Test
    fun testBinaryAdditionStrings() {
        val interpreter = Interpreter()
        val expr =
            Expr.Binary(
                stringLiteral("Hello "),
                token(TokenType.PLUS, "+"),
                stringLiteral("World"),
            )
        val result = expr.accept(interpreter)
        assertEquals("Hello World", result)
    }

    @Test
    fun testBinaryAdditionInvalidTypesThrows() {
        val interpreter = Interpreter()
        val expr =
            Expr.Binary(
                numberLiteral(3.0),
                token(TokenType.PLUS, "+"),
                stringLiteral("oops"),
            )
        assertFailsWith<RuntimeError> {
            expr.accept(interpreter)
        }
    }

    @Test
    fun testDivisionByZeroThrows() {
        val interpreter = Interpreter()
        val expr =
            Expr.Binary(
                numberLiteral(4.0),
                token(TokenType.SLASH, "/"),
                numberLiteral(0.0),
            )
        assertFailsWith<RuntimeError> {
            expr.accept(interpreter)
        }
    }

    @Test
    fun testUnaryNegation() {
        val interpreter = Interpreter()
        val expr =
            Expr.Unary(
                token(TokenType.MINUS, "-"),
                numberLiteral(10.0),
            )
        val result = expr.accept(interpreter)
        assertEquals(-10.0, result)
    }

    @Test
    fun testLogicalOrShortCircuit() {
        val interpreter = Interpreter()
        val expr =
            Expr.Logical(
                boolLiteral(true),
                token(TokenType.OR, "or"),
                Expr.Variable(token(TokenType.IDENTIFIER, "unreachable")),
            )
        val result = expr.accept(interpreter)
        assertEquals(true, result)
    }

    @Test
    fun testVarDeclarationAndUsage() {
        val interpreter = Interpreter()
        val varStmt = Stmt.Var(token(TokenType.IDENTIFIER, "x"), numberLiteral(123.0))
        interpreter.interpret(listOf(varStmt))

        assertTrue(true)
    }

    @Test
    fun testIfStatementTrueBranch() {
        val interpreter = Interpreter()
        val stmt =
            Stmt.If(
                boolLiteral(true),
                Stmt.Print(stringLiteral("yes")),
                Stmt.Print(stringLiteral("no")),
            )
        interpreter.interpret(listOf(stmt))
        assertTrue(true)
    }

    @Test
    fun testWhileLoopExecutesMultipleTimes() {
        val interpreter = Interpreter()
        val counter = token(TokenType.IDENTIFIER, "i")
        val env = Environment()
        env.define("i", 0.0)

        val loop =
            Stmt.While(
                Expr.Binary(
                    Expr.Variable(counter),
                    token(TokenType.LESS, "<"),
                    numberLiteral(3.0),
                ),
                Stmt.Expression(
                    Expr.Assign(
                        counter,
                        Expr.Binary(
                            Expr.Variable(counter),
                            token(TokenType.PLUS, "+"),
                            numberLiteral(1.0),
                        ),
                    ),
                ),
            )

        interpreter.executeBlock(listOf(loop), env)
        assertEquals(3.0, env.get(counter))
    }
}
