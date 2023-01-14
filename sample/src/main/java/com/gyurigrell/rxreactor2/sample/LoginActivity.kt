/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.sample

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.gyurigrell.rxreactor2.sample.databinding.ActLoginBinding
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    private var disposeBag = CompositeDisposable()

    private lateinit var reactor: LoginReactor

    private val contactService = ContactServiceImpl(this, Schedulers.io())
    private val reactorProvider: LoginReactorProvider by viewModels { LoginReactorProvider.Factory(contactService) }

    private lateinit var binding: ActLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Settings the initial state in the factory is optional and only needed if the state needs
        // to survive the app getting killed in the background.
        reactorProvider.initialState = savedInstanceState?.getParcelable(VIEW_STATE_KEY)
        reactor = reactorProvider.reactor
        bind(reactor)

        populateAutoComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // The following is optional and only needed if something in your state needs to survive
        // the app getting killed in the background. Be aware that there are strict limits on the
        // size of what can go into the view state
        outState.putParcelable(VIEW_STATE_KEY, reactor.currentState)
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        reactor.action.accept(LoginReactor.Action.PopulateAutoComplete)
    }

    private fun mayRequestContacts(): Boolean {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(binding.email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) }
                .show()
        } else {
            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
        }
        return false
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        // Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(
            this@LoginActivity,
            android.R.layout.simple_dropdown_item_1line,
            emailAddressCollection
        )

        binding.email.setAdapter(adapter)
    }

    private fun bind(reactor: LoginReactor) {
        bindActions(reactor)
        bindViewState(reactor)
    }

    private fun bindActions(reactor: LoginReactor) {
        // Subscribe to UI changes, convert to actions and push to viewModel
        binding.email.textChanges()
            .skipInitialValue()
            .map { LoginReactor.Action.UsernameChanged(it.toString()) }
            .subscribe(reactor.action)
            .addTo(disposeBag)

        binding.password.textChanges()
            .skipInitialValue()
            .map { LoginReactor.Action.PasswordChanged(it.toString()) }
            .subscribe(reactor.action)
            .addTo(disposeBag)

        binding.signIn.clicks()
            .map { LoginReactor.Action.Login }
            .subscribe(reactor.action)
            .addTo(disposeBag)
    }

    private fun bindViewState(reactor: LoginReactor) {
        // Subscribe to state changes from the viewModel and bind to UI
        reactor.state
            .flatMapMaybe {
                if (it.autoCompleteEmails == null) Maybe.empty() else Maybe.just(it.autoCompleteEmails)
            }
            .subscribe(this::addEmailsToAutoComplete)
            .addTo(disposeBag)

        reactor.state
            .map { it.isBusy }
            .distinctUntilChanged()
            .subscribe(binding.loginProgress.visibility())
            .addTo(disposeBag)

        reactor.state
            .map { !it.isBusy }
            .distinctUntilChanged()
            .subscribe(binding.loginForm.visibility())
            .addTo(disposeBag)

        reactor.state
            .map { if (it.isUsernameValid) "" else getString(R.string.error_invalid_email) }
            .distinctUntilChanged()
            .subscribe(binding.emailLayout::setError)
            .addTo(disposeBag)

        reactor.state
            .map { if (it.isPasswordValid) "" else getString(R.string.error_invalid_password) }
            .distinctUntilChanged()
            .subscribe(binding.passwordLayout::setError)
            .addTo(disposeBag)

        reactor.state
            .map { it.loginEnabled }
            .distinctUntilChanged()
            .subscribe(binding.signIn::setEnabled)
            .addTo(disposeBag)

        reactor.effect
            .subscribe(this::handleEffect)
            .addTo(disposeBag)
    }

    private fun handleEffect(effect: LoginReactor.Effect) = when (effect) {
        is LoginReactor.Effect.ShowError ->
            Snackbar.make(binding.loginForm, effect.message, Snackbar.LENGTH_LONG).show()

        is LoginReactor.Effect.LoggedIn ->
            Snackbar.make(binding.loginForm, "Login succeeded", Snackbar.LENGTH_SHORT)
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        finish()
                    }
                })
                .show()
    }

    companion object {
        private const val VIEW_STATE_KEY = "view_state"

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0
    }
}
