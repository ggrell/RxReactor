package com.gyurigrell.rxreactor.sample

import io.reactivex.Observable

/**
 * Do not let me check this in without adding a comment about the class.
 */
interface ContactService {
    fun loadEmails(): Observable<List<String>>
}
