package org.example.runtime

import org.example.ast.Stmt
import org.example.interpreter.Interpreter
import org.example.interpreter.Return

class LoxFunction(val declaration: Stmt.Function, val closure: Environment) : LoxCallable {
    override fun arity(): Int {
        return declaration.params.size
    }

    override fun call(
        interpreter: Interpreter,
        arguments: List<Any?>,
    ): Any? {
        val environment = Environment(closure)
        for (i in arguments.indices) {
            environment.define(
                declaration.params[i].lexeme,
                arguments[i],
            )
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (e: Return) {
            return e.value
        }
        return null
    }

    override fun toString(): String {
        val functionName = declaration.name?.lexeme ?: "anonymous"
        return "<fn $functionName>"
    }
}
