/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.android

import com.gyurigrell.rxreactor2.ReactorWithEffects
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Specialized implementation of [ReactorWithEffects] that ensures [effect] and [state] Observables emit on
 * [AndroidSchedulers.mainThread]
 */
class AndroidReactorWithEffect<Action, Mutation : ReactorWithEffects.MutationWithEffect<Effect>, State, Effect>(
    initialState: State
) : ReactorWithEffects<Action, Mutation, State, Effect>(initialState) {
    override fun transformEffect(effect: Observable<Effect>): Observable<Effect> = effect.observeOn(AndroidSchedulers.mainThread())

    override fun transformState(state: Observable<State>): Observable<State> = state.observeOn(AndroidSchedulers.mainThread())
}