import com.github.h0tk3y.liblinks.classes.Classpath
import com.github.h0tk3y.liblinks.classes.readCalls
import org.junit.Assert
import org.junit.Test
import java.io.File

class CallsTest {
    @Test
    fun testSimple() {
        val jar = File(javaClass.getResource("simpleProject.jar").file)
        val classpath = Classpath(listOf(jar))
        val calls = readCalls(classpath)
            .filter { it.methodInfo.className.startsWith("demo/") }
            .map { it.methodInfo.run { "${className}:${name}:${descriptor}" } }
            .toSet()

        val expectedMethods = setOf(
            "demo/Greeter:<init>:(Ljava/lang/String;)V",
            "demo/KotlinGreetingJoiner:<init>:(Ldemo/Greeter;)V",
            "demo/KotlinGreetingJoiner:addName:(Ljava/lang/String;)V",
            "demo/KotlinGreetingJoiner:getJoinedGreeting:()Ljava/lang/String;",
            "demo/AB:a:()V",
            "demo/AB:<init>:()V",
            "demo/AskerKt:abc:(I)V",
            "demo/HelloWorldKt:someFun:()I",
            "demo/Greeter:getGreeting:()Ljava/lang/String;")

        Assert.assertEquals(expectedMethods, calls)
    }
}