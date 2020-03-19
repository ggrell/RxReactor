package com.gyurigrell.rxreactor1.android

import com.gyurigrell.rxreactor1.ReactorWithEffects
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

/**
 * Specialized implementation of [ReactorWithEffects] that ensures [effect] and [state] Observables emit on
 * [AndroidSchedulers.mainThread]
 */
class AndroidReactorWithEffect<Action, Mutation : ReactorWithEffects.MutationWithEffect<Effect>, State, Effect>(
    initialState: State
) : ReactorWithEffects<Action, Mutation, State, Effect>(initialState) {
    override fun transformEffect(effect: Observable<Effect>): Observable<Effect> = effect.observeOn(AndroidSchedulers.mainThread())

    override fun transformAction(action: Observable<Action>): Observable<Action> = action.observeOn(AndroidSchedulers.mainThread())
}