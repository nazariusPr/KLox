package org.example.runtime

import org.example.interpreter.RuntimeError
import org.example.scanner.Token

class Environment(val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw RuntimeError(
            name,
            "Undefined variable '${name.lexeme}'.",
        )
    }

    fun define(
        lexeme: String,
        value: Any?,
    ) {
        values[lexeme] = value
    }

    fun assign(
        name: Token,
        value: Any?,
    ) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(
            name,
            "Undefined variable '" + name.lexeme + "'.",
        )
    }
}
