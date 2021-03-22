/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.android

import com.gyurigrell.rxreactor2.Reactor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Specialized implementation of [Reactor] that ensures [state] Observables emits on [AndroidSchedulers.mainThread]
 */
abstract class AndroidReactor<Action: Any, Mutation: Any, State: Any>(
    initialState: State
) : Reactor<Action, Mutation, State>(initialState) {
    override fun transformState(state: Observable<State>): Observable<State> = 
        state.observeOn(AndroidSchedulers.mainThread())
}
