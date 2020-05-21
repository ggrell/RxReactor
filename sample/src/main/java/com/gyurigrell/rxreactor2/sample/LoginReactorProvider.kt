/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoginReactorProvider private constructor(private val contactService: ContactService) : ViewModel() {
    var initialState: LoginReactor.State? = null
    val reactor: LoginReactor
        get() = LoginReactor(contactService, initialState
            ?: LoginReactor.State())

    override fun onCleared() {
        reactor.clearSubscriptions()
    }

    class Factory(private val contactService: ContactService): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginReactorProvider(contactService) as T
    }
}