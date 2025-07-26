package org.example

import org.example.scanner.Scanner
import org.example.scanner.Token
import java.io.File
import kotlin.system.exitProcess

class Lox {
    companion object {
        var hadError = false

        fun error(
            line: Int,
            message: String,
        ) {
            report(line, "", message)
        }

        fun report(
            line: Int,
            where: String,
            message: String,
        ) {
            System.err.println(
                "[line $line] Error$where: $message",
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

        for (token in tokens) {
            println(token)
        }
    }
}
