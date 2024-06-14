package com.location.configgen.core.codeGen

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 10:43
 * description：
 */
abstract class ClassSpec<T : ClassSpec<T>>(
    val className: String, val inner: Boolean
) {
    abstract fun addDoc(doc: String)
    abstract fun addInnerClass(classSpec: T)

    abstract fun build(): Any
}