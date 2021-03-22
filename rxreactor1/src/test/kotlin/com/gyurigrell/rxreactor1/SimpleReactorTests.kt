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
}