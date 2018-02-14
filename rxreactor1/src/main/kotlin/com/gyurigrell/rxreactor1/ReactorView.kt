package com.gyurigrell.rxreactor1

import rx.subscriptions.CompositeSubscription

/**
 * Do not let me check this in without adding a comment about the class.
 */
interface ReactorView<Action, Mutation, State> {
    var disposeBag: CompositeSubscription

    var reactor: Reactor<Action, Mutation, State>?

    fun bind(reactor: Reactor<Action, Mutation, State>)
}
