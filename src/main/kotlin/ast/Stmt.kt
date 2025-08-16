package org.example.ast

import org.example.scanner.Token

sealed class Stmt {
    interface Visitor<R> {
        fun visitBreakStmt(stmt: Break): R

        fun visitBlockStmt(stmt: Block): R

        fun visitContinueStmt(stmt: Continue): R

        fun visitEmptyStmt(stmt: Empty): R

        fun visitExpressionStmt(stmt: Expression): R

        fun visitIfStmt(stmt: If): R

        fun visitPrintStmt(stmt: Print): R

        fun visitVarStmt(stmt: Var): R

        fun visitWhileStmt(stmt: While): R
    }

    class Break : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBreakStmt(this)
    }

    data class Block(val statements: List<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBlockStmt(this)
    }

    class Continue : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitContinueStmt(this)
    }

    class Empty : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitEmptyStmt(this)
    }

    data class Expression(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpressionStmt(this)
    }

    data class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitIfStmt(this)
    }

    data class Print(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrintStmt(this)
    }

    data class Var(val name: Token, val initializer: Expr?) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVarStmt(this)
    }

    data class While(val condition: Expr, val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitWhileStmt(this)
    }

    abstract fun <R> accept(visitor: Visitor<R>): R
}
