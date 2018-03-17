package com.gyurigrell.rxreactor1

import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Convenience extensions to [Subscription]
 */

/**
 * Simplifies adding a [Subscription] to a [CompositeSubscription] after a [rx.Observable.subscribe] call.
 */
fun Subscription.disposedBy(composite: CompositeSubscription) {
    composite.add(this)
}
