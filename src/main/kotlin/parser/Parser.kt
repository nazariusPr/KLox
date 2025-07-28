package org.example.parser

import org.example.Lox
import org.example.ast.Expr
import org.example.scanner.Token
import org.example.scanner.TokenType

class Parser(val tokens: List<Token>) {
    private var current: Int = 0

    private class ParseError : RuntimeException()

    fun parse(): Expr? {
        return try {
            expression()
        } catch (_: ParseError) {
            null
        }
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator: Token = previous()
            val right: Expr = unary()

            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        return when {
            match(TokenType.FALSE) -> Expr.Literal(false)
            match(TokenType.TRUE) -> Expr.Literal(true)
            match(TokenType.NIL) -> Expr.Literal(null)
            match(TokenType.NUMBER, TokenType.STRING) -> Expr.Literal(previous().literal)
            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
                Expr.Grouping(expr)
            }

            else -> throw error(peek(), "Expect expression.")
        }
    }

    private fun consume(
        type: TokenType,
        message: String,
    ): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(
        token: Token,
        message: String,
    ): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        do {
            advance()

            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS,
                TokenType.FUN,
                TokenType.VAR,
                TokenType.FOR,
                TokenType.IF,
                TokenType.WHILE,
                TokenType.PRINT,
                TokenType.RETURN,
                -> return

                else -> {
                    // do nothing
                }
            }
        } while (!isAtEnd())
    }
}
