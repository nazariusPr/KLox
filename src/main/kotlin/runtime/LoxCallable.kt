package org.example.runtime

import org.example.interpreter.Interpreter

interface LoxCallable {
    fun arity(): Int

    fun call(
        interpreter: Interpreter,
        arguments: List<Any?>,
    ): Any?
}
