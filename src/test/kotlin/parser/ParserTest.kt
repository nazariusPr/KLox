package parser

import org.example.ast.Expr
import org.example.ast.Stmt
import org.example.parser.Parser
import org.example.scanner.Scanner
import org.example.scanner.Token
import org.example.scanner.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParserTest {
    private fun parseSource(source: String): List<Stmt> {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens)
        return parser.parse()
    }

    @Test
    fun `test variable declaration without initializer`() {
        val stmts = parseSource("var a;")
        assertEquals(1, stmts.size)

        val varStmt = stmts[0] as Stmt.Var
        assertEquals("a", varStmt.name.lexeme)
        assertNull(varStmt.initializer)
    }

    @Test
    fun `test variable declaration with initializer`() {
        val stmts = parseSource("var a = 42;")
        val varStmt = stmts[0] as Stmt.Var

        assertEquals("a", varStmt.name.lexeme)
        assertIs<Expr.Literal>(varStmt.initializer)
        assertEquals(42.0, varStmt.initializer.value)
    }

    @Test
    fun `test print statement`() {
        val stmts = parseSource("print 123;")
        val printStmt = stmts[0] as Stmt.Print

        assertIs<Expr.Literal>(printStmt.expression)
        assertEquals(123.0, printStmt.expression.value)
    }

    @Test
    fun `test expression statement`() {
        val stmts = parseSource("1 + 2;")
        val exprStmt = stmts[0] as Stmt.Expression
        val binary = exprStmt.expression as Expr.Binary

        assertIs<Expr.Literal>(binary.left)
        assertIs<Expr.Literal>(binary.right)
    }

    @Test
    fun `test if statement without else`() {
        val stmts = parseSource("if (true) print 1;")
        val ifStmt = stmts[0] as Stmt.If

        assertIs<Expr.Literal>(ifStmt.condition)
        assertIs<Stmt.Print>(ifStmt.thenBranch)
        assertNull(ifStmt.elseBranch)
    }

    @Test
    fun `test if statement with else`() {
        val stmts = parseSource("if (false) print 1; else print 2;")
        val ifStmt = stmts[0] as Stmt.If

        assertIs<Expr.Literal>(ifStmt.condition)
        assertIs<Stmt.Print>(ifStmt.thenBranch)
        assertIs<Stmt.Print>(ifStmt.elseBranch)
    }

    @Test
    fun `test while statement`() {
        val stmts = parseSource("while (true) print 1;")
        val whileStmt = stmts[0] as Stmt.While

        assertIs<Expr.Literal>(whileStmt.condition)
        assertIs<Stmt.Print>(whileStmt.body)
    }

    @Test
    fun `test for loop expansion`() {
        val stmts = parseSource("for (var i = 0; i < 10; i = i + 1) print i;")
        val block = stmts[0] as Stmt.Block
        assertTrue(block.statements[0] is Stmt.Var)

        val whileStmt = block.statements[1] as Stmt.While
        assertIs<Expr.Binary>(whileStmt.condition)
    }

    @Test
    fun `test block statement`() {
        val stmts = parseSource("{ var a = 1; print a; }")
        val blockStmt = stmts[0] as Stmt.Block

        assertEquals(2, blockStmt.statements.size)
        assertTrue(blockStmt.statements[0] is Stmt.Var)
        assertTrue(blockStmt.statements[1] is Stmt.Print)
    }

    @Test
    fun `test grouping expression`() {
        val stmts = parseSource("(1 + 2) * 3;")
        val exprStmt = stmts[0] as Stmt.Expression
        val binaryOuter = exprStmt.expression as Expr.Binary

        assertIs<Expr.Grouping>(binaryOuter.left)
    }

    @Test
    fun `test unary expression`() {
        val stmts = parseSource("-5;")
        val exprStmt = stmts[0] as Stmt.Expression
        val unary = exprStmt.expression as Expr.Unary

        assertEquals(TokenType.MINUS, unary.operator.type)
        assertIs<Expr.Literal>(unary.right)
    }

    @Test
    fun `test logical and expression`() {
        val stmts = parseSource("true and false;")
        val exprStmt = stmts[0] as Stmt.Expression
        val logical = exprStmt.expression as Expr.Logical

        assertEquals(TokenType.AND, logical.operator.type)
    }

    @Test
    fun `test logical or expression`() {
        val stmts = parseSource("false or true;")
        val exprStmt = stmts[0] as Stmt.Expression
        val logical = exprStmt.expression as Expr.Logical

        assertEquals(TokenType.OR, logical.operator.type)
    }

    @Test
    fun `test power operator`() {
        val stmts = parseSource("2 ** 3;")
        val exprStmt = stmts[0] as Stmt.Expression
        val binary = exprStmt.expression as Expr.Binary

        assertEquals(TokenType.STAR_STAR, binary.operator.type)
    }
}
