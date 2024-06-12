import com.location.configgen.core.codeGen.FileCreate
import org.json.simple.JSONArray

/**
 *
 * @author tianxiaolong
 * time：2024/6/12 14:06
 * description：
 */
class TestFileCreateInstance(
    packageName: String,
    outputDir: String,
    json: String,
    className: String,
    unstableArray: Boolean = true
) : FileCreate<TestTypeSpecBuilderWrapper>(packageName, outputDir, json, className, unstableArray) {
    override fun writeFile(fileComment: String, classSpec: TestTypeSpecBuilderWrapper) {

    }

    override fun createTypeSpecBuilder(
        className: String,
        isInner: Boolean
    ): TestTypeSpecBuilderWrapper  = TestTypeSpecBuilderWrapper(className, isInner)

    override fun addStaticFiled(typeSpecBuilder: TestTypeSpecBuilderWrapper, key: String, v: Any) {
    }

    override fun addNormalArray(
        typeSpecBuilder: TestTypeSpecBuilderWrapper,
        key: String,
        jsArray: JSONArray
    ) {
    }
}