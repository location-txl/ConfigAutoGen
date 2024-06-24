package com.location.configgen.core.dynamic

/**
 *
 * @author tianxiaolong
 * time：2024/6/20 21:10
 * description：
 */
@CustomObjectScopeMarker
interface CustomPropertyScope<T> {
    fun add(value: T)
}

/**
 * 自定义列表对象的作用域
 * 调用 add 方法添加一个对象
 */
@CustomObjectScopeMarker
interface CustomListObjectScope {
    fun add(action: DynamicObjectApi.() -> Unit)
}