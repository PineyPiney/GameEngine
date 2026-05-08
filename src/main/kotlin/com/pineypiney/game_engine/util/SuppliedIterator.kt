package com.pineypiney.game_engine.util

class SuppliedIterator<E, R>(src: Iterable<E>, val func: (E) -> R) : Iterator<R> {
	val iter = src.iterator()
	override fun next(): R = func(iter.next())
	override fun hasNext(): Boolean = iter.hasNext()
}