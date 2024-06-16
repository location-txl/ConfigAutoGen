package com.location.confimerge_kotlin

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 17:05
 * description：
 */

fun propertySpec(
    name: String,
    type: TypeName,
    body: PropertySpec.Builder.() -> Unit = {}
): PropertySpec = PropertySpec.builder(name = name, type = type).apply(body).build()


inline fun FunSpec.Builder.controlFlow(
    controlFlow: String,
    vararg args: Any,
    block: FunSpec.Builder.() -> Unit
) {
    beginControlFlow(controlFlow, args)
    try {
        block()
    } finally {
        endControlFlow()
    }

}