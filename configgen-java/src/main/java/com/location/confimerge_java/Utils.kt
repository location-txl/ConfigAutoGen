package com.location.confimerge_java

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName

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