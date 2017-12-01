package com.gyurigrell.rxreactor.sample

import android.accounts.Account
import com.gyurigrell.rxreactor.Reactor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Do not let me check this in without adding a comment about the class.
 */
class LoginReactor :
        Reactor<LoginReactor.Action, LoginReactor.Mutation, LoginReactor.State>(initialState = LoginReactor.State()) {

    sealed class Action {
        class EnterScreen : Action()
        class UsernameChanged(val username: String) : Action()
        class PasswordChanged(val password: String) : Action()
        class Login : Action()
    }

    sealed class Mutation {
        class ResetState : Mutation()
        class SetUsername(val username: String) : Mutation()
        class SetPassword(val password: String) : Mutation()
        class SetBusy(val busy: Boolean) : Mutation()
        class LoggedIn(val account: Account) : Mutation()
        class SetError(val message: String) : Mutation()
    }

    data class State(
            val username: String = "",
            val password: String = "",
            val environment: String = "",
            val isUsernameValid: Boolean = true,
            val isPasswordValid: Boolean = true,
            val loginError: String? = null,
            val isBusy: Boolean = false,
            val account: Account? = null
    ) {
        val loginEnabled: Boolean
            get() = (isUsernameValid && username.isNotEmpty()) && (isPasswordValid && password.isNotEmpty())
    }

    override fun transformState(state: Observable<State>): Observable<State> {
        return state.observeOn(AndroidSchedulers.mainThread())
    }

    override fun mutate(action: Action): Observable<Mutation> {
        when (action) {
            is Action.EnterScreen -> {
//                analytics.enterScreen(screenName: AnalyticsConstants.Login.loginScreen, properties: nil)
                return Observable.just(Mutation.ResetState())
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
        }
    }

    private fun login(): Observable<Mutation> {
        return Observable.just(Math.random() > 0.5)
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

    override fun reduce(state: State, mutation: Mutation): State {
        when (mutation) {
            is Mutation.SetUsername -> {
                return state.copy(
                        loginError = null,
                        username = mutation.username,
                        isUsernameValid = isTextValid(mutation.username))
            }
            is Mutation.SetPassword -> {
                return state.copy(
                        loginError = null,
                        password = mutation.password,
                        isPasswordValid = isTextValid(mutation.password))
            }
            is Mutation.SetBusy -> {
                return state.copy(
                        isBusy = mutation.busy,
                        account = null,
                        loginError = null)
            }
            is Mutation.SetError -> {
                return state.copy(
                        isBusy = false,
                        loginError = mutation.message)
            }
            is Mutation.LoggedIn -> {
                return state.copy(
                        isBusy = false,
                        loginError = null,
                        account = mutation.account)
            }
            else -> return state
        }
    }

    private fun isTextValid(text: String): Boolean {
        if (text.isEmpty()) return false
        return true
    }
}

