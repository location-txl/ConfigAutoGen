package com.location.configgen.core

import org.gradle.api.Project
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/6 21:06
 * description：
 */
val Project.configMergeJavaSourceDir
    get() = "${buildDir.absolutePath}${File.separator}generated${File.separator}source${File.separator}configMerge${File.separator}"