/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

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
 * @property initialState the initial state of the reactor, from which the {@see currentState} will be initialized.
 */
abstract class Reactor<Action: Any, Mutation: Any, State: Any>(
    val initialState: State
) {
    /**
     * Accepts the actions from the view, which then potentially cause mutations of the current state.
     */
    val action: PublishRelay<Action> = PublishRelay.create()

    /**
     * The current state of the view to which the reactor is bound.
     */
    var currentState: State = initialState
        private set

    /**
     * The state stream output from the reactor, emitting every time the state is modified via a mutation.
     */
    val state: Observable<State> by lazy { createStateStream() }

    /**
     * Subscriptions in the reactor
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected var subscriptions = CompositeDisposable()

    /**
     * Commits mutation from the action. This is the best place to perform side-effects such as async tasks.
     * @param action the action initiated by the user on the view
     * @return an observable which emits 0..n mutations
     */
    open fun mutate(action: Action): Observable<Mutation> = Observable.empty()

    /**
     * Given the current state and a mutation, returns the mutated state.
     * @param state the current state
     * @param mutation the mutation to apply to the state
     * @return the mutated state
     */
    open fun reduce(state: State, mutation: Mutation): State = state

    /**
     * Override to apply transformation to action observable
     */
    open fun transformAction(action: Observable<Action>): Observable<Action> = action

    /**
     * Override to apply transformation to mutation observable
     */
    open fun transformMutation(mutation: Observable<Mutation>): Observable<Mutation> = mutation

    /**
     * Override to apply transformation to state observable. This is a good place to apply an observeOn to switch
     * to main scheduler
     */
    open fun transformState(state: Observable<State>): Observable<State> = state

    /**
     * Clear all subscriptions in the reactor. Only needs to be called if your reactor uses hot observables.
     */
    open fun clearSubscriptions() = subscriptions.clear()

    private fun createStateStream(): Observable<State> {
        val transformedAction = transformAction(action)
        val mutation = transformedAction.flatMap { action ->
            mutate(action).onErrorResumeNext { _: Throwable -> Observable.empty() }
        }
        val transformedMutation = transformMutation(mutation)
        val state = transformedMutation
            .scan(initialState) { state, mutate -> reduce(state, mutate) }
//            .scan(initialState) { state, mutate -> reduce(state, mutate) }
            .onErrorResumeNext { _: Throwable -> Observable.empty() }
            .startWith(initialState)
        val transformedState = transformState(state)
            .doOnNext { currentState = it }
            .replay(1)
        return transformedState.apply { subscriptions.add(connect()) }
    }
}
