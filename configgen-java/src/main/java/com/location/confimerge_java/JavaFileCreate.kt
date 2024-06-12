package com.location.confimerge_java

import com.location.configgen.core.codeGen.DataType
import com.location.configgen.core.codeGen.FileCreate
import com.location.configgen.core.codeGen.JsArrayType
import com.location.configgen.core.codeGen.methodSpec
import com.location.configgen.core.datanode.ValueType
import com.location.configgen.core.datanode.fieldName
import com.location.configgen.core.datanode.methodName
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
class JavaFileCreate(packageName: String, outputDir: String, json: String, className: String) :
    FileCreate<JavaTypeSpec>(packageName, outputDir, json, className) {
        companion object{
            @VisibleForTesting var inTest = false
        }
    override fun writeFile(fileComment: String, classSpec: JavaTypeSpec) {
        val typeSpec = classSpec.build()
        val javaFile = JavaFile.builder(packageName, typeSpec as TypeSpec)
            .addFileComment(fileComment)
            .build()
        javaFile.writeTo(File(outputDir))

        if(inTest){
            javaFile.writeTo(System.out)
        }
    }

    override fun createTypeSpecBuilder(className: String, isInner:Boolean): JavaTypeSpec = JavaTypeSpec(className, isInner)
    override fun createDataTypeSpecBuilder(className: String, isInner: Boolean): JavaTypeSpec  = createTypeSpecBuilder(className, isInner)
    override fun addLazyField(
        typeSpecBuilder: JavaTypeSpec,
        key: String,
        jsArray: JSONArray,
        objTypeSpec: JavaTypeSpec,
        typeMap: Map<String, DataType>,
        objType:DataType.ObjectType
    ) {
       with(typeSpecBuilder.classType){
           val fieldName = key.fieldName
           val fieldType = ParameterizedTypeName.get(ClassName.get(List::class.java), ClassName.get(objType.pkgName, objType.className))
           addField(fieldSpec(fieldName, fieldType){
               addModifiers(Modifier.PRIVATE, Modifier.STATIC)
               initializer("null")
           })
           addMethod(methodSpec(key.methodName){
               addModifiers(Modifier.PUBLIC, Modifier.STATIC)
               returns(fieldType)
               controlFlow("if($fieldName == null)"){
                   controlFlow("synchronized(${className}.class)"){
                       controlFlow("if($fieldName == null)"){
                           addStatement("$fieldName = new \$T<>()", ArrayList::class.java)
                           jsArray.map { it as JSONObject }.forEach {
                                addComment("value:$it")
//                                addStatement("$fieldName.add(new \$T(${createNewInstanceParam(it, typeMap, this)}))", ClassName.get(objType.pkgName, objType.className))
                               val codeBlockBuilder = CodeBlock.builder()
                               addStatement(
                                   codeBlockBuilder
                                       .add(
                                           "$fieldName.add(new \$T(",
                                           ClassName.get(objType.pkgName, objType.className)
                                       )
                                       .add(createNewInstanceParam(it, typeMap, this))
                                       .add(")")
                                       .build()
                               )
                           }
                       }
                   }
               }

           })
       }
    }

    private fun createNewInstanceParam(jsObj: JSONObject, typeMap: Map<String, DataType>, methodSpecBuilder: MethodSpec.Builder): CodeBlock {
        val codeBlockList = mutableListOf<CodeBlock>()
        typeMap.forEach { (k, v) ->
//            if(v.isList){
//                methodSpecBuilder.addStatement("\$T ${k}${Random.nextInt(1000)}List = new \$T<>()", v., ArrayList::class.java)
//            }else{
//
//            }
            when(v){
                is DataType.ObjectType -> {
                    builder.append("new \$T(${createNewInstanceParam(jsObj[v.rawKey] as JSONObject, v.dataTypeMap, methodSpecBuilder)}),")
                }
                is DataType.BasisType -> {

                    when(v.type){
                        ValueType.STRING -> builder.append("\"${jsObj[v.rawKey]}\",")
                        ValueType.INT, ValueType.BOOLEAN, ValueType.DOUBLE -> builder.append("${jsObj[v.rawKey]},")
                        ValueType.LONG -> builder.append("${jsObj[v.rawKey]}L,")
                        ValueType.FLOAT -> builder.append("${jsObj[v.rawKey]}f,")
                    }
                }
                is DataType.UnknownType -> builder.append("null,")
            }


        }
        return CodeBlock.join()
    }

    override fun addProperty(typeSpecBuilder: JavaTypeSpec, propertyMap: Map<String, DataType>) {
           with(typeSpecBuilder.classType){
               val constructor = MethodSpec.constructorBuilder().apply {
                   addModifiers(Modifier.PUBLIC)
               }
               propertyMap.forEach { (key, value) ->
                   val typeName:TypeName  = when(value){
                          is DataType.BasisType -> value.type.typeName(box = value.isList || value.canNull)
                          is DataType.ObjectType -> ClassName.get(value.pkgName, value.className)
                            is DataType.UnknownType -> ClassName.get(Object::class.java)
                   }.let {
                       if(value.isList) ParameterizedTypeName.get(ClassName.get(List::class.java), it) else it
                   }
                   addField(fieldSpec(key, typeName){
                       addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                   })
                   constructor.addParameter(typeName, key)
                   constructor.addStatement("this.${key} = $key")
               }
               addMethod(constructor.build())
           }
    }


    private fun ValueType.typeName(box:Boolean = false): TypeName {
        val typeName = when(this){
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
                jsArray[0]!!.valueType.typeName(box = true)
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