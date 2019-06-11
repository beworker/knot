package de.halfbit.knot

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/** Creates a [TestCompositeKnot]. */
fun <State : Any> testCompositeKnot(
    block: CompositeKnotBuilder<State>.() -> Unit
): TestCompositeKnot<State> {
    val actionSubject = PublishSubject.create<Any>()
    return CompositeKnotBuilder<State>()
        .also(block)
        .build(actionSubject)
        .let { compositeKnot ->
            DefaultTestCompositeKnot(
                compositeKnot,
                actionSubject
            )
        }
}

interface TestCompositeKnot<State : Any> : CompositeKnot<State> {
    val action: Observable<Any>
    val actionConsumer: Consumer<Any>
}

internal class DefaultTestCompositeKnot<State : Any>(
    private val compositeKnot: DefaultCompositeKnot<State>,
    private val actionSubject: PublishSubject<Any>
) : TestCompositeKnot<State> {

    override val action: Observable<Any> = actionSubject
    override val actionConsumer: Consumer<Any> = Consumer { actionSubject.onNext(it) }

    override fun <Change : Any, Action : Any> registerPrime(
        block: PrimeBuilder<State, Change, Action>.() -> Unit
    ) = compositeKnot.registerPrime(block)

    override fun compose() = compositeKnot.compose()
    override val change: Consumer<Any> = compositeKnot.change
    override val state: Observable<State> = compositeKnot.state
    override val disposable: Disposable = compositeKnot.disposable
}
