/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2

import com.gyurigrell.rxreactor2.ReactorWithEffectsTests.TestReactor.*
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

/**
 * Unit tests for [ReactorWithEffects]
 */
class ReactorWithEffectsTests {
    @Test
    fun `SimpleAction updates State simpleAction to true`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestObserver.create<State>()
        reactor.state.subscribe(output)

        // Act
        reactor.action.accept(Action.SimpleAction)

        // Assert
        output.assertNoErrors()
            .assertValueCount(2)
            .assertValueAt(0, State(false))
            .assertValueAt(1, State(true))
    }

    @Test
    fun `ActionWithValue updates State actionWithValue to correct string`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestObserver.create<State>()
        reactor.state.subscribe(output)
        val theValue = "I love apple pie"

        // Act
        reactor.action.accept(Action.ActionWithValue(theValue))

        // Assert
        output.assertNoErrors()
            .assertValueCount(2)
            .assertValueAt(0, State())
            .assertValueAt(1, State(false, theValue))
    }

    @Test
    fun `ActionFiresEffectOne emits the effect `() {
        // Arrange
        val reactor = TestReactor()
        val output = TestObserver.create<State>()
        val effects = TestObserver.create<Effect>()
        reactor.state.subscribe(output)
        reactor.effect.subscribe(effects)

        // Act
        reactor.action.accept(Action.ActionFiresEffectOne)

        // Assert
        output.assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, State())
        effects.assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, Effect.EffectOne)
    }

    @Test
    fun `ActionFiresEffectWithValue emits the effect with the correct value`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestObserver.create<State>()
        val effects = TestObserver.create<Effect>()
        reactor.state.subscribe(output)
        reactor.effect.subscribe(effects)
        val theValue = "Millions of peaches, peaches for me"

        // Act
        reactor.action.accept(Action.ActionFiresEffectWithValue(theValue))

        // Assert
        output.assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, State())
        effects.assertNoErrors()
            .assertValueCount(1)
            .assertValueAt(0, Effect.EffectWithValue(theValue))
    }

    class TestReactor(
        initialState: State = State()
    ) : ReactorWithEffects<Action, Mutation, State, Effect>(initialState) {
        sealed class Action {
            object SimpleAction : Action()
            data class ActionWithValue(val theValue: String) : Action()
            object ActionFiresEffectOne : Action()
            data class ActionFiresEffectWithValue(val theValue: String) : Action()
        }

        sealed class Mutation : MutationWithEffect<Effect> {
            object SimpleActionMutation : Mutation()
            data class ActionWithValueMutation(val theValue: String) : Mutation()
            data class FireEffect(override val effect: Effect) : Mutation()
        }

        data class State(
            val simpleAction: Boolean = false,
            val actionWithValue: String = ""
        )

        sealed class Effect {
            object EffectOne : Effect()
            data class EffectWithValue(val theValue: String) : Effect()
        }

        override fun mutate(action: Action): Observable<Mutation> = when (action) {
            is Action.SimpleAction -> Observable.just(Mutation.SimpleActionMutation)

            is Action.ActionWithValue -> Observable.just(Mutation.ActionWithValueMutation(action.theValue))

            is Action.ActionFiresEffectOne -> Observable.just(Mutation.FireEffect(Effect.EffectOne))

            is Action.ActionFiresEffectWithValue ->
                Observable.just(Mutation.FireEffect(Effect.EffectWithValue(action.theValue)))
        }

        override fun reduce(state: State, mutation: Mutation): State = when (mutation) {
            is Mutation.SimpleActionMutation -> state.copy(simpleAction = true)

            is Mutation.ActionWithValueMutation -> state.copy(actionWithValue = mutation.theValue)

            is Mutation.FireEffect -> state // This will never happen, but need to be exhaustive
        }
    }
}
