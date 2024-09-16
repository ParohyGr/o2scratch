package com.parohy.outwo.core

sealed interface State<out E, out V>
data object Loading                      : State<Nothing, Nothing>
data class  Content<out V>(val value: V) : State<Nothing, V>
data class  Failure<out E>(val value: E) : State<E, Nothing>

inline fun <E, V, V2> State<E, V>.map(f: (V) -> V2): State<E, V2> = when(this) {
  is Content -> Content(f(value))
  is Loading -> this
  is Failure -> this
}

val SUCCESS = Content(Unit)

val <E, V> State<E, V>?.isLoading: Boolean get() = this is Loading

val <E, V> State<E, V>?.valueOrNull: V? get() = (this as? Content)?.value

inline fun <E, V, A> State<E, V>?.ifContent(f: (V) -> A): A? = valueOrNull?.let(f)

fun <T> Result<T>.toState(): State<Throwable, T> = fold(::Content, ::Failure)
