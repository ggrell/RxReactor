import systems.danger.kotlin.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

fun parseXml(file: File): Document? =
    runCatching {
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
            .also { it.documentElement.normalize() }
    }.getOrNull()

fun normalizePath(rawPath: String, workspaceRoot: String): String {
    val normalizedPath = rawPath.replace('\\', '/')
    val normalizedWorkspace = workspaceRoot.replace('\\', '/').removeSuffix("/")
    return when {
        normalizedWorkspace.isNotBlank() && normalizedPath.startsWith("$normalizedWorkspace/") ->
            normalizedPath.removePrefix("$normalizedWorkspace/")
        normalizedPath.startsWith("./") -> normalizedPath.removePrefix("./")
        else -> normalizedPath
    }
}

danger(args) {
    val workspaceRoot = System.getenv("GITHUB_WORKSPACE").orEmpty()
    val changedFiles = (git.modifiedFiles + git.createdFiles).toSet()

    // ── Detekt: inline comments on changed files ──────────────────────────
    val detektReports = File(".").walk()
        .filter { it.isFile && it.name == "detekt.xml" }
        .toList()
    var detektIssueCount = 0

    detektReports.forEach { reportFile ->
        val report = parseXml(reportFile) ?: return@forEach
        val errors = report.getElementsByTagName("error")
        for (i in 0 until errors.length) {
            val node = errors.item(i) as? Element ?: continue
            val sourcePath = node.getAttribute("file")
            if (sourcePath.isBlank()) continue

            val path = normalizePath(sourcePath, workspaceRoot)
            if (path !in changedFiles) continue

            val line = node.getAttribute("line").toIntOrNull() ?: 1
            val message = node.getAttribute("message").ifBlank { "Detekt issue found" }
            val rule = node.getAttribute("source")
                .substringAfterLast('.')
                .ifBlank { "unknown-rule" }

            warn("Detekt [$rule]: $message", path, line)
            detektIssueCount++
        }
    }

    // ── Android Lint: inline comments for all 4 Android modules ───────────
    // Walk tree for lint XML reports; AGP 8.x names them lint-results-<variant>.xml
    val lintXmlFiles = File(".").walk()
        .filter { it.isFile && it.name.startsWith("lint-results") && it.extension == "xml" }
        .toList()
    val lintDedup = mutableSetOf<String>()
    var lintIssueCount = 0

    lintXmlFiles.forEach { reportFile ->
        val report = parseXml(reportFile) ?: return@forEach
        val issues = report.getElementsByTagName("issue")
        for (i in 0 until issues.length) {
            val issue = issues.item(i) as? Element ?: continue
            val id = issue.getAttribute("id").ifBlank { "lint" }
            val severity = issue.getAttribute("severity").ifBlank { "Warning" }
            val message = issue.getAttribute("message").ifBlank { "Android lint issue found" }
            val locations = issue.getElementsByTagName("location")

            for (j in 0 until locations.length) {
                val location = locations.item(j) as? Element ?: continue
                val sourcePath = location.getAttribute("file")
                if (sourcePath.isBlank()) continue

                val path = normalizePath(sourcePath, workspaceRoot)
                if (path !in changedFiles) continue

                val line = location.getAttribute("line").toIntOrNull()
                    ?: location.getAttribute("startLine").toIntOrNull()
                    ?: 1
                val dedupKey = "$id|$path|$line|$message"
                if (!lintDedup.add(dedupKey)) continue

                warn("Android Lint [$id/$severity]: $message", path, line)
                lintIssueCount++
            }
        }
    }

    // ── JUnit test results ────────────────────────────────────────────────
    val testReports = File(".").walk()
        .filter {
            it.isFile &&
                it.extension == "xml" &&
                it.name.startsWith("TEST-") &&
                it.path.contains("/test-results/")
        }
        .toList()

    var totalTests = 0
    var totalSkipped = 0
    var totalFailures = 0
    var totalErrors = 0
    val failedTests = mutableListOf<String>()

    testReports.forEach { reportFile ->
        val report = parseXml(reportFile) ?: return@forEach
        val testcases = report.getElementsByTagName("testcase")
        for (i in 0 until testcases.length) {
            val testcase = testcases.item(i) as? Element ?: continue
            totalTests++

            val skipped = testcase.getElementsByTagName("skipped").length > 0
            if (skipped) totalSkipped++

            val failureCount = testcase.getElementsByTagName("failure").length
            val errorCount = testcase.getElementsByTagName("error").length
            totalFailures += failureCount
            totalErrors += errorCount

            if (failureCount > 0 || errorCount > 0) {
                val className = testcase.getAttribute("classname").ifBlank { "UnknownClass" }
                val testName = testcase.getAttribute("name").ifBlank { "unknownTest" }
                failedTests += "$className#$testName"
            }
        }
    }

    if (testReports.isNotEmpty()) {
        val failed = totalFailures + totalErrors
        val summary = "JUnit: $totalTests tests, $failed failures/errors, $totalSkipped skipped."
        if (failed > 0) {
            val preview = failedTests.take(20).joinToString("\n") { "- $it" }
            fail("$summary\n\nFailing tests:\n$preview")
        } else {
            message(summary)
        }
    }

    // ── Kotlin Compiler Warnings: inline comments ─────────────────────────
    val buildLog = File("build.log")
    if (buildLog.exists()) {
        val warningRegex = Regex("""^w:\s+(.+?):(\d+):\d+\s+(.+)$""")

        buildLog.forEachLine { rawLine ->
            val line = rawLine.trim()
            warningRegex.matchEntire(line)?.destructured?.let { (filePath, lineNum, message) ->
                val relPath = normalizePath(filePath, workspaceRoot)
                warn("Kotlin compiler: $message", relPath, lineNum.toInt())
            }
        }
    }

    message("Static analysis summary: Detekt $detektIssueCount, Android Lint $lintIssueCount.")
}
