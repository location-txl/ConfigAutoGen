package com.location.configgen.core.config

import com.location.configgen.core.datanode.Node
import com.location.configgen.core.datanode.toNode
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File

interface ParseConfig {

    /**
     * 合并配置
     * @param pathList List<String> 配置文件路径 优先级从低到高
     * @return RawConfig  返回合并后的配置数据
     */
    fun mergeConfig(pathList: List<String>): RawConfig

    /**
     * 是否是有效的配置文件
     * @param fileName String 文件名
     * @return Boolean
     */
    fun isValidFile(fileName: String): Boolean

    /**
     * 解析配置为对象节点
     * @param config String
     * @return Node.ObjectNode
     */
    fun parseConfig(config: String): Node.ObjectNode
}

data class RawConfig(
    val content: String, val attribute: ConfigAttribute = ConfigAttribute.DEFAULT
)

data class ConfigAttribute(
    val className: String?
) {
    companion object {
        val DEFAULT = ConfigAttribute(null)
    }
}

val defaultParseConfig: ParseConfig
    get() = JsonSimpleParseConfig


object JsonSimpleParseConfig : ParseConfig {
    override fun mergeConfig(pathList: List<String>): RawConfig {
        val jsonParser = JSONParser()
        var className: String? = null
        var previousPath: String? = null
        val config: Any? = pathList.fold(null) { oldObj: Any?, path: String ->
            val (jsonStr, tmpClassName) = readJsonFile(path)
            val newObj = jsonParser.parse(jsonStr)
            try {
                return@fold if (oldObj == null) {
                    newObj
                } else {
                    require(oldObj.javaClass.simpleName == newObj.javaClass.simpleName) {
                        "json type not match s1 path:${path} type:${oldObj.javaClass.simpleName} " +
                                "s2 path:${previousPath} type:${newObj.javaClass.simpleName}"
                    }

                    if (oldObj is JSONObject) {
                        mergeJsObj(oldObj, newObj as JSONObject)
                    } else if (oldObj is JSONArray) {
                        mergeJsArray(oldObj, newObj as JSONArray)
                    }
                    oldObj
                }
            } finally {
                tmpClassName?.let { className = it }
                previousPath = path
            }
        }
        println("testConfig:$config")
        require(config != null)
        return RawConfig(
            content = config.toString(), attribute = ConfigAttribute(className)
        )
    }

    override fun isValidFile(fileName: String): Boolean = fileName.endsWith(".json")

    override fun parseConfig(config: String): Node.ObjectNode {
        val jsonParser = JSONParser()
        val jsonObj = jsonParser.parse(config) as? JSONObject
        require(jsonObj != null) {
            "json config only support root element is JsonObject"
        }
        return jsonObj.toNode()
    }


    private fun mergeJsObj(source: JSONObject, merge: JSONObject) {
        source.forEach { k, v ->
            if (v is JSONObject) {
                val mV = merge.remove(k) ?: return@forEach
                require(mV is JSONObject) {
                    "merge json fail $k type not match mergeType is ${mV.javaClass.simpleName}"
                }

                mergeJsObj(v, mV)
            } else if (v is JSONArray) {
                val mV = merge.remove(k) ?: return@forEach
                require(mV is JSONArray) {
                    "merge json fail $k type not match mergeType is ${mV.javaClass.simpleName}"
                }
                mergeJsArray(v, mV)
            }
        }
        source.putAll(merge)
    }

    private fun mergeJsArray(source: JSONArray, mergeValue: JSONArray) {
        source.clear()
        source.addAll(mergeValue)
    }

    private const val EXEGESIS = "//"
    private val classRegex = Regex(" *//@class *([a-zA-Z]+)")
    private const val STATE_PARSE_HEADER = 1
    private const val STATE_PARSE_CONTENT = 2

    private fun readJsonFile(path: String): Pair<String, String?> {
        val file = File(path)
        val sb = StringBuilder()
        val lineList = file.readLines(charset = Charsets.UTF_8)
        var className: String? = null

        var state = STATE_PARSE_HEADER
        for (line in lineList) {
            val checkLine = line.trimStart()
            val isEXEGESIS = checkLine.startsWith(EXEGESIS)
            if (isEXEGESIS.not() && state == STATE_PARSE_HEADER) {
                state = STATE_PARSE_CONTENT
            }
            when (state) {
                STATE_PARSE_HEADER -> {
                    classRegex.find(checkLine)?.let {
                        className = it.groupValues[1]
                    }
                }

                STATE_PARSE_CONTENT -> {
                    if (isEXEGESIS.not()) {
                        sb.append(line)
                    }
                }
            }
        }
        return sb.toString() to className
    }


}



