// configAutoGen plugin automatically generated, please do not modify
// generateTime:2024-06-16 16:12:35
// Version:1.0.0
// SourceCode:https://github.com/TLocation/ConfigAutoGen
package com.location

import kotlin.Boolean
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Nothing
import kotlin.String
import kotlin.collections.List

/**
 * SourceJson:{
 * "uu":[
 *     {
 *         "age_str":null,
 *         "name":"tom",
 *         "age": 18,
 *         "time": 1,
 *         "language": [
 *             "java",
 *             "kotlin",
 *             "c++"
 *         ]
 *     },
 *     {
 *         "age_str":"20",
 *         "name": "jerry",
 *         "age": 20,
 *         "time": 1718172620223,
 *         "config": {
 *             "id": 1
 *         },
 *         "ids":[
 *             {
 *
 *               "u_id":1.2,
 *               "id":1
 *             },
 *             {
 *               "id":1,
 *               "id_str":"1"
 *             }
 *         ]
 *     },
 *     {
 *         "name": null,
 *         "age": 22,
 *         "config": {
 *             "level": 1
 *         },
 *         "ids":[
 *             {
 *               "id":1
 *             },
 *             {
 *               "id":1,
 *               "id_str":"1"
 *             }
 *         ],
 *         "ints":[1,2,3,4,5],
 *         "user":null,
 *         "longs":[]
 *     }
 * ],
 * "like_list":[
 *          {
 *              "ids":[1,2,3,4,5],
 *              "name":"tom",
 *              "id":1,
 *              "float_test":12.0,
 *              "time":1718271131975,
 *              "double_test":3.4028235E38,
 *              "l_config":{
 *                  "id":1,
 *                  "user":{
 *                     "id":1
 *                  }
 *              }
 *          }
 *          ,
 *           {
 *               "l_config":{
 *                   "id":2
 *               },
 *               "name":null
 *           },
 *           {
 *              "ids":[],
 *           },
 *           {
 *
 *           },
 *           null
 *     ],
 *     "null_list":null,
 *     "test_empty_list":[],
 *     "test_no_empty_list":[1,2,3,4,5,6,7,8],
 *     "id":1,
 *     "id_str":"1",
 *     "id_float":1.2,
 *     "id_boolean":true
 * }
 */
public object UserManager {
  /**
   * key:uu
   * value:[{"name":"tom","age_str":null,"language":["java","kotlin","c++"],"time":1,"age":18},{"name":"jerry","ids":[{"u_id":1.2,"id":1},{"id_str":"1","id":1}],"age_str":"20","time":1718172620223,"config":{"id":1},"age":20},{"longs":[],"ints":[1,2,3,4,5],"name":null,"ids":[{"id":1},{"id_str":"1","id":1}],"config":{"level":1},"user":null,"age":22}]
   */
  public val uu: List<com.location.UserManager.Uu> by lazy {
    val languageList843 = listOf("java", "kotlin", "c++")
    val idsList387 = listOf(com.location.UserManager.Uu.Ids(uId = 1.2f, id = 1, idStr = null),
      com.location.UserManager.Uu.Ids(uId = null, id = 1, idStr = "1"))
    val idsList321 = listOf(com.location.UserManager.Uu.Ids(uId = null, id = 1, idStr = null),
      com.location.UserManager.Uu.Ids(uId = null, id = 1, idStr = "1"))
    val intsList300 = listOf(1, 2, 3, 4, 5)
    listOf(com.location.UserManager.Uu(name = "tom", ageStr = null, language = languageList843,
      time = 1L, age = 18, ids = null, config = null, longs = null, ints = null, user = null),
      com.location.UserManager.Uu(name = "jerry", ageStr = "20", language = null, time =
      1718172620223L, age = 20, ids = idsList387, config =
      com.location.UserManager.Uu.Config(id = 1, level = null), longs = null, ints = null,
        user = null), com.location.UserManager.Uu(name = null, ageStr = null, language = null,
        time = null, age = 22, ids = idsList321, config = com.location.UserManager.Uu.Config(id
        = null, level = 1), longs = listOf(), ints = intsList300, user = null))

  }

  /**
   * key:id_boolean value:true
   */
  public const val idBoolean: Boolean = true

  /**
   * key:id_str value:1
   */
  public const val idStr: String = "1"

  /**
   * key:id value:1
   */
  public const val id: Int = 1

  /**
   * key:test_empty_list value:[]
   */
  public val testEmptyList: List<Nothing> by lazy {
    listOf()
  }

  /**
   * key:like_list
   * value:[{"double_test":3.4028235E38,"float_test":12.0,"name":"tom","ids":[1,2,3,4,5],"id":1,"time":1718271131975,"l_config":{"id":1,"user":{"id":1}}},{"name":null,"l_config":{"id":2}},{"ids":[]},{},null]
   */
  public val likeList: List<com.location.UserManager.LikeList> by lazy {
    val idsList643 = listOf(1, 2, 3, 4, 5)
    listOf(com.location.UserManager.LikeList(doubleTest = 3.4028235E38, floatTest = 12.0f, name
    = "tom", ids = idsList643, id = 1, time = 1718271131975L, lConfig =
    com.location.UserManager.LikeList.LConfig(id = 1, user =
    com.location.UserManager.LikeList.LConfig.User(id = 1))),
      com.location.UserManager.LikeList(doubleTest = null, floatTest = null, name = null, ids
      = null, id = null, time = null, lConfig = com.location.UserManager.LikeList.LConfig(id =
      2, user = null)), com.location.UserManager.LikeList(doubleTest = null, floatTest = null,
        name = null, ids = listOf(), id = null, time = null, lConfig = null),
      com.location.UserManager.LikeList(doubleTest = null, floatTest = null, name = null, ids
      = null, id = null, time = null, lConfig = null))

  }

  /**
   * key:null_list value:null
   */
  public val nullList: Nothing? = null

  /**
   * key:test_no_empty_list value:[1,2,3,4,5,6,7,8]
   */
  public val testNoEmptyList: List<Int> by lazy {
    listOf(1, 2, 3, 4, 5, 6, 7, 8)
  }

  /**
   * key:id_float value:1.2
   */
  public const val idFloat: Float = 1.2f

  /**
   * key:uu -
   * value:[{"name":"tom","age_str":null,"language":["java","kotlin","c++"],"time":1,"age":18},{"name":"jerry","ids":[{"u_id":1.2,"id":1},{"id_str":"1","id":1}],"age_str":"20","time":1718172620223,"config":{"id":1},"age":20},{"longs":[],"ints":[1,2,3,4,5],"name":null,"ids":[{"id":1},{"id_str":"1","id":1}],"config":{"level":1},"user":null,"age":22}]
   */
  public final data class Uu(
    public val name: String?,
    public val ageStr: String?,
    public val language: List<String>?,
    public val time: Long?,
    public val age: Int,
    public val ids: List<com.location.UserManager.Uu.Ids>?,
    public val config: com.location.UserManager.Uu.Config?,
    public val longs: List<Nothing>?,
    public val ints: List<Int>?,
    public val user: Nothing?,
  ) {
    /**
     * key:ids - value:JsArrayType(type={u_id=JsArrayType(type=FLOAT, canNull=true, isList=false),
     * id=JsArrayType(type=INT, canNull=false, isList=false), id_str=JsArrayType(type=STRING,
     * canNull=true, isList=false)}, canNull=true, isList=true)
     */
    public final data class Ids(
      public val uId: Float?,
      public val id: Int,
      public val idStr: String?,
    )

    /**
     * key:config - value:JsArrayType(type={id=JsArrayType(type=INT, canNull=true, isList=false),
     * level=JsArrayType(type=INT, canNull=true, isList=false)}, canNull=true, isList=false)
     */
    public final data class Config(
      public val id: Int?,
      public val level: Int?,
    )
  }

  /**
   * key:like_list -
   * value:[{"double_test":3.4028235E38,"float_test":12.0,"name":"tom","ids":[1,2,3,4,5],"id":1,"time":1718271131975,"l_config":{"id":1,"user":{"id":1}}},{"name":null,"l_config":{"id":2}},{"ids":[]},{},null]
   */
  public final data class LikeList(
    public val doubleTest: Double?,
    public val floatTest: Float?,
    public val name: String?,
    public val ids: List<Int>?,
    public val id: Int?,
    public val time: Long?,
    public val lConfig: com.location.UserManager.LikeList.LConfig?,
  ) {
    /**
     * key:l_config - value:JsArrayType(type={id=JsArrayType(type=INT, canNull=false, isList=false),
     * user=JsArrayType(type={id=JsArrayType(type=INT, canNull=false, isList=false)}, canNull=true,
     * isList=false)}, canNull=true, isList=false)
     */
    public final data class LConfig(
      public val id: Int,
      public val user: com.location.UserManager.LikeList.LConfig.User?,
    ) {
      /**
       * key:user - value:JsArrayType(type={id=JsArrayType(type=INT, canNull=false, isList=false)},
       * canNull=true, isList=false)
       */
      public final data class User(
        public val id: Int,
      )
    }
  }
}
