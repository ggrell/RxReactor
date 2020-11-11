/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor3

/**
 * An optional interface to apply to your view which provides some formality around how to set up the reactor and the
 * bindings between controls and state.
 */
@Deprecated("This will be removed in 1.0")
interface ReactorView<Action, Mutation, State> {
    var reactor: Reactor<Action, Mutation, State>

    fun bindControls(reactor: Reactor<Action, Mutation, State>)
    fun bindState(reactor: Reactor<Action, Mutation, State>)
    fun bindEffects(reactor: Reactor<Action, Mutation, State>)
}

fun <Action, Mutation, State> ReactorView<Action, Mutation, State>.bind(reactor: Reactor<Action, Mutation, State>) {
    this.reactor = reactor
    bindControls(reactor)
    bindState(reactor)
    bindEffects(reactor)
}