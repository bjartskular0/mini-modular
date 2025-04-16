tasks.register("buildAndRun") {
    val buildTasks = getTasksByName("build", true)
    val runTasks = getTasksByName("run", true).filter { t -> t.project.name == "app" }

    println(buildTasks)
    println(runTasks)

    runTasks.forEach { t ->
        t.mustRunAfter(buildTasks)
    }

    dependsOn(buildTasks)
    dependsOn(runTasks)
}