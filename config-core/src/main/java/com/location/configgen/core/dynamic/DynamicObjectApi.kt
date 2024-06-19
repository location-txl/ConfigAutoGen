package com.location.configgen.core.dynamic

import com.location.configgen.core.DynamicObject
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 16:43
 * description：
 */
interface DynamicObjectApi {
    fun addProperty(name: String, value: Any)


    fun subProject(action: Action<NamedDomainObjectContainer<DynamicObject>>)
}