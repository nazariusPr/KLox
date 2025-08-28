package org.example.parser

import org.example.Lox
import org.example.ast.Expr
import org.example.ast.Stmt
import org.example.scanner.Token
import org.example.scanner.TokenType

class Parser(val tokens: List<Token>) {
    private var current: Int = 0
    private var loopDepth: Int = 0
    private var functionDepth: Int = 0

    private class ParseError : RuntimeException()

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    private fun declaration(): Stmt {
        try {
            if (match(TokenType.FUN)) return function("function")
            if (match(TokenType.VAR)) return varDeclaration()

            return statement()
        } catch (_: ParseError) {
            synchronize()
            return Stmt.Empty()
        }
    }

    private fun function(kind: String): Stmt.Function {
        val name: Token = consume(TokenType.IDENTIFIER, "Expect $kind name.")
        val parameters = parseFunctionParameters()
        val body = parseFunctionBody()
        return Stmt.Function(name, parameters, body)
    }

    private fun varDeclaration(): Stmt.Var {
        val name: Token = consume(TokenType.IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        if (match(TokenType.BREAK)) return breakStatement()
        if (match(TokenType.CONTINUE)) return continueStatement()
        if (match(TokenType.FOR)) return forStatement()
        if (match(TokenType.IF)) return ifStatement()
        if (match(TokenType.RETURN)) return returnStatement()
        if (match(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        if (match(TokenType.WHILE)) return whileStatement()

        return expressionStatement()
    }

    private fun breakStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Cannot use 'break' outside of a loop.")
        }
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Break()
    }

    private fun continueStatement(): Stmt {
        if (loopDepth == 0) {
            error(previous(), "Cannot use 'break' outside of a loop.")
        }
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Continue()
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer: Stmt? =
            when {
                match(TokenType.SEMICOLON) -> null
                match(TokenType.VAR) -> varDeclaration()
                else -> expressionStatement()
            }

        var condition = if (!match(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        val increment = if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")

        loopDepth++
        var body: Stmt = statement()
        loopDepth--

        increment?.let {
            body = Stmt.Block(listOf(body, Stmt.Expression(it)))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        initializer?.let {
            body = Stmt.Block(listOf(it, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")

        val thenBranch = statement()
        val elseBranch: Stmt? = if (match(TokenType.ELSE)) statement() else null

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun returnStatement(): Stmt {
        if (functionDepth == 0) {
            error(previous(), "Cannot return from top-level code.")
        }

        val keyword = previous()
        var value: Expr? = null
        if (!check(TokenType.SEMICOLON)) {
            value = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")

        return Stmt.Expression(value)
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition: Expr = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")

        loopDepth++
        val body: Stmt = statement()
        loopDepth--

        return Stmt.While(condition, body)
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            when (expr) {
                is Expr.Variable -> {
                    val name = expr.name
                    return Expr.Assign(name, value)
                }

                else -> error(equals, "Invalid assignment target.")
            }
        }

        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
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
        var expr = power()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = power()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun power(): Expr {
        var expr = unary()

        if (match(TokenType.STAR_STAR)) {
            val operator = previous()
            val right = power()
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

        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun primary(): Expr {
        return when {
            match(TokenType.FALSE) -> Expr.Literal(false)
            match(TokenType.TRUE) -> Expr.Literal(true)
            match(TokenType.NIL) -> Expr.Literal(null)
            match(TokenType.NUMBER, TokenType.STRING) -> Expr.Literal(previous().literal)
            match(TokenType.IDENTIFIER) -> Expr.Variable(previous())
            match(TokenType.FUN) -> lambda()
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

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren =
            consume(
                TokenType.RIGHT_PAREN,
                "Expect ')' after arguments.",
            )
        return Expr.Call(callee, paren, arguments)
    }

    private fun lambda(): Expr.Lambda {
        val parameters = parseFunctionParameters()
        val body = parseFunctionBody()
        return Expr.Lambda(parameters, body)
    }

    private fun parseFunctionParameters(): List<Token> {
        val parameters = mutableListOf<Token>()
        consume(TokenType.LEFT_PAREN, "Expect '(' before parameters.")

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Can't have more than 255 parameters.")
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        return parameters
    }

    private fun parseFunctionBody(): List<Stmt> {
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.")
        functionDepth++
        val body = block()
        functionDepth--
        return body
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
                TokenType.RETURN,
                -> return

                else -> {
                    // do nothing
                }
            }
        } while (!isAtEnd())
    }
}
