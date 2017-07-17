package io.github.chrislo27.rhre3.registry.json

/**
 * Verifies an object to ensure fields haven't been missed.
 */
interface Verifiable {

    fun verify(): String?

}