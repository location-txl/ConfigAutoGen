package com.location.configgen.core.codeGen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeSpec.Builder
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.util.Locale
import javax.lang.model.element.Modifier
import kotlin.random.Random
import kotlin.random.nextUInt

const val GENERATE_VERSION = "1.0.2"
private val currentTime:String
    get() {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        return format.format(java.util.Date())
    }

/**
 *
 * @author tianxiaolong
 * time：2023/7/19 14:54
 * description：
 */
class JavaFileCreate(
    private  val packageName:String,
    private val outputDir: String,
    private val json: String,
    private val className: String,
) {

    companion object{
        private var varIndex = 0
    }
//     = android.defaultConfig?.applicationId ?: "com.marsmarch.config.plugin"
    fun create() {
        val jsonParser = JSONParser()
        val jsPoint = jsonParser.parse(json)
        val classSpec = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("RawJson:$json")
        parseJsonObj(classSpec, jsPoint as JSONObject)
        val javaFile = JavaFile.builder(packageName, classSpec.build())
            .addFileComment("""
                config-merge plugin automatically generated, please do not modify
                time:$currentTime
                author:tianxiaolong
                Version:$GENERATE_VERSION
                SourceCode:gitLab/RobotUI/config-merge
            """.trimIndent())
            .build()
        javaFile.writeTo(File(outputDir))

    }

    private fun parseJsonObj(classSpec: Builder, jsObj:JSONObject){
        jsObj.forEach { key, value ->
            when(value){
                is JSONObject -> {
                    val innerClass = TypeSpec.classBuilder(key.toString().className)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addJavadoc("key:$key - value:$value")
                    parseJsonObj(innerClass, value)
                    classSpec.addType(innerClass.build())
                }
                is JSONArray -> {
                    if(value.isNotEmpty()){
                       parseJsonArray(classSpec, key.toString(), value, "$packageName.$className")
                    }
                }
                else -> {
                   addMethod(classSpec, key.toString(), value)
                }
            }
        }
    }

    private fun parseJsonArray(classSpec: Builder, key: String, jsArray: JSONArray, innerPkgName: String){
        when(jsArray[0]){
            is JSONObject -> {
                val innerClass = TypeSpec.classBuilder(key.className)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("key:$key - value:$jsArray")
                val innerClassData = createFieldClass(
                    innerClass,
                    key.className,
                    jsArray[0] as JSONObject,
                    innerPkgName + "." + key.className,
                    ""
                )
                classSpec.addType(innerClass.build())

                //------------------------------
                val field = fieldSpec(
                    name = key.fieldName,
                    type = ParameterizedTypeName.get(
                        ClassName.get(List::class.java),
                        ClassName.get(innerPkgName, key.className)
                    ),
                ){
                    addJavadoc("key:$key - value:$jsArray")
                    addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                }
                classSpec.addField(field)
                classSpec.addMethod(methodSpec(
                    name = key.methodName + "List",
                ){
                    addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    returns(
                        field.type
                    )
                    controlFlow("if(${field.name} == null)") {
                        controlFlow("synchronized(${className}.class)") {
                            controlFlow("if(${field.name} == null)") {
                                addStatement("${field.name} = new \$T<>()", ArrayList::class.java)
                                jsArray.forEach {
                                    addComment("value:$it")
                                    //list.add(new xxx()
                                    addStatement(
                                        "${field.name}.add(${
                                            innerClassData.createInstance(
                                                it as JSONObject,
                                                this,
                                            )
                                        })", ClassName.get(innerPkgName, key.className)
                                    )
                                }
                            }
                        }
                    }
                    addStatement("return ${field.name}")
                })
            }

            /**
             * "key":[
             *
             *
             * ]
             */
            is JSONArray -> {
                   throw IllegalArgumentException("暂不支持多维数组")
            }
            else -> {
                val field = fieldSpec(
                    name = key.fieldName,
                    type = ParameterizedTypeName.get(
                        ClassName.get(List::class.java),
                        jsArray[0]!!.typeName(box = true)
                    ),
                ) {
                    addJavadoc("key:$key - value:$jsArray")
                    addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                }
                classSpec.addField(field)
                classSpec.addMethod(methodSpec(
                    name = key.methodName + "List",
                ) {
                    addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    returns(
                        field.type
                    )

                    controlFlow("if(${field.name} == null)") {
                        /**
                         * synchronized
                         */
                        /**
                         * synchronized
                         */
                        /**
                         * synchronized
                         */

                        /**
                         * synchronized
                         */
                        controlFlow("synchronized(${className}.class)") {
                            controlFlow("if(${field.name} == null)") {
                                addStatement("${field.name} = new \$T<>()", ArrayList::class.java)
                                jsArray.forEach {
                                    addComment("value:$it")
                                    when (it!!.javaType()) {
                                        JavaType.STRING -> addStatement(
                                            "${field.name}.add(\$S)",
                                            it
                                        )

                                        JavaType.INT, JavaType.BOOLEAN, JavaType.DOUBLE -> addStatement(
                                            "${field.name}.add(\$L)",
                                            it
                                        )

                                        JavaType.LONG -> addStatement(
                                            "${field.name}.add(\$L)",
                                            "${it}L"
                                        )

                                        JavaType.FLOAT -> addStatement(
                                            "${field.name}.add(\$L)",
                                            "${it}f"
                                        )
                                    }

                                }
                            }
                        }
                    }
                    addStatement("return ${field.name}")
                })
            }
        }

    }


    private fun createFieldClass(
        classSpec: Builder,
        className: String,
        obj: JSONObject,
        innerPkgName: String,
        root: String
    ): InnerClassData {
        val constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE)
        val map = LinkedHashMap<String,Any>()
        obj.forEach { key, value ->
            when(value){
                is JSONObject -> {
                    val fieldName = key.toString().fieldName
                    val innerClass = TypeSpec.classBuilder(key.toString().className)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .addJavadoc("key:$key - value:$value")
                    val innerClassData = createFieldClass(
                        innerClass,
                        key.toString().className,
                        value,
                        innerPkgName + "." + key.toString().className,
                        "$root$className."
                    )
                    classSpec.addType(innerClass.build())
                    val field = fieldSpec(
                        name = fieldName,
                        type = ClassName.get(innerPkgName, key.toString().className),
                    ) {
                        addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    }
                    constructorBuilder.addParameter(
                        ClassName.get(innerPkgName, key.toString().className),
                        fieldName)
                    classSpec.addField(field)
                    map[fieldName] = innerClassData
                }
                is JSONArray -> {
                    if(value.isNotEmpty()){
                        when(value[0]){
                            is JSONObject -> {
                                val innerClass = TypeSpec.classBuilder(key.toString().className)
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                    .addJavadoc("key:$key - value:$value")
                                val innerClassData = createFieldClass(
                                    innerClass,
                                    key.toString().className,
                                    value[0] as JSONObject,
                                    innerPkgName + "." + key.toString().className,
                                    "$root$className."
                                )
                                classSpec.addType(innerClass.build())
                                val fieldName = key.toString().fieldName
                                val field = fieldSpec(
                                    name = fieldName,
                                    type = ParameterizedTypeName.get(
                                        ClassName.get(List::class.java),
                                        ClassName.get(innerPkgName, key.toString().className)
                                    ),
                                ) {
                                    addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                }
                                constructorBuilder.addParameter(
                                    field.type,
                                    fieldName
                                )
                                classSpec.addField(field)
                                map[fieldName] = ListInstanceParam(innerClassData)
                            }
                            is JSONArray -> {
                                throw IllegalArgumentException("not support json array child is json array")
                            }
                            else -> {
                                val fieldName = key.toString().fieldName
                                val field = fieldSpec(
                                    name = fieldName,
                                    type = ParameterizedTypeName.get(
                                        ClassName.get(List::class.java),
                                        value[0]!!.typeName(box = true)
                                    ),
                                ) {
                                    addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                }
                                constructorBuilder.addParameter(
                                    field.type,
                                    fieldName
                                )
                                map[fieldName] = ListInstanceParam(value[0]!!)
                            }
                        }
                    }
                }
                else -> {
                    val fieldName = key.toString().fieldName
                    map[fieldName] = value
                    constructorBuilder.addParameter(
                        value.typeName(),
                        fieldName
                    )
                    classSpec.addField(fieldSpec(
                        name = fieldName,
                        type = value.typeName()
                    ){
                        addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    })
                }
            }
        }
        classSpec.fieldSpecs.forEach {
            constructorBuilder.addStatement("this.${it.name} = ${it.name}")
        }
        classSpec.addMethod(constructorBuilder.build())
        return InnerClassData(
            className =  root + classSpec.build().name,
            classSpec = classSpec,
            map = map
        )
    }

    private data class ListInstanceParam(val data:Any)

    private class InnerClassData(
        private val className:String,
        private val classSpec: Builder,
        private val map:LinkedHashMap<String,Any>
    ){
        fun createInstance(jsObj:JSONObject, methodBuilder:MethodSpec.Builder):String{
            val  builder = StringBuilder()
            builder.append("new $className(")
            var first = true
            map.forEach { (key, type) ->
                if(!first){
                    builder.append(",")
                }
                if(first){
                    first = false
                }
                when(type.javaType()){
                    JavaType.STRING -> {
                        builder.append("\"${jsObj[key]}\"")
                    }
                    JavaType.INT -> {
                        builder.append(jsObj[key])
                    }
                    JavaType.LONG -> {
                        builder.append("${jsObj[key]}L")
                    }
                    JavaType.FLOAT -> {
                        builder.append("${jsObj[key]}f")
                    }
                    JavaType.DOUBLE -> {
                        builder.append(jsObj[key])
                    }
                    is InnerClassData -> {
                        builder.append((type as InnerClassData).createInstance(jsObj[key] as JSONObject, methodBuilder))
                    }
                    is ListInstanceParam -> {
                        type as ListInstanceParam
                        when(type.data){
                            is InnerClassData -> {
                                val filedName = "${key}List${Random.nextUInt()}"
                                methodBuilder.addStatement(
                                    "List<${type.data.className}> $filedName = new \$T<>()",
                                    ArrayList::class.java
                                )
                                val jsArray = jsObj[key] as JSONArray
                                jsArray.forEach {
                                    methodBuilder.addComment("value:$it")
                                    methodBuilder.addStatement(
                                        "$filedName.add(${type.data.createInstance(it as JSONObject, methodBuilder)})"
                                    )
                                }
                                builder.append(filedName)
                            }
                            else -> {
                                val filedName = "${key}${varIndex++}List"
                                methodBuilder.addStatement(
                                    "List<${type.data.typeName(box = true)}> $filedName = new \$T<>()",
                                    ArrayList::class.java
                                )
                                val jsArray = jsObj[key] as JSONArray
                                jsArray.forEach {
                                    methodBuilder.addComment("value:$it")
                                    if(it is String){
                                        methodBuilder.addStatement("${filedName}.add(\$S)", it)
                                    }else{
                                        methodBuilder.addStatement("${filedName}.add(\$L)", it)
                                    }
                                }
                                builder.append(filedName)
                            }
                        }
                    }
                }
            }
            builder.append(")")
            return builder.toString()
        }
    }

    private fun addMethod(classSpec:TypeSpec.Builder, key:String, value:Any){
        classSpec.addMethod(methodSpec(
            key.methodName
        ){
            addComment("key:$key - value:$value")
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            when(value.javaType()){
                JavaType.STRING -> {
                    returns(String::class.java)
                    addStatement("return \$S", value)
                }
                JavaType.INT -> {
                    returns(Int::class.java)
                    addStatement("return \$L", value)
                }
                JavaType.BOOLEAN -> {
                    returns(Boolean::class.java)
                    addStatement("return \$L", value)
                }
                JavaType.DOUBLE -> {
                    returns(Double::class.java)
                    addStatement("return \$L", value)
                }
                JavaType.LONG -> {
                    returns(Long::class.java)
                    addStatement("return \$L", "${value}L")
                }
                JavaType.FLOAT -> {
                    returns(Float::class.java)
                    addStatement("return \$L", "${value}f")
                }
                else -> {
                    returns(String::class.java)
                    addStatement("return \$S", value.toString())
                }
            }
        })
    }







    fun test(packageName: String) {
        val test = FieldSpec.builder(String::class.java, "test").build()
        val method = methodSpec("test") {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            returns(String::class.java)
            addStatement("return \$S", "Hello World!")
        }
        var typeSpec = TypeSpec.classBuilder("Test")
            .addModifiers(Modifier.PUBLIC)
            .addField(test).addMethod(method).build()

        //add inner class
        val innerClass = TypeSpec.classBuilder("InnerClass")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addMethod(methodSpec("test") {
                addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                returns(String::class.java)
                addStatement("return \$S", "Hello World!")
            })
            .build()
        typeSpec = typeSpec.toBuilder().addType(innerClass).build()

        JavaFile.builder(packageName, typeSpec)
            .build()
            .writeTo(File(outputDir))
    }

}

