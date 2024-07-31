package dev.medzik.ktor.annotations.processor

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.medzik.ktor.annotations.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class Processor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val controllers = resolver
            .getSymbolsWithAnnotation(Controller::class)
            .filterIsInstance<KSClassDeclaration>()

        if (!controllers.iterator().hasNext()) return emptyList()

        val allControllers = mutableListOf<String>()

        for (controller in controllers) {
            val className = controller.toClassName().simpleName
            val packageName = controller.toClassName().packageName

            val fileBuilder = FileSpec.builder(packageName, "${className}Generated")
                .addFileComment("Generated code for ${controller.toClassName().simpleName} controller")

            fileBuilder.addImport("io.ktor.http", "HttpMethod")
            fileBuilder.addImport("io.ktor.server.application", "call")
            fileBuilder.addImport("io.ktor.server.routing", "route")

            val controllerAnnotationValue = controller.annotations.first { it.shortName.getShortName() == "Controller" }.arguments
            val controllerRouteValue = controllerAnnotationValue[0].value as String

            val handlers = mutableListOf<String>()

            for (declaredFunction in controller.getDeclaredFunctions()) {
                val functionName = declaredFunction.simpleName.getShortName()

                val parameters = declaredFunction.parameters.map {
                    when(val javaClass = it.type.resolve().toTypeName()) {
                        ApplicationCall::class.asTypeName() -> "call" to ApplicationCall::class.asTypeName()
                        else -> throw IllegalStateException("invalid function parameter type: $javaClass")
                    }
                }

                for (annotation in declaredFunction.annotations) {
                    val annotationName = annotation.shortName.getShortName()
                    val annotationValue = annotation.arguments.first().value as ArrayList<*>

                    fun genFunSpec(method: String): List<String> {
                        val specs = mutableListOf<String>()

                        annotationValue.forEach { functionRoute ->
                            specs.add("""
                                // $method handler for $functionRoute route. Generated from $functionName function.
                                route("$controllerRouteValue$functionRoute", HttpMethod.$method) {
                                  handle {
                                    controller.${functionName}(${parameters.joinToString { "${it.first}, " }})
                                  }
                                }
                            """.trimIndent())
                        }

                        return specs
                    }

                    when (annotationName) {
                        GET::class.simpleName -> handlers.addAll(
                            genFunSpec("Get")
                        )
                        POST::class.simpleName -> handlers.addAll(
                            genFunSpec("Post")
                        )
                        PUT::class.simpleName -> handlers.addAll(
                            genFunSpec("Put")
                        )
                        PATCH::class.simpleName -> handlers.addAll(
                            genFunSpec("Patch")
                        )
                        DELETE::class.simpleName -> handlers.addAll(
                            genFunSpec("Delete")
                        )
                        HEAD::class.simpleName -> handlers.addAll(
                            genFunSpec("Head")
                        )
                        OPTIONS::class.simpleName -> handlers.addAll(
                            genFunSpec("Options")
                        )
                    }
                }
            }

            val funCode = StringBuilder()
            handlers.forEach { funCode.appendLine(it) }

            val generatedClass = TypeSpec.objectBuilder("${className}Generated")
                .addModifiers(KModifier.INTERNAL)
                .addProperty(
                    PropertySpec.builder("controller", controller.toClassName())
                        .addModifiers(KModifier.PRIVATE)
                        .initializer(CodeBlock.of("%T()", controller.toClassName()))
                        .build()
                )
                .addFunction(
                    FunSpec.builder("addRoutes")
                        .addParameter("routing", Routing::class)
                        .addCode("routing.addGeneratedRoutes()")
                        .build()
                )
                .addFunction(
                    FunSpec.builder("addGeneratedRoutes")
                        .addModifiers(KModifier.PRIVATE)
                        .receiver(Routing::class)
                        .addCode(funCode.toString())
                        .build()
                )
                .build()

            fileBuilder.addType(generatedClass)

            val fileSpec = fileBuilder.build()
            fileSpec.writeTo(codeGenerator, Dependencies(false))

            allControllers.add("${fileSpec.packageName}.${fileSpec.name}")
        }

        FileSpec.builder("dev.medzik.ktor.annotated", "InitRoutes")
            .addFunction(
                FunSpec.builder("initRoutes")
                    .receiver(Routing::class)
                    .addCode(
                        """
                        ${allControllers.map { 
                            "$it.addRoutes(this)"
                        }.joinToString { "$it\n" }}
                        """.trimIndent()
                    )
                    .build()
            )
            .addImport("io.ktor.server.routing", "routing")
            .addFunction(
                FunSpec.builder("configureRouting")
                    .receiver(Application::class)
                    .addCode(
                        """
                        routing {
                          initRoutes()
                        }
                        """.trimIndent()
                    )
                    .build()
            )
            .build()
            .writeTo(codeGenerator, Dependencies(false))

        return (controllers).filterNot { it.validate() }.toList()
    }

    private fun Resolver.getSymbolsWithAnnotation(kClass: KClass<*>) = getSymbolsWithAnnotation(kClass.qualifiedName!!)
}
