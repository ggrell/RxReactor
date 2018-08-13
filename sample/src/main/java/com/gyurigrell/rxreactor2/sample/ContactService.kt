package com.gyurigrell.rxreactor2.sample

import io.reactivex.Observable

/**
 * Handle access to a list of contacts that will be used in the email text view
 */
interface ContactService {
    fun loadEmails(): Observable<List<String>>
}