private val String.methodName
    get() = "get${this.className}"

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


val String.className
    get() = if(this.contains("_")){
        this.split("_").joinToString("") {
            it.replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) firstChar.uppercase() else firstChar.toString()
            }
        }
    }else{
        this.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
    }



private fun Any.typeName(box:Boolean = false): TypeName {
     val typeName = when (this) {
        is String -> ClassName.get(String::class.java)
        is Int -> TypeName.INT
        is Boolean -> TypeName.BOOLEAN
        is Double -> if(this > Float.MAX_VALUE) TypeName.DOUBLE else TypeName.FLOAT
        is Long -> if(this > Int.MAX_VALUE) TypeName.LONG else TypeName.INT
        is Float -> TypeName.FLOAT
        else -> ClassName.get(String::class.java)
    }
    return if(box) typeName.box() else typeName
}

private fun Any.javaType():Any{
    return when (this) {
        is String -> JavaType.STRING
        is Int -> JavaType.INT
        is Boolean -> JavaType.BOOLEAN
        is Double -> if(this > Float.MAX_VALUE) JavaType.DOUBLE else JavaType.FLOAT
        is Long -> if(this > Int.MAX_VALUE) JavaType.LONG else JavaType.INT
        is Float -> JavaType.FLOAT
        else -> this
    }
}

