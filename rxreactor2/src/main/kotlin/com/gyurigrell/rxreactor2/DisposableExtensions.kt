package com.gyurigrell.rxreactor2

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Convenience extensions to [Disposable]
 */

/**
 * Simplifies adding a [Disposable] to a [CompositeDisposable] after a [io.reactivex.Observable.subscribe] call.
 */
fun Disposable.disposedBy(composite: CompositeDisposable) {
    composite.add(this)
}
