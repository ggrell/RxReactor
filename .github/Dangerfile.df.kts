@file:DependsOn("io.github.ackeecz:danger-kotlin-lint:2.0.0")
@file:DependsOn("io.github.ackeecz:danger-kotlin-testing:1.0.2")
@file:DependsOn("com.gianluz:danger-kotlin-android-lint-plugin:0.1.0")

import io.github.ackeecz.danger.lint.BuildFoldersMatcher
import io.github.ackeecz.danger.lint.DetektConfig
import io.github.ackeecz.danger.lint.LintPlugin
import io.github.ackeecz.danger.testing.JUnitConfig
import io.github.ackeecz.danger.testing.TestingPlugin
import com.gianluz.dangerkotlin.androidlint.AndroidLint
import systems.danger.kotlin.*
import java.io.File

danger(args) {

    // ── Detekt: inline comments on changed files ──────────────────────────
    register plugin LintPlugin
    LintPlugin.findAndProcessDetektReports(
        DetektConfig(
            discovery = DetektConfig.Discovery(
                buildFoldersMatcher = BuildFoldersMatcher.All
            )
        )
    )

    // ── Android Lint: inline comments for all 4 Android modules ───────────
    register plugin AndroidLint
    // Walk tree for lint XML reports; AGP 8.x names them lint-results-<variant>.xml
    val lintXmlFiles = File(".").walk()
        .filter { it.isFile && it.name.startsWith("lint-results") && it.extension == "xml" }
        .toList()
    lintXmlFiles.forEach { reportFile ->
        AndroidLint.report(reportFile.absolutePath)
    }

    // ── JUnit test results ────────────────────────────────────────────────
    // Discovers **/build/test-results/TEST-*.xml across all modules
    register plugin TestingPlugin
    TestingPlugin.findAndProcessJUnitReports(JUnitConfig())

    // ── Kotlin Compiler Warnings: inline comments ─────────────────────────
    val buildLog = File("build.log")
    if (buildLog.exists()) {
        val warningRegex = Regex("""^w:\s+(.+?):(\d+):\d+\s+(.+)$""")
        val workspaceRoot = System.getenv("GITHUB_WORKSPACE").orEmpty()

        buildLog.forEachLine { rawLine ->
            val line = rawLine.trim()
            warningRegex.matchEntire(line)?.destructured?.let { (filePath, lineNum, message) ->
                val relPath = when {
                    workspaceRoot.isNotEmpty() && filePath.startsWith(workspaceRoot) ->
                        filePath.removePrefix("$workspaceRoot/")
                    else -> filePath
                }
                warn("Kotlin compiler: $message", relPath, lineNum.toInt())
            }
        }
    }
}
