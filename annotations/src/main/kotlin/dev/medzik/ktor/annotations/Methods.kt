// Annotation values is used in the KSP processor.
@file:Suppress("UNUSED")

package dev.medzik.ktor.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class GET(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class POST(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class PUT(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class PATCH(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class DELETE(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class HEAD(vararg val routes: String = ["/"])

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class OPTIONS(vararg val routes: String = ["/"])
