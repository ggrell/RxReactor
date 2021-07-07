/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor3

import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

/**
 * A Reactor is an UI-independent layer which manages the state of a view. The foremost role of a
 * reactor is to separate control flow from a view. Every view has its corresponding reactor and
 * delegates all logic to its reactor. A reactor has no dependency to a view, so it can be easily
 * tested.
 *
 * @param Action the type of the action, which is generally either an enum or a Kotlin sealed class. Actions need to be
 * publicly available since actions are passed to the reactor via this type (using the {@see action} relay observer.
 * @param Mutation the type of the mutation. This type is only used internally in the reactor to map an action to  0..n
 * mutations.
 * @param State the type of the state that the reactor holds and modifies.
 * @param Effect the type of the effect that is emitted for side-effects that don't modify state
 * @property initialState the initial state of the reactor, from which the {@see currentState} will be initialized.
 */
abstract class ReactorWithEffects<Action : Any, Mutation : Any, State : Any, Effect : Any>(
    initialState: State
) : Reactor<Action, Mutation, State>(initialState) {
    /**
     * The effect stream output from the reactor.
     */
    val effect: Observable<Effect> by lazy { transformEffect(effectRelay) }

    private val effectRelay: PublishRelay<Effect> = PublishRelay.create()

    /**
     * Override to modify the effect observable
     */
    open fun transformEffect(effect: Observable<Effect>): Observable<Effect> = effect

    /**
     * Emits all effects provided by the Observable
     * @param effect tan Observable that emits effects
     */
    protected fun emitEffect(effect: Observable<Effect>) {
        effect.subscribe(effectRelay).also { subscriptions.add(it) }
    }

    /**
     * Simplified way to emits effects
     * @param effect one or more Effects to be emitted
     */
    protected fun emitEffect(vararg effect: Effect) {
        Observable.fromIterable(effect.asIterable()).subscribe(effectRelay).also { subscriptions.add(it) }
    }
}
