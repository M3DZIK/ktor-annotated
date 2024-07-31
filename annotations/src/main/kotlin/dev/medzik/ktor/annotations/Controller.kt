// Annotation values is used in the KSP processor.
@file:Suppress("UNUSED")

package dev.medzik.ktor.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Controller(val route: String = "")
