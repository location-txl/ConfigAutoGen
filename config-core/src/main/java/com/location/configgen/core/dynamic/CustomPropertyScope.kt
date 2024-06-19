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

@CustomObjectScopeMarker
interface CustomListObjectScope {
    fun add(action: DynamicObjectApi.() -> Unit)
}