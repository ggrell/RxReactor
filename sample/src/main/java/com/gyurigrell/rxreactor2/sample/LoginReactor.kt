/*
 * Copyright (c) 2020, Gyuri Grell and RxReactor contributors. All rights reserved
 *
 * Licensed under BSD 3-Clause License.
 * https://opensource.org/licenses/BSD-3-Clause
 */

package com.gyurigrell.rxreactor2.sample

import android.accounts.Account
import android.os.Parcelable
import com.gyurigrell.rxreactor2.android.AndroidReactorWithEffects
import com.gyurigrell.rxreactor2.sample.LoginReactor.Action
import com.gyurigrell.rxreactor2.sample.LoginReactor.Effect
import com.gyurigrell.rxreactor2.sample.LoginReactor.Mutation
import com.gyurigrell.rxreactor2.sample.LoginReactor.State
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

/**
 * Do not let me check this in without adding a comment about the class.
 */
class LoginReactor(
    private val contactService: ContactService,
    initialState: State = State()
) : AndroidReactorWithEffects<Action, Mutation, State, Effect>(
    initialState
) {

    sealed class Action {
        object EnterScreen : Action()
        data class UsernameChanged(val username: String) : Action()
        data class PasswordChanged(val password: String) : Action()
        object Login : Action()
        object PopulateAutoComplete : Action()
    }

    sealed class Mutation {
        data class SetUsername(val username: String) : Mutation()
        data class SetPassword(val password: String) : Mutation()
        data class SetBusy(val busy: Boolean) : Mutation()
        data class SetAutoCompleteEmails(val emails: List<String>) : Mutation()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        data class LoggedIn(val account: Account) : Effect()
    }

    @Parcelize
    data class State(
        val username: String = "",
        val password: String = "",
        val environment: String = "",
        val isUsernameValid: Boolean = true,
        val isPasswordValid: Boolean = true,
        val isBusy: Boolean = false,
//            val account: Account? = null,
        val autoCompleteEmails: List<String>? = null
    ) : Parcelable {
        val loginEnabled: Boolean
            get() = (isUsernameValid && username.isNotEmpty()) && (isPasswordValid && password.isNotEmpty())
    }

    override fun mutate(action: Action): Observable<Mutation> = when (action) {
        is Action.EnterScreen ->
            Observable.empty()

        is Action.UsernameChanged ->
            Observable.just(Mutation.SetUsername(action.username))

        is Action.PasswordChanged ->
            Observable.just(Mutation.SetPassword(action.password))

        is Action.Login ->
            login()

        is Action.PopulateAutoComplete ->
            loadEmails()
    }

    override fun reduce(state: State, mutation: Mutation) = when (mutation) {
        is Mutation.SetUsername ->
            state.copy(
                username = mutation.username,
                isUsernameValid = mutation.username.isNotBlank()
            )

        is Mutation.SetPassword ->
            state.copy(
                password = mutation.password,
                isPasswordValid = mutation.password.isNotBlank()
            )

        is Mutation.SetBusy ->
            state.copy(isBusy = mutation.busy)

        is Mutation.SetAutoCompleteEmails ->
            state.copy(autoCompleteEmails = mutation.emails)
    }

    private fun loadEmails(): Observable<Mutation> =
        contactService.loadEmails().map { Mutation.SetAutoCompleteEmails(it) }

    /**
     * Fake login that accepts two values in [LoginReactor.DUMMY_CREDENTIALS] as valid logins.
     */
    private fun login(): Observable<Mutation> =
        Observable.just(DUMMY_CREDENTIALS.contains("${currentState.username}:${currentState.password}"))
            .delay(3, TimeUnit.SECONDS)
            .flatMap<Mutation> { success ->
                if (success) {
                    emitEffect(Effect.LoggedIn(Account("test", "test")))
                } else {
                    emitEffect(Effect.ShowError("Some error message"))
                }
                Observable.just(Mutation.SetBusy(false))
            }
            .startWith(Mutation.SetBusy(true))

    companion object {
        private val DUMMY_CREDENTIALS = arrayOf("joe@example.com:hello", "kamala@example.com:world")
    }
}
