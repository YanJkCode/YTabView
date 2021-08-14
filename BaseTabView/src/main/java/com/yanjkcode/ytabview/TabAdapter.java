package com.yanjkcode.ytabview;

import android.view.View;

public interface TabAdapter {
    /**
     * 设置tab数据
     *
     * @param tabView 以getTabViewId获取的ID创建的tab
     * @param tab     数据
     */
    void setTabView(View tabView, String tab);

    /**
     * 获取tab布局ID
     *
     * @return LayoutId
     */
    int getTabViewId();

    /**
     * 被切换到的tab
     *
     * @param tab 当前被选中的tab
     */
    void tabChecked(View tab);

    /**
     * 上一个tab
     *
     * @param lastTab 上一次被选中的tab
     */
    void lastTabChecked(View lastTab);

    /**
     * 获取缩放倍数
     *
     * @return 只需要返回你需要缩放的倍数即可 比如0.3f 就是放大0.3倍  默认0 不放大
     */
    float getTabScale();
}
