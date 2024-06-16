package com.location.confimerge_kotlin

import com.location.configgen.core.codeGen.DataType
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.codeGen.fieldName
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.valueType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.joinToCode
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import kotlin.random.Random

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class KotlinClassGenerate(packageName: String, outputDir: String, json: String, className: String) :
    ClassGenerate<KotlinClassSpec>(packageName, outputDir, json, className) {

    override fun writeFile(fileComment: String, classSpec: KotlinClassSpec) {
        val typeSpec = classSpec.build()
        val javaFile =
            FileSpec.builder(rootPackageName, rootClassName).addType(typeSpec as TypeSpec).addFileComment(fileComment)
                .build()
        javaFile.writeTo(File(outputDir))
    }

    override val generateVersion: String
        get() = "1.0.0"

    override fun createClassSpec(className: String, isInner: Boolean): KotlinClassSpec =
        KotlinClassSpec(KotlinClassSpec.Type.Object, className, isInner)

    override fun createDataClassSpec(className: String, isInner: Boolean): KotlinClassSpec = KotlinClassSpec(KotlinClassSpec.Type.Data, className, isInner)



    override fun addLazyField(
        classSpec: KotlinClassSpec, jsArray: JSONArray, objType: DataType.ObjectType
    ) {
        val typeMap = objType.dataTypeMap
        val key = objType.rawKey
            with(classSpec.classType) {
                val fieldType = List::class.asClassName().parameterizedBy(ClassName(objType.pkgName, objType.className))

                addProperty(propertySpec(
                    key.fieldName, fieldType
                ) {
                   addKdoc("key:$key value:$jsArray")
                    delegate(CodeBlock.builder().beginControlFlow("lazy")
                        .addStatement(CodeBlock.builder().also {
                            val initParamsList = mutableListOf<CodeBlock>()
                            jsArray.mapNotNull { it as? JSONObject}.forEach { jsItem ->
                                initParamsList.add(CodeBlock.builder().add("%T(", ClassName(objType.pkgName, objType.className))
                                    .add(createNewInstanceParam(jsItem, typeMap, it))
                                    .add(")")
                                    .build())
                            }
                            it.addStatement(initParamsList.joinToCode(prefix = "listOf(", suffix = ")").toString())
                            //add("listOf(").add(")")
                        }

                            .build().toString())
                        .endControlFlow()
                        .build()
                    )
                })
            }
    }



    private fun getDataTypeDefValue(dataType: DataType) = "null"

    private fun createNewInstanceParam(
        jsObj: JSONObject, typeMap: Map<String, DataType>, parentCodeBlockBuilder:CodeBlock.Builder
    ): CodeBlock {
        fun createParam(
            k:String?,
            dataType: DataType, value: Any, methodSpecBuilder: CodeBlock.Builder
        ): CodeBlock {
            val paramCodeBlock = CodeBlock.builder().add("")
            if(k != null){
                paramCodeBlock.add("$k = ")
            }
            return when (dataType) {
                is DataType.ObjectType -> {
                    paramCodeBlock.add("%T(", ClassName(dataType.pkgName, dataType.className)).add(
                        createNewInstanceParam(
                            value as JSONObject, dataType.dataTypeMap, methodSpecBuilder
                        )
                    ).add(")")
                }

                is DataType.BasisType -> {
                    when (dataType.type) {
                        ValueType.STRING -> paramCodeBlock.add(
                            "%S", value
                        )


                        ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> paramCodeBlock.add(
                            "$value"
                        )

                        ValueType.LONG -> paramCodeBlock.add(
                            "%L", "${value}L"
                        )

                        ValueType.FLOAT -> paramCodeBlock.add(
                            "%L", "${value}f"
                        )

                    }
                }

                is DataType.UnknownType -> paramCodeBlock.add("null")
            }.build()
        }

        val codeBlockList = mutableListOf<CodeBlock>()
        typeMap.forEach { (k, dataType) ->
            val value = jsObj[dataType.rawKey]
            if (value == null && dataType.canNull.not()) {
                error("$json in ${dataType.rawKey} is null, but canNull is false, please submit issue to fix it https://github.com/TLocation/ConfigAutoGen/issues")
            } else if (value == null) {
                codeBlockList.add(CodeBlock.of("$k = ${getDataTypeDefValue(dataType)}"))
                return@forEach
            }


            codeBlockList.add(if (dataType.isList) {
                val childArray =
                    value as? JSONArray ?: error("k:${dataType.rawKey} value is not JSONArray")
                val validArray = childArray.filterNotNull()
                if(validArray.isEmpty()){
                    CodeBlock.of("$k = listOf()")
                }else{
                    val tmpFieldName = "${k}List${Random.nextInt(1000)}"
                    //为了可读性
                    val childCodeBlockList = mutableListOf<CodeBlock>()
                    validArray.forEach { childItem ->
                        childCodeBlockList.add(createParam(null, dataType, childItem, parentCodeBlockBuilder))
                    }
                    parentCodeBlockBuilder.addStatement(
                        childCodeBlockList.joinToCode(
                            prefix = if(validArray.isNotEmpty()) "val $tmpFieldName = listOf(" else "val $tmpFieldName = emptyList()",
                            suffix = ")"
                        ).toString()
                    )
                    CodeBlock.of("$k = $tmpFieldName")
                }
            } else {
                createParam(k, dataType, value, parentCodeBlockBuilder)
            })
        }
        return codeBlockList.joinToCode()
    }



    override fun addProperty(classSpec: KotlinClassSpec, propertyMap: Map<String, DataType>) {
        require(classSpec.type == KotlinClassSpec.Type.Data){
            "only data class can add property"
        }
        with(classSpec.classType){
           val constructor =  FunSpec.constructorBuilder()
            propertyMap.forEach { (key, value) ->
                val typeName: TypeName = when (value) {
                    is DataType.BasisType -> value.type.asTypeName()
                    is DataType.ObjectType -> ClassName(value.pkgName, value.className)
                    is DataType.UnknownType -> NOTHING
                }.let {
                    val type = if (value.isList) List::class.asClassName().parameterizedBy(
                        it
                    ) else it
                    type.copy(nullable = value.canNull)
                }
                val propertySpec = propertySpec(key.fieldName, typeName) {
                    initializer(key.fieldName)
                }
                addProperty(propertySpec)
                constructor.addParameter(key.fieldName,  typeName)
            }
            primaryConstructor(constructor.build())
        }

    }





    override fun addBasicArray(classSpec: KotlinClassSpec, key: String, jsArray: JSONArray) {
        val type = jsArray.firstOrNull()?.valueType
        classSpec.classType.addProperty(propertySpec(
            key.fieldName, List::class.asClassName().parameterizedBy(
                type?.asTypeName()
                    ?: NOTHING
            )
        ) {
            if (type == null && jsArray.isNotEmpty()) {
                error("generate list fail key:$key value:$jsArray")
            }
            addModifiers(
                KModifier.PUBLIC,
            ).addKdoc("key:$key value:$jsArray")
            delegate(CodeBlock.builder()
                .beginControlFlow("lazy")


                .addStatement(CodeBlock.builder().add("listOf(")
                    .also {
                        it.add(jsArray.joinToCode { item ->
                            when (type) {
                                ValueType.STRING -> CodeBlock.of("%S", item)
                                ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> CodeBlock.of(
                                    "%L",
                                    item
                                )
                                ValueType.LONG -> CodeBlock.of("%L", "${item}L")
                                ValueType.FLOAT -> CodeBlock.of("%L", "${item}f")
                                else -> {
                                    error("unknown error")
                                }
                            }
                        })

                    }
                    .add(")")
                    .build().toString())
                .endControlFlow()
                .build())

        })
    }

    override fun addStaticFiled(typeSpecBuilder: KotlinClassSpec, key: String, v: Any) {
        typeSpecBuilder.classType.addProperty(propertySpec(
            key.fieldName, v.valueType.asTypeName()
        ) {
            addModifiers(
                KModifier.CONST,
                KModifier.PUBLIC,
            ).addKdoc("key:$key value:$v")
            when (v.valueType) {
                ValueType.STRING -> initializer("%S", v)
                ValueType.INT, ValueType.BOOLEAN, ValueType.LONG -> initializer("%L", v)
                ValueType.FLOAT -> initializer("%Lf", v)
                ValueType.DOUBLE -> initializer("%Ld", v)
            }
        })
    }

    override fun addStaticUnknownFiled(typeSpecBuilder: KotlinClassSpec, key: String) {
        typeSpecBuilder.classType.addProperty(propertySpec(
            key.fieldName, NOTHING.copy(nullable = true)
        ) {
            addModifiers(
                KModifier.PUBLIC,
            ).addKdoc("key:$key value:null")
           initializer("null")
        })
    }

}

private fun ValueType.asTypeName(): TypeName = when (type) {
    String::class.java -> STRING
    else -> type.asTypeName()
}
