package com.pineypiney.game_engine.util.exceptions

class InitialisationException : Exception {

	/**
	 * Constructs an `InitialisationException` with the specified detail message.
	 *
	 * @param message
	 * The detail message
	 */
	constructor(message: String? = null) : super(message)

	/**
	 * Constructs an `InitialisationException` with the specified detail message
	 * and cause.
	 *
	 *
	 *  Note that the detail message associated with `cause` is
	 * *not* automatically incorporated into this exception's detail
	 * message.
	 *
	 * @param message
	 * The detail message
	 *
	 * @param cause
	 * The cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown)
	 */
	constructor(message: String?, cause: Throwable?) : super(message, cause)

	/**
	 * Constructs an `InitialisationException` with the specified cause and a
	 * detail message of `(cause?.toString() ?: null)`
	 * (which typically contains the class and detail message of `cause`).
	 * This constructor is useful for IO exceptions that are little more
	 * than wrappers for other throwables.
	 *
	 * @param cause
	 * The cause.  (A null value is permitted, and indicates that the cause is nonexistent or unknown)
	 */
	constructor(cause: Throwable?) : super(cause)

}