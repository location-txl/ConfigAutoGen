package com.location.configgen.core

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 14:54
 * description：
 */
open class BaseConfigWeaverExtension {
    var title: String? = null

    fun customObject(project: Project) {

    }
}