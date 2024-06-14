package com.location.confimerge_java

import com.location.configgen.core.codeGen.DataType
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.codeGen.fieldName
import com.location.configgen.core.codeGen.methodName
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.valueType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.internal.impldep.org.jetbrains.annotations.VisibleForTesting
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.File
import javax.lang.model.element.Modifier
import kotlin.random.Random

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class JavaClassGenerate(packageName: String, outputDir: String, json: String, className: String) :
    ClassGenerate<JavaClassSpec>(packageName, outputDir, json, className) {
    companion object {
        @VisibleForTesting
        var inTest = false
    }

    override fun writeFile(fileComment: String, classSpec: JavaClassSpec) {
        val typeSpec = classSpec.build()
        val javaFile =
            JavaFile.builder(rootPackageName, typeSpec as TypeSpec).addFileComment(fileComment)
                .build()
        javaFile.writeTo(File(outputDir))

        if (inTest) {
            javaFile.writeTo(System.out)
        }
    }

    override val generateVersion: String
        get() = "1.0.0"

    override fun createClassSpec(className: String, isInner: Boolean): JavaClassSpec =
        JavaClassSpec(className, isInner)

    override fun createDataClassSpec(className: String, isInner: Boolean): JavaClassSpec =
        createClassSpec(className, isInner)

    override fun addLazyField(
        classSpec: JavaClassSpec, jsArray: JSONArray, objType: DataType.ObjectType
    ) {
        val typeMap = objType.dataTypeMap
        val key = objType.rawKey
        with(classSpec.classType) {
            val fieldName = key.fieldName
            val fieldType = ParameterizedTypeName.get(
                ClassName.get(List::class.java), ClassName.get(objType.pkgName, objType.className)
            )
            addField(fieldSpec(fieldName, fieldType) {
                addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                initializer("null")
            })
            addMethod(methodSpec(key.methodName) {
                addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                returns(fieldType)
                controlFlow("if($fieldName == null)") {
                    controlFlow("synchronized(${rootClassName}.class)") {
                        controlFlow("if($fieldName == null)") {
                            addStatement("$fieldName = new \$T<>()", ArrayList::class.java)
                            jsArray.mapNotNull { it as? JSONObject }.forEach {
                                addComment("value:$it")

                                val codeBlockBuilder = CodeBlock.builder()
                                addStatement(
                                    codeBlockBuilder.add(
                                        "$fieldName.add(new \$T(",
                                        ClassName.get(objType.pkgName, objType.className)
                                    ).add(createNewInstanceParam(it, typeMap, this)).add("))")
                                        .build()
                                )
                            }
                        }
                    }
                }
                addStatement("return $fieldName")

            })
        }
    }


    private fun getDataTypeTypeName(dataType: DataType) = when (dataType) {
        is DataType.ObjectType -> ClassName.get(dataType.pkgName, dataType.className)
        is DataType.BasisType -> dataType.type.typeName(box = true)
        is DataType.UnknownType -> ClassName.get(Object::class.java)
    }

    private fun getDataTypeDefValue(dataType: DataType) = if (dataType.isList) {
        "null"
    } else {
        when (dataType) {
            is DataType.ObjectType -> "null"
            is DataType.BasisType -> when (dataType.type) {
                ValueType.STRING -> "null"
                ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> "0"
                ValueType.LONG -> "0L"
                ValueType.FLOAT -> "0f"
            }

            is DataType.UnknownType -> "null"
        }
    }

    private fun createNewInstanceParam(
        jsObj: JSONObject, typeMap: Map<String, DataType>, methodSpecBuilder: MethodSpec.Builder
    ): CodeBlock {
        fun createParam(
            dataType: DataType, value: Any, methodSpecBuilder: MethodSpec.Builder
        ): CodeBlock {
            return when (dataType) {
                is DataType.ObjectType -> {
                    CodeBlock.builder()
                        .add("new \$T(", ClassName.get(dataType.pkgName, dataType.className)).add(
                            createNewInstanceParam(
                                value as JSONObject, dataType.dataTypeMap, methodSpecBuilder
                            )
                        ).add(")").build()
                }

                is DataType.BasisType -> {
                    when (dataType.type) {
                        ValueType.STRING -> CodeBlock.of(
                            "\$S", value
                        )


                        ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> CodeBlock.of(
                            "\$L", value
                        )

                        ValueType.LONG -> CodeBlock.of(
                            "\$L", "${value}L"
                        )

                        ValueType.FLOAT -> CodeBlock.of(
                            "\$L", "${value}f"
                        )

                    }
                }

                is DataType.UnknownType -> CodeBlock.of("null")
            }
        }

        val codeBlockList = mutableListOf<CodeBlock>()
        typeMap.forEach { (k, dataType) ->
            val value = jsObj[dataType.rawKey]
            if (value == null && dataType.canNull.not()) {
                error("$json in ${dataType.rawKey} is null, but canNull is false, please submit issue to fix it https://github.com/TLocation/ConfigAutoGen/issues")
            } else if (value == null) {
                codeBlockList.add(CodeBlock.of(getDataTypeDefValue(dataType)))
                return@forEach
            }


            codeBlockList.add(if (dataType.isList) {
                val tmpFieldName = "${k}List_${Random.nextInt(1000)}"
                methodSpecBuilder.addStatement(
                    "\$T $tmpFieldName = new \$T<>()", ParameterizedTypeName.get(
                        ClassName.get(List::class.java), getDataTypeTypeName(dataType)
                    ), ArrayList::class.java
                )
                val childArray =
                    value as? JSONArray ?: error("k:${dataType.rawKey} value is not JSONArray")

                childArray.filterNotNull().forEach { childItem ->
                    val builder = CodeBlock.builder()
                    builder.add("$tmpFieldName.add(")
                    builder.add(createParam(dataType, childItem, methodSpecBuilder))
                    builder.add(")")
                    methodSpecBuilder.addStatement(builder.build())
                }
                CodeBlock.of(tmpFieldName)
            } else {
                createParam(dataType, value, methodSpecBuilder)
            })
        }
        return CodeBlock.join(codeBlockList, ", ")
    }


    override fun addProperty(classSpec: JavaClassSpec, propertyMap: Map<String, DataType>) {
        with(classSpec.classType) {
            val constructor = MethodSpec.constructorBuilder().apply {
                addModifiers(Modifier.PUBLIC)
            }
            propertyMap.forEach { (key, value) ->
                val typeName: TypeName = when (value) {
                    is DataType.BasisType -> value.type.typeName(box = value.isList)
                    is DataType.ObjectType -> ClassName.get(value.pkgName, value.className)
                    is DataType.UnknownType -> ClassName.get(Object::class.java)
                }.let {
                    if (value.isList) ParameterizedTypeName.get(
                        ClassName.get(List::class.java), it
                    ) else it
                }
                addField(fieldSpec(key, typeName) {
                    addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                })
                constructor.addParameter(typeName, key)
                constructor.addStatement("this.${key} = $key")
            }
            addMethod(constructor.build())
        }
    }


    private fun ValueType.typeName(box: Boolean = false): TypeName {
        val typeName = when (this) {
            ValueType.STRING -> ClassName.get(String::class.java)
            ValueType.INT -> TypeName.INT
            ValueType.BOOLEAN -> TypeName.BOOLEAN
            ValueType.LONG -> TypeName.LONG
            ValueType.FLOAT -> TypeName.FLOAT
            ValueType.DOUBLE -> TypeName.DOUBLE
        }
        return if (box) typeName.box() else typeName
    }


    override fun addBasicArray(classSpec: JavaClassSpec, key: String, jsArray: JSONArray) {
        val rawClassSpec = classSpec.classType
        val field = fieldSpec(
            name = key.fieldName,
            type = ParameterizedTypeName.get(
                ClassName.get(List::class.java),
                jsArray.firstOrNull()?.valueType?.typeName(box = true)
                    ?: ClassName.get(Object::class.java)
            ),
        ) {
            addJavadoc("key:$key - value:$jsArray")
            addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
        }
        rawClassSpec.addField(field)
        rawClassSpec.addMethod(methodSpec(
            name = key.methodName + "List",
        ) {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            returns(
                field.type
            )

            controlFlow("if(${field.name} == null)") {
                controlFlow("synchronized(${rootClassName}.class)") {
                    controlFlow("if(${field.name} == null)") {
                        addStatement("${field.name} = new \$T<>()", ArrayList::class.java)
                        jsArray.forEach {
                            addComment("value:$it")
                            when (it!!.valueType) {
                                ValueType.STRING -> addStatement(
                                    "${field.name}.add(\$S)", it
                                )

                                ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> addStatement(
                                    "${field.name}.add(\$L)", it
                                )

                                ValueType.LONG -> addStatement(
                                    "${field.name}.add(\$L)", "${it}L"
                                )

                                ValueType.FLOAT -> addStatement(
                                    "${field.name}.add(\$L)", "${it}f"
                                )
                            }

                        }
                    }
                }
            }
            addStatement("return ${field.name}")
        })
    }

    override fun addStaticFiled(typeSpecBuilder: JavaClassSpec, key: String, v: Any) {

        val fieldSpec = FieldSpec.builder(
            v.valueType.type, key.fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
        ).addJavadoc("key:$key value:$v")
        when (v.valueType) {
            ValueType.STRING -> fieldSpec.initializer("\$S", v)
            ValueType.INT, ValueType.BOOLEAN, ValueType.LONG -> fieldSpec.initializer("\$L", v)
            ValueType.FLOAT -> fieldSpec.initializer("\$Lf", v)
            ValueType.DOUBLE -> fieldSpec.initializer("\$Ld", v)
        }
        typeSpecBuilder.classType.addField(fieldSpec.build())
    }

    override fun addStaticUnknownFiled(typeSpecBuilder: JavaClassSpec, key: String) {
        val fieldSpec = FieldSpec.builder(
            ClassName.get(Object::class.java),
            key.fieldName,
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL
        ).addJavadoc("key:$key value:null").initializer("null")
        typeSpecBuilder.classType.addField(fieldSpec.build())
    }
}