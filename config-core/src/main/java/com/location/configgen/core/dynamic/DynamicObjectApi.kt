package com.location.configgen.core.dynamic

import groovy.lang.GroovyInterceptable
import kotlin.experimental.ExperimentalTypeInference

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 16:43
 * description：
 * gradle 中动态创建 对象 接口声明
 */
@CustomObjectScopeMarker
interface DynamicObjectApi {

    /**
     * 给当前对象添加属性
     * @param name String 属性名
     * @param value String  属性值
     * 这里的方法重载主要将错误从运行时提前到编译时期 这里只允许添加基本类型
     */
    fun addProperty(name: String, value: String)
    fun addProperty(name: String, value: Int)
    fun addProperty(name: String, value: Long)
    fun addProperty(name: String, value: Float)
    fun addProperty(name: String, value: Double)
    fun addProperty(name: String, value: Boolean)



    /**
     * 添加一个列表属性
     * @param name String 属性名
     * @param action [@kotlin.ExtensionFunctionType] Function1<CustomPropertyScope, Unit>
     */
    @OptIn(ExperimentalTypeInference::class)
    fun <T> addListProperty(
        name: String,
        @BuilderInference action: CustomPropertyScope<T>.() -> Unit
    )


    /**
     * 添加一个列表对象属性
     * @param name String
     * @param action [@kotlin.ExtensionFunctionType] Function1<CustomListObjectScope, Unit>
     */
    fun addListObject(name: String, action: CustomListObjectScope.() -> Unit)


    /**
     * 添加一个子结构体
     * @param name String
     * @param action [@kotlin.ExtensionFunctionType] Function1<DynamicObjectApi, Unit>
     */
    fun addObject(name:String, action:DynamicObjectApi.() -> Unit)


}



