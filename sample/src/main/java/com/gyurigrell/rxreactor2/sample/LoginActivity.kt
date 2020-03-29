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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.gyurigrell.rxreactor2.Reactor
import com.gyurigrell.rxreactor2.android.ReactorProvider
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    private var disposeBag = CompositeDisposable()

    // @Inject
    private lateinit var factory: LoginViewModelFactory

    inner class LoginViewModelFactory(private val contactService: ContactService) : ReactorProvider.Factory {

        var initialState: LoginViewModel.State? = null

        override fun <Action, Mutation, State, T : Reactor<Action, Mutation, State>> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(contactService, initialState ?: LoginViewModel.State()) as T
        }
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)

        val contactService = ContactServiceImpl(this, Schedulers.io())
        factory = LoginViewModelFactory(contactService)

        // Settings the initial state in the factory is optional and only needed if the state needs
        // to survive the app getting killed in the background.
        factory.initialState = savedInstanceState?.getSerializable(VIEW_STATE_KEY) as? LoginViewModel.State

        viewModel = ReactorProvider.of(this, factory).get(LoginViewModel::class.java)
        bind(viewModel)

        populateAutoComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // The following is optional and only needed if something in your state needs to survive
        // the app getting killed in the background. Be aware that there are strict limits on the
        // size of what can go into the view state
        outState.putSerializable(VIEW_STATE_KEY, viewModel.currentState)
    }

    private fun populateAutoComplete() {
        if (!mayRequestContacts()) {
            return
        }

        viewModel.action.accept(LoginViewModel.Action.PopulateAutoComplete)
    }

    private fun mayRequestContacts(): Boolean {
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

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line,
                emailAddressCollection)

        email.setAdapter(adapter)
    }

    private fun bind(viewModel: LoginViewModel) {
        bindActions(viewModel)
        bindViewState(viewModel)
    }

    private fun bindActions(viewModel: LoginViewModel) {
        // Subscribe to UI changes, convert to actions and push to viewModel
        email.textChanges()
                .skipInitialValue()
                .map { LoginViewModel.Action.UsernameChanged(it.toString()) }
                .subscribe(viewModel.action)
                .addTo(disposeBag)

        password.textChanges()
                .skipInitialValue()
                .map { LoginViewModel.Action.PasswordChanged(it.toString()) }
                .subscribe(viewModel.action)
                .addTo(disposeBag)

        email_sign_in_button.clicks()
                .map { LoginViewModel.Action.Login }
                .subscribe(viewModel.action)
                .addTo(disposeBag)
    }

    private fun bindViewState(viewModel: LoginViewModel) {
        // Subscribe to state changes from the viewModel and bind to UI
        viewModel.state.flatMapMaybe {
            if (it.autoCompleteEmails == null) Maybe.empty() else Maybe.just(it.autoCompleteEmails)
        }
                .subscribe(this::addEmailsToAutoComplete)
                .addTo(disposeBag)

        viewModel.state.map { it.isBusy }
                .distinctUntilChanged()
                .subscribe(login_progress.visibility())
                .addTo(disposeBag)

        viewModel.state.map { !it.isBusy }
                .distinctUntilChanged()
                .subscribe(login_form.visibility())
                .addTo(disposeBag)

        viewModel.state.map { if (it.isUsernameValid) "" else "Invalid username" }
                .distinctUntilChanged()
                .subscribe(email_input_layout::setError)
                .addTo(disposeBag)

        viewModel.state.map { if (it.isPasswordValid) "" else "Invalid password" }
                .distinctUntilChanged()
                .subscribe(password_input_layout::setError)
                .addTo(disposeBag)

        viewModel.state.map { it.loginEnabled }
                .distinctUntilChanged()
                .subscribe(email_sign_in_button::setEnabled)
                .addTo(disposeBag)

        viewModel.effect
                .subscribe(this::handleEffect)
                .addTo(disposeBag)
    }

    private fun handleEffect(effect: LoginViewModel.Effect) = when (effect) {
        is LoginViewModel.Effect.ShowError ->
            Snackbar.make(login_form, effect.message, Snackbar.LENGTH_LONG).show()

        is LoginViewModel.Effect.LoggedIn ->
            Snackbar.make(login_form, "Login succeeded", Snackbar.LENGTH_SHORT)
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
