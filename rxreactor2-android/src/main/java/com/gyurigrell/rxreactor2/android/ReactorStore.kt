package com.gyurigrell.rxreactor2.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.gyurigrell.rxreactor2.Reactor
import com.gyurigrell.rxreactor2.android.ReactorStoreFragment.Companion.reactorStoreFragmentFor

/**
 * Class to store [Reactor]s.
 *
 * An instance of `ReactorStore` must be retained through configuration changes:
 * if an owner of this `ReactorStore` is destroyed and recreated due to configuration
 * changes, new instance of an owner should still have the same old instance of
 * `ReactorStore`.
 *
 * If an owner of this `ReactorStore` is destroyed and is not going to be recreated,
 * then it should call [clear] on this `ReactorStore`, so `Reactors` would
 * be notified that they are no longer used.
 *
 * [ReactorStore.of] provides a `ReactorStore` for activities and fragments.
 */
class ReactorStore {
    private var store = HashMap<String, Reactor<*, *, *>>()

    fun <Action, Mutation, State> get(key: String): Reactor<Action, Mutation, State>? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as? Reactor<Action, Mutation, State>
    }

    fun <Action, Mutation, State> put(key: String, reactor: Reactor<Action, Mutation, State>) {
        store[key] = reactor
    }

    /**
     * Clears internal storage and notifies ViewModels that they are no longer used.
     */
    fun clear() {
//        for (reactor in store.values) {
//            reactor.onCleared()
//        }
        store.clear()
    }

    companion object {
        /**
         * Returns the [ReactorStore] of the given fragment.
         *
         * @param fragment a fragment whose `ReactorStore` is requested
         * @return a `ReactorStore`
         */
        fun of(fragment: Fragment): ReactorStore {
            return reactorStoreFragmentFor(fragment).reactorStore
        }

        /**
         * Returns the [ReactorStore] of the given activity.
         *
         * @param activity an activity whose `ReactorStore` is requested
         * @return a `ReactorStore`
         */
        fun of(activity: FragmentActivity): ReactorStore {
            return reactorStoreFragmentFor(activity).reactorStore
        }
    }
}
