package com.bobekos.bobek.scanner.overlay


internal sealed class Optional<out T> {
    class Some<out T>(val element: T): Optional<T>()
    object None: Optional<Nothing>()
}