package com.gyurigrell.rxreactor1

import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.lang.kotlin.addTo
import rx.subscriptions.CompositeSubscription

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
abstract class ReactorWithEffects<Action, Mutation, State, Effect>(
        initialState: State
) : Reactor<Action, Mutation, State>(initialState) {
    /**
     * The effect stream output from the reactor.
     */
    val effect: Observable<Effect> by lazy { effects }

    protected val effects: PublishRelay<Effect> = PublishRelay.create()
}
