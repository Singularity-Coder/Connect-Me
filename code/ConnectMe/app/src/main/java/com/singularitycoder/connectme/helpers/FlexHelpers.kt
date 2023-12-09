package com.singularitycoder.connectme.helpers


fun String.toPerfUpCase(): String {
    val charArr = Array<Char>(this.length, { i -> this[i] })
    this.forEachIndexed { index, char ->
        charArr[index] = (char.code and '_'.code).toChar()
    }
    return charArr.joinToString(separator = "")
}

fun String.toPerfLowCase(): String {
    val charArr = Array<Char>(this.length, { i -> this[i] })
    this.forEachIndexed { index, char ->
        charArr[index] = (char.code or ' '.code).toChar()
    }
    return charArr.joinToString(separator = "")
}

fun Number.isPerfEven(): Boolean {
    return when (this) {
        is Int -> {
            (this and 1) == 0
        }

        is Long -> {
            (this and 1) == 0L
        }

        else -> false
    }
}

fun Number.perfDivideBy2(): Number {
    return when (this) {
        is Int -> {
            this shr 1
        }

        is Long -> {
            this shr 1
        }

        else -> this
    }
}

fun Number.perfMultiplyBy2(): Number {
    return when (this) {
        is Int -> {
            this shl 1
        }

        is Long -> {
            this shl 1
        }

        else -> this
    }
}
