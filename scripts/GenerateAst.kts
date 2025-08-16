import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess

if (args.size != 1) {
    println("Usage: generate_ast <output directory>")
    exitProcess(64)
}

val outputDir = args[0]

// Expr
defineAst(
    outputDir,
    "Expr",
    listOf(
        "Assign & val name: Token, val value: Expr",
        "Binary & val left: Expr, val operator: Token, val right: Expr",
        "Grouping & val expression: Expr",
        "Literal & val value: Any?",
        "Logical & val left: Expr, val operator: Token, val right: Expr",
        "Unary & val operator: Token, val right: Expr",
        "Variable & val name: Token",
    ),
)

// Stmt
defineAst(
    outputDir,
    "Stmt",
    listOf(
        "Break &",
        "Block & val statements: List<Stmt>",
        "Continue &",
        "Empty &",
        "Expression & val expression: Expr",
        "If & val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?",
        "Print & val expression: Expr",
        "Var & val name: Token, val initializer: Expr?",
        "While & val condition: Expr, val body: Stmt",
    ),
)

fun defineAst(
    outputDir: String,
    baseName: String,
    types: List<String>,
) {
    val path = "$outputDir/$baseName.kt"
    PrintWriter(File(path), "UTF-8").use { writer ->
        writer.println("package org.example.ast")
        writer.println()
        writer.println("import org.example.scanner.Token")
        writer.println()
        writer.println("sealed class $baseName {")

        defineVisitor(writer, baseName, types)

        for ((index, type) in types.withIndex()) {
            val className = type.split("&")[0].trim()
            val fieldList = type.split("&")[1].trim()

            defineType(writer, baseName, className, fieldList)

            if (index != types.lastIndex) {
                writer.println()
            }
        }

        writer.println()
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")

        writer.println("}")
    }
}

fun defineVisitor(
    writer: PrintWriter,
    baseName: String,
    types: List<String>,
) {
    writer.println("    interface Visitor<R> {")

    for ((index, type) in types.withIndex()) {
        val typeName = type.substringBefore("&").trim()
        writer.println("        fun visit${typeName}$baseName(${baseName.lowercase()}: $typeName): R")

        if (index != types.lastIndex) {
            writer.println()
        }
    }

    writer.println("    }")
    writer.println()
}

fun defineType(
    writer: PrintWriter,
    baseName: String,
    className: String,
    fieldList: String,
) {
    val classKeyword = if (fieldList.isNotEmpty()) "data " else ""
    val constructorSignature = if (fieldList.isNotEmpty()) "($fieldList)" else ""

    writer.println("    ${classKeyword}class $className$constructorSignature : $baseName() {")
    writer.println("        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit$className$baseName(this)")
    writer.println("    }")
}
