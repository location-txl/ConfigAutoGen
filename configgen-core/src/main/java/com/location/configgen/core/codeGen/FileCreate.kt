package com.location.configgen.core.codeGen

import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.fieldName
import com.location.configgen.core.datanode.nodeType
import com.location.configgen.core.datanode.valueType
import org.gradle.internal.impldep.org.jetbrains.annotations.VisibleForTesting
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.Locale

/**
 *
 * @author tianxiaolong
 * time：2024/6/7 17:19
 * description：
 */
abstract class FileCreate<T : TypeSpecBuilderWrapper>(
    protected val packageName:String,
    protected val outputDir: String,
    protected val json: String,
    protected val className: String,
    /**
     * 是否生成不稳定的数组 如果为 true 则会扫描整个数组的所有元素 生产一个最全的Object
     */
    private val unstableArray:Boolean = true
) {

    fun create(){
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)
        val classSpec = createTypeSpecBuilder(className, false).apply {
            addJavaDoc("RawJson:$json")
        }
        parseJsonObj(classSpec, jsPoint as JSONObject)
        writeFile("""
             config-merge plugin automatically generated, please do not modify
                time:$currentTime
                author:tianxiaolong
                Version:$GENERATE_VERSION
                SourceCode:gitLab/RobotUI/config-merge
        """.trimIndent(), classSpec)
    }
    abstract fun writeFile(
        fileComment:String,
        classSpec:T)



    abstract fun createTypeSpecBuilder(className: String, isInner:Boolean): T


    abstract fun createDataTypeSpecBuilder(className: String, isInner:Boolean): T






    private fun parseJsonObj(typeSpecBuilder: T, jsObj: JSONObject){

        jsObj.forEach { k, v ->
            when (v) {
                is JSONObject -> {
                    val innerClass = createTypeSpecBuilder(k.toString().className, true).apply {
                        addJavaDoc("key:$k - value:$v")
                    }
                    parseJsonObj(innerClass, v)
                    typeSpecBuilder.addType(innerClass.build())
                }

                is JSONArray -> {
                    if(v.isNotEmpty()){
                        parseJsonArray(typeSpecBuilder, k.toString(), v, "$packageName.$className")
                    }else{
                        println("$k is empty array")
                    }
                }
                else -> {
                    if(v == null){
                        addStaticUnknownFiled(typeSpecBuilder, k.toString())
                    }else{
                        addStaticFiled(typeSpecBuilder, k.toString(), v)
                    }
                }

            }
        }
    }


    protected abstract fun addProperty(typeSpecBuilder: T, propertyMap:Map<String, DataType>)


    @VisibleForTesting
    fun parseJsArrayType(jsArray: JSONArray, initValue:Map<String, JsArrayType>? = null): Map<String, JsArrayType> {
        val filedMap = mutableMapOf<String,JsArrayType>().apply {
            if(initValue != null){
                putAll(initValue)
            }
        }
        var index = 0
        var firstScan = true
        do {
            val newKeyIsNull = initValue != null || firstScan.not()

            val obj = jsArray[index++] as? JSONObject ?: continue
            //可以为空的字段
            with(filedMap.keys.toMutableList()) {
                removeAll(obj.keys.map { it.toString() })
                forEach { canNullKey ->
                    filedMap[canNullKey] = filedMap[canNullKey]!!.copy(isNull = true)
                }
            }

            obj.forEach { k, v ->
                val oldType = filedMap[k]
                when(v){
                    is JSONArray -> {
                        if (oldType == null && v.isEmpty()) {
                            filedMap[k.toString()] = JsArrayType(Unit, newKeyIsNull, isList = true)
                        } else if(v.isNotEmpty()) {
                            when(val childV = v[0].also {
                                if(it == null){
                                    throw IllegalArgumentException("not support array first child is null")
                                }
                            }){
                                is JSONObject -> {
                                    filedMap[k.toString()] =
                                        oldType?.copy(
                                            type = parseJsArrayType(v, oldType.type as? Map<String, JsArrayType>),
                                            isList = true,
                                        ) ?: JsArrayType(
                                            parseJsArrayType(v, null), isNull = newKeyIsNull,
                                            isList = true,
                                        )
                                }
                                is JSONArray -> {
                                    throw IllegalArgumentException("not support array in array")
                                }
                                else -> {
                                    filedMap[k.toString()] = oldType?.copy(type = childV!!.valueType, isList = true)?: JsArrayType(childV!!.valueType, newKeyIsNull, isList =  true)                                }
                            }

                        }
                    }
                    is JSONObject -> {
                        filedMap[k.toString()] = JsArrayType(parseJsArrayType(JSONArray().apply {
                            add(v)
                        }, initValue = oldType?.type as? Map<String, JsArrayType>), newKeyIsNull)
                    }
                    else -> {
                        if(oldType == null){
                            filedMap[k.toString()] = JsArrayType(v?.valueType ?: Unit, newKeyIsNull || v == null)
                        }else{
                            if(v == null){
                                filedMap[k.toString()] = oldType.copy(isNull = true)
                            }else {
                                val valueType = v.valueType
                                if(oldType.type.javaClass == valueType.javaClass) {
                                    val oldValueType = oldType.type as ValueType
                                    if(oldValueType.groupId == valueType.groupId && oldValueType.ordinal < valueType.ordinal){
                                        //可以向上提升
                                        filedMap[k.toString()] = oldType.copy(type = valueType)
                                    }else if(oldValueType.groupId != valueType.groupId){
                                        throw IllegalArgumentException("not support type change")
                                    }
                                }else if(oldType.type is Unit){
                                    //直接替换类型
                                    filedMap[k.toString()] = oldType.copy(type = valueType)
                                }else{
                                    throw IllegalArgumentException("not support type change")
                                }
                            }
                        }
                    }
                }
            }
            firstScan = false
        }while (unstableArray && index < jsArray.size )
        return filedMap
    }

    @VisibleForTesting
     fun createPropertyClass(rootClass:T, classPrefix:String, innerPkgName: String,  typeMap:Map<String, JsArrayType>):Map<String, DataType>{
         val propertyMap = mutableMapOf<String, DataType>()
        typeMap.forEach { (k, v) ->
            propertyMap[k.fieldName] = when(v.type){
                is ValueType -> {
                    DataType.BasisType(v.type, k, canNull = v.isNull, isList = v.isList)
                }
                is Map<*,*> -> {
                    val innerClass = createDataTypeSpecBuilder(k.className, true)
                    innerClass.addJavaDoc("key:$k - value:$v")
                    val className = k.className
                    val nextPkgName = "$innerPkgName.${className}"
                    val innerPropertyMap = createPropertyClass(innerClass, "$classPrefix.${className}",
                        nextPkgName, v.type as Map<String, JsArrayType>)
                    addProperty(innerClass, innerPropertyMap)
                    rootClass.addType(innerClass.build())
                    DataType.ObjectType(
                        pkgName = innerPkgName,
                        className = k.className,
                        dataTypeMap = innerPropertyMap,
                        k,
                        canNull = v.isNull,
                        isList = v.isList
                    )
                }
                is Unit -> {
                    //未知类型
                    DataType.UnknownType(k, canNull = v.isNull, isList = v.isList)
                }
                else -> {
                    throw IllegalArgumentException("not support type:${v.type}")
                }
            }


        }

      return propertyMap
    }
    private fun parseJsonArray(typeSpecBuilder: T, key:String, jsArray:JSONArray, innerPkgName:String){

        when(jsArray[0]){
            is JSONObject -> {
                val typeMap = parseJsArrayType(jsArray).also {
                    println("parseJsonArray obj:$it")
                }
                val innerClass = createDataTypeSpecBuilder(key.className, true)
                innerClass.addJavaDoc("key:$key - value:$jsArray")
                val propertyMap = createPropertyClass(
                    innerClass,
                    key.className,
                    innerPkgName + "." + key.className,
                    typeMap
                )
                addProperty(innerClass, propertyMap)
                typeSpecBuilder.addType(innerClass.build())
                addLazyField(typeSpecBuilder, key, jsArray, innerClass, propertyMap, DataType.ObjectType(innerPkgName, key.className,
                    dataTypeMap = propertyMap,
                    key,
                    canNull = false, isList = true))





//                val typeObject = if(unstableArray){
//
//                }else{
//
//                }
//                val innerClass = createTypeSpecBuilder(key.className, true).apply {
//                    addJavaDoc("key:$key - value:$jsArray")
//                }
//                parseJsonObj(innerClass, jsArray[0] as JSONObject)
//                typeSpecBuilder.addType(innerClass.build())
            }
            is JSONArray -> {
                println("not support array in array")
            }
            else -> {
                addNormalArray(typeSpecBuilder, key, jsArray)
            }
        }
    }

    abstract fun addLazyField(
        typeSpecBuilder: T,
        key: String,
        jsArray: JSONArray,
        objTypeSpec: T,
        typeMap: Map<String, DataType>,
        objType:DataType.ObjectType,
    )


    private fun parseJsObjToType(obj:JSONObject){

    }

    protected abstract fun addNormalArray(
        typeSpecBuilder: T,
        key: String,
        jsArray: JSONArray
    )


    private fun parseJsonNode(jsObj: JSONObject): Node {
        val node = Node.ObjectNode(className, mutableMapOf())
        jsObj.forEach { k, v ->
            if(v == null){
                return@forEach
            }
            when (v) {
                is JSONObject -> {
                    val childNode = parseJsonNode(v)
                    node.property[k.toString().fieldName] = childNode
                }

                is JSONArray -> {

                }

                else -> {
                    node.property[k.toString().fieldName] = Node.ValueNode(v, v.nodeType)
                }
            }
        }

        return node

    }




    protected val currentTime:String
        get() {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            return format.format(java.util.Date())
        }

    abstract fun addStaticFiled(typeSpecBuilder: T, key: String, v: Any)
    abstract fun addStaticUnknownFiled(typeSpecBuilder: T, key: String)

}



    private val String.fieldName
    get() = if (this.contains("_")) {
        this.split("_").joinToString("") {
            it.replaceFirstChar { firstChar ->
                if (firstChar.isUpperCase()) firstChar.lowercase() else firstChar.toString()
            }
        }

    } else {
        this.replaceFirstChar {
            if (it.isUpperCase()) {
                it.lowercase()
            } else {
                it.toString()
            }
        }
    }