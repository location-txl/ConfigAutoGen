package com.location.configgen.core.codeGen

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
    val nameProperty = PropertySpec.builder("name", String::class).initializer("name").build()
    val ageProperty = PropertySpec.builder("age", Int::class).initializer("age").build()
    val personClass =
        TypeSpec.classBuilder(className).addModifiers(KModifier.DATA).addProperty(nameProperty)
            .addProperty(ageProperty).primaryConstructor(
                FunSpec.constructorBuilder().addParameter("name", String::class).addParameter(
                    ParameterSpec.builder("age", Int::class).defaultValue("%L", 0).build()
                )


                    .build()
            ).build()

    val file = FileSpec.builder("com.example", className).addType(personClass).build()


    file.writeTo(System.out)


    val map = mutableMapOf<Int, Int>()
    map[1] = 1
    map[2] = 2
    map[2] = 2

    val map2 = mutableMapOf<Int, Int>()

    map2[2] = 2
    map2[1] = 1
    map2[6] = 1

    println(map == map2)
    println(map.hashCode())
    println(map2.hashCode())

}


