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

package io.github.lamtran.rpd.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.github.lamtran.rpd.RxProgressDialog;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final long TIME_DELAY = 2000L;

  private CompositeDisposable mCompositeDisposable;

  private Flowable<String> mLoginFlowable;

  private Observable<String> mLoginObservable;

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button_observable:
        mCompositeDisposable.add(RxProgressDialog.from(this)
            .withMessage("Logging in...")
            .forObservable(mLoginObservable)
            .subscribe(id -> Toast.makeText(this, id, Toast.LENGTH_SHORT).show(),
                throwable -> Log.w(TAG, throwable.getMessage())));
        break;
      case R.id.button_flowable:
        mCompositeDisposable.add(RxProgressDialog.from(this)
            .forFlowable(mLoginFlowable, BackpressureStrategy.DROP)
            .subscribe(id -> Toast.makeText(this, id, Toast.LENGTH_SHORT).show(),
                throwable -> Log.w(TAG, throwable.getMessage())));
        break;
      default:
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mLoginObservable = Observable
        .timer(TIME_DELAY, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .map(aLong -> "User id is " + UUID.randomUUID().toString());
    mLoginFlowable = Flowable
        .timer(TIME_DELAY, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        .map(aLong -> "User id is " + UUID.randomUUID().toString());

    mCompositeDisposable = new CompositeDisposable();

    findViewById(R.id.button_observable).setOnClickListener(this);
    findViewById(R.id.button_flowable).setOnClickListener(this);
  }

  @Override
  protected void onDestroy() {
    if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
      mCompositeDisposable.clear();
    }
    super.onDestroy();
  }
}
