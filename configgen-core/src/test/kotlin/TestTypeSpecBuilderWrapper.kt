import com.location.configgen.core.codeGen.TypeSpecBuilderWrapper

/**
 *
 * @author tianxiaolong
 * time：2024/6/12 14:05
 * description：
 */
class TestTypeSpecBuilderWrapper(className: String, isInner: Boolean) :
    TypeSpecBuilderWrapper(className, isInner) {
    override fun addJavaDoc(doc: String) {

    }

    override fun addType(typeSpec: Any) {
    }

    override fun build(): Any  = Unit
}