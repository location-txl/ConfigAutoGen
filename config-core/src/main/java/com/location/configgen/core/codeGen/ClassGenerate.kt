package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import org.jetbrains.annotations.VisibleForTesting
import java.util.LinkedHashMap
import java.util.Locale

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:19
 * description：
 */
abstract class ClassGenerate<T : ClassSpec<T>>(
    protected val rootPackageName: String,
    protected val outputDir: String,
    protected val rootNode: Node.ObjectNode,
    protected val rootClassName: String,
    /**
     * 是否生成不稳定的数组 如果为 true 则会扫描整个数组的所有元素 生产一个最全的Object
     */
    private val unstableArray: Boolean = true
) {

    /**
     * 生成代码版本
     */
    abstract val generateVersion: String

    fun create() {
        val classSpec = createClassSpec(rootClassName, isInner = false).apply {
            addDoc("sourceData:${rootNode.docs}")
        }
        parseObjNode(
            classSpec = classSpec,
            objectNode = rootNode
        )
        writeFile(
            """
            ConfigWeaver plugin automatically generated, please do not modify
            generateTime:$currentTime
            Version:$generateVersion
            SourceCode:https://github.com/location-txl/ConfigWeaver
        """.trimIndent(), classSpec
        )
    }

    /**
     * 将 class 写入文件
     * @param fileComment String 文件注释
     * @param classSpec T 文件信息
     */
    abstract fun writeFile(
        fileComment: String, classSpec: T
    )


    abstract fun createClassSpec(className: String, isInner: Boolean): T


    abstract fun createDataClassSpec(className: String, isInner: Boolean): T


    /**
     * 解析 json 对象并将里面的每个字段注入到 classSpec 中
     * @param classSpec T 类的信息
     * @param objectNode JSONObject json 对象
     */
    private fun parseObjNode(classSpec: T, objectNode: Node.ObjectNode) {

        objectNode.forEach { (k, v) ->
            when (v) {
                is Node.ObjectNode -> {
                    val innerClass = createClassSpec(k.className, isInner = true).apply {
                        addDoc("key:$k - value:${v.docs}")
                    }
                    parseObjNode(innerClass, v)
                    classSpec.addInnerClass(innerClass)
                }

                is Node.ListNode -> {
                    if (v.list.isNotEmpty()) {
                        parseListNode(
                            classSpec, k.toString(), v, "$rootPackageName.$rootClassName"
                        )
                    } else {
                        addBasicArray(classSpec, k.toString(), listOf())
                    }
                }

                is Node.ValueNode -> {
                    addStaticFiled(classSpec, k.toString(), v)
                }
                else -> {
                    addStaticUnknownFiled(classSpec, k.toString())
                }

            }
        }
    }


    protected abstract fun addProperty(classSpec: T, propertyMap: Map<String, DataType>)


    @VisibleForTesting
    fun parseListNodeType(
        listNode: Node.ListNode, initValue: Map<String, ListNodeType>? = null
    ): Map<String, ListNodeType> {
        val filedMap = LinkedHashMap<String, ListNodeType>().apply {
            if (initValue != null) {
                putAll(initValue)
            }
        }
        var index = 0
        var firstScan = true
        do {
            val typeCanNull = initValue != null || firstScan.not()

            val obj = listNode[index++] as? Node.ObjectNode ?: continue
            //可以为空的字段
            with(filedMap.keys.toMutableList()) {
                removeAll(obj.map { it.key })
                forEach { canNullKey ->
                    filedMap[canNullKey] = filedMap[canNullKey]!!.copy(canNull = true)
                }
            }

            obj.forEach { (k, v) ->
                val oldType = filedMap[k]
                when (v) {
                    is Node.ListNode -> {
                        if (oldType == null && v.isEmpty()) {
                            filedMap[k.toString()] =
                                ListNodeType(Unit, canNull = typeCanNull, isList = true)
                        } else if (v.isNotEmpty()) {
                            when (val childV = v[0].also {
                                require(it != null) {
                                    "not support array first child is null"
                                }
                            }) {
                                is Node.ObjectNode -> {
                                    @Suppress("UNCHECKED_CAST")
                                    filedMap[k.toString()] =
                                        oldType?.copy(
                                            type = parseListNodeType(
                                                v, oldType.type as? Map<String, ListNodeType>
                                            ),
                                            isList = true,
                                        ) ?: ListNodeType(
                                            parseListNodeType(v, null), canNull = typeCanNull,
                                            isList = true,
                                        )
                                }

                                is Node.ListNode -> {
                                    throw IllegalArgumentException("not support array in array")
                                }

                                is Node.ValueNode -> {
                                    filedMap[k.toString()] =
                                        oldType?.copy(type = childV.valueType, isList = true)
                                            ?: ListNodeType(
                                                childV.valueType,
                                                canNull = typeCanNull,
                                                isList = true
                                            )
                                }

                                else -> {
                                    error("unknown error")
                                }
                            }

                        }
                    }

                    is Node.ObjectNode -> {
                        @Suppress("UNCHECKED_CAST")
                        filedMap[k.toString()] = ListNodeType(
                            parseListNodeType(
                                Node.ListNode(listOf(v), v.docs),
                                initValue = oldType?.type as? Map<String, ListNodeType>
                            ),
                            canNull = typeCanNull
                        )
                    }

                    else -> {
                        if (oldType == null) {
                            filedMap[k.toString()] =
                                ListNodeType(
                                    (v as? Node.ValueNode)?.valueType ?: Unit,
                                    typeCanNull || v == null
                                )
                        } else {
                            if (v == null) {
                                filedMap[k.toString()] = oldType.copy(canNull = true)
                            } else if (v is Node.ValueNode) {
                                val valueType = v.valueType
                                if (oldType.type.javaClass == valueType.javaClass) {
                                    val oldValueType = oldType.type as ValueType
                                    if (oldValueType.groupId == valueType.groupId && oldValueType.ordinal < valueType.ordinal) {
                                        //可以向上提升
                                        filedMap[k.toString()] = oldType.copy(type = valueType)
                                    } else if (oldValueType.groupId != valueType.groupId) {
                                        throw IllegalArgumentException("not support type change")
                                    }
                                } else if (oldType.type is Unit) {
                                    //直接替换类型
                                    filedMap[k.toString()] = oldType.copy(type = valueType)
                                } else {
                                    throw IllegalArgumentException("not support type change")
                                }
                            } else {
                                error("unknown error")
                            }
                        }
                    }
                }
            }
            firstScan = false
        } while (unstableArray && index < listNode.size)
        return filedMap
    }

    @VisibleForTesting
    fun createPropertyClass(
        parentClass: T,
        classPrefix: String,
        innerPkgName: String,
        typeMap: Map<String, ListNodeType>
    ): Map<String, DataType> {
        val propertyMap = LinkedHashMap<String, DataType>()
        typeMap.forEach { (k, v) ->
            propertyMap[k.fieldName] = when (v.type) {
                is ValueType -> {
                    DataType.BasisType(v.type, k, canNull = v.canNull, isList = v.isList)
                }

                is Map<*, *> -> {
                    val innerClass = createDataClassSpec(k.className, true)
                    innerClass.addDoc("key:$k - value:$v")
                    val className = k.className
                    val nextPkgName = "$innerPkgName.${className}"
                    @Suppress("UNCHECKED_CAST") val innerPropertyMap = createPropertyClass(
                        innerClass,
                        "$classPrefix.${className}",
                        nextPkgName,
                        v.type as Map<String, ListNodeType>
                    )
                    addProperty(innerClass, innerPropertyMap)
                    parentClass.addInnerClass(innerClass)
                    DataType.ObjectType(
                        pkgName = innerPkgName,
                        className = k.className,
                        dataTypeMap = innerPropertyMap,
                        rawKey = k,
                        canNull = v.canNull,
                        isList = v.isList
                    )
                }

                is Unit -> {
                    //未知类型
                    DataType.UnknownType(rawKey = k, canNull = v.canNull, isList = v.isList)
                }

                else -> {
                    throw IllegalArgumentException("not support type:${v.type}")
                }
            }


        }

        return propertyMap
    }

    private fun parseListNode(
        parentClass: T, key: String, listNode: Node.ListNode, innerPkgName: String
    ) {

        when (listNode[0]) {
            is Node.ObjectNode -> {
                val typeMap = parseListNodeType(listNode).also {
                    println("parseJsonArray obj:$it")
                }
                val innerClass = createDataClassSpec(key.className, true)
                innerClass.addDoc("key:$key - value:$listNode")
                val propertyMap = createPropertyClass(
                    innerClass, key.className, innerPkgName + "." + key.className, typeMap
                )
                addProperty(innerClass, propertyMap)
                parentClass.addInnerClass(innerClass)
                addLazyField(
                    parentClass, listNode, DataType.ObjectType(
                        pkgName = innerPkgName,
                        className = key.className,
                        dataTypeMap = propertyMap,
                        rawKey = key,
                        canNull = false,
                        isList = true
                    )
                )
            }

            is Node.ListNode -> {
                println("not support array in array")
            }

            is Node.ValueNode -> {
                addBasicArray(parentClass, key, listNode.map { it as? Node.ValueNode })
            }

            else -> {
                addBasicArray(parentClass, key, listOf())
            }
        }
    }

    /**
     * 添加延迟初始化的 list 字段
     * @param classSpec T 宿主类
     * @param listNode JSONArray 数据源
     * @param objType ObjectType 字段
     */
    abstract fun addLazyField(
        classSpec: T,
        listNode: Node.ListNode,
        objType: DataType.ObjectType,
    )


    protected abstract fun addBasicArray(
        classSpec: T, key: String, list: List<Node.ValueNode?>
    )


    private val currentTime: String
        get() {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return format.format(java.util.Date())
        }

    abstract fun addStaticFiled(typeSpecBuilder: T, key: String, v: Node.ValueNode)
    abstract fun addStaticUnknownFiled(typeSpecBuilder: T, key: String)

}


