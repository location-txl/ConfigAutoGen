package com.location.configmerge.codeGen

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:39
 * description：
 */
fun main() {
    val className = "Person"

    val nameProperty = PropertySpec.builder("name", String::class)
        .initializer("name")
        .build()

    val ageProperty = PropertySpec.builder("age", Int::class)
        .initializer("age")
        .build()

    val personClass = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)

        .addProperty(nameProperty)
        .addProperty(ageProperty)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("name", String::class)
                .addParameter( ParameterSpec.builder("age", Int::class)
                    .defaultValue("%L", 0)
                    .build())


                .build()
        )
        .build()

    val file = FileSpec.builder("com.example", className)
        .addType(personClass)
        .build()


    file.writeTo(System.out)
}


