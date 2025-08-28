package org.example.util

fun Char.isAlpha(): Boolean {
    return this.isLetter() || this == '_'
}

fun Char.isAlphaNumeric(): Boolean {
    return this.isAlpha() || this.isDigit()
}
