package com.location.configgen.core

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
 */
open class BaseConfigWeaverExtension(project: Project) {
    val customObject: NamedDomainObjectContainer<DynamicObject> =
        project.container(DynamicObject::class.java, Factory(project))

    var debugLog = false

    fun customObject(action: Action<NamedDomainObjectContainer<DynamicObject>>) {
        action.execute(customObject)
    }
}