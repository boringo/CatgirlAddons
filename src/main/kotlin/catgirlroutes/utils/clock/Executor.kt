package catgirlroutes.utils.clock

import catgirlroutes.utils.profile
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Simplified Executor class that allows repeating execution of code without extension functions.
 * @param delay Lambda that returns the delay in milliseconds.
 * @param profileName A profile name for profiling.
 * @param func A standard lambda function to run repeatedly.
 */
open class Executor(
    val delay: () -> Long,
    private val profileName: String = "Unspecified catgirl executor",
    val func: () -> Unit
) {

    // Secondary constructor that accepts a Long delay instead of a lambda returning Long
    constructor(delay: Long, profileName: String = "Unspecified catgirl executor", func: () -> Unit)
            : this({ delay }, profileName, func)

    internal val clock = Clock()
    internal var shouldFinish = false

    open fun run(): Boolean {
        if (shouldFinish) return true
        profile(profileName) {
            if (clock.hasTimePassed(delay(), true)) {
                runCatching {
                    func()  // Directly invoke the function
                }
            }
        }
        return false
    }

    /**
     * LimitedExecutor that stops after a certain number of executions.
     */
    class LimitedExecutor(
        delay: Long,
        private val repeats: Int,
        profileName: String = "Unspecified catgirl executor",
        func: () -> Unit
    ) : Executor(delay, profileName, func) {

        private var totalRepeats = 0

        override fun run(): Boolean {
            if (shouldFinish) return true
            if (clock.hasTimePassed(delay(), true)) {
                runCatching {
                    func()
                    if (totalRepeats++ >= repeats) destroyExecutor()  // Stop after the specified repeats
                }
            }
            return false
        }
    }

    fun destroyExecutor() {
        shouldFinish = true
    }

    companion object {
        private val executors = ArrayList<Executor>()

        fun Executor.register() {
            executors.add(this)
        }

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
            executors.removeAll { it.run() }
        }
    }
}