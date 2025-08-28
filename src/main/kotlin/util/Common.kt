package org.example.util

fun stringify(obj: Any?): String {
    if (obj == null) return "nil"

    if (obj is Double) {
        var text = obj.toString()
        if (text.endsWith(".0")) {
            text = text.dropLast(2)
        }
        return text
    }

    return obj.toString()
}
