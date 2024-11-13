package com.location.confimerge_java

import com.google.gson.Gson
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 17:05
 * description：
 */
fun fieldSpec(
    name: String,
    type: TypeName,
    body: FieldSpec.Builder.() -> Unit = {}
): FieldSpec = FieldSpec.builder(type, name).apply(body).build()


inline fun MethodSpec.Builder.controlFlow(
    controlFlow: String,
    vararg args: Any,
    block: MethodSpec.Builder.() -> Unit
) {
    beginControlFlow(controlFlow, args)
    try {
        block()
    } finally {
        endControlFlow()
    }

}

fun classSpec(
    name: ClassName,
    body: TypeSpec.Builder.() -> Unit = {}
): TypeSpec = TypeSpec.classBuilder(name).apply(body).build()

fun constructorSpec(
    body: MethodSpec.Builder.() -> Unit = {}
): MethodSpec = MethodSpec.constructorBuilder().apply(body).build()

fun methodSpec(
    name: String,
    body: MethodSpec.Builder.() -> Unit = {}
): MethodSpec = MethodSpec.methodBuilder(name).apply(body).build()

fun parameterSpec(
    type: TypeName,
    name: String,
    body: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec = ParameterSpec.builder(type, name).apply(body).build()


fun main() {
    println("1233")
    val gson = Gson()
    val json = """
        {
        "name":"123",
        "info":{
           "id":1,
           "u_id":"1321"
        },
        "age":null
        }
    """.trimIndent()

    val fromJson = gson.fromJson(json, Map::class.java)
    println("fromJson:$fromJson")
}