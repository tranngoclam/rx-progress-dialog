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

package io.github.lamtran.rpd.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.lamtran.rpd.RxProgressDialog
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var disposables: CompositeDisposable = CompositeDisposable()

    private var flowable: Flowable<String> = Flowable
            .timer(TIME_DELAY_IN_SECONDS, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .map { "User id is " + java.util.UUID.randomUUID().toString() }

    private var observable: Observable<String> = Observable
            .timer(TIME_DELAY_IN_SECONDS, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .map { "User id is " + java.util.UUID.randomUUID().toString() }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_observable ->
                disposables.add(RxProgressDialog.from(this)
                        .withMessage("Logging in...")
                        .forObservable(observable)
                        .subscribe({ id ->
                            Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
                        }, { throwable ->
                            Log.w(TAG, throwable.message)
                        }))
            R.id.button_flowable ->
                disposables.add(RxProgressDialog.from(this)
                        .forFlowable(flowable, BackpressureStrategy.DROP)
                        .subscribe({ id ->
                            Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
                        }, { throwable ->
                            Log.w(TAG, throwable.message)
                        }))
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.button_observable).setOnClickListener(this)
        findViewById<View>(R.id.button_flowable).setOnClickListener(this)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName

        private const val TIME_DELAY_IN_SECONDS = 2L
    }
}
