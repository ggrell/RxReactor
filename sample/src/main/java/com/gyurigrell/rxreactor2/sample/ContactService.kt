/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.sample

import io.reactivex.Observable

/**
 * Handle access to a list of contacts that will be used in the email text view
 */
interface ContactService {
    fun loadEmails(): Observable<List<String>>
}
