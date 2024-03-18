package io.github.lobodpav.spock.validator

fun interface Validator<T> {

    /** Gets an error text if the argument is not valid. Returns `null` if there was no validation error. */
    fun validate(argument: T): String?
}
