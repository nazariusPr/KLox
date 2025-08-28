package org.example.util

import org.example.interpreter.Interpreter
import org.example.runtime.LoxCallable

val clock =
    object : LoxCallable {
        override fun arity() = 0

        override fun call(
            interpreter: Interpreter,
            arguments: List<Any?>,
        ) = System.currentTimeMillis() / 1000.0

        override fun toString() = "<native fn>"
    }

val print =
    object : LoxCallable {
        override fun arity() = 1

        override fun call(
            interpreter: Interpreter,
            arguments: List<Any?>,
        ): Any? {
            val res = stringify(arguments[0])
            print(res)
            return null
        }

        override fun toString() = "<native fn>"
    }
