package com.location.configgen.core.codeGen

import com.location.configgen.core.config.GITHUB_URL
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import org.gradle.internal.impldep.org.jetbrains.annotations.VisibleForTesting
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
    private val rootNode: Node.ObjectNode,
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
            SourceCode:$GITHUB_URL
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


    /**
     * 创建一个用于生成Class 的对象
     * @param className String 类名
     * @param isInner Boolean 是否是内部类 对于生成 java 代码 内部类需要加上 static
     * @return T
     * @see ClassSpec
     */
    abstract fun createClassSpec(className: String, isInner: Boolean): T


    /**
     * 创建数据类 当前用于列表内的自定义结构体
     * @see createClassSpec
     */
    abstract fun createDataClassSpec(className: String, isInner: Boolean): T


    /**
     * 解析 ObjectNode 并将里面的每个字段注入到 classSpec 中
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
                            classSpec, k, v, "$rootPackageName.$rootClassName"
                        )
                    } else {
                        addBasicArray(classSpec, k, listOf())
                    }
                }

                is Node.ValueNode -> {
                    addStaticFiled(classSpec, k, v)
                }
                else -> {
                    addStaticUnknownFiled(classSpec, k)
                }

            }
        }
    }

    /**
     * 给当前类添加属性
     * @param classSpec T 创建类的实例
     * @param propertyMap Map<String, DataType> 需要添加的属性
     * 在这个方法中需要将属性添加到 classSpec 中 且需要暴露改属性的赋值函数
     */
    protected abstract fun addProperty(classSpec: T, propertyMap: Map<String, DataType>)


    /**
     * 解析列表里面的所有字段 这里会判断[unstableArray]属性
     * 如果为 true 则会扫描整个数组的所有元素 生产一个最全的属性集
     *
     * 假设当前有需要解析的 json list 为
     *
     *  ```
     *     [
     *       {
     *        "id" : 1,
     *        "name" : "java"
     *       },
     *       {
     *        "id" : 1719402751297,
     *        "name" : "kotlin",
     *        "desc" : "great language"
     *       }
     *     ]
     * ```
     * 在这个 json 中 id 和 name 是必须的字段 但是 desc 是可选的字段
     * 如果 [unstableArray] 为 false 则只会解析到 id 和 name 字段 当实际赋值时 desc 字段无法赋值 会直接报错
     * 在全量扫描时 也会对类型进行类型提升 在第一个iterm 中 `id` 会别解析为 `int` 在第二个iterm 中 `id` 会被解析为 `long` 进行类型提升
     *
     * @param listNode ListNode 数据源
     * @param initValue Map<String, ListNodeType>? 上一次扫描的类型 在 list 里面嵌套 object 里面嵌套 list 的情况下会多次扫描
     * @return Map<String, ListNodeType> 返回 list 里面包含的属性
     */
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
        //是否是第一次扫描
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
                            filedMap[k] =
                                ListNodeType(Unit, canNull = typeCanNull, isList = true)
                        } else if (v.isNotEmpty()) {
                            when (val childV = v[0].also {
                                require(it != null) {
                                    "not support array first child is null"
                                }
                            }) {
                                is Node.ObjectNode -> {
                                    @Suppress("UNCHECKED_CAST")
                                    filedMap[k] =
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
                                    filedMap[k] =
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
                        filedMap[k] = ListNodeType(
                            parseListNodeType(
                                Node.ListNode(listOf(v), v.docs),
                                initValue = oldType?.type as? Map<String, ListNodeType>
                            ),
                            canNull = typeCanNull
                        )
                    }

                    else -> {
                        if (oldType == null) {
                            filedMap[k] =
                                ListNodeType(
                                    (v as? Node.ValueNode)?.valueType ?: Unit,
                                    typeCanNull || v == null
                                )
                        } else {
                            if (v == null) {
                                filedMap[k] = oldType.copy(canNull = true)
                            } else if (v is Node.ValueNode) {
                                val valueType = v.valueType
                                if (oldType.type.javaClass == valueType.javaClass) {
                                    val oldValueType = oldType.type as ValueType
                                    if (oldValueType.groupId == valueType.groupId && oldValueType.ordinal < valueType.ordinal) {
                                        //可以向上提升
                                        filedMap[k] = oldType.copy(type = valueType)
                                    } else if (oldValueType.groupId != valueType.groupId) {
                                        throw IllegalArgumentException("not support type change")
                                    }
                                } else if (oldType.type is Unit) {
                                    //直接替换类型
                                    filedMap[k] = oldType.copy(type = valueType)
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

    /**
     * 为当前[parentClass] 添加属性
     * @param parentClass T 类的描述信息
     * @param classPrefix String 类的前缀 对于内部类需要加上外部类的前缀
     * @param innerPkgName String 要基于[parentClass]创建内部类的时候的包名
     * @param typeMap Map<String, ListNodeType> 当前[parentClass]应该包含的属性信息
     * @return Map<String, DataType> 返回当前[parentClass]包含的属性结构信息 有序的 创建[parentClass]时需要根据当前返回值去填充属性
     */
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

    /**
     * 为[listNode]生成属性结构 并赋值
     * @param parentClass T
     * @param key String
     * @param listNode ListNode
     * @param innerPkgName String
     */
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


    /**
     * 为基础类型列表赋值 如 string int long等等
     * @param classSpec T
     * @param key String
     * @param list List<ValueNode?>
     */
    protected abstract fun addBasicArray(
        classSpec: T, key: String, list: List<Node.ValueNode?>
    )


    private val currentTime: String
        get() {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return format.format(java.util.Date())
        }

    /**
     * 添加静态字段
     * @param typeSpecBuilder T
     * @param key String
     * @param v ValueNode
     */
    abstract fun addStaticFiled(typeSpecBuilder: T, key: String, v: Node.ValueNode)


    /**
     * 添加静态的未知字段 如当前 json 为
     * ```
     * {
     *    "name" : null
     * }
     * ```
     * 对于 name 属性 无法推断类型 会被赋值为未知类型 在 Java 中 为 [Object] Kotlin中为[Nothing]
     * @param typeSpecBuilder T
     * @param key String
     */
    abstract fun addStaticUnknownFiled(typeSpecBuilder: T, key: String)

}


