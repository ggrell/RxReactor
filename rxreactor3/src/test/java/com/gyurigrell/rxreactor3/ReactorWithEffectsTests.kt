/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

@file:Suppress("NoWildcardImports")

package com.gyurigrell.rxreactor3

import com.gyurigrell.rxreactor3.ReactorWithEffectsTests.TestReactor.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver

import org.junit.Test

/**
 * Unit tests for [ReactorWithEffects]
 */
class ReactorWithEffectsTests {
    @Test
    fun `SimpleAction updates State simpleAction to true`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestObserver.create<State>()
        reactor.state.subscribe(states)

        // Act
        reactor.action.accept(Action.SimpleAction)

        // Assert
        states.assertNoErrors()
        states.assertValues(State(false), State(true))
    }

    @Test
    fun `ActionWithValue updates State actionWithValue to correct string`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestObserver.create<State>()
        reactor.state.subscribe(states)
        val theValue = "I love apple pie"

        // Act
        reactor.action.accept(Action.ActionWithValue(theValue))

        // Assert
        states.assertNoErrors()
        states.assertValues(State(), State(false, theValue))
    }

    @Test
    fun `ActionFiresEffectOne emits the effect `() {
        // Arrange
        val reactor = TestReactor()
        val states = TestObserver.create<State>()
        val effects = TestObserver.create<Effect>()
        reactor.state.subscribe(states)
        reactor.effect.subscribe(effects)

        // Act
        reactor.action.accept(Action.ActionFiresEffectOne)

        // Assert
        states.assertNoErrors()
        states.assertValue(State())
        effects.assertNoErrors()
        effects.assertValue(Effect.EffectOne)
    }

    @Test
    fun `ActionFiresEffectWithValue emits the effect with the correct value`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestObserver.create<State>()
        val effects = TestObserver.create<Effect>()
        reactor.state.subscribe(states)
        reactor.effect.subscribe(effects)
        val theValue = "Millions of peaches, peaches for me"

        // Act
        reactor.action.accept(Action.ActionFiresEffectWithValue(theValue))

        // Assert
        states.assertNoErrors()
        states.assertValue(State())
        effects.assertNoErrors()
        effects.assertValue(Effect.EffectWithValue(theValue))
    }

    private class TestReactor(
        initialState: State = State()
    ) : ReactorWithEffects<Action, Mutation, State, Effect>(initialState) {
        sealed class Action {
            object SimpleAction : Action()
            data class ActionWithValue(val theValue: String) : Action()
            object ActionFiresEffectOne : Action()
            data class ActionFiresEffectWithValue(val theValue: String) : Action()
        }

        sealed class Mutation {
            object SimpleActionMutation : Mutation()
            data class ActionWithValueMutation(val theValue: String) : Mutation()
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
            is Action.SimpleAction ->
                Observable.just(Mutation.SimpleActionMutation)

            is Action.ActionWithValue ->
                Observable.just(Mutation.ActionWithValueMutation(action.theValue))

            is Action.ActionFiresEffectOne -> {
                emitEffect(Effect.EffectOne)
                Observable.empty() // No mutations
            }

            is Action.ActionFiresEffectWithValue -> {
                emitEffect(Effect.EffectWithValue(action.theValue))
                Observable.empty() // No mutations
            }
        }

        override fun reduce(state: State, mutation: Mutation): State = when (mutation) {
            is Mutation.SimpleActionMutation -> state.copy(simpleAction = true)

            is Mutation.ActionWithValueMutation -> state.copy(actionWithValue = mutation.theValue)
        }
    }
}
