/*
 * Copyright (c) 2021, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor1

import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class SimpleReactorTests {
    @Test
    fun `each method is invoked`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestSubscriber.create<List<String>>()
        reactor.state.subscribe(states)

        // Act
        reactor.action.call(arrayListOf("action"))

        // Assert
        states.assertNoErrors()
        states.assertValues(
                arrayListOf("transformedState"),
                arrayListOf("action", "transformedAction", "mutation", "transformedMutation", "transformedState")
        )
    }

    @Test
    fun `state replay current state`() {
        // Arrange
        val reactor = CounterReactor()
        val states = TestSubscriber.create<Int>()
        reactor.state.subscribe(states) // state: 0

        // Act
        reactor.action.call(Unit) // state: 1
        reactor.action.call(Unit) // state: 2

        // Assert
        states.assertValues(0, 1, 2)
    }

    @Test
    fun `stream ignores error from mutate`() {
        // Arrange
        val reactor = CounterReactor()
        val states = TestSubscriber.create<Int>()
        reactor.state.subscribe(states)
        reactor.stateForTriggerError = 2

        // Act
        reactor.action.call(Unit)
        reactor.action.call(Unit)
        reactor.action.call(Unit)
        reactor.action.call(Unit)
        reactor.action.call(Unit)

        // Assert
        states.assertValues(0, 1, 2, 3, 4, 5)
    }

    class TestReactor : SimpleReactor<List<String>, List<String>>(initialState = ArrayList()) {
        // 1. ["action"] + ["transformedAction"]
        override fun transformAction(action: Observable<List<String>>): Observable<List<String>> =
                action.map { it + "transformedAction" }

        // 2. ["action", "transformedAction"] + ["mutation"]
        override fun mutate(action: List<String>): Observable<List<String>> =
                Observable.just(action + "mutation")

        // 3. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
        override fun transformMutation(mutation: Observable<List<String>>): Observable<List<String>> =
                mutation.map { it + "transformedMutation" }

        // 4. [] + ["action", "transformedAction", "mutation", "transformedMutation"]
        override fun reduce(state: List<String>, mutation: List<String>): List<String> = state + mutation

        // 5. ["action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
        override fun transformState(state: Observable<List<String>>): Observable<List<String>> =
                state.map { it + "transformedState" }
    }

    class CounterReactor : SimpleReactor<Unit, Int>(initialState = 0) {
        var stateForTriggerError: Int? = null
        var stateForTriggerCompleted: Int? = null

        override fun mutate(action: Unit): Observable<Unit> = when (currentState) {
            stateForTriggerError -> {
                val results = arrayOf(Observable.just(action), Observable.error(TestError()))
                Observable.concat(results.asIterable())
            }
            stateForTriggerCompleted -> {
                val results = arrayOf(Observable.just(action), Observable.empty())
                Observable.concat(results.asIterable())
            }
            else -> {
                Observable.just(action)
            }
        }

        override fun reduce(state: Int, mutation: Unit): Int {
            return state + 1
        }
    }
}