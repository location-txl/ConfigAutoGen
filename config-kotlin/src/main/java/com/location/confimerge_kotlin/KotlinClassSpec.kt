package com.location.confimerge_kotlin

import com.location.configgen.core.codeGen.ClassSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class KotlinClassSpec(val type: Type, className: String, isInner: Boolean) :
    ClassSpec<KotlinClassSpec>(className, isInner) {
        sealed interface Type{
            object Data : Type
            object Object : Type
        }
    val classType: TypeSpec.Builder = when(type){
        is Type.Data -> TypeSpec.classBuilder(className).apply {
            addModifiers(KModifier.PUBLIC, KModifier.FINAL, KModifier.DATA)
        }
        is Type.Object -> TypeSpec.objectBuilder(className).apply {
            addModifiers(KModifier.PUBLIC)
        }
    }

    override fun addDoc(doc: String) {
        classType.addKdoc(doc)

    }

    override fun addInnerClass(classSpec: KotlinClassSpec) {
        classType.addType(classSpec.classType.build())
    }

    override fun build(): Any = classType.build()
}