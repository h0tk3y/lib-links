
import com.github.h0tk3y.liblinks.classes.Classpath
import com.github.h0tk3y.liblinks.classes.Lookup
import com.github.h0tk3y.liblinks.classes.MethodInfo
import org.junit.Assert
import org.junit.Test
import java.io.File

class LookupTest {
    val jar = File(javaClass.getResource("simpleProject.jar").file)
    val lookup = Lookup(Classpath(listOf(jar)))

    @Test
    fun testBasicLookup() {
        Assert.assertEquals(
            setOf("demo/A", "demo/AB", "demo/Iface"),
            lookup.getHierarchyOfClass("demo/A").map { it.className }.toSet())
    }

    @Test
    fun testJavaLangCall() {
        Assert.assertTrue(lookup.hasMethod(MethodInfo("java/lang/Object", "equals", "(Ljava/lang/Object;)Z")))
    }

    @Test
    fun testCheckCall() {
        val selfCall = MethodInfo("demo/A", "a", "()V")
        val ifaceCall = MethodInfo("demo/A", "ifaceFun", "()D")
        val superClassCall = MethodInfo("demo/A", "b", "()V")

        listOf(selfCall, ifaceCall, superClassCall).forEach {
            Assert.assertTrue(lookup.hasMethod(it))
        }

        val unknownCall = MethodInfo("demo/A", "x", "()I")
        Assert.assertFalse(lookup.hasMethod(unknownCall))
    }
}