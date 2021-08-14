package com.yanjkcode.ytabview;

public interface OnTabCheckedListener {
    /**
     * 返回当前选中的下标 从0开始
     *
     * @param position 当前选中的下标
     */
    void onTabChecked(int position);
}