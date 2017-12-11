package com.gyurigrell.rxreactor.sample

import android.accounts.Account
import android.util.Log
import com.gyurigrell.rxreactor.Reactor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Do not let me check this in without adding a comment about the class.
 */
class LoginReactor(val contactService: ContactService) :
        Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>(
                initialState = LoginReactor.State(),
                debug = true) {

    sealed class Action {
        object EnterScreen : Action()
        data class UsernameChanged(val username: String) : Action()
        data class PasswordChanged(val password: String) : Action()
        object Login : Action()
        object PopulateAutoComplete : Action()
    }

    sealed class Mutation {
        object ResetState : Mutation()
        data class SetUsername(val username: String) : Mutation()
        data class SetPassword(val password: String) : Mutation()
        data class SetBusy(val busy: Boolean) : Mutation()
        data class LoggedIn(val account: Account) : Mutation()
        data class SetError(val message: String) : Mutation()
        data class SetAutoCompleteEmails(val emails: List<String>) : Mutation()
    }

    sealed class Trigger {
        data class ShowError(val message: String) : Trigger()
    }

    data class State(
            val username: String = "",
            val password: String = "",
            val environment: String = "",
            val isUsernameValid: Boolean = true,
            val isPasswordValid: Boolean = true,
            val isBusy: Boolean = false,
            val account: Account? = null,
            val autoCompleteEmails: List<String>? = null,
            val trigger: Trigger? = null
    ) {
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
                return Observable.just(Mutation.ResetState)
            }
            is Action.UsernameChanged -> {
                return Observable.just(Mutation.SetUsername(action.username))
            }
            is Action.PasswordChanged -> {
                return Observable.just(Mutation.SetPassword(action.password))
            }
            is Action.Login -> {
                val observables = arrayListOf(Observable.just(Mutation.SetBusy(true)), login())
                return Observable.concat(observables)
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
                        trigger = null,
                        username = mutation.username,
                        isUsernameValid = isTextValid(mutation.username))
            }
            is Mutation.SetPassword -> {
                return state.copy(
                        trigger = null,
                        password = mutation.password,
                        isPasswordValid = isTextValid(mutation.password))
            }
            is Mutation.SetBusy -> {
                return state.copy(
                        trigger = null,
                        isBusy = mutation.busy,
                        account = null)
            }
            is Mutation.SetError -> {
                return state.copy(
                        trigger = Trigger.ShowError(mutation.message),
                        isBusy = false)
            }
            is Mutation.LoggedIn -> {
                return state.copy(
                        trigger = null,
                        isBusy = false,
                        account = mutation.account)
            }
            is Mutation.SetAutoCompleteEmails -> {
                return state.copy(
                        trigger = null,
                        autoCompleteEmails = mutation.emails)
            }
            else -> return state
        }
    }

    override fun logDebug(message: String) {
        Log.d("LoginReactor", message)
    }

    private fun loadEmails(): Observable<Mutation> {
        return contactService.loadEmails().map { Mutation.SetAutoCompleteEmails(it) }
    }

    private fun login(): Observable<Mutation> {
        return Observable.just(Math.random() > 0.8)
                .delay(1, TimeUnit.SECONDS) //
                .flatMap { success ->
                    val mutations = arrayListOf<Mutation>(Mutation.SetBusy(false))
                    if (success) {
                        mutations.add(Mutation.LoggedIn(Account("test", "test")))
                    } else {
                        mutations.add(Mutation.SetError("Some error message"))
                    }
                    Observable.fromIterable(mutations.asIterable())
                }
    }

    private fun isTextValid(text: String): Boolean {
        if (text.isEmpty()) return false
        return true
    }
}

