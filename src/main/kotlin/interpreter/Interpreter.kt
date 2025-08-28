package org.example.interpreter

import org.example.Lox
import org.example.ast.Expr
import org.example.ast.Stmt
import org.example.runtime.Environment
import org.example.runtime.LoxCallable
import org.example.runtime.LoxFunction
import org.example.scanner.Token
import org.example.scanner.TokenType
import org.example.util.clock
import org.example.util.print
import org.example.util.stringify
import kotlin.math.pow

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = Environment()
    private var environment: Environment = globals

    init {
        globals.define("clock", clock)
        globals.define("print", print)
    }

    fun interpret(statements: List<Stmt?>) {
        try {
            for (stmt in statements) {
                execute(stmt)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitBreakStmt(stmt: Stmt.Break) {
        throw Break()
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitContinueStmt(stmt: Stmt.Continue) {
        throw Continue()
    }

    override fun visitEmptyStmt(stmt: Stmt.Empty) {
        // Do nothing
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name!!, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        var value: Any? = null
        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body)
            } catch (_: Break) {
                break
            } catch (_: Continue) {
                continue
            }
        }
    }

    private fun execute(stmt: Stmt?) = stmt?.accept(this)

    fun executeBlock(
        statements: List<Stmt?>,
        environment: Environment,
    ) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value: Any? = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? = environment.get(expr.name)

    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String || right is String) {
                    stringify(left) + stringify(right)
                } else {
                    throw RuntimeError(
                        expr.operator,
                        "Operands must be two numbers or two strings.",
                    )
                }
            }

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)

                if (right == 0.0) {
                    throw RuntimeError(expr.operator, "Division by zero.")
                }

                (left as Double) / (right as Double)
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            TokenType.STAR_STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double).pow(right as Double)
            }

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            TokenType.BANG_EQUAL -> left != right
            TokenType.EQUAL_EQUAL -> left == right

            else -> {
                // unreachable
            }
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = mutableListOf<Any?>()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(
                expr.paren,
                "Can only call functions and classes.",
            )
        }

        if (arguments.size != callee.arity()) {
            throw RuntimeError(
                expr.paren,
                "Expected " +
                    callee.arity() + " arguments but got " +
                    arguments.size + ".",
            )
        }

        return callee.call(this, arguments)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? = evaluate(expr.expression)

    override fun visitLambdaExpr(expr: Expr.Lambda): Any {
        val function = Stmt.Function(null, expr.params, expr.body)
        return LoxFunction(function, environment)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? = expr.value

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left
            }
        } else {
            if (!isTruthy(left)) {
                return left
            }
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.BANG -> !isTruthy(right)

            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            else -> {
                // unreachable
            }
        }
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        if (obj is Double && obj == 0.0) return false
        if (obj is String && obj.isEmpty()) return false
        return true
    }

    private fun checkNumberOperand(
        operator: Token,
        operand: Any?,
    ) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(
        operator: Token,
        left: Any?,
        right: Any?,
    ) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be numbers.")
    }
}

class RuntimeError(val token: Token, message: String) : RuntimeException(message)

class Break : RuntimeException()

class Continue : RuntimeException()

class Return(val value: Any?) : RuntimeException()
