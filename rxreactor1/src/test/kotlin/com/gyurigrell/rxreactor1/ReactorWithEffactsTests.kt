package com.gyurigrell.rxreactor1

import com.gyurigrell.rxreactor1.ReactorWithEffectsTests.TestReactor.*
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

/**
 * Unit tests for [ReactorWithEffects]
 */
class ReactorWithEffectsTests {
    @Test
    fun `SimpleAction updates State simpleAction to true`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestSubscriber.create<TestReactor.State>()
        reactor.state.subscribe(output)

        // Act
        reactor.action.call(Action.SimpleAction)

        // Assert
        output.assertNoErrors()
        output.assertValueCount(2)
        output.assertValues(State(false), State(true))
    }

    @Test
    fun `ActionWithValue updates State actionWithValue to correct string`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestSubscriber.create<State>()
        reactor.state.subscribe(output)
        val theValue = "I love apple pie"

        // Act
        reactor.action.call(Action.ActionWithValue(theValue))

        // Assert
        output.assertNoErrors()
        output.assertValueCount(2)
        output.assertValues(State(), State(false, theValue))
    }

    @Test
    fun `ActionFiresEffectOne emits the effect `() {
        // Arrange
        val reactor = TestReactor()
        val output = TestSubscriber.create<State>()
        val effects = TestSubscriber.create<Effect>()
        reactor.state.subscribe(output)
        reactor.effect.subscribe(effects)

        // Act
        reactor.action.call(Action.ActionFiresEffectOne)

        // Assert
        output.assertNoErrors()
        output.assertValueCount(1)
        output.assertValue(State())
        effects.assertNoErrors()
        effects.assertValueCount(1)
        effects.assertValue(Effect.EffectOne)
    }

    @Test
    fun `ActionFiresEffectWithValue emits the effect with the correct value`() {
        // Arrange
        val reactor = TestReactor()
        val output = TestSubscriber.create<TestReactor.State>()
        val effects = TestSubscriber.create<TestReactor.Effect>()
        reactor.state.subscribe(output)
        reactor.effect.subscribe(effects)
        val theValue = "Millions of peaches, peaches for me"

        // Act
        reactor.action.call(TestReactor.Action.ActionFiresEffectWithValue(theValue))

        // Assert
        output.assertNoErrors()
        output.assertValueCount(1)
        output.assertValue(State())
        effects.assertNoErrors()
        effects.assertValueCount(1)
        effects.assertValue(Effect.EffectWithValue(theValue))
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
