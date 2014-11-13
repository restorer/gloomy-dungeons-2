/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zame.game.libs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews.RemoteView;
import zame.game.R;

@RemoteView
public class AbsoluteLayout extends WeightedLayout {
    public AbsoluteLayout(Context context) {
        super(context);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsoluteLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams)child.getLayoutParams();

                myApplyLayoutParams(child, lp);
                myMeasureChild(child, lp);
            }
        }
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT},
     * a height of {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}
     * and with the coordinates (0, 0).
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams)child.getLayoutParams();

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int childLeft = myPaddingLeft + calcChildPosition(myWidth, mWidthWeightSum, lp.x, lp.xWeight, lp.cxWeight, lp.rxWeight, childWidth);
                int childTop = myPaddingTop + calcChildPosition(myHeight, mHeightWeightSum, lp.y, lp.yWeight, lp.cyWeight, lp.byWeight, childHeight);

                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AbsoluteLayout.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return (p instanceof AbsoluteLayout.LayoutParams);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    protected int calcChildPosition(
        int mySize,
        float myWeightSum,
        int childPosition,
        float startPosWeight,
        float centerPosWeight,
        float endPosWeight,
        int childSize
    ) {
        if (childPosition > 0) {
            return childPosition;
        } else if (startPosWeight > 0f) {
            return (int)((float)mySize / myWeightSum * startPosWeight);
        } else if (centerPosWeight > 0f) {
            return (int)((float)mySize / myWeightSum * centerPosWeight - (float)childSize / 2f);
        } else if (endPosWeight > 0f) {
            return (int)((float)mySize / myWeightSum * endPosWeight - (float)childSize);
        } else {
            return childPosition;
        }
    }

    protected void myMeasureChild(View child, LayoutParams lp) {
        final int childWidthMeasureSpec = myGetChildMeasureSpec(
            myWidth, mWidthWeightSum, myPaddingLeft + myPaddingRight,
            lp.width, lp.widthWeight
        );

        final int childHeightMeasureSpec = myGetChildMeasureSpec(
            myHeight, mHeightWeightSum, myPaddingTop + myPaddingBottom,
            lp.height, lp.heightWeight
        );

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    public static int myGetChildMeasureSpec(int mySize, float myWeightSum, int myPadding, int childDimension, float childWeight) {
        int resultSize = 0;
        int resultMode = 0;
        int maxSize = Math.max(0, mySize - myPadding);

        if (childWeight > 0f && childDimension <= 0) {
            resultSize = Math.min(maxSize, (int)((float)mySize / myWeightSum * childWeight));
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension >= 0) {
            resultSize = Math.min(maxSize, childDimension);
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.MATCH_PARENT) {
            resultSize = maxSize;
            resultMode = MeasureSpec.EXACTLY;
        } else if (childDimension == LayoutParams.WRAP_CONTENT) {
            resultSize = maxSize;
            resultMode = MeasureSpec.AT_MOST;
        }

        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    /**
     * Per-child layout information associated with AbsoluteLayout.
     * See
     * {@link android.R.styleable#AbsoluteLayout_Layout Absolute Layout Attributes}
     * for a list of all child view attributes that this class supports.
     */
    public static class LayoutParams extends WeightedLayout.LayoutParams {
        /**
         * The horizontal, or X, location of the child within the view group.
         */
        public int x;
        /**
         * The vertical, or Y, location of the child within the view group.
         */
        public int y;

        public float xWeight;
        public float yWeight;
        public float cxWeight;
        public float cyWeight;
        public float rxWeight;
        public float byWeight;

        /**
         * Creates a new set of layout parameters with the specified width,
         * height and location.
         *
         * @param width the width, either {@link #MATCH_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param height the height, either {@link #MATCH_PARENT}, {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param x the X location of the child
         * @param y the Y location of the child
         */
        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);

            this.x = x;
            this.y = y;
        }

        /**
         * Creates a new set of layout parameters. The values are extracted from
         * the supplied attributes set and context. The XML attributes mapped
         * to this set of layout parameters are:
         *
         * <ul>
         *   <li><code>layout_x</code>: the X location of the child</li>
         *   <li><code>layout_y</code>: the Y location of the child</li>
         *   <li>All the XML attributes from
         *   {@link android.view.ViewGroup.LayoutParams}</li>
         * </ul>
         *
         * @param c the application environment
         * @param attrs the set of attributes fom which to extract the layout
         *              parameters values
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AbsoluteLayout_Layout);
            x = a.getDimensionPixelOffset(R.styleable.AbsoluteLayout_Layout_layout_x, 0);
            y = a.getDimensionPixelOffset(R.styleable.AbsoluteLayout_Layout_layout_y, 0);
            xWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_xWeight, 0);
            yWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_yWeight, 0);
            cxWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_cxWeight, 0);
            cyWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_cyWeight, 0);
            rxWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_rxWeight, 0);
            byWeight = a.getFloat(R.styleable.AbsoluteLayout_Layout_layout_byWeight, 0);
            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}

