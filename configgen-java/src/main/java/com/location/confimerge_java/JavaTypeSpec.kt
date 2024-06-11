package com.location.confimerge_java

import com.location.configgen.core.codeGen.TypeSpecBuilderWrapper
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class JavaTypeSpec(className: String, isInner:Boolean) : TypeSpecBuilderWrapper(className, isInner) {
    val classType: TypeSpec.Builder = TypeSpec.classBuilder(className).apply {
        addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        if(isInner){
            addModifiers(Modifier.STATIC)
        }
    }

    override fun addJavaDoc(doc: String) {
        classType.addJavadoc(doc)
    }

    override fun addType(typeSpec: Any) {
        classType.addType(typeSpec as TypeSpec)
    }

    override fun build(): Any = classType.build()
}