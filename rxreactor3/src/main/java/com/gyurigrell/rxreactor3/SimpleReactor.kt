/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor3

import io.reactivex.rxjava3.core.Observable

/**
 * A simple reactor where there is no separate definition for mutations (the mutations are same as the actions)
 * @param Action the type of the action, which is generally either an enum or a Kotlin sealed class. Actions need to be
 * publicly available since actions are passed to the reactor via this type (using the {@see action} relay observer.
 * @param State the type of the state that the reactor holds and modifies.
 * @property initialState the initial state of the reactor, from which the {@see currentState} will be initialized.
 * @property debug default is false. When set to true, each action, mutation and state change is logged
 * via {@link logDebug}
 */
abstract class SimpleReactor<Action, State>(initialState: State) :
        Reactor<Action, Action, State>(initialState) {
    override fun mutate(action: Action): Observable<Action> {
        return Observable.just(action)
    }
}
