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

package io.github.lamtran.rpd;

import org.reactivestreams.Publisher;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.annotation.StringRes;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public final class RxProgressDialog {

  public static Builder from(@NonNull Activity activity) {
    return new Builder(activity);
  }

  private final Builder builder;

  private RxProgressDialog(Builder builder) {
    this.builder = builder;
  }

  private <T> Flowable<T> forFlowable(Flowable<T> source, BackpressureStrategy backpressureStrategy) {
    return Flowable.using(this::makeDialog,
        new Function<ProgressDialog, Publisher<? extends T>>() {
          @Override
          public Publisher<? extends T> apply(@NonNull ProgressDialog dialog) throws Exception {
            return Flowable.create(emitter -> {
              if (builder.cancelable) {
                dialog.setOnCancelListener(dialogInterface -> emitter.onComplete());
              }
              dialog.setOnDismissListener(dialogInterface -> emitter.onComplete());
              source.subscribe(emitter::onNext, emitter::onError, emitter::onComplete);
            }, backpressureStrategy);
          }
        }, Dialog::dismiss);
  }

  private <T> Observable<T> forObservable(Observable<T> source) {
    return Observable.using(this::makeDialog,
        new Function<ProgressDialog, ObservableSource<? extends T>>() {
          @Override
          public ObservableSource<? extends T> apply(@NonNull ProgressDialog dialog) throws Exception {
            return Observable.create(emitter -> {
              if (builder.cancelable) {
                dialog.setOnCancelListener(dialogInterface -> emitter.onComplete());
              }
              dialog.setOnDismissListener(dialogInterface -> emitter.onComplete());
              source.subscribe(emitter::onNext, emitter::onError, emitter::onComplete);
            });
          }
        }, Dialog::dismiss);
  }

  private ProgressDialog makeDialog() {
    return ProgressDialog.show(builder.activity, builder.title, builder.message, builder.indeterminate, builder.cancelable);
  }

  public static class Builder {

    private final @NonNull Activity activity;

    private boolean cancelable;

    private boolean indeterminate;

    private CharSequence message;

    private CharSequence title;

    public Builder(@NonNull Activity activity) {
      this.activity = activity;
      this.indeterminate = true;
      this.message = activity.getString(R.string.loading);
    }

    public <T> Flowable<T> forFlowable(Flowable<T> source, BackpressureStrategy backpressureStrategy) {
      return new RxProgressDialog(this).forFlowable(source, backpressureStrategy);
    }

    public <T> Observable<T> forObservable(Observable<T> source) {
      return new RxProgressDialog(this).forObservable(source);
    }

    public Builder withCancelable(boolean cancelable) {
      this.cancelable = cancelable;
      return this;
    }

    public Builder withIndeterminate(boolean indeterminate) {
      this.indeterminate = indeterminate;
      return this;
    }

    public Builder withMessage(@StringRes int message) {
      this.message = activity.getString(message);
      return this;
    }

    public Builder withMessage(CharSequence message) {
      this.message = message;
      return this;
    }

    public Builder withTitle(CharSequence title) {
      this.title = title;
      return this;
    }

    public Builder withTitle(@StringRes int title) {
      this.title = activity.getString(title);
      return this;
    }
  }
}