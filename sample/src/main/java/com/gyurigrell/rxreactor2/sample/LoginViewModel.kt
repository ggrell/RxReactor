package com.gyurigrell.rxreactor2.sample

import android.accounts.Account
import android.os.Parcelable
import android.util.Log
import com.gyurigrell.rxreactor2.Reactor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Do not let me check this in without adding a comment about the class.
 */
class LoginViewModel(private val contactService: ContactService,
                     initialState: State = State(),
                     debug: Boolean = false) :
    Reactor<LoginViewModel.Action, LoginViewModel.Mutation, LoginViewModel.State>(
        initialState,
        debug) {

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
        @IgnoredOnParcel val trigger: Trigger? = null
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
        Log.d("LoginViewModel", message)
    }

    private fun loadEmails(): Observable<Mutation> {
        return contactService.loadEmails().map { Mutation.SetAutoCompleteEmails(it) }
    }

    /**
     * Fake login that accepts two values in [LoginViewModel.DUMMY_CREDENTIALS] as valid logins.
     */
    private fun login(): Observable<Mutation> {
        return Observable.just(DUMMY_CREDENTIALS.contains("${currentState.username}:${currentState.password}"))
            .delay(20, TimeUnit.SECONDS)
            .flatMap { success ->
                val mutation = if (success) {
                    Mutation.LoggedIn(Account("test", "test"))
                } else {
                    Mutation.SetError("Some error message")
                }
                Observable.just(mutation, Mutation.SetBusy(false))
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

