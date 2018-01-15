
import com.github.h0tk3y.liblinks.classes.Classpath
import com.github.h0tk3y.liblinks.classes.readClassInfo
import org.junit.Assert
import org.junit.Test
import java.io.File

class ClasspathTest {
    private fun testClasspath(files: List<File>) {
        val classpath = Classpath(files)
        val entries = classpath.classes.values
        entries.forEach { entry ->
            val classInfo = readClassInfo(entry.getInputStream())
            classInfo.methods.forEach { method -> Assert.assertEquals(entry.innerName, method.className) }
        }
    }

    @Test
    fun testSimpleJar() {
        val jar = File(javaClass.getResource("simpleProject.jar").file)
        testClasspath(listOf(jar))
    }

    @Test
    fun testSimpleDirectory() {
        val directory = File(javaClass.getResource("simpleProject.jar").file, "../classesDir")
        testClasspath(listOf(directory))
    }
}