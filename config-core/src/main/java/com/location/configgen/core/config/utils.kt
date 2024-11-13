package com.location.configgen.core.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import org.json.simple.parser.JSONParser
import java.io.Reader

/**
 *
 * @author tianxiaolong
 * time：2024/6/21 16:37
 * description：
 */


private val reg = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
fun checkPropertyValid(key: String, prefix:String = "property"): String {
    if (!reg.matches(key)) {
        error("$prefix '$key' is incorrect naming  check reg:$reg")
    }
    return key
}


class CustomJsonReader(reader: Reader) : JsonReader(reader) {
    override fun nextString(): String {
        return super.nextString().also {
            println("nextStr:$it")
        }


    }

    override fun skipValue() {
        super.skipValue()
        println("skip value")
    }
}

fun main() {
    val gson = GsonBuilder()
        .create()
    val jsonWithComments = """
{
//dsadsadsa
  "name": "John", // This is a line comment
  "age": 30, /* This is a block comment */
  "city": "New York",
  "isStudent": false,
  "t_f":20.0
}
"""
    val jsObj = gson.fromJson(jsonWithComments, JsonObject::class.java)
    val map = jsObj.toMap()
    println("jsobj:$jsObj")
    println("jsMap:$map")


    val jsonParser = JSONParser()
    jsonParser.parse("11")


}

fun JsonObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    this.entrySet().forEach {
        map[it.key] = it.value.toAny()
    }
    return map
}

fun JsonElement.toAny(): Any {
    return when (this) {
        is JsonObject -> this.toMap()
        is com.google.gson.JsonArray -> this.toList()
        else -> this.asString
    }
}

fun JsonArray.toList(): List<Any> {
    val list = ArrayList<Any>(size())
    this.forEach {
        list.add(it.toAny())
    }
    return list.toList()
}