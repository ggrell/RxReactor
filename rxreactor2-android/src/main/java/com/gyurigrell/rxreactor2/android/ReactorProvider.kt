/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.gyurigrell.rxreactor2.Reactor

/**
 * An utility class that provides ``Reactor`s` for a scope.
 * 
 * Default `ReactorProvider` for an `Activity` or a `Fragment` can be obtained
 * from [ReactorProvider.of].
 */
class ReactorProvider(private val reactorStore: ReactorStore, private val factory: Factory) {
    /**
     * Returns an existing `Reactor` or creates a new one in the scope (usually, a fragment or
     * an activity), associated with this [ReactorProvider].
     * <p>
     * The created `Reactor` is associated with the given scope and will be retained
     * as long as the scope is alive (e.g. if it is an activity, until it is
     * finished or process is killed).
     *
     * @param modelClass The class of the `Reactor` to create an instance of it if it is not
     *                   present.
     * @param <T>        The type parameter for the `Reactor`.
     * @return A `Reactor` that is an instance of the given type [T].
     */
    fun <Action, Mutation, State, T : Reactor<Action, Mutation, State>> get(modelClass: Class<T>): T {
        val canonicalName = modelClass.canonicalName
            ?: throw IllegalArgumentException("Local and anonymous classes can not be Reactors")
        return get("$DEFAULT_KEY:$canonicalName", modelClass)
    }

    /**
     * Returns an existing `Reactor` or creates a new one in the scope (usually, a fragment or
     * an activity), associated with this [ReactorProvider].
     * <p>
     * The created `Reactor` is associated with the given scope and will be retained
     * as long as the scope is alive (e.g. if it is an activity, until it is
     * finished or process is killed).
     *
     * @param key        The key to use to identify the `Reactor`.
     * @param modelClass The class of the `Reactor` to create an instance of it if it is not
     *                   present.
     * @param <T>        The type parameter for the `Reactor`.
     * @return A `Reactor` that is an instance of the given type [T].
     */
    operator fun <Action, Mutation, State, T : Reactor<Action, Mutation, State>> get(key: String, modelClass: Class<T>): T {
        var reactor: Reactor<Action, Mutation, State>? = reactorStore.get(key)

        if (modelClass.isInstance(reactor)) {
            @Suppress("UNCHECKED_CAST")
            return reactor as T
        } else {
            if (reactor != null) {
                // TODO: log a warning.
            }
        }

        reactor = factory.create(modelClass)
        reactorStore.put(key, reactor)

        return reactor
    }

    /**
     * Implementations of `Factory` interface are responsible for instantiating `Reactors`.
     */
    interface Factory {
        /**
         * Creates a new instance of the given `Class`.
         *
         * @param modelClass a `Class` whose instance is requested
         * @param <T>        The type parameter for the `Reactor`.
         * @return a newly created Reactor */
        fun <Action, Mutation, State, T : Reactor<Action, Mutation, State>> create(modelClass: Class<T>): T
    }

    companion object {
        private const val DEFAULT_KEY = "com.gyurigrell.rxreactor.ReactorProvider"

        @JvmField
        val defaultFactory: Factory = NewInstanceFactory()

        /**
         * Creates a [`Reactor`Provider], which retains `Reactors` while a scope of given `Activity`
         * is alive. 
         * <p>
         * It uses the given [Factory] to instantiate new `Reactors`.
         *
         * @param fragment a fragment, in whose scope `Reactors` should be retained
         * @param factory  a [Factory] to instantiate new `Reactors`
         * @return a `ReactorProvider` instance
         */
        @JvmStatic
        @JvmOverloads
        fun of(fragment: Fragment, factory: Factory = defaultFactory): ReactorProvider {
            return ReactorProvider(ReactorStore.of(fragment), factory)
        }

        /**
         * Creates a [`Reactor`Provider], which retains `Reactors` while a scope of given `Activity`
         * is alive.
         * <p>
         * It uses the given [Factory] to instantiate new `Reactors`.
         *
         * @param activity an activity, in whose scope `Reactors` should be retained
         * @param factory  a [Factory] to instantiate new `Reactors`
         * @return a `ReactorProvider` instance
         */
        @JvmStatic
        @JvmOverloads
        fun of(activity: FragmentActivity, factory: Factory = defaultFactory): ReactorProvider {
            return ReactorProvider(ReactorStore.of(activity), factory)
        }

        /**
         * Simple factory, which calls empty constructor on the give class.
         */
        class NewInstanceFactory : Factory {
            override fun <Action, Mutation, State, T : Reactor<Action, Mutation, State>> create(modelClass: Class<T>): T {
                return modelClass.newInstance()
            }
        }
    }
}
