package com.gyurigrell.rxreactor

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

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
 * @property debug default is false. When set to true, each action, mutation and state change is logged
 * via {@link logDebug}
 */
abstract class Reactor<Action, Mutation, State>(val initialState: State, val debug: Boolean = false) {
    /**
     * Accepts the actions from the view, which then potentially cause mutations of the current state.
     */
    val action: PublishRelay<Action> = PublishRelay.create()

    /**
     * The current state of the view to which the reactor is bound.
     */
    var currentState: State
        private set

    /**
     * The state stream output from the reactor, emitting every time the state is modified via a mutation.
     */
    val state: Observable<State>

    private var disposeBag = CompositeDisposable()

    init {
        state = createStateStream()
        currentState = initialState
    }

    /**
     * Commits mutation from the action. This is the best place to perform side-effects such as async tasks.
     * @param action the action initiated by the user on the view
     * @return an observable which emits 0..n mutations
     */
    open fun mutate(action: Action): Observable<Mutation> {
        return Observable.empty()
    }

    /**
     * Given the current state and a mutation, returns the mutated state.
     * @param state the current state
     * @param mutation the mutation to apply to the state
     * @return the mutated state
     */
    open fun reduce(state: State, mutation: Mutation): State {
        return state
    }

    /**
     *
     */
    open fun transformAction(action: Observable<Action>): Observable<Action> {
        if (debug) {
            return action.doOnEach { logNotification("action", it) }
        }
        return action
    }

    /**
     *
     */
    open fun transformMutation(mutation: Observable<Mutation>): Observable<Mutation> {
        if (debug) {
            return mutation.doOnEach { logNotification("mutation", it) }
        }
        return mutation
    }

    /**
     *
     */
    open fun transformState(state: Observable<State>): Observable<State> {
        if (debug) {
            return state.doOnEach { logNotification("state", it) }
        }
        return state
    }

    /**
     *
     */
    open fun logDebug(message: String) {
        println(message)
    }

    private fun <T> logNotification(label: String, notification: Notification<T>) {
        val note: String = when {
            notification.isOnNext -> {
                "onNext: ${notification.value}"
            }
            notification.isOnComplete -> {
                "onComplete"
            }
            notification.isOnError -> {
                "onError: ${notification.error}"
            }
            else -> {
                "unknown"
            }
        }
        logDebug("$label $note")
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
