package com.location.confimerge_java

import com.location.configgen.core.codeGen.ClassSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class JavaClassSpec(className: String, isInner: Boolean) :
    ClassSpec<JavaClassSpec>(className, isInner) {
    val classType: TypeSpec.Builder = TypeSpec.classBuilder(className).apply {
        addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        if (isInner) {
            addModifiers(Modifier.STATIC)
        }
    }

    override fun addDoc(doc: String) {
        classType.addJavadoc(doc)
    }

    override fun addInnerClass(classSpec: JavaClassSpec) {
        classType.addType(classSpec.classType.build())
    }

    override fun build(): Any = classType.build()
}