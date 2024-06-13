// config-merge plugin automatically generated, please do not modify
//    time:2024-06-13 19:17:38
//    author:tianxiaolong
//    Version:1.0.2
//    SourceCode:gitLab/RobotUI/config-merge
package com.location;

import com.location.UserManager.LikeList.LConfig;
import com.location.UserManager.LikeList.LConfig.User;
import com.location.UserManager.Uu.Config;
import com.location.UserManager.Uu.Ids;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * RawJson:{
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
 *     "null_list":null
 * }
 */
public final class UserManager {
	private static volatile List<Uu> uu = null;

	private static volatile List<LikeList> likelist = null;

	/**
	 * key:null_list value:null
	 */
	public static final Object nulllist = null;

	public static List<Uu> getUu() {
		if(uu == null) {
			synchronized(UserManager.class) {
				if(uu == null) {
					uu = new ArrayList<>();
					// value:{"name":"tom","age_str":null,"language":["java","kotlin","c++"],"time":1,"age":18}
					List<String> language508List = new ArrayList<>();
					language508List.add("java");
					language508List.add("kotlin");
					language508List.add("c++");
					uu.add(new Uu("tom", null, language508List, 1L, 18, null, null, null, null, null));
					// value:{"name":"jerry","ids":[{"u_id":1.2,"id":1},{"id_str":"1","id":1}],"age_str":"20","time":1718172620223,"config":{"id":1},"age":20}
					List<Ids> ids515List = new ArrayList<>();
					ids515List.add(new Ids(1.2f, 1, null));
					ids515List.add(new Ids(0f, 1, "1"));
					uu.add(new Uu("jerry", "20", null, 1718172620223L, 20, ids515List, new Config(1, 0), null, null, null));
					// value:{"longs":[],"ints":[1,2,3,4,5],"name":null,"ids":[{"id":1},{"id_str":"1","id":1}],"config":{"level":1},"user":null,"age":22}
					List<Ids> ids925List = new ArrayList<>();
					ids925List.add(new Ids(0f, 1, null));
					ids925List.add(new Ids(0f, 1, "1"));
					List<Object> longs963List = new ArrayList<>();
					List<Integer> ints483List = new ArrayList<>();
					ints483List.add(1);
					ints483List.add(2);
					ints483List.add(3);
					ints483List.add(4);
					ints483List.add(5);
					uu.add(new Uu(null, null, null, 0L, 22, ids925List, new Config(0, 1), longs963List, ints483List, null));
				}
			}
		}
		return uu;
	}

	public static List<LikeList> getLikeList() {
		if(likelist == null) {
			synchronized(UserManager.class) {
				if(likelist == null) {
					likelist = new ArrayList<>();
					// value:{"double_test":3.4028235E38,"float_test":12.0,"name":"tom","ids":[1,2,3,4,5],"id":1,"time":1718271131975,"l_config":{"id":1,"user":{"id":1}}}
					List<Integer> ids535List = new ArrayList<>();
					ids535List.add(1);
					ids535List.add(2);
					ids535List.add(3);
					ids535List.add(4);
					ids535List.add(5);
					likelist.add(new LikeList(3.4028235E38, 12.0f, "tom", ids535List, 1, 1718271131975L, new LConfig(1, new User(1))));
					// value:{"name":null,"l_config":{"id":2}}
					likelist.add(new LikeList(0, 0f, null, null, 0, 0L, new LConfig(2, null)));
					// value:{"ids":[]}
					List<Integer> ids87List = new ArrayList<>();
					likelist.add(new LikeList(0, 0f, null, ids87List, 0, 0L, null));
					// value:{}
					likelist.add(new LikeList(0, 0f, null, null, 0, 0L, null));
				}
			}
		}
		return likelist;
	}

	/**
	 * key:uu - value:[{"name":"tom","age_str":null,"language":["java","kotlin","c++"],"time":1,"age":18},{"name":"jerry","ids":[{"u_id":1.2,"id":1},{"id_str":"1","id":1}],"age_str":"20","time":1718172620223,"config":{"id":1},"age":20},{"longs":[],"ints":[1,2,3,4,5],"name":null,"ids":[{"id":1},{"id_str":"1","id":1}],"config":{"level":1},"user":null,"age":22}]
	 */
	public static final class Uu {
		public final String name;

		public final String agestr;

		public final List<String> language;

		public final long time;

		public final int age;

		public final List<Ids> ids;

		public final Config config;

		public final List<Object> longs;

		public final List<Integer> ints;

		public final Object user;

		public Uu(String name, String agestr, List<String> language, long time, int age, List<Ids> ids,
				  Config config, List<Object> longs, List<Integer> ints, Object user) {
			this.name = name;
			this.agestr = agestr;
			this.language = language;
			this.time = time;
			this.age = age;
			this.ids = ids;
			this.config = config;
			this.longs = longs;
			this.ints = ints;
			this.user = user;
		}

		/**
		 * key:ids - value:JsArrayType(type={u_id=JsArrayType(type=FLOAT, isNull=true, isList=false), id=JsArrayType(type=INT, isNull=false, isList=false), id_str=JsArrayType(type=STRING, isNull=true, isList=false)}, isNull=true, isList=true)
		 */
		public static final class Ids {
			public final float uid;

			public final int id;

			public final String idstr;

			public Ids(float uid, int id, String idstr) {
				this.uid = uid;
				this.id = id;
				this.idstr = idstr;
			}
		}

		/**
		 * key:config - value:JsArrayType(type={id=JsArrayType(type=INT, isNull=true, isList=false), level=JsArrayType(type=INT, isNull=true, isList=false)}, isNull=true, isList=false)
		 */
		public static final class Config {
			public final int id;

			public final int level;

			public Config(int id, int level) {
				this.id = id;
				this.level = level;
			}
		}
	}

	/**
	 * key:like_list - value:[{"double_test":3.4028235E38,"float_test":12.0,"name":"tom","ids":[1,2,3,4,5],"id":1,"time":1718271131975,"l_config":{"id":1,"user":{"id":1}}},{"name":null,"l_config":{"id":2}},{"ids":[]},{},null]
	 */
	public static final class LikeList {
		public final double doubletest;

		public final float floattest;

		public final String name;

		public final List<Integer> ids;

		public final int id;

		public final long time;

		public final LConfig lconfig;

		public LikeList(double doubletest, float floattest, String name, List<Integer> ids, int id,
						long time, LConfig lconfig) {
			this.doubletest = doubletest;
			this.floattest = floattest;
			this.name = name;
			this.ids = ids;
			this.id = id;
			this.time = time;
			this.lconfig = lconfig;
		}

		/**
		 * key:l_config - value:JsArrayType(type={id=JsArrayType(type=INT, isNull=false, isList=false), user=JsArrayType(type={id=JsArrayType(type=INT, isNull=false, isList=false)}, isNull=true, isList=false)}, isNull=true, isList=false)
		 */
		public static final class LConfig {
			public final int id;

			public final User user;

			public LConfig(int id, User user) {
				this.id = id;
				this.user = user;
			}

			/**
			 * key:user - value:JsArrayType(type={id=JsArrayType(type=INT, isNull=false, isList=false)}, isNull=true, isList=false)
			 */
			public static final class User {
				public final int id;

				public User(int id) {
					this.id = id;
				}
			}
		}
	}
}
