package org.example.scanner

import org.example.Lox
import org.example.util.isAlpha
import org.example.util.isAlphaNumeric

class Scanner(val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    val tokens = mutableListOf<Token>()

    companion object {
        private val keywords: Map<String, TokenType> =
            mapOf(
                "and" to TokenType.AND,
                "break" to TokenType.BREAK,
                "continue" to TokenType.CONTINUE,
                "class" to TokenType.CLASS,
                "else" to TokenType.ELSE,
                "false" to TokenType.FALSE,
                "for" to TokenType.FOR,
                "fun" to TokenType.FUN,
                "if" to TokenType.IF,
                "nil" to TokenType.NIL,
                "or" to TokenType.OR,
                "return" to TokenType.RETURN,
                "super" to TokenType.SUPER,
                "this" to TokenType.THIS,
                "true" to TokenType.TRUE,
                "var" to TokenType.VAR,
                "while" to TokenType.WHILE,
            )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        when (val c: Char = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> {
                addToken(if (match('*')) TokenType.STAR_STAR else TokenType.STAR)
            }

            '!' -> {
                addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            }

            '=' -> {
                addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            }

            '<' -> {
                addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            }

            '>' -> {
                addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            }

            '/' -> {
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            ' ', '\r', '\t' -> {
                // Ignore whitespace.
            }

            '\n' -> line++

            '"' -> string()

            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isAlpha()) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character: $c")
                }
            }
        }
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(
        type: TokenType,
        literal: Any?,
    ) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (expected != source[current]) return false

        advance()
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        advance()
        val str = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, str)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }

        val num = source.substring(start, current)
        addToken(TokenType.NUMBER, num.toDouble())
    }

    private fun identifier() {
        while (peek().isAlphaNumeric()) advance()

        val text = source.substring(start, current)
        val type: TokenType = keywords[text] ?: TokenType.IDENTIFIER

        addToken(type)
    }
}
