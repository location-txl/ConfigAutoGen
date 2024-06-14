package com.location.configgen.core.codeGen

import org.junit.Test

import com.google.common.truth.Truth.assertThat

/**
 *
 * @author tianxiaolong
 * time：2024/6/14 17:32
 * description：
 */
class CodeNameUtilKtTest {

    @Test
    fun getMethodName() {
        assertThat("test_method".methodName).isEqualTo("getTestMethod")
        assertThat("TestMethod".methodName).isEqualTo("getTestMethod")
        assertThat("testMethod".methodName).isEqualTo("getTestMethod")

    }

    @Test
    fun getClassName() {
        assertThat("test_class".className).isEqualTo("TestClass")
        assertThat("TestClass".className).isEqualTo("TestClass")
        assertThat("testClass".className).isEqualTo("TestClass")
    }

    @Test
    fun getFieldName() {
        assertThat("test_field".fieldName).isEqualTo("testField")
        assertThat("TestField".fieldName).isEqualTo("testField")
        assertThat("testField".fieldName).isEqualTo("testField")
    }
}