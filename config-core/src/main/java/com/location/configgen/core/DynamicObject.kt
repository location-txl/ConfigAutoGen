package com.location.configgen.core

import com.location.configgen.core.dynamic.DynamicObjectApi
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 16:24
 * description：
 * 在 gradle 中动态配置结构体
 */
class DynamicObject(val name: String, val project: Project) : DynamicObjectApi {

    private val subObjects = project.container(DynamicObject::class.java, Factory(project))

    private val propertyMap = mutableMapOf<String, Any>()

    /**
     * 添加属性
     * @param name String 属性名
     * @param value Any 属性值
     */
    override fun addProperty(name: String, value: Any) {
        println("addProperty name = $name, value = $value")
        if (propertyMap.containsKey(name)) {
            error("property $name already exists")
        }
        propertyMap[name] = value
    }


    override fun subProject(action: Action<NamedDomainObjectContainer<DynamicObject>>) {
        action.execute(subObjects)
    }

    fun withFlavor(name: String, action: DynamicObjectApi.() -> Unit) {
        val flavorDynamic = object : DynamicObjectApi {
            override fun addProperty(name: String, value: Any) {

            }

            override fun subProject(action: Action<NamedDomainObjectContainer<DynamicObject>>) {
            }
        }
        flavorDynamic.action()
    }

    class Factory(private val project: Project) : NamedDomainObjectFactory<DynamicObject> {
        override fun create(name: String): DynamicObject = DynamicObject(name, project)
    }

}