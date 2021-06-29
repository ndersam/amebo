package com.amebo.core.common


sealed class Either<out L, out R> {
    data class Left<out L>(val data: L) : Either<L, Nothing>()
    data class Right<out R>(val data: R) : Either<Nothing, R>()
}
