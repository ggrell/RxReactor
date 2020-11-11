/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor1

import com.gyurigrell.rxreactor1.ReactorWithMutationEffectsTests.TestReactor.*
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

/**
 * Unit tests for [ReactorWithEffects]. These test use the deprecated [ReactorWithEffects.MutationWithEffect] marker
 * on [Mutation]s which are now obsolete.
 */
class ReactorWithMutationEffectsTests {
    @Test
    fun `SimpleAction updates State simpleAction to true`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestSubscriber.create<State>()
        reactor.state.subscribe(states)

        // Act
        reactor.action.call(Action.SimpleAction)

        // Assert
        states.assertNoErrors()
        states.assertValues(State(false), State(true))
    }

    @Test
    fun `ActionWithValue updates State actionWithValue to correct string`() {
        // Arrange
        val reactor = TestReactor()
        val states = TestSubscriber.create<State>()
        reactor.state.subscribe(states)
        val theValue = "I love apple pie"

        // Act
        reactor.action.call(Action.ActionWithValue(theValue))

        // Assert
        states.assertNoErrors()
        states.assertValues(State(), State(false, theValue))
    }

    @Test
    fun `ActionFiresEffectOne emits the effect `() {
        // Arrange
        val reactor = TestReactor()
        val states = TestSubscriber.create<State>()
        val effects = TestSubscriber.create<Effect>()
        reactor.state.subscribe(states)
        reactor.effect.subscribe(effects)

        // Act
        reactor.action.call(Action.ActionFiresEffectOne)

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
        val states = TestSubscriber.create<State>()
        val effects = TestSubscriber.create<Effect>()
        reactor.state.subscribe(states)
        reactor.effect.subscribe(effects)
        val theValue = "Millions of peaches, peaches for me"

        // Act
        reactor.action.call(Action.ActionFiresEffectWithValue(theValue))

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
