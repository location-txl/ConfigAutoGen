import com.google.common.truth.Truth.assertThat
import com.location.configgen.core.codeGen.JsArrayType
import com.location.configgen.core.datanode.ValueType
import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

/**
 *
 * @author tianxiaolong
 * time：2024/6/12 14:03
 * description：
 */
class ClassGenerateTest {
    companion object{
        private const val FILE_GENERATE_PATH = "build/testCodeGenerate"

        @JvmStatic
        @BeforeClass
        fun init(){
            val file = File(FILE_GENERATE_PATH)
            if(file.exists()){
                file.deleteRecursively()
            }
            file.mkdirs()
        }
    }



    val testArrayJson = """
        [
                        {
                            "age_str":null,
                            "name":"tom",
                            "age": 18,
                            "time": 1,
                            "language": [
                                "java",
                                "kotlin",
                                "c++"
                            ]
                        },
                        {
                            "age_str":"20",
                            "name": "jerry",
                            "age": 20,
                            "time": 1718172620223,
                            "config": {
                                "id": 1
                            },
                            "ids":[
                                {
                                
                                  "u_id":1.2,
                                  "id":1
                                },
                                {
                                  "id":1,
                                  "id_str":"1"
                                }
                            ]
                        },
                        {
                            "name": null,
                            "age": 22,
                            "config": {
                                "level": 1
                            },
                            "ids":[
                                {
                                  "id":1
                                },
                                {
                                  "id":1,
                                  "id_str":"1"
                                }
                            ],
                            "ints":[1,2,3,4,5],
                            "user":null,
                            "longs":[]
                        }
                    ]
    """.trimIndent()


    @Before
    fun start(){
        init()
    }

    @Test
    fun parseArrayTest(){
        println("parseArrayTest")
        val fileCreate = TestFileCreateInstance("", "", "", "")
        val json = testArrayJson
        val jsArray =  JSONParser().parse(json) as JSONArray
        val typeMap = fileCreate.parseJsArrayType(jsArray)

        val jsObject = GsonBuilder()
            .setPrettyPrinting()
            .create().toJson(typeMap)
        println(jsObject.toString())
        assertThat(typeMap["age_str"]).isEqualTo(
            JsArrayType(
                ValueType.STRING,
                canNull = true,
                isList = false
            )
        )
        assertThat(typeMap["name"]).isEqualTo(
            JsArrayType(
                ValueType.STRING,
                canNull = true,
                isList = false
            )
        )
        assertThat(typeMap["age"]).isEqualTo(
            JsArrayType(
                ValueType.INT,
                canNull = false,
                isList = false
            )
        )
        assertThat(typeMap["time"]).isEqualTo(
            JsArrayType(
                ValueType.LONG,
                canNull = true,
                isList = false
            )
        )
        assertThat(typeMap["language"]).isEqualTo(
            JsArrayType(
                ValueType.STRING,
                canNull = true,
                isList = true
            )
        )
        assertThat(typeMap["config"]).isEqualTo(JsArrayType(mapOf(
            "id" to JsArrayType(
            type = ValueType.INT,
                canNull = true,
            isList =  false,

                 ),
            "level" to JsArrayType(
                type = ValueType.INT,
                canNull = true,
                isList = false,
            )
        ),
            canNull = true,
            isList = false))
        assertThat(typeMap["ids"]).isEqualTo(
            JsArrayType(
                mapOf(
                    "u_id" to JsArrayType(ValueType.FLOAT, canNull = true, isList = false),
                    "id" to JsArrayType(ValueType.INT, canNull = false, isList = false),
                    "id_str" to JsArrayType(ValueType.STRING, canNull = true, isList = false),
                ),
                canNull = true,
                isList = true,
            )
        )
        assertThat(typeMap["ints"]).isEqualTo(
            JsArrayType(
                ValueType.INT,
                canNull = true,
                isList = true
            )
        )
        assertThat(typeMap["user"]).isEqualTo(JsArrayType(Unit, canNull = true, isList = false))
        assertThat(typeMap["longs"]).isEqualTo(JsArrayType(Unit, canNull = true, isList = true))

    }


    @Test
    fun testCodeGenerate(){
        //
        val fileCreate = TestFileCreateInstance("com.location", FILE_GENERATE_PATH, """
            { 
            "uu":${testArrayJson},
            "like_list":[
                     {
                         "ids":[1,2,3,4,5],
                         "name":"tom",
                         "id":1,
                         "float_test":12.0,
                         "time":1718271131975,
                         "double_test":3.4028235E38,
                         "l_config":{
                             "id":1,
                             "user":{
                                "id":1
                             }
                         }
                     }
                     ,
                      {
                          "l_config":{
                              "id":2
                          },
                          "name":null
                      },
                      {
                         "ids":[],
                      },
                      {
                      
                      },
                      null
                ],
                "null_list":null,
                "test_empty_list":[],
                "test_no_empty_list":[1,2,3,4,5,6,7,8],
            }
        """.trimIndent(), "UserManager")
        fileCreate.create()
    }
}