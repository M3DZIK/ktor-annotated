package dev.medzik.ktor.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.medzik.ktor.annotations.Controller
import kotlin.reflect.KClass

class Processor(codeGenerator: CodeGenerator) : SymbolProcessor {
    private val controllerProcessor = ControllerProcessor(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val controllerClasses = resolver.getClassesWithAnnotation(Controller::class)
        if (!controllerClasses.iterator().hasNext()) return emptyList()

        for (controller in controllerClasses) {
            controllerProcessor.process(controller)
        }

        controllerProcessor.finish()

        return emptyList()
    }

    private fun Resolver.getSymbolsWithAnnotation(kClass: KClass<*>) = getSymbolsWithAnnotation(kClass.qualifiedName!!)
    private fun Resolver.getClassesWithAnnotation(kClass: KClass<*>) = getSymbolsWithAnnotation(kClass)
        .filterIsInstance<KSClassDeclaration>()
}
