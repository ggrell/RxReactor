package com.gyurigrell.rxreactor2.sample

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.gyurigrell.rxreactor2.Reactor
import com.gyurigrell.rxreactor2.ReactorView
import com.jakewharton.rxbinding2.support.design.widget.RxTextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), ReactorView<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State> {
    override var disposeBag = CompositeDisposable()
    override var reactor: Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>? = null
        set(value) {
            if (value != null) {
                bind(value)
            }
            field = value
        }

    override fun bind(reactor: Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>) {
        bindActions(reactor)
        bindViewState(reactor)
    }

    private fun bindActions(reactor: Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>) {
        // Subscribe to UI changes, convert to actions and push to reactor
        RxTextView.textChanges(email)
                .skipInitialValue()
                .map { LoginReactor.Action.UsernameChanged(it.toString()) }
                .subscribe(reactor.action)

        RxTextView.textChanges(password)
                .skipInitialValue()
                .map { LoginReactor.Action.PasswordChanged(it.toString()) }
                .subscribe(reactor.action)

        RxView.clicks(email_sign_in_button)
                .map { LoginReactor.Action.Login }
                .subscribe(reactor.action)
    }

    private fun bindViewState(reactor: Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>) {
        // Subscribe to state changes from the reactor and bind to UI
        reactor.state.flatMapMaybe { if (it.autoCompleteEmails == null) Maybe.empty() else Maybe.just(it.autoCompleteEmails) }
                .subscribe(this::addEmailsToAutoComplete)

        reactor.state.map { it.isBusy }
                .distinctUntilChanged()
                .subscribe(RxView.visibility(login_progress))

        reactor.state.map { !it.isBusy }
                .distinctUntilChanged()
                .subscribe(RxView.visibility(login_form))

        reactor.state.map { if (it.isUsernameValid) "" else "Invalid username" }
                .distinctUntilChanged()
                .subscribe(RxTextInputLayout.error(email_input_layout))

        reactor.state.map { if (it.isPasswordValid) "" else "Invalid password" }
                .distinctUntilChanged()
                .subscribe(RxTextInputLayout.error(password_input_layout))

        reactor.state.map { it.loginEnabled }
                .distinctUntilChanged()
                .subscribe(RxView.enabled(email_sign_in_button))

        reactor.state.flatMapMaybe { if (it.trigger == null) Maybe.empty() else Maybe.just(it.trigger) }
                .subscribe { trigger ->
                    when (trigger) {
                        is LoginReactor.Trigger.ShowError -> {
                            Snackbar.make(login_form, trigger.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }

        reactor.state.flatMapMaybe { if (it.account == null) Maybe.empty() else Maybe.just(it.account) }
                .subscribe { account ->
                    Snackbar.make(login_form, "Login succeeded", Snackbar.LENGTH_SHORT)
                            .addCallback(object : Snackbar.Callback() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    finish()
                                }
                            })
                            .show()
                }
    }

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        reactor = LoginReactor(contactService = ContactServiceImpl(this, Schedulers.io()))
        populateAutoComplete()
//        // Set up the login form.
//        populateAutoComplete()
//        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
//            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                attemptLogin()
//                return@OnEditorActionListener true
//            }
//            false
//        })
//
//        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        reactor?.action?.accept(LoginReactor.Action.PopulateAutoComplete)
    }

    private fun mayRequestContacts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
//    private fun attemptLogin() {
//        if (mAuthTask != null) {
//            return
//        }
//
//        // Reset errors.
//        email.error = null
//        password.error = null
//
//        // Store values at the time of the login attempt.
//        val emailStr = email.text.toString()
//        val passwordStr = password.text.toString()
//
//        var cancel = false
//        var focusView: View? = null
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
//            password.error = getString(R.string.error_invalid_password)
//            focusView = password
//            cancel = true
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(emailStr)) {
//            email.error = getString(R.string.error_field_required)
//            focusView = email
//            cancel = true
//        } else if (!isEmailValid(emailStr)) {
//            email.error = getString(R.string.error_invalid_email)
//            focusView = email
//            cancel = true
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView?.requestFocus()
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true)
//            mAuthTask = UserLoginTask(emailStr, passwordStr)
//            mAuthTask!!.execute(null as Void?)
//        }
//    }

//    private fun isEmailValid(email: String): Boolean {
//        //TODO: Replace this with your own logic
//        return email.contains("@")
//    }

//    private fun isPasswordValid(password: String): Boolean {
//        //TODO: Replace this with your own logic
//        return password.length > 4
//    }

    /**
     * Shows the progress UI and hides the login form.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private fun showProgress(show: Boolean) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
//
//            login_form.visibility = if (show) View.GONE else View.VISIBLE
//            login_form.animate()
//                    .setDuration(shortAnimTime)
//                    .alpha((if (show) 0 else 1).toFloat())
//                    .setListener(object : AnimatorListenerAdapter() {
//                        override fun onAnimationEnd(animation: Animator) {
//                            login_form.visibility = if (show) View.GONE else View.VISIBLE
//                        }
//                    })
//
//            login_progress.visibility = if (show) View.VISIBLE else View.GONE
//            login_progress.animate()
//                    .setDuration(shortAnimTime)
//                    .alpha((if (show) 1 else 0).toFloat())
//                    .setListener(object : AnimatorListenerAdapter() {
//                        override fun onAnimationEnd(animation: Animator) {
//                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
//                        }
//                    })
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            login_progress.visibility = if (show) View.VISIBLE else View.GONE
//            login_form.visibility = if (show) View.GONE else View.VISIBLE
//        }
//    }

//    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
//        return CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
//                ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE + " = ?",
//                arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
//    }

//    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
//        val emails = ArrayList<String>()
//        cursor.moveToFirst()
//        while (!cursor.isAfterLast) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS))
//            cursor.moveToNext()
//        }
//
//        addEmailsToAutoComplete(emails)
//    }

//    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {
//
//    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

//    object ProfileQuery {
//        val PROJECTION = arrayOf(
//                ContactsContract.CommonDataKinds.Email.ADDRESS,
//                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
//        val ADDRESS = 0
//        val IS_PRIMARY = 1
//    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
//    inner class UserLoginTask internal constructor(private val mEmail: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {
//
//        override fun doInBackground(vararg params: Void): Boolean? {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000)
//            } catch (e: InterruptedException) {
//                return false
//            }
//
//            return DUMMY_CREDENTIALS
//                    .map { it.split(":") }
//                    .firstOrNull { it[0] == mEmail }
//                    ?.let {
//                        // Account exists, return true if the password matches.
//                        it[1] == mPassword
//                    }
//                    ?: true
//        }
//
//        override fun onPostExecute(success: Boolean?) {
//            mAuthTask = null
//            showProgress(false)
//
//            if (success!!) {
//                finish()
//            } else {
//                password.error = getString(R.string.error_incorrect_password)
//                password.requestFocus()
//            }
//        }
//
//        override fun onCancelled() {
//            mAuthTask = null
//            showProgress(false)
//        }
//    }

    companion object {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private val REQUEST_READ_CONTACTS = 0

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}
