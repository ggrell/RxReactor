package com.gyurigrell.rxreactor1

import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

/**
 * Do not let me check this in without adding a comment about the class.
 */
class ReactorTests {
    @Test
    fun eachMethodIsInvoked() {
        // Arrange
        val reactor = TestReactor()
        val output = TestSubscriber.create<List<String>>()
        reactor.state.subscribe(output)

        // Act
        reactor.action.call(arrayListOf("action"))

        // Assert
        output.assertNoErrors()
        output.assertValueCount(2)
        output.assertValues(arrayListOf("transformedState"),
                            arrayListOf("action",
                                        "transformedAction",
                                        "mutation",
                                        "transformedMutation",
                                        "transformedState"))
    }

    @Test
    fun stateReplayCurrentState() {
        // Arrange
        val reactor = CounterReactor()
        val output = TestSubscriber.create<Int>()
        reactor.state.subscribe(output) // state: 0

        // Act
        reactor.action.call(Irrelevant.INSTANCE) // state: 1
        reactor.action.call(Irrelevant.INSTANCE) // state: 2

        // Assert
        output.assertValues(0, 1, 2)
    }

    @Test
    fun testStreamIgnoresErrorFromMutate() {
        // Arrange
        val reactor = CounterReactor()
        val output = TestSubscriber.create<Int>()
        reactor.state.subscribe(output)
        reactor.stateForTriggerError = 2

        // Act
        reactor.action.call(Irrelevant.INSTANCE)
        reactor.action.call(Irrelevant.INSTANCE)
        reactor.action.call(Irrelevant.INSTANCE)
        reactor.action.call(Irrelevant.INSTANCE)
        reactor.action.call(Irrelevant.INSTANCE)

        // Assert
        output.assertValueCount(6)
        output.assertValues(0, 1, 2, 3, 4, 5)
    }
}

class TestReactor : Reactor<List<String>, List<String>, List<String>>(initialState = ArrayList()) {
    // 1. ["action"] + ["transformedAction"]
    override fun transformAction(action: Observable<List<String>>): Observable<List<String>> {
        return action.map { it + "transformedAction" }
    }

    // 2. ["action", "transformedAction"] + ["mutation"]
    override fun mutate(action: List<String>): Observable<List<String>> {
        return Observable.just(action + "mutation")
    }

    // 3. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
    override fun transformMutation(mutation: Observable<List<String>>): Observable<List<String>> {
        return mutation.map { it + "transformedMutation" }
    }

    // 4. [] + ["action", "transformedAction", "mutation", "transformedMutation"]
    override fun reduce(state: List<String>, mutation: List<String>): List<String> {
        return state + mutation
    }

    // 5. ["action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
    override fun transformState(state: Observable<List<String>>): Observable<List<String>> {
        return state.map { it + "transformedState" }
    }
}

class CounterReactor : Reactor<Irrelevant, Irrelevant, Int>(initialState = 0) {
    var stateForTriggerError: Int? = null
    var stateForTriggerCompleted: Int? = null

    override fun mutate(action: Irrelevant): Observable<Irrelevant> {
        if (currentState == stateForTriggerError) {
            val results = arrayOf(Observable.just(action), Observable.error(TestError()))
            return Observable.concat(results.asIterable())
        } else if (currentState == stateForTriggerCompleted) {
            val results = arrayOf(Observable.just(action), Observable.empty())
            return Observable.concat(results.asIterable())
        } else {
            return Observable.just(action)
        }
    }

    override fun reduce(state: Int, mutation: Irrelevant): Int {
        return state + 1
    }
}

class TestError : Error()

enum class Irrelevant {
    INSTANCE
}
