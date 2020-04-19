/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.sample

import android.accounts.Account
import com.gyurigrell.rxreactor2.android.AndroidReactorWithEffects
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Do not let me check this in without adding a comment about the class.
 */
class LoginViewModel(
        private val contactService: ContactService,
        initialState: State = State()
) : AndroidReactorWithEffects<LoginViewModel.Action, LoginViewModel.Mutation, LoginViewModel.State, LoginViewModel.Effect>(
        initialState) {

    sealed class Action {
        object EnterScreen : Action()
        data class UsernameChanged(val username: String) : Action()
        data class PasswordChanged(val password: String) : Action()
        object Login : Action()
        object PopulateAutoComplete : Action()
    }

    sealed class Mutation: MutationWithEffect<Effect> {
        data class SetUsername(val username: String) : Mutation()
        data class SetPassword(val password: String) : Mutation()
        data class SetBusy(val busy: Boolean) : Mutation()
//        data class LoggedIn(val account: Account) : Mutation()
//        data class SetError(val message: String) : Mutation()
        data class SetAutoCompleteEmails(val emails: List<String>) : Mutation()
        data class EmitEffect(override val effect: Effect) : Mutation()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        data class LoggedIn(val account: Account) : Effect()
    }

    data class State(
            val username: String = "",
            val password: String = "",
            val environment: String = "",
            val isUsernameValid: Boolean = true,
            val isPasswordValid: Boolean = true,
            val isBusy: Boolean = false,
//            val account: Account? = null,
            val autoCompleteEmails: List<String>? = null
    ) : Serializable {
        val loginEnabled: Boolean
            get() = (isUsernameValid && username.isNotEmpty()) && (isPasswordValid && password.isNotEmpty())
    }

    override fun transformState(state: Observable<State>): Observable<State> {
        return super.transformState(state).observeOn(AndroidSchedulers.mainThread())
    }

    override fun mutate(action: Action): Observable<Mutation> {
        when (action) {
            is Action.EnterScreen -> {
//                analytics.enterScreen(screenName: AnalyticsConstants.Login.loginScreen, properties: nil)
                return Observable.empty()
            }

            is Action.UsernameChanged -> {
                return Observable.just(Mutation.SetUsername(action.username))
            }

            is Action.PasswordChanged -> {
                return Observable.just(Mutation.SetPassword(action.password))
            }

            is Action.Login -> {
                return login()
            }

            is Action.PopulateAutoComplete -> {
                return loadEmails()
            }
        }
    }

    override fun reduce(state: State, mutation: Mutation): State {
        when (mutation) {
            is Mutation.SetUsername -> {
                return state.copy(
                        username = mutation.username,
                        isUsernameValid = isTextValid(mutation.username))
            }

            is Mutation.SetPassword -> {
                return state.copy(
                        password = mutation.password,
                        isPasswordValid = isTextValid(mutation.password))
            }

            is Mutation.SetBusy -> {
                return state.copy(isBusy = mutation.busy)
            }

            is Mutation.SetAutoCompleteEmails -> {
                return state.copy(autoCompleteEmails = mutation.emails)
            }

            else -> return state
        }
    }

    private fun loadEmails(): Observable<Mutation> {
        return contactService.loadEmails().map { Mutation.SetAutoCompleteEmails(it) }
    }

    /**
     * Fake login that accepts two values in [LoginViewModel.DUMMY_CREDENTIALS] as valid logins.
     */
    private fun login(): Observable<Mutation> {
        return Observable.just(DUMMY_CREDENTIALS.contains("${currentState.username}:${currentState.password}"))
                .delay(3, TimeUnit.SECONDS)
                .flatMap { success ->
                    val triggerEffect = if (success) {
                        Mutation.EmitEffect(Effect.LoggedIn(Account("test", "test")))
                    } else {
                        Mutation.EmitEffect(Effect.ShowError("Some error message"))
                    }
                    Observable.just(Mutation.SetBusy(false), triggerEffect)
                }
                .startWith(Mutation.SetBusy(true))
    }

    private fun isTextValid(text: String): Boolean {
        if (text.isEmpty()) return false
        return true
    }

    companion object {
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }
}

