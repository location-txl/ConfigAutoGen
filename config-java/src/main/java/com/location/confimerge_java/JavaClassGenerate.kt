package com.location.confimerge_java

import com.location.configgen.core.codeGen.DataType
import com.location.configgen.core.codeGen.ClassGenerate
import com.location.configgen.core.codeGen.fieldName
import com.location.configgen.core.codeGen.methodName
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.defJavaOptions
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.internal.impldep.org.jetbrains.annotations.VisibleForTesting
import java.io.File
import java.util.Collections
import javax.lang.model.element.Modifier
import kotlin.random.Random

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class JavaClassGenerate(
    packageName: String,
    outputDir: String,
    rootNode: Node.ObjectNode,
    className: String
) :
    ClassGenerate<JavaClassSpec>(packageName, outputDir, rootNode, className) {
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
        classSpec: JavaClassSpec, listNode: Node.ListNode, objType: DataType.ObjectType
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
                returns(
                    fieldType.annotated(
                        AnnotationSpec.builder(
                            ClassName.get(
                                defJavaOptions.nullSafeAnnotation.packageName,
                                defJavaOptions.nullSafeAnnotation.notNull
                            )
                        ).build()
                    )
                )
                controlFlow("if($fieldName == null)") {
                    controlFlow("synchronized(${rootClassName}.class)") {
                        controlFlow("if($fieldName == null)") {
                            val objList = listNode.mapNotNull { it as? Node.ObjectNode }
                            val tmpFieldName = fieldName.listRandomName
                            addStatement(
                                "\$T $tmpFieldName = new \$T<>(\$L)",
                                fieldType,
                                ArrayList::class.java,
                                objList.size
                            )
                            objList.forEach {
                                addComment("value:$it")

                                val codeBlockBuilder = CodeBlock.builder()
                                addStatement(
                                    codeBlockBuilder.add(
                                        "$tmpFieldName.add(new \$T(",
                                        ClassName.get(objType.pkgName, objType.className)
                                    ).add(createNewInstanceParam(it, typeMap, this)).add("))")
                                        .build()
                                )
                            }
                            addStatement(
                                "$fieldName = \$T.unmodifiableList($tmpFieldName)", ClassName.get(
                                    Collections::class.java
                                )
                            )
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

    private val String.listRandomName: String
        get() = "${this}_${Random.nextInt(1000)}"

    private fun createNewInstanceParam(
        objNode: Node.ObjectNode,
        typeMap: Map<String, DataType>,
        methodSpecBuilder: MethodSpec.Builder
    ): CodeBlock {
        fun createParam(
            dataType: DataType, value: Node, methodSpecBuilder: MethodSpec.Builder
        ): CodeBlock {
            return when (dataType) {
                is DataType.ObjectType -> {
                    CodeBlock.builder()
                        .add("new \$T(", ClassName.get(dataType.pkgName, dataType.className)).add(
                            createNewInstanceParam(
                                value as Node.ObjectNode, dataType.dataTypeMap, methodSpecBuilder
                            )
                        ).add(")").build()
                }

                is DataType.BasisType -> {
                    val v = (value as Node.ValueNode).value
                    when (dataType.type) {
                        ValueType.STRING -> CodeBlock.of(
                            "\$S", v
                        )


                        ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> CodeBlock.of(
                            "\$L", v
                        )

                        ValueType.LONG -> CodeBlock.of(
                            "\$L", "${v}L"
                        )

                        ValueType.FLOAT -> CodeBlock.of(
                            "\$L", "${v}f"
                        )

                    }
                }

                is DataType.UnknownType -> CodeBlock.of("null")
            }
        }

        val codeBlockList = mutableListOf<CodeBlock>()
        typeMap.forEach { (k, dataType) ->
            val value = objNode[dataType.rawKey]
            if (value == null && dataType.canNull.not()) {
                error("${objNode.docs} in ${dataType.rawKey} is null, but canNull is false, please submit issue to fix it https://github.com/TLocation/ConfigAutoGen/issues")
            } else if (value == null) {
                codeBlockList.add(CodeBlock.of(getDataTypeDefValue(dataType)))
                return@forEach
            }


            codeBlockList.add(if (dataType.isList) {
                val tmpFieldName = k.listRandomName
                val childArray =
                    (value as? Node.ListNode)?.filterNotNull()
                        ?: error("k:${dataType.rawKey} value is not JSONArray")
                methodSpecBuilder.addStatement(
                    "\$T $tmpFieldName = new \$T<>(\$L)",
                    ParameterizedTypeName.get(
                        ClassName.get(List::class.java), getDataTypeTypeName(dataType)
                    ),
                    ArrayList::class.java, childArray.size,
                )


                childArray.forEach { childItem ->
                    val builder = CodeBlock.builder()
                    builder.add("$tmpFieldName.add(")
                    builder.add(createParam(dataType, childItem, methodSpecBuilder))
                    builder.add(")")
                    methodSpecBuilder.addStatement(builder.build())
                }
                CodeBlock.of(
                    "\$T.unmodifiableList($tmpFieldName)",
                    ClassName.get(Collections::class.java)
                )
            } else {
                createParam(dataType, value, methodSpecBuilder)
            })
        }
        return CodeBlock.join(codeBlockList, ", ")
    }

    private val DataType.canNullSafe: Boolean
        get() = this.isList || (this as? DataType.BasisType)?.type.let { t -> t == ValueType.STRING || t == null }


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
                    if (
                        defJavaOptions.nullSafe
                        && value.canNullSafe
                    ) {
                        if (value.canNull) {
                            addAnnotation(
                                ClassName.get(
                                    defJavaOptions.nullSafeAnnotation.packageName,
                                    defJavaOptions.nullSafeAnnotation.nullable
                                )
                            )
                        } else {
                            addAnnotation(
                                ClassName.get(
                                    defJavaOptions.nullSafeAnnotation.packageName,
                                    defJavaOptions.nullSafeAnnotation.notNull
                                )
                            )
                        }
                    }


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


    override fun addBasicArray(classSpec: JavaClassSpec, key: String, list: List<Node.ValueNode?>) {
        val rawClassSpec = classSpec.classType
        val field = fieldSpec(
            name = key.fieldName,
            type = ParameterizedTypeName.get(
                ClassName.get(List::class.java),
                list.firstOrNull()?.valueType?.typeName(box = true)
                    ?: ClassName.get(Object::class.java)
            ),
        ) {
            addJavadoc("key:$key - value:$list")
            addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
        }
        rawClassSpec.addField(field)
        rawClassSpec.addMethod(methodSpec(
            name = key.methodName + "List",
        ) {
            addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            returns(
                field.type.annotated(
                    AnnotationSpec.builder(
                        ClassName.get(
                            defJavaOptions.nullSafeAnnotation.packageName,
                            defJavaOptions.nullSafeAnnotation.notNull
                        )
                    ).build()
                )
            )

            controlFlow("if(${field.name} == null)") {
                controlFlow("synchronized(${rootClassName}.class)") {
                    controlFlow("if(${field.name} == null)") {
                        val tempKey = "tmpList"
                        addStatement(
                            "\$T $tempKey = new \$T<>(\$L)",
                            field.type,
                            ArrayList::class.java,
                            list.size
                        )
                        list.forEach {
                            addComment("value:$it")
                            when (it!!.valueType) {
                                ValueType.STRING -> addStatement(
                                    "${tempKey}.add(\$S)", it
                                )

                                ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> addStatement(
                                    "${tempKey}.add(\$L)", it
                                )

                                ValueType.LONG -> addStatement(
                                    "${tempKey}.add(\$L)", "${it}L"
                                )

                                ValueType.FLOAT -> addStatement(
                                    "${tempKey}.add(\$L)", "${it}f"
                                )
                            }

                        }
                        addStatement(
                            "${field.name} = \$T.unmodifiableList($tempKey)", ClassName.get(
                                Collections::class.java
                            )
                        )
                    }
                }
            }
            addStatement("return ${field.name}")
        })
    }

    //TODO 确认生成数据是否有问题
    override fun addStaticFiled(typeSpecBuilder: JavaClassSpec, key: String, v: Node.ValueNode) {

        val fieldSpec = FieldSpec.builder(
            v.valueType.type, key.fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
        )
            .addJavadoc("key:$key value:$v")
        when (v.valueType) {
            ValueType.STRING -> fieldSpec.initializer("\$S", v)
            ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> fieldSpec.initializer("\$L", v)
            ValueType.FLOAT -> fieldSpec.initializer("\$Lf", v)
            ValueType.LONG -> fieldSpec.initializer("\$LL", v)
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
        if (defJavaOptions.nullSafe) {
            fieldSpec.addAnnotation(
                ClassName.get(
                    defJavaOptions.nullSafeAnnotation.packageName,
                    defJavaOptions.nullSafeAnnotation.nullable
                )
            )
        }
        typeSpecBuilder.classType.addField(fieldSpec.build())
    }
}