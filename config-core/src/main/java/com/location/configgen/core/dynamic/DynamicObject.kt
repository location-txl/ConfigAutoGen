package com.location.configgen.core.dynamic

import com.location.configgen.core.config.checkPropertyValid
import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.ValueType
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random
import kotlin.random.nextUInt

/**
 *
 * @author tianxiaolong
 * time：2024/6/19 16:24
 * description：
 * 在 gradle 中动态配置结构体
 */
@CustomObjectScopeMarker
class DynamicObject(val name: String, private val project: Project) : DynamicObjectApi {
    private val id = Random.nextUInt()

    private val propertyMap = mutableMapOf<String, Node?>()

    private val flavorLazyMap = mutableMapOf<String, DynamicObjectApi.() -> Unit>()



    fun getObjectNode(flavorList: List<String>): Node.ObjectNode? {
        val tmpPropertyMap = propertyMap.toMutableMap()
        flavorList.forEach {
            val tempObject = DynamicObject(name + Random.nextUInt(), project)
            flavorLazyMap[it]?.let { lazyDynamicAction ->
                lazyDynamicAction(tempObject)
            }
            val flavorProperty = tempObject.getObjectNode(listOf())?.property
            if(flavorProperty != null){
                mergeProperty(tmpPropertyMap, flavorProperty)
            }
        }
        return if(tmpPropertyMap.isEmpty()) null else Node.ObjectNode(tmpPropertyMap.toMap(), docs = "")
    }

    private fun mergeProperty(srcMap: MutableMap<String, Node?>, mergeMap: Map<String, Node?>) {
        val replaceMap = mergeMap.filter { tmpEntity ->
            tmpEntity.value is Node.ValueNode || tmpEntity.value is Node.ListNode
        }
        val objectMap = mergeMap.filter { tmpEntity -> tmpEntity.value is Node.ObjectNode }
        srcMap.putAll(replaceMap)
        objectMap.forEach { (k, node) ->
            if(srcMap.containsKey(k)){
                val (map,doc) = (srcMap[k] as? Node.ObjectNode) ?:error("custom object merge error '$k' wwo instances of different types.")
                val tempSrcMap = map.toMutableMap()
                mergeProperty(tempSrcMap, (node as Node.ObjectNode).property)
                srcMap[k] = Node.ObjectNode(tempSrcMap, doc)
            }else{
                srcMap[k] = node
            }
        }
    }

    private fun checkPropertyKey(key: String) {
        checkPropertyValid(key)
        if (propertyMap.containsKey(key)) {
            error("property $key already exists")
        }
    }

    private fun addProperty(name: String, value: Any, type: ValueType) {
        checkPropertyKey(name)
        println("addProperty${this.hashCode()} name = $name, value = $value")
        propertyMap[name] = Node.ValueNode(value, docs = "", type = type)
    }

    override fun addProperty(name: String, value: String) {
        addProperty(name, value, ValueType.STRING)
    }

    override fun addProperty(name: String, value: Int) {
        addProperty(name, value, ValueType.INT)
    }

    override fun addProperty(name: String, value: Long) {
        addProperty(name, value, ValueType.LONG)
    }

    override fun addProperty(name: String, value: Float) {
        addProperty(name, value, ValueType.FLOAT)
    }

    override fun addProperty(name: String, value: Double) {
        addProperty(name, value, ValueType.DOUBLE)
    }

    override fun addProperty(name: String, value: Boolean) {
        addProperty(name, value, ValueType.BOOLEAN)
    }

    @OptIn(ExperimentalTypeInference::class)
    override fun <T> addListProperty(
        name: String,
        @BuilderInference action: CustomPropertyScope<T>.() -> Unit
    ) {
        checkPropertyKey(name)
        val list = mutableListOf<Node.ValueNode>()
        val customPropertyScope = object : CustomPropertyScope<T> {
            override fun add(value: T) {
                if (value != null) {
                    if (list.isNotEmpty() && value!!::class.java != list.first().value::class.java) {
                        error("list type must be same")
                    }
                    list.add(Node.ValueNode(value, docs = ""))
                }
            }
        }
        customPropertyScope.action()
        propertyMap[name] = Node.ListNode(list.toList(), docs = "")
        println("addListProperty:$list")
    }

    override fun addListObject(name: String, action: CustomListObjectScope.() -> Unit) {
        checkPropertyKey(name)
        val list = mutableListOf<Node.ObjectNode>()
        val customListObjectScope = object : CustomListObjectScope {
            override fun add(action: DynamicObjectApi.() -> Unit) {
                val subDynamic = DynamicObject(name, project = this@DynamicObject.project)
                subDynamic.action()
                //ListObject 不支持 flavor
                val subNode = subDynamic.getObjectNode(listOf())
                if(subNode != null){
                    list.add(subNode)
                }
            }
        }
        customListObjectScope.action()
        propertyMap[name] = Node.ListNode(list.toList(), docs = "")
    }

    override fun addObject(name: String, action: DynamicObjectApi.() -> Unit) {
        checkPropertyKey(name)
        val subObject = DynamicObject(name, project)
        subObject.action()
        propertyMap[name] = subObject.getObjectNode(listOf())
    }


    fun withFlavor(key: String, action: @CustomObjectScopeMarker DynamicObjectApi.() -> Unit) {
        if(flavorLazyMap.containsKey(key)){
           error("withFlavor $key already exists")
        }
        flavorLazyMap[key] = action
    }

    class Factory(private val project: Project) : NamedDomainObjectFactory<DynamicObject> {
        override fun create(name: String): DynamicObject = DynamicObject(name, project)
    }


    fun methodMissing(name: String, args: Any): Any {
        println("invokeMethod methodName:$name")
        return ""
    }

}

