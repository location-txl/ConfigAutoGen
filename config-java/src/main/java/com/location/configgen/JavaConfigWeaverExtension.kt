package com.location.configgen

import com.location.configgen.core.BaseConfigWeaverExtension
import org.gradle.api.Project

open class JavaConfigWeaverExtension(project: Project) : BaseConfigWeaverExtension(project) {
    val javaOptions: JavaOptions = defJavaOptions

    fun java(action: JavaOptions.() -> Unit) {
        javaOptions.action()
    }
}

/**
 * @param nullSafe 是否生成nullSafe的代码
 * 为true后 会对所有可空不可空的字段添加nullSafe注解
 */
data class NullSafeAnnotation(
    val packageName:String,
    val nullable:String,
    val notNull:String
){
    companion object{
        @JvmStatic
        val ANDROIDX = NullSafeAnnotation("androidx.annotation","Nullable","NonNull")
    }
}


data class JavaOptions(
    var nullSafe:Boolean,
    var nullSafeAnnotation: NullSafeAnnotation = NullSafeAnnotation.ANDROIDX,
    )


/**
 * 默认的java配置
 * @see [com.location.confimerge_java.JavaClassGenerate]
 */
private val defJavaOptions = JavaOptions(false)