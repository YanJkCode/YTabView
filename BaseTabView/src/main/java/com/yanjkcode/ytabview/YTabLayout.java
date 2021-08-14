package com.yanjkcode.ytabview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.yanjkcode.basetabview.R;

import java.util.List;

public class YTabLayout extends LinearLayout {
    private Paint mPaint;
    private RectF rect;//下划线矩形

    private int tabChecked = -1;//当前选中
    private View checkedView;
    private View lastCheckedView;
    private int tabLineColor;//颜色
    private int tabLineHeight;//高度
    private int duration;

    private int tabItemSpace;//间距
    private float tabLineRadius;//圆角角度

    private boolean showTabLine;//是否显示tab下划线

    private int startLeft, startRight, endLeft, endRight;

    private boolean isSetOrientation;
    private LayoutInflater inflater;
    private TabAdapter tabAdapter;
    private ValueAnimator valueAnimator;
    private boolean isRunAnim;
    private int tabLineOffset;
    private boolean tabLineOffsetDirection;
    private int tabLineOffsetLeft;
    private int tabLineOffsetRight;

    private boolean isLoadPadding;

    private ViewPager viewPager;
    private ViewPager2 viewPager2;
    private float checkedScale;
    private boolean isLoadTab;

    public YTabLayout(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public YTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public YTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        isSetOrientation = true;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        setWillNotDraw(false);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.YTabLayout);
        if (ta != null) {
            showTabLine = ta.getBoolean(R.styleable.YTabLayout_showTabLine, true);
            tabLineColor = ta.getColor(R.styleable.YTabLayout_lineColor, Color.BLUE);
            duration = ta.getInt(R.styleable.YTabLayout_duration, 150);
            tabLineHeight = (int) ta.getLayoutDimension(R.styleable.YTabLayout_tabLineHeight, 0);
            if (tabLineHeight != -2) {
                tabLineHeight /= 2;
            }
            tabLineRadius = ta.getLayoutDimension(R.styleable.YTabLayout_tabLineRadius, 0);
            tabItemSpace = (int) ta.getDimension(R.styleable.YTabLayout_tabSpace, 0) / 2;
            tabLineOffset = (int) ta.getDimension(R.styleable.YTabLayout_tabLineOffset, 0);
            tabLineOffsetLeft = (int) ta.getDimension(R.styleable.YTabLayout_tabLineOffsetLeft, 0);
            tabLineOffsetRight = (int) ta.getDimension(R.styleable.YTabLayout_tabLineOffsetRight, 0);
            tabLineOffsetDirection = (int) ta.getInt(R.styleable.YTabLayout_tabLineOffsetDirection, 0) == 0;
            ta.recycle();
        }

        rect = new RectF();
        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(moveAnim);
        valueAnimator.addListener(listenerAdapter);
        valueAnimator.setDuration(0);
        isLoadTab = false;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(tabLineColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isLoadPadding) {
            isLoadPadding = true;
            if (getOrientation() == HORIZONTAL) {
                setPadding(tabLineOffsetLeft, 0, tabLineOffsetRight, 0);
            } else {
                if (tabLineOffsetDirection) {
                    setPadding(tabLineOffsetLeft, tabLineOffset, tabLineOffsetRight, 0);
                } else {
                    setPadding(tabLineOffsetLeft, 0, tabLineOffsetRight, tabLineOffset);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showTabLine) {
            canvas.drawRoundRect(rect, tabLineRadius, tabLineRadius, mPaint);
        }
    }

    public void addTab(List<String> tabs) {
        for (String tab : tabs) {
            if (tabAdapter == null) {
                throw new NullPointerException("tabAdapter == null 未设置适配器");
            }
            if (!isSetOrientation) {
                setOrientation(HORIZONTAL);
            }
            if (inflater == null) {
                inflater = LayoutInflater.from(getContext());
            }
            int tabViewId = tabAdapter.getTabViewId();
            if (tabViewId > 0) {
                View tabView = inflater.inflate(tabViewId, null);
                tabAdapter.setTabView(tabView, tab);
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(tabItemSpace, 0, tabItemSpace, 0);
                tabView.setTag(getChildCount());

                tabView.setOnClickListener(onClickListener);
                addView(tabView, params);
            }
        }
        if (tabChecked == -1) {
            post(new Runnable() {
                @Override
                public void run() {
                    checked(0);
                }
            });
        }
    }

    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Object tag = view.getTag();
            if (tag != null)
                checked((Integer) tag);
        }
    };

    public void checked(int index) {
        if (index < 0 || index > getChildCount() || isRunAnim || index == tabChecked) {
            return;
        }
        tabChecked = index;
        if (checkedView != null) {
            lastCheckedView = checkedView;
        }
        checkedView = getChildAt(tabChecked);
        tabAdapter.tabChecked(checkedView);
        checkedScale = tabAdapter.getTabScale();
        if (getOrientation() == HORIZONTAL) {
            if (lastCheckedView != null) {
                tabAdapter.lastTabChecked(lastCheckedView);

                startLeft = lastCheckedView.getLeft();
                startRight = lastCheckedView.getRight();
            } else {
                startLeft = checkedView.getLeft();
                startRight = checkedView.getRight();
            }
            endLeft = checkedView.getLeft();
            endRight = checkedView.getRight();
        } else {
            if (lastCheckedView != null) {
                tabAdapter.lastTabChecked(lastCheckedView);
                startLeft = lastCheckedView.getTop();
                startRight = lastCheckedView.getBottom();
            } else {
                startLeft = checkedView.getTop();
                startRight = checkedView.getBottom();
            }
            endLeft = checkedView.getTop();
            endRight = checkedView.getBottom();
        }
        isRunAnim = true;
        valueAnimator.start();
        if (viewPager != null) {
            viewPager.setCurrentItem(index);
        } else if (viewPager2 != null) {
            viewPager2.setCurrentItem(index);
        } else {
            if (onTabCheckedListener != null) onTabCheckedListener.onTabChecked(index);
        }
    }

    private final ValueAnimator.AnimatorUpdateListener moveAnim = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            int left = (int) (startLeft + ((endLeft - startLeft) * animatedFraction));
            int right = (int) (startRight + ((endRight - startRight) * animatedFraction));
            int halfHeight = checkedView.getHeight() / 2;
            if (tabLineHeight == -2) {
                tabLineHeight = getTabLineHeight(checkedView);
            }
            if (tabLineRadius == -2) {
                tabLineRadius = checkedView.getHeight() / 2f;
            }
            if (getOrientation() == HORIZONTAL) {
                int y = checkedView.getTop() + halfHeight;
                rect.set(left - tabLineOffsetLeft,
                        offsetLine(y - tabLineHeight),
                        right + tabLineOffsetRight,
                        offsetLine(y + tabLineHeight));
            } else {
                rect.set(checkedView.getLeft() - tabLineOffsetLeft,
                        offsetLine(left + halfHeight - tabLineHeight),
                        checkedView.getRight() + tabLineOffsetRight,
                        offsetLine(right - halfHeight + tabLineHeight));
            }
            float scale = 1f + checkedScale * animatedFraction;
            checkedView.setScaleX(scale);
            checkedView.setScaleY(scale);
            if (lastCheckedView != null) {
                float lastScale = (1f + checkedScale) - checkedScale * animatedFraction;
                lastCheckedView.setScaleX(lastScale);
                lastCheckedView.setScaleY(lastScale);
            }
            postInvalidate();
        }
    };

    private int getTabLineHeight(View tab) {
        if (tab instanceof TextView) {
            Paint.FontMetrics fm = ((TextView) tab).getPaint().getFontMetrics();
            return (int) ((fm.descent - fm.ascent) / 2);
        } else {
            if (tab instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) tab;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View childAt = group.getChildAt(i);
                    if (childAt instanceof TextView) {
                        Paint.FontMetrics fm = ((TextView) childAt).getPaint().getFontMetrics();
                        return (int) ((fm.descent - fm.ascent) / 2);
                    }
                }
            }
        }
        return 0;
    }

    private int offsetLine(int curIndex) {
        if (tabLineOffsetDirection) {
            return curIndex - tabLineOffset;
        } else {
            return curIndex + tabLineOffset;
        }
    }

    private final AnimatorListenerAdapter listenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isLoadTab) {
                isLoadTab = true;
                valueAnimator.setDuration(duration);
            }
            isRunAnim = false;
        }
    };

    public void setTabAdapter(TabAdapter tabAdapter) {
        this.tabAdapter = tabAdapter;
    }

    public void bindViewPager(@NonNull ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (onTabCheckedListener != null) onTabCheckedListener.onTabChecked(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void bindViewPager(@NonNull ViewPager2 viewPager) {
        this.viewPager2 = viewPager;
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (onTabCheckedListener != null) onTabCheckedListener.onTabChecked(position);
            }
        });
    }

    private OnTabCheckedListener onTabCheckedListener;

    public void setOnTabCheckedListener(OnTabCheckedListener onTabCheckedListener) {
        this.onTabCheckedListener = onTabCheckedListener;

    }
}
