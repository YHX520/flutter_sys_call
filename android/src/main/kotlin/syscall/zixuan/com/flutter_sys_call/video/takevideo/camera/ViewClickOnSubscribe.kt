package syscall.zixuan.com.flutter_sys_call.video.takevideo.camera

import android.view.View
import rx.Observable
import rx.Subscriber
import rx.android.MainThreadSubscription

import java.util.ArrayList


/**
 * 防抖点击事件绑定  可以参照rxbinding
 *
 *
 * ViewClickOnSubscribe click = new ViewClickOnSubscribe();
 * click.addOnClickListener(tv_test);
 * click.addOnClickListener(tv_test1);
 * click.addOnClickListener(tv_test2);
 *
 * subscription = Observable.create(click).throttleFirst(500 , TimeUnit.MILLISECONDS ).subscribe(new Action1<View>() {
 * @Override
 * public void call(View view) {
 * switch (view.getId()) {
 * case R.id.tv_test:
 * Log.i("you", "test");
 * break;
 * case R.id.tv_test1:
 * Log.i("you", "test1");
 * break;
 * }
 * }
 * });
 *
 * Subscription subscription;
 *
 * @Override
 * protected void onDestroy() {
 * super.onDestroy();
 * subscription.unsubscribe();
 * }
</View> */

class ViewClickOnSubscribe : Observable.OnSubscribe<View> {

    /**
     * 注册防抖点击的控件
     */
    private val clickViews = ArrayList<View>()

    /**
     * 添加控件点击事件
     * @param v
     */
    fun addOnClickListener(v: View) {
        clickViews.add(v)
    }

    override fun call(subscriber: Subscriber<in View>) {
        val listener = View.OnClickListener { v ->
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(v)
            }
        }
        for (v in clickViews) {
            v.setOnClickListener(listener)
        }
        subscriber.add(object : MainThreadSubscription() {
            protected override fun onUnsubscribe() {
                val iterator = clickViews.iterator()
                while (iterator.hasNext()) {
                    iterator.next().setOnClickListener(null)
                    iterator.remove()
                }
            }
        })
    }
}
