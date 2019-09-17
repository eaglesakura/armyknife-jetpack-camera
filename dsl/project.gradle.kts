/**
 * Clean Intellij Idea's project files and caches.
 * e.g.)
 *  ./gradlew ideaClean
 */
task("ideaClean") {
    group = "project"
    description = "clean up Intellij(Android Studio) files."
    doLast {
        val deletes = mutableListOf<File>()

        fileTree(".").forEach { file ->
            if (file.isFile && file.name.endsWith(".iml")) {
                deletes.add(file)
            }
        }

        file(".idea").deleteRecursively()
        deletes.forEach { file ->
            println("rm $file")
            file.delete()
        }
    }
}