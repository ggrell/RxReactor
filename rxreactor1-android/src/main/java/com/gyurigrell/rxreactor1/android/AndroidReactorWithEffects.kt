/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor1.android

import com.gyurigrell.rxreactor1.ReactorWithEffects
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

/**
 * Specialized implementation of [ReactorWithEffects] that ensures [effect] and [state] Observables emit on
 * [AndroidSchedulers.mainThread]
 */
abstract class AndroidReactorWithEffects<Action, Mutation, State, Effect>(
    initialState: State
) : ReactorWithEffects<Action, Mutation, State, Effect>(initialState) {
    override fun transformEffect(effect: Observable<Effect>): Observable<Effect> = 
        effect.observeOn(AndroidSchedulers.mainThread())

    override fun transformState(state: Observable<State>): Observable<State> = 
        state.observeOn(AndroidSchedulers.mainThread())
}
