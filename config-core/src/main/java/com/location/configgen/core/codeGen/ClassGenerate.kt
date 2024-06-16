package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.valueType
import org.jetbrains.annotations.VisibleForTesting
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
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
    protected val json: String,
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
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)
        val classSpec = createClassSpec(rootClassName, isInner = false).apply {
            addDoc("SourceJson:$json")
        }
        parseJsonObj(
            classSpec = classSpec,
            jsObj = jsPoint as? JSONObject ?: error("root element must be json object")
        )
        writeFile(
            """
            configAutoGen plugin automatically generated, please do not modify
            generateTime:$currentTime
            Version:$generateVersion
            SourceCode:https://github.com/TLocation/ConfigAutoGen
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
     * @param jsObj JSONObject json 对象
     */
    private fun parseJsonObj(classSpec: T, jsObj: JSONObject) {

        jsObj.forEach { k, v ->
            when (v) {
                is JSONObject -> {
                    val innerClass = createClassSpec(k.toString().className, isInner = true).apply {
                        addDoc("key:$k - value:$v")
                    }
                    parseJsonObj(innerClass, v)
                    classSpec.addInnerClass(innerClass)
                }

                is JSONArray -> {
                    if (v.isNotEmpty()) {
                        parseJsonArray(
                            classSpec, k.toString(), v, "$rootPackageName.$rootClassName"
                        )
                    } else {
                        addBasicArray(classSpec, k.toString(), v)
                    }
                }

                else -> {
                    if (v == null) {
                        addStaticUnknownFiled(classSpec, k.toString())
                    } else {
                        addStaticFiled(classSpec, k.toString(), v)
                    }
                }

            }
        }
    }


    protected abstract fun addProperty(classSpec: T, propertyMap: Map<String, DataType>)


    @VisibleForTesting
    fun parseJsArrayType(
        jsArray: JSONArray, initValue: Map<String, JsArrayType>? = null
    ): Map<String, JsArrayType> {
        val filedMap = LinkedHashMap<String, JsArrayType>().apply {
            if (initValue != null) {
                putAll(initValue)
            }
        }
        var index = 0
        var firstScan = true
        do {
            val typeCanNull = initValue != null || firstScan.not()

            val obj = jsArray[index++] as? JSONObject ?: continue
            //可以为空的字段
            with(filedMap.keys.toMutableList()) {
                removeAll(obj.keys.map { it.toString() })
                forEach { canNullKey ->
                    filedMap[canNullKey] = filedMap[canNullKey]!!.copy(canNull = true)
                }
            }

            obj.forEach { k, v ->
                val oldType = filedMap[k]
                when (v) {
                    is JSONArray -> {
                        if (oldType == null && v.isEmpty()) {
                            filedMap[k.toString()] =
                                JsArrayType(Unit, canNull = typeCanNull, isList = true)
                        } else if (v.isNotEmpty()) {
                            when (val childV = v[0].also {
                                require(it != null) {
                                    "not support array first child is null"
                                }
                            }) {
                                is JSONObject -> {
                                    @Suppress("UNCHECKED_CAST")
                                    filedMap[k.toString()] =
                                        oldType?.copy(
                                            type = parseJsArrayType(
                                                v, oldType.type as? Map<String, JsArrayType>
                                            ),
                                            isList = true,
                                        ) ?: JsArrayType(
                                            parseJsArrayType(v, null), canNull = typeCanNull,
                                            isList = true,
                                        )
                                }

                                is JSONArray -> {
                                    throw IllegalArgumentException("not support array in array")
                                }

                                else -> {
                                    filedMap[k.toString()] =
                                        oldType?.copy(type = childV!!.valueType, isList = true)
                                            ?: JsArrayType(
                                                childV!!.valueType,
                                                canNull = typeCanNull,
                                                isList = true
                                            )
                                }
                            }

                        }
                    }

                    is JSONObject -> {
                        @Suppress("UNCHECKED_CAST")
                        filedMap[k.toString()] = JsArrayType(
                            parseJsArrayType(JSONArray().apply {
                                add(v)
                            }, initValue = oldType?.type as? Map<String, JsArrayType>),
                            canNull = typeCanNull
                        )
                    }

                    else -> {
                        if (oldType == null) {
                            filedMap[k.toString()] =
                                JsArrayType(v?.valueType ?: Unit, typeCanNull || v == null)
                        } else {
                            if (v == null) {
                                filedMap[k.toString()] = oldType.copy(canNull = true)
                            } else {
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
                            }
                        }
                    }
                }
            }
            firstScan = false
        } while (unstableArray && index < jsArray.size)
        return filedMap
    }

    @VisibleForTesting
    fun createPropertyClass(
        parentClass: T, classPrefix: String, innerPkgName: String, typeMap: Map<String, JsArrayType>
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
                        v.type as Map<String, JsArrayType>
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

    private fun parseJsonArray(
        parentClass: T, key: String, jsArray: JSONArray, innerPkgName: String
    ) {

        when (jsArray[0]) {
            is JSONObject -> {
                val typeMap = parseJsArrayType(jsArray).also {
                    println("parseJsonArray obj:$it")
                }
                val innerClass = createDataClassSpec(key.className, true)
                innerClass.addDoc("key:$key - value:$jsArray")
                val propertyMap = createPropertyClass(
                    innerClass, key.className, innerPkgName + "." + key.className, typeMap
                )
                addProperty(innerClass, propertyMap)
                parentClass.addInnerClass(innerClass)
                addLazyField(
                    parentClass, jsArray, DataType.ObjectType(
                        pkgName = innerPkgName,
                        className = key.className,
                        dataTypeMap = propertyMap,
                        rawKey = key,
                        canNull = false,
                        isList = true
                    )
                )
            }

            is JSONArray -> {
                println("not support array in array")
            }

            else -> {
                addBasicArray(parentClass, key, jsArray)
            }
        }
    }

    /**
     * 添加延迟初始化的 list 字段
     * @param classSpec T 宿主类
     * @param jsArray JSONArray 数据源
     * @param objType ObjectType 字段
     */
    abstract fun addLazyField(
        classSpec: T,
        jsArray: JSONArray,
        objType: DataType.ObjectType,
    )


    protected abstract fun addBasicArray(
        classSpec: T, key: String, jsArray: JSONArray
    )


    private val currentTime: String
        get() {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return format.format(java.util.Date())
        }

    abstract fun addStaticFiled(typeSpecBuilder: T, key: String, v: Any)
    abstract fun addStaticUnknownFiled(typeSpecBuilder: T, key: String)

}


