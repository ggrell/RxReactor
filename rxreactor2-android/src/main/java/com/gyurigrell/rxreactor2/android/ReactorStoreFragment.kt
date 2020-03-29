/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

import java.util.*

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ReactorStoreFragment @SuppressLint("ValidFragment") constructor(
    val reactorStore: ReactorStore = ReactorStore()) : Fragment() {

    init {
        retainInstance = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reactorStoreFragmentManager.reactorStoreFragmentCreated(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        reactorStore.clear()
    }

    companion object {
        private val reactorStoreFragmentManager = ReactorStoreFragmentManager()

        fun reactorStoreFragmentFor(fragment: Fragment) : ReactorStoreFragment {
            return reactorStoreFragmentManager.reactorStoreFragmentFor(fragment)
        }

        fun reactorStoreFragmentFor(activity: FragmentActivity) : ReactorStoreFragment {
            return reactorStoreFragmentManager.reactorStoreFragmentFor(activity)
        }
    }

    internal class ReactorStoreFragmentManager {
        private val notCommittedActivityHolders = HashMap<Activity, ReactorStoreFragment>()
        private val notCommittedFragmentHolders = HashMap<Fragment, ReactorStoreFragment>()

        private val activityCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {}

            override fun onActivityResumed(activity: Activity?) {}

            override fun onActivityStarted(activity: Activity?) {}

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

            override fun onActivityStopped(activity: Activity?) {}

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

            override fun onActivityDestroyed(activity: Activity?) {
                val fragment = notCommittedActivityHolders.remove(activity!!)
                if (fragment != null) {
//                    Log.e(LOG_TAG, "Failed to save a ViewModel for " + activity!!)
                }
            }
        }

        private var activityCallbacksIsAdded = false

        private val parentDestroyedCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, parentFragment: Fragment) {
                super.onFragmentDestroyed(fm, parentFragment)
                val fragment = notCommittedFragmentHolders.remove(parentFragment)
                if (fragment != null) {
//                    Log.e(LOG_TAG, "Failed to save a ViewModel for " + parentFragment!!)
                }
            }
        }

        fun reactorStoreFragmentCreated(holderFragment: Fragment) {
            val parentFragment = holderFragment.parentFragment
            if (parentFragment != null) {
                notCommittedFragmentHolders.remove(parentFragment)
                parentFragment.parentFragmentManager.unregisterFragmentLifecycleCallbacks(parentDestroyedCallback)
            } else {
                notCommittedActivityHolders.remove(holderFragment.requireActivity())
            }
        }

        fun reactorStoreFragmentFor(activity: FragmentActivity): ReactorStoreFragment {
            val fm = activity.supportFragmentManager
            var reactorStoreFragment = findReactorStoreFragment(fm)
            if (reactorStoreFragment != null) {
                return reactorStoreFragment
            }
            reactorStoreFragment = notCommittedActivityHolders[activity]
            if (reactorStoreFragment != null) {
                return reactorStoreFragment
            }

            if (!activityCallbacksIsAdded) {
                activityCallbacksIsAdded = true
                activity.application.registerActivityLifecycleCallbacks(activityCallbacks)
            }
            reactorStoreFragment = createReactorStoreFragment(fm)
            notCommittedActivityHolders[activity] = reactorStoreFragment
            return reactorStoreFragment
        }

        fun reactorStoreFragmentFor(parentFragment: Fragment): ReactorStoreFragment {
            val fm = parentFragment.childFragmentManager
            var reactorStoreFragment = findReactorStoreFragment(fm)
            if (reactorStoreFragment != null) {
                return reactorStoreFragment
            }
            reactorStoreFragment = notCommittedFragmentHolders[parentFragment]
            if (reactorStoreFragment != null) {
                return reactorStoreFragment
            }

            parentFragment.parentFragmentManager.registerFragmentLifecycleCallbacks(parentDestroyedCallback, false)
            reactorStoreFragment = createReactorStoreFragment(fm)
            notCommittedFragmentHolders[parentFragment] = reactorStoreFragment
            return reactorStoreFragment
        }

        private fun findReactorStoreFragment(manager: FragmentManager): ReactorStoreFragment? {
            if (manager.isDestroyed) {
                throw IllegalStateException("Can't access ViewModels from onDestroy")
            }

            val fragmentByTag = manager.findFragmentByTag(STORE_FRAGMENT_TAG)
            if (fragmentByTag != null && fragmentByTag !is ReactorStoreFragment) {
                throw IllegalStateException("Unexpected fragment instance was returned by HOLDER_TAG")
            }
            return fragmentByTag as? ReactorStoreFragment
        }

        private fun createReactorStoreFragment(fragmentManager: FragmentManager): ReactorStoreFragment {
            val newStoreFragment = ReactorStoreFragment()
            fragmentManager
                .beginTransaction()
                .add(newStoreFragment, STORE_FRAGMENT_TAG)
                .commitAllowingStateLoss()
            return newStoreFragment
        }

        companion object {
            private const val STORE_FRAGMENT_TAG = "com.gyurigrell.rxreactor.ReactorStoreFragment"
        }
    }
}
