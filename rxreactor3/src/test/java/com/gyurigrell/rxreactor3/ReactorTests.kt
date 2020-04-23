/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor3

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Test

/**
 * Unit tests for [Reactor]
 */
class ReactorTests {
    @Test
    fun `each method is invoked`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestObserver.create<List<String>>()
        reactor.state.subscribe(output)

        // Act
        reactor.action.accept(arrayListOf("action"))

        // Assert
        output
            .assertNoErrors()
            .assertValues(
                mutableListOf("transformedState"),
                mutableListOf("action", "transformedAction", "mutation", "transformedMutation", "transformedState")
            )
    }

    @Test
    fun `state replay current state`() {
        // Arrange
        val reactor = CounterReactor()
        val output = TestObserver.create<Int>()
        reactor.state.subscribe(output) // state: 0

        // Act
        reactor.action.accept(Unit) // state: 1
        reactor.action.accept(Unit) // state: 2

        // Assert
        output.assertValues(0, 1, 2)
    }

    @Test
    fun `stream ignores error from mutate`() {
        // Arrange
        val reactor = CounterReactor()
        val output = TestObserver.create<Int>()
        reactor.state.subscribe(output)
        reactor.stateForTriggerError = 2

        // Act
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)
        reactor.action.accept(Unit)

        // Assert
        output.assertValues(0, 1, 2, 3, 4, 5)
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

    class CounterReactor : Reactor<Unit, Unit, Int>(initialState = 0) {
        var stateForTriggerError: Int? = null
        var stateForTriggerCompleted: Int? = null

        override fun mutate(action: Unit): Observable<Unit> {
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

        override fun reduce(state: Int, mutation: Unit): Int {
            return state + 1
        }
    }

    class TestError : Error()
}
