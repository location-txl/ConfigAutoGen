package com.location.configgen.core.helper

import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.codeGen.ClassSpec
import com.location.configgen.core.codeGen.DataType
import com.location.configgen.core.datanode.Node

/**
 *
 * @author tianxiaolong
 * time：2024/6/28 14:53
 * description：
 */
class TestClassGenerateImpl(
    rootPackageName: String,
    outputDir: String,
    rootNode: Node.ObjectNode,
    rootClassName: String,
    unstableArray: Boolean = true
) : ClassGenerate<TestClassSpec>(
    rootPackageName,
    outputDir,
    rootNode,
    rootClassName,
    unstableArray
) {
    override val generateVersion: String
        get() = "1.0.0"

    override fun createClassSpec(className: String, isInner: Boolean): TestClassSpec =
        TestClassSpec(className, isInner)

    override fun createDataClassSpec(className: String, isInner: Boolean): TestClassSpec =
        createClassSpec(className, isInner)

    override fun addStaticUnknownFiled(typeSpecBuilder: TestClassSpec, key: String) {
    }

    override fun addStaticFiled(typeSpecBuilder: TestClassSpec, key: String, v: Node.ValueNode) {
    }

    override fun addBasicArray(classSpec: TestClassSpec, key: String, list: List<Node.ValueNode?>) {
    }

    override fun addLazyField(
        classSpec: TestClassSpec,
        listNode: Node.ListNode,
        objType: DataType.ObjectType
    ) {
    }

    override fun addProperty(classSpec: TestClassSpec, propertyMap: Map<String, DataType>) {
    }

    override fun writeFile(fileComment: String, classSpec: TestClassSpec) {
    }
}


class TestClassSpec(className: String, inner: Boolean) :
    ClassSpec<TestClassSpec>(className, inner) {
    override fun addDoc(doc: String) {
    }

    override fun build(): Any = Unit

    override fun addInnerClass(classSpec: TestClassSpec) {
    }

}