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

    /**
     * 添加注释
     * @param doc String
     */
    abstract fun addDoc(doc: String)

    /**
     * 添加内部类
     * @param classSpec T
     */
    abstract fun addInnerClass(classSpec: T)


    /**
     * 将当前类构建出来 不再更改
     * @return Any
     */
    abstract fun build(): Any
}