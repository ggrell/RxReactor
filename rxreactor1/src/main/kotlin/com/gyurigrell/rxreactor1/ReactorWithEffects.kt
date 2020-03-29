/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor1

import com.gyurigrell.rxreactor1.ReactorWithEffects.MutationWithEffect
import com.jakewharton.rxrelay.PublishRelay
import rx.Observable

/**
 * A Reactor is an UI-independent layer which manages the state of a view. The foremost role of a
 * reactor is to separate control flow from a view. Every view has its corresponding reactor and
 * delegates all logic to its reactor. A reactor has no dependency to a view, so it can be easily
 * tested.
 *
 * @param Action the type of the action, which is generally either an enum or a Kotlin sealed class. Actions need to be
 * publicly available since actions are passed to the reactor via this type (using the {@see action} relay observer.
 * @param Mutation the type of the mutation. This type is only used internally in the reactor to map an action to  0..n
 * mutations. It must implement [MutationWithEffect], and a single mutation should override `effect` and provide a
 * non-null value.
 * @param State the type of the state that the reactor holds and modifies.
 * @param Effect the type of the effect that is emitted for side-effects that don't modify state
 * @property initialState the initial state of the reactor, from which the {@see currentState} will be initialized.
 */
abstract class ReactorWithEffects<Action, Mutation : MutationWithEffect<Effect>, State, Effect>(
    initialState: State
) : Reactor<Action, Mutation, State>(initialState) {
    /**
     * The effect stream output from the reactor.
     */
    val effect: Observable<Effect> by lazy { transformEffect(effectRelay) }

    private val effectRelay: PublishRelay<Effect> = PublishRelay.create()

    /**
     * Checks to see if the mutation has an effect set. If it does, emits it via [ReactorWithEffects.effectRelay] and
     * swallows the [Mutation], otherwise lets the [Mutation] pass through.
     */
    override fun transformMutation(mutation: Observable<Mutation>): Observable<Mutation> = mutation.flatMap { m ->
        // If its a TriggerEffect mutation, emit it as an Effect and prevent State emission
        if (m.effect != null) {
            effectRelay.call(m.effect)
            return@flatMap Observable.empty<Mutation>()
        }
        Observable.just(m)
    }

    /**
     * Override to modify the effect observable
     */
    open fun transformEffect(effect: Observable<Effect>): Observable<Effect> = effect

    /**
     * The interface that needs to be applied to the [Mutation] sealed class defined in this [ReactorWithEffects]. It
     * applies a field named [effect] which defaults to `null`, meaning that mutation doesn't emit effects. Generally
     * there should only be a single mutation that has an override where it provides an effect.
     * @param Effect this is just the [Effect] type defined in the reactor.
     * ```
     *     sealed class Mutation: MutationWithEffect<Effect> {
     *         object Mutation1 : Mutation()
     *         data class Mutation2(val someValue): Mutation()
     *         data class EmitEffect(override val effect: Effect): Mutation()
     *     }
     *  ```
     */
    interface MutationWithEffect<Effect> {
        val effect: Effect?
            get() = null
    }
}
