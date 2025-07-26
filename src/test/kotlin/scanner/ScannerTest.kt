package scanner

import org.example.Lox
import org.example.scanner.Scanner
import org.example.scanner.Token
import org.example.scanner.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class ScannerTest {
    @Test
    fun `scan single symbol`() {
        val tokens = scan("(")

        assertEquals(2, tokens.size)
        assertEquals(TokenType.LEFT_PAREN, tokens[0].type)
        assertEquals(TokenType.EOF, tokens[1].type)
    }

    @Test
    fun `scan keyword`() {
        val tokens = scan("var")

        assertEquals(TokenType.VAR, tokens[0].type)
        assertEquals("var", tokens[0].lexeme)
    }

    @Test
    fun `scan identifier`() {
        val tokens = scan("myVar")

        assertEquals(TokenType.IDENTIFIER, tokens[0].type)
        assertEquals("myVar", tokens[0].lexeme)
    }

    @Test
    fun `scan number`() {
        val tokens = scan("123.45")

        assertEquals(TokenType.NUMBER, tokens[0].type)
        assertEquals(123.45, tokens[0].literal)
    }

    @Test
    fun `scan string`() {
        val tokens = scan("\"hello\"")

        assertEquals(TokenType.STRING, tokens[0].type)
        assertEquals("hello", tokens[0].literal)
    }

    @Test
    fun `scan equal-equal`() {
        val tokens = scan("==")

        assertEquals(TokenType.EQUAL_EQUAL, tokens[0].type)
        assertEquals("==", tokens[0].lexeme)
    }

    @Test
    fun `scan comment`() {
        val tokens = scan("// A very cool comment \n // Next cool comment")

        assertEquals(1, tokens.size)
        assertEquals(TokenType.EOF, tokens[0].type)
    }

    @Test
    fun `scan unexpected symbol`() {
        val tokens = scan("$")

        assertEquals(true, Lox.hadError)
        assertEquals(TokenType.EOF, tokens[0].type)
    }

    private fun scan(source: String): List<Token> {
        val scanner = Scanner(source)
        return scanner.scanTokens()
    }
}
