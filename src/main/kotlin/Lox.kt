package org.example

import org.example.interpreter.Interpreter
import org.example.interpreter.RuntimeError
import org.example.parser.Parser
import org.example.scanner.Scanner
import org.example.scanner.Token
import org.example.scanner.TokenType
import java.io.File
import kotlin.system.exitProcess

class Lox {
    companion object {
        val interpreter = Interpreter()
        var hadError = false
        var hadRuntimeError = false

        fun error(
            line: Int,
            message: String,
        ) {
            report(line, "", message)
        }

        fun error(
            token: Token,
            message: String,
        ) {
            if (token.type == TokenType.EOF) {
                report(token.line, "at end", message)
            } else {
                report(token.line, "at '${token.lexeme}'", message)
            }
        }

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            hadRuntimeError = true
        }

        fun report(
            line: Int,
            where: String,
            message: String,
        ) {
            System.err.println(
                "[line $line] Error $where: $message",
            )
            hadError = true
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val lox = Lox()

            when (args.size) {
                0 -> lox.runPrompt()
                1 -> lox.runFile(args[0])
                else -> {
                    println("Usage: klox [script]")
                    exitProcess(64)
                }
            }
        }
    }

    private fun runFile(fileName: String) {
        val bytes: ByteArray = File(fileName).readBytes()
        run(bytes.toString(Charsets.UTF_8))

        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    private fun runPrompt() {
        while (true) {
            print("> ")
            val line = readLine()
            if (line == null) break

            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens)
        val expressions = parser.parse()

        if (hadError || expressions == null) return
        interpreter.interpret(expressions)
    }
}
