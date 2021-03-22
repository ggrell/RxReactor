/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor3.android

import com.gyurigrell.rxreactor3.Reactor
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

/**
 * Specialized implementation of [Reactor] that ensures [state] Observables emits on [AndroidSchedulers.mainThread]
 */
class AndroidReactor<Action: Any, Mutation: Any, State: Any>(
    initialState: State
) : Reactor<Action, Mutation, State>(initialState) {
    override fun transformState(state: Observable<State>): Observable<State> = 
        state.observeOn(AndroidSchedulers.mainThread())
}
