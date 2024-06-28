package com.location.configgen.core

import com.location.configgen.core.config.checkPropertyValid
import com.location.configgen.core.dynamic.DynamicObject
import com.location.configgen.core.dynamic.DynamicObject.Factory
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 14:54
 * description：
 * 基础的插件配置
 */
open class BaseConfigWeaverExtension(project: Project) {
    val customObject: NamedDomainObjectContainer<DynamicObject> =
        project.container(DynamicObject::class.java, Factory(project)).apply {
            whenObjectAdded {
                //监测 create 的对象名字是否符合标准
                checkPropertyValid(it.name)
            }
        }


    /**
     * 是否开启debug日志
     */
    var debugLog = false

    /**
     * 添加自定义对象
     * @param action Action<NamedDomainObjectContainer<DynamicObject>>
     */
    fun customObject(action: Action<NamedDomainObjectContainer<DynamicObject>>) {
        action.execute(customObject)
    }
}