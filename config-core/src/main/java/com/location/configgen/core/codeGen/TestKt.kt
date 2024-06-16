package com.location.configgen.core.codeGen

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:39
 * description：
 */
fun main() {
    val forName = Class.forName("com.location.configgen.core.codeGen.UserA")
    forName.methods

//    forName.getMethod("a").invoke(null).let {
//         println(it)
//     }


}

object UserA {
    //    @JvmStatic
    fun a() = "123"
}


