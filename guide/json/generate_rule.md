# 生成规则
插件会扫描config目录下的所有后缀为`json`的文件 文件名根据`_`进行生成大驼峰的类名

限制
- json根节点必须是一个对象
- 不可以数组嵌套数组

## Object

1. 支持自动推断类型
2. 当Object没有被嵌套在一个数组中时 其内的所有基础属性可以通过类直接访问
3. Object嵌套Object会生成一个内部类

举例 有如下json文件
```json
{
  "name": "test",
  "age": 18,
  "address": {
    "city": "beijing",
    "street": "haidian"
  }
}
```
会生成如下类
::: code-group
```kotlin 
public object Test{
    public const val name = "test"
    public const val age = 18
    public object Address{
        public const val city = "beijing"
        public const val street = "haidian"
    }
}
```
```java
public final class Test {
    public static final String name = "test";
    public static final int age = 18;
    public static final class Address {
        public static final String city = "beijing";
        public static final String street = "haidian";
    }
}

```
::: 

## Array
1. 数组里面可以是基础类型 也可以是Object
2. 为Object时会扫描所有Object的属性 生成一个数据类(Java为一个私有构造函数的类)
3. 数组的第一项不可以为 null
4. 数组的元素为Object时可以内部继续嵌套数组或者Object
5. 数组里面的类型必须一致 否则编译会报错

举例 有如下json文件
```json
{
  "basicList": [1,2,3,4,5],
  "emptyList": [],
  "object_list": [
    {
      "name": "test",
      "age": 18
    },
    {
      "name": "test2",
      "age": 19,
      "sex": "man"
    }
  ]
}
```

会生成如下类
::: code-group
```kotlin
public object Test {

  public val emptyList: List<Nothing> by lazy {
    listOf()
  }

  public val objectList: List<com.location.configgen.Test.ObjectList> by lazy {
    listOf(com.location.configgen.Test.ObjectList(name = "test", age = 18, sex = null),
        com.location.configgen.Test.ObjectList(name = "test2", age = 19, sex = "man"))

  }


  /**
   * key:basicList value:[1, 2, 3, 4, 5]
   */
  public val basicList: List<Int> by lazy {
    listOf(1, 2, 3, 4, 5)
  }

  public final data class ObjectList(
    public val name: String,
    public val age: Int,
    public val sex: String?,
  )
}
```
```java
public final class Test {

  private static volatile List<Object> emptyList;

  private static volatile List<ObjectList> objectList = null;

  private static volatile List<Integer> basicList;

  public static List<Object> getEmptyListList() {
    if(emptyList == null) {
      synchronized(Test.class) {
        if(emptyList == null) {
          emptyList = new ArrayList<>();
        }
      }
    }
    return emptyList;
  }

  public static List<ObjectList> getObjectList() {
    if(objectList == null) {
      synchronized(Test.class) {
        if(objectList == null) {
          objectList = new ArrayList<>();
          objectList.add(new ObjectList("test", 18, null));
          objectList.add(new ObjectList("test2", 19, "man"));
        }
      }
    }
    return objectList;
  }

  public static List<Integer> getBasicListList() {
    if(basicList == null) {
      synchronized(Test.class) {
        if(basicList == null) {
          basicList = new ArrayList<>();
          basicList.add(1);
          basicList.add(2);
          basicList.add(3);
          basicList.add(4);
          basicList.add(5);
        }
      }
    }
    return basicList;
  }

  public static final class ObjectList {
    public final String name;

    public final int age;

    public final String sex;

    public ObjectList(String name, int age, String sex) {
      this.name = name;
      this.age = age;
      this.sex = sex;
    }
  }
}

```
:::