private enum class JavaType(val typeName: String) {
    STRING("String"),
    INT("Int"),
    BOOLEAN("Boolean"),
    DOUBLE("Double"),
    LONG("Long"),
    FLOAT("Float"),
}

private inline fun MethodSpec.Builder.controlFlow(
    controlFlow: String,
    vararg args: Any,
    block: MethodSpec.Builder.() -> Unit
) {
    beginControlFlow(controlFlow, args)
    try {
        block()
    } finally {
        endControlFlow()
    }

}








const val L = "\$L"
const val T = "\$T"
const val N = "\$N"
const val S = "\$S"
const val W = "\$W"

fun javaFile(
    packageName: String,
    typeSpec: TypeSpec,
    body: JavaFile.Builder.() -> Unit
): JavaFile = JavaFile.builder(packageName, typeSpec).apply(body).build()

fun classSpec(
    name: ClassName,
    body: TypeSpec.Builder.() -> Unit = {}
): TypeSpec = TypeSpec.classBuilder(name).apply(body).build()

fun constructorSpec(
    body: MethodSpec.Builder.() -> Unit = {}
): MethodSpec = MethodSpec.constructorBuilder().apply(body).build()

fun methodSpec(
    name: String,
    body: MethodSpec.Builder.() -> Unit = {}
): MethodSpec = MethodSpec.methodBuilder(name).apply(body).build()

fun parameterSpec(
    type: TypeName,
    name: String,
    body: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec = ParameterSpec.builder(type, name).apply(body).build()

fun fieldSpec(
    name: String,
    type: TypeName,
    body: FieldSpec.Builder.() -> Unit = {}
): FieldSpec = FieldSpec.builder(type, name).apply(body).build()

fun String.toClassName(): ClassName = ClassName.bestGuess(this)

/**
 * Try to parse [value] as a qualified layout XML class name. Unlike normal qualified class name
 * references, nested classes are separated by dollar signs ('$') instead of periods ('.').
 *
 * @throws IllegalArgumentException if [value] fails to parse
 */
fun parseLayoutClassName(value: String, filename: String): ClassName {
    return try {
        val lastDot = value.lastIndexOf('.')
        val packageName = if (lastDot == -1) "" else value.substring(0, lastDot)
        val simpleNames = value.substring(lastDot + 1).split('$')
        ClassName.get(packageName, simpleNames.first(), *simpleNames.drop(1).toTypedArray())
    } catch (e: Exception) {
        throw IllegalArgumentException("Unable to parse \"$value\" as class in $filename.xml", e)
    }
}