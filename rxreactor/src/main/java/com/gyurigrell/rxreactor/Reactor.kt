package com.gyurigrell.rxreactor

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 * Do not let me check this in without adding a comment about the class.
 */
abstract class Reactor<Action, Mutation, State>(val initialState: State) {
    val action: PublishRelay<Action> = PublishRelay.create()

    var currentState: State
        private set

    val state: Observable<State>

    private var disposeBag = CompositeDisposable()

    init {
        state = createStateStream()
        currentState = initialState
    }

    open fun mutate(action: Action): Observable<Mutation> {
        return Observable.empty()
    }

    open fun reduce(state: State, mutation: Mutation): State {
        return state
    }

    open fun transformAction(action: Observable<Action>): Observable<Action> {
        return action
    }

    open fun transformMutation(mutation: Observable<Mutation>): Observable<Mutation> {
        return mutation
    }

    open fun transformState(state: Observable<State>): Observable<State> {
        return state
    }

    private fun createStateStream(): Observable<State> {
        val transformedAction = transformAction(action)
        val mutation = transformedAction.flatMap { action ->
            mutate(action).onErrorResumeNext { _: Throwable -> Observable.empty() }
        }
        val transformedMutation = transformMutation(mutation)
        val state = transformedMutation
                .scan(initialState) { state, mutate -> reduce(state, mutate) }
                .onErrorResumeNext { _: Throwable -> Observable.empty() }
                .startWith(initialState)
        val transformedState = transformState(state)
                .doOnNext { currentState = it }
                .replay(1)
        val disposable = transformedState.connect()
        disposeBag.add(disposable)
        return transformedState
    }
}
