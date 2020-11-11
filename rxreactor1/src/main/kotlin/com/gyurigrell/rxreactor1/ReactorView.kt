/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor1

/**
 * An optional interface to apply to your view which provides some formality around how to set up the reactor and the
 * bindings between controls and state.
 */
@Deprecated("This will be removed in 1.0")
interface ReactorView {
    var reactor: Reactor<*, *, *>

    fun <Action, Mutation, State> bindViews(reactor: Reactor<Action, Mutation, State>)
    fun <Action, Mutation, State> bindState(reactor: Reactor<Action, Mutation, State>)
}

fun <Action, Mutation, State> ReactorView.bind(reactor: Reactor<Action, Mutation, State>) {
    this.reactor = reactor
    bindViews(reactor)
    bindState(reactor)
}