package com.gyurigrell.rxreactor.sample

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
            val cursor = context.contentResolver.query(ProfileQuery.URI, ProfileQuery.PROJECTION, ProfileQuery.SELECTION,
                    ProfileQuery.SELECTION_ARGS, ProfileQuery.SORT_ORDER)
            try {
                val emails = ArrayList<String>(cursor.count)
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
        val URI = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1

        val SELECTION = ContactsContract.Contacts.Data.MIMETYPE + " = ?"
        val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        val SORT_ORDER = ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
    }
}
