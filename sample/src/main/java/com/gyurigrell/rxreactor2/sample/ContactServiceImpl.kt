package com.gyurigrell.rxreactor2.sample

import android.app.Activity
import android.net.Uri
import android.provider.ContactsContract
import io.reactivex.Observable
import io.reactivex.Scheduler

class ContactServiceImpl(
    private val context: Activity,
    private val scheduler: Scheduler) : ContactService {
    override fun loadEmails(): Observable<List<String>> {
        return Observable.create<List<String>> { emitter ->
            val cursor = context.contentResolver.query(ProfileQuery.URI,
                                                       ProfileQuery.PROJECTION,
                                                       ProfileQuery.SELECTION,
                                                       ProfileQuery.SELECTION_ARGS,
                                                       ProfileQuery.SORT_ORDER)
            if (cursor == null) {
                emitter.onNext(listOf())
                emitter.onComplete()
                return@create
            }
            try {
                val emails = mutableListOf<String>()
                while (cursor.moveToNext() && !emitter.isDisposed) {
                    emails.add(cursor.getString(ProfileQuery.ADDRESS))
                }
                emitter.onNext(emails)
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            } finally {
                cursor.close()
            }

        }.subscribeOn(scheduler)
    }

    object ProfileQuery {
        val URI: Uri = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
        val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        const val ADDRESS = 0

        const val SELECTION = ContactsContract.Contacts.Data.MIMETYPE + " = ?"
        val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        const val SORT_ORDER = ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
    }
}
