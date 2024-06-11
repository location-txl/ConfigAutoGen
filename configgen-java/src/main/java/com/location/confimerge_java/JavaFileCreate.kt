package com.location.confimerge_java

import com.location.configgen.core.codeGen.FileCreate
import com.location.configgen.core.codeGen.methodSpec
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.fieldName
import com.location.configgen.core.datanode.methodName
import com.location.configgen.core.datanode.valueType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.json.simple.JSONArray
import java.io.File
import javax.lang.model.element.Modifier

/**
 *
 * @author tianxiaolong
 * time：2024/6/11 16:00
 * description：
 */
class JavaFileCreate(packageName: String, outputDir: String, json: String, className: String) :
    FileCreate<JavaTypeSpec>(packageName, outputDir, json, className) {
    override fun writeFile(fileComment: String, classSpec: JavaTypeSpec) {
        val typeSpec = classSpec.build()
        val javaFile = JavaFile.builder(packageName, typeSpec as TypeSpec)
            .addFileComment(fileComment)
            .build()
        javaFile.writeTo(File(outputDir))
    }

    override fun createTypeSpecBuilder(className: String, isInner:Boolean): JavaTypeSpec = JavaTypeSpec(className, isInner)

    private fun Any.typeName(box:Boolean = false): TypeName {
        val typeName = when(valueType){
            ValueType.STRING ->  ClassName.get(String::class.java)
            ValueType.INT ->  TypeName.INT
            ValueType.BOOLEAN ->  TypeName.BOOLEAN
            ValueType.LONG ->  TypeName.LONG
            ValueType.FLOAT ->  TypeName.FLOAT
            ValueType.DOUBLE ->  TypeName.DOUBLE
        }
        return if(box) typeName.box() else typeName
    }



    override fun addNormalArray(typeSpecBuilder: JavaTypeSpec, key: String, jsArray: JSONArray) {
        val classSpec = typeSpecBuilder.classType
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
                            when (it!!.valueType) {
                                ValueType.STRING -> addStatement(
                                    "${field.name}.add(\$S)",
                                    it
                                )

                                ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> addStatement(
                                    "${field.name}.add(\$L)",
                                    it
                                )

                                ValueType.LONG -> addStatement(
                                    "${field.name}.add(\$L)",
                                    "${it}L"
                                )

                                ValueType.FLOAT -> addStatement(
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

    override fun addStaticFiled(typeSpecBuilder: JavaTypeSpec, key: String, v: Any) {

        val fieldSpec = FieldSpec.builder(
            v.valueType.type,
            key.fieldName,
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL
        )
            .addJavadoc("key:$key value:$v")
        when(v.valueType){
            ValueType.STRING -> fieldSpec.initializer("\$S", v)
            ValueType.INT,ValueType.BOOLEAN,ValueType.LONG -> fieldSpec.initializer("\$L", v)
            ValueType.FLOAT -> fieldSpec.initializer("\$Lf", v)
            ValueType.DOUBLE -> fieldSpec.initializer("\$Ld", v)
        }
        typeSpecBuilder.classType.addField(fieldSpec.build())
    }
}