package dev.medzik.ktor.annotations.processor

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.medzik.ktor.annotations.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ControllerProcessor(private val codeGenerator: CodeGenerator) {
    private val controllers = mutableListOf<ClassName>()

    fun process(controller: KSClassDeclaration) {
        val className = controller.toClassName()
        controllers.add(className)

        val controllerRoute = controller.annotations
            .first { it.annotationType.toTypeName() == Controller::class.asTypeName() }
            .arguments
            .first()
            .value as String

        val fileBuilder = FileSpec.builder(className.packageName, "${className.simpleName}Generated")
            .addFileComment("Generated code for ${className.simpleName} controller")

        fileBuilder.addImport("io.ktor.http", "HttpMethod")
        fileBuilder.addImport("io.ktor.server.application", "call")
        fileBuilder.addImport("io.ktor.server.routing", "route")

        val funBuilder = FunSpec.builder("addRoutes")
            .addModifiers(KModifier.INTERNAL)
            .addParameter("routing", Routing::class)

        controller.getDeclaredFunctions().forEach {
            processFunction(funBuilder, it, controllerRoute)
        }

        val generatedClass = TypeSpec.objectBuilder("${className.simpleName}Generated")
            .addProperty(
                PropertySpec.builder("controller", controller.toClassName())
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(CodeBlock.of("%T()", controller.toClassName()))
                    .build()
            )
            .addFunction(funBuilder.build())
            .build()

        fileBuilder
            .addType(generatedClass)
            .build()
            .writeTo(codeGenerator, Dependencies(false))
    }

    fun finish() {
        FileSpec.builder("dev.medzik.ktor.annotated", "ConfigureRoutes")
            .addImport("io.ktor.server.routing", "routing")
            .addFunction(
                FunSpec.builder("configureRouting")
                    .receiver(Application::class)
                    .beginControlFlow("routing")
                    .addCode(
                        controllers.map {
                            "${it.packageName}.${it.simpleName}Generated.addRoutes(this)"
                        }.joinToString { "$it\n" }
                    )
                    .endControlFlow()
                    .build()
            )
            .build()
            .writeTo(codeGenerator, Dependencies(false))
    }

    private fun processFunction(funBuilder: FunSpec.Builder, function: KSFunctionDeclaration, controllerRoute: String) {
        function.annotations.forEach { annotation ->
            when (annotation.annotationType.toTypeName()) {
                GET::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Get, function, controllerRoute)
                POST::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Post, function, controllerRoute)
                PUT::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Put, function, controllerRoute)
                PATCH::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Patch, function, controllerRoute)
                DELETE::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Delete, function, controllerRoute)
                HEAD::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Head, function, controllerRoute)
                OPTIONS::class.asTypeName() -> generateFunction(funBuilder, annotation, HttpMethod.Options, function, controllerRoute)
                else -> throw IllegalStateException("unsupported annotation: ${annotation.shortName.getShortName()}")
                // ignore other annotations
//                else -> return@forEach
            }
        }
    }

    private fun generateFunction(funBuilder: FunSpec.Builder, annotation: KSAnnotation, method: HttpMethod, function: KSFunctionDeclaration, controllerRoute: String) {
        val routes = annotation.arguments.first().value as ArrayList<*>

        val codeBlockBuilder = CodeBlock.builder()

        routes.forEach {
            val finalRoute = "$controllerRoute$it"

            codeBlockBuilder
                .addStatement("// ${method.value} $finalRoute")
                .addStatement("// Generated from $function function")
                .beginControlFlow("routing.route(%S, HttpMethod(%S))", finalRoute, method.value)
                .beginControlFlow("handle")
                .functionCall(function)
                .endControlFlow()
                .endControlFlow()
        }

        funBuilder.addCode(codeBlockBuilder.build())
    }

    private fun CodeBlock.Builder.functionCall(function: KSFunctionDeclaration) = apply {
        val name = function.simpleName.getShortName()
        val params = function.parameters.map {
            when (val javaClass = it.type.resolve().toTypeName()) {
                ApplicationCall::class.asTypeName() -> "call"
                ApplicationRequest::class.asTypeName() -> "call.request"
                ApplicationResponse::class.asTypeName() -> "call.response"
                else -> throw IllegalStateException("invalid function parameter type: $javaClass")
            }
        }

        addStatement("controller.$name(${params.joinToString { "$it, " }})")
    }
}
