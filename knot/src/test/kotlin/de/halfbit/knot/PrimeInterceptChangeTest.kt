package de.halfbit.knot

import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Test

class PrimeInterceptChangeTest {

    private data class State(val value: String)
    private data class ChangeA(val value: String)
    private data class ChangeB(val value: String)
    private object Action

    @Test
    fun `Prime (single) changes { intercept } receives Change`() {
        val watcher = PublishSubject.create<ChangeA>()
        val observer = watcher.test()
        val knot = compositeKnot<State> {
            state { initial = State("empty") }
        }
        knot.registerPrime<ChangeA, Action> {
            changes {
                reduce<ChangeA> { this + Action }
                intercept<ChangeA> { change ->
                    change.doOnNext {
                        watcher.onNext(it.copy(value = "${it.value} intercepted"))
                    }
                }
            }
        }
        knot.compose()
        knot.change.accept(ChangeA("change a"))
        observer.assertValues(ChangeA("change a intercepted"))
    }

    @Test
    fun `Prime (many) changes { intercept } receives Change`() {
        val watcher = PublishSubject.create<Any>()
        val observer = watcher.test()

        val knot = compositeKnot<State> {
            state { initial = State("empty") }
        }

        knot.registerPrime<ChangeA, Action> {
            changes {
                reduce<ChangeA> { only }
                intercept<ChangeA> { change ->
                    change.doOnNext {
                        if (it.value == "change a") {
                            watcher.onNext(it.copy(value = "${it.value} intercepted"))
                        }
                    }
                }
            }
        }

        knot.registerPrime<ChangeB, Action> {
            changes {
                reduce<ChangeB> { only }
                intercept<ChangeB> { change ->
                    change.doOnNext {
                        if (it.value == "change b") {
                            watcher.onNext(it.copy(value = "${it.value} intercepted"))
                        }
                    }
                }
            }
        }

        knot.compose()
        knot.change.accept(ChangeA("change a"))
        knot.change.accept(ChangeB("change b"))
        observer.assertValues(
            ChangeA("change a intercepted"),
            ChangeB("change b intercepted")
        )
    }
}