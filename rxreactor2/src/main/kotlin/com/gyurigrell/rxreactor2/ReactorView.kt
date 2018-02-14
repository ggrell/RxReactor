package com.gyurigrell.rxreactor2

import io.reactivex.disposables.CompositeDisposable

/**
 * Do not let me check this in without adding a comment about the class.
 */
interface ReactorView<Action, Mutation, State> {
    var disposeBag: CompositeDisposable

    var reactor: Reactor<Action, Mutation, State>?

    fun bind(reactor: Reactor<Action, Mutation, State>)
}
