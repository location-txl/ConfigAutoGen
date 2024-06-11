package com.location.configgen.core.codeGen

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 10:43
 * description：
 */
abstract class TypeSpecBuilderWrapper {
    abstract fun addJavaDoc(doc: String)
    abstract fun addType(typeSpec: Any)

    abstract fun build():Any
}