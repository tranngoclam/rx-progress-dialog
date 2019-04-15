/*
 * MIT License
 *
 * Copyright (c) 2017 Lam Tran (tranngoclam288@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.lamtran.rpd

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.concurrent.CancellationException

class RxProgressDialog private constructor(private val builder: Builder) {

    private fun <T> forFlowable(source: Flowable<T>, backpressureStrategy: BackpressureStrategy): Flowable<T> = Flowable.using(
            { this.makeDialog() },
            { dialog -> process(source, backpressureStrategy, dialog) },
            { it.dismiss() }
    )

    private fun <T> forObservable(source: Observable<T>): Observable<T> = Observable.using(
            { this.makeDialog() },
            { dialog -> process(source, dialog) },
            { it.dismiss() }
    )

    private fun <T> process(upstream: Observable<T>, dialog: AlertDialog): Observable<T> =
            Observable.create { emitter ->
                dialog.setOnCancelListener { emitter.onError(CancellationException()) }
                val disposable = upstream.subscribe(
                        { emitter.onNext(it) },
                        { emitter.onError(it) },
                        { emitter.onComplete() }
                )
                emitter.setDisposable(disposable)
            }

    private fun <T> process(upstream: Flowable<T>, backpressureStrategy: BackpressureStrategy, dialog: AlertDialog): Flowable<T> =
            Flowable.create({ emitter ->
                dialog.setOnCancelListener { emitter.onError(CancellationException()) }
                val disposable = upstream.subscribe(
                        { emitter.onNext(it) },
                        { emitter.onError(it) },
                        { emitter.onComplete() }
                )
                emitter.setDisposable(disposable)
            }, backpressureStrategy)

    private fun makeDialog(): AlertDialog {
        val view = LayoutInflater.from(builder.activity).inflate(R.layout.dialog_spinner, null, false)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress)
        progressBar.isIndeterminate = builder.indeterminate

        val message = view.findViewById<AppCompatTextView>(R.id.message)
        message.text = builder.message

        val dialog = AlertDialog.Builder(builder.activity)
                .setView(view)
                .setCancelable(builder.cancelable)
        return dialog.show()
    }

    class Builder(val activity: Activity) {

        var cancelable: Boolean = false

        var indeterminate: Boolean = false

        var message: CharSequence? = null

        init {
            this.indeterminate = true
            this.message = activity.getString(R.string.loading)
        }

        fun <T> forFlowable(source: Flowable<T>, backpressureStrategy: BackpressureStrategy): Flowable<T> =
                RxProgressDialog(this).forFlowable(source, backpressureStrategy)

        fun <T> forObservable(source: Observable<T>): Observable<T> =
                RxProgressDialog(this).forObservable(source)

        fun withCancelable(cancelable: Boolean): Builder = apply { this.cancelable = cancelable }

        fun withIndeterminate(indeterminate: Boolean): Builder = apply { this.indeterminate = indeterminate }

        fun withMessage(@StringRes message: Int): Builder = apply { this.message = activity.getString(message) }

        fun withMessage(message: CharSequence): Builder = apply { this.message = message }
    }

    companion object {

        fun from(activity: Activity): Builder = Builder(activity)
    }
}
