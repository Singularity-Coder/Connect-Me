package com.singularitycoder.connectme.helpers

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.InputStream
import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager

// https://kotlinlang.org/docs/custom-script-deps-tutorial.html
// https://www.kodeco.com/books/kotlin-apprentice/v2.0/chapters/24-scripting-with-kotlin
// https://github.com/Kotlin/kotlin-script-examples

// https://www.tabnine.com/code/java/classes/javax.script.ScriptEngineManager
// https://stackoverflow.com/questions/7143343/is-there-a-java-equivalent-of-the-python-eval-function
// https://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
// FIXME It would be nice if we can dynamically construct views with code coming from GPT but this is not even printing to console. Too many unknowns. Even though everything is java, I dont think we can construct android views with this
object CodeExecutor {

    // https://stackoverflow.com/questions/15656530/is-it-possible-to-execute-a-java-code-string-at-runtime-in-android
    // https://stackoverflow.com/questions/4044044/how-does-the-java-compiler-find-classes-without-header-files
    fun executeCode(
        javaClassName: String,
        commandsList: List<String?>,
        headersList: List<String?> = emptyList(),
    ) {
        // build commands into new java file
        try {
            val fileWriter = FileWriter("$javaClassName.java")
            BufferedWriter(fileWriter).apply {
                this.write("")
                for (header in headersList) this.append(header)
                this.append("class $javaClassName { public static void main(String args[]) { ")
                for (cmd in commandsList) this.append(cmd)
                this.append(" } }")
                this.close()
            }
        } catch (e: Exception) {
            println("Error: " + e.message)
        }

        // set path, compile, & run
        try {
            Runtime.getRuntime().exec(
                arrayOf(
                    /* path */ "java -cp .", // FIXME where is this?
                    /* compile */ "javac $javaClassName.java", // Ex: "Example.java"
                    /* run */ "java $javaClassName" // Ex: "Example"
                )
            )
        } catch (e: Exception) {
            println("Error: " + e.message)
        }
    }

    // execute a DOS or unix command in the workflow of Java program execution
    // https://stackoverflow.com/questions/7143343/is-there-a-java-equivalent-of-the-python-eval-function
    fun executeCode2(
        command: String = "cmd /c dir /s",
        homeDir: String = "C:\\WINDOWS"
    ) = try {
        val process = Runtime.getRuntime().exec("$command $homeDir")

        // Process output
        val inputStream: InputStream = process.inputStream
        var ch: Int
        while (inputStream.read().also { ch = it } != -1) {
            print(ch.toChar())
        }
    } catch (_: Exception) {
    }

    // // https://docs.oracle.com/javase/6/docs/technotes/guides/scripting/programmer_guide/index.html
    fun evalScript(code: String = "print('Hello, World')") {
        val factory = ScriptEngineManager()
        val engine: ScriptEngine = factory.getEngineByName("JavaScript")
        engine.eval(code) // evaluate JavaScript code from String
    }

    // Does not support java but maybe we can construct websites from GPT and load them in webviews
    fun listAllSupportedScripts() {
        val manager = ScriptEngineManager()

        // Get the list of all available engines. This seems to be empty - https://stackoverflow.com/questions/42533123/wildfly-8-2-scriptenginemanager-getenginefactories-is-empty-on-server-startup
        val list: List<ScriptEngineFactory> = manager.engineFactories

        // Print the details of each engine
        for (f in list) {
            println("Engine Name: ${f.engineName}")
            println("Engine Version: ${f.engineVersion}")
            println("Language Name: ${f.languageName}")
            println("Language Version: ${f.languageVersion}")
            println("Engine Short Names: ${f.names}")
            println("Mime Types: ${f.mimeTypes}")
            println("----------------------------")
        }

        /**
        RESULT:
        Mozilla Rhino
        1.6 release 2
        ECMAScript
        1.6
        [js]
        [application/javascript, application/ecmascript, text/javascript, text/ecmascript]
        [js, rhino, JavaScript, javascript, ECMAScript, ecmascript]
         */
    }
}