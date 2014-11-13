package zame.game.libs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import zame.game.R;

@RemoteView
public abstract class WeightedLayout extends ViewGroup {
	protected float mWidthWeightSum;
	protected float mHeightWeightSum;
	protected float mPaddingLeftWeight;
	protected float mPaddingTopWeight;
	protected float mPaddingRightWeight;
	protected float mPaddingBottomWeight;

	protected int myWidth;
	protected int myHeight;
	protected int myPaddingLeft;
	protected int myPaddingTop;
	protected int myPaddingRight;
	protected int myPaddingBottom;

	public WeightedLayout(Context context) {
		super(context);
	}

	public WeightedLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFromAttributes(context, attrs);
	}

	public WeightedLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFromAttributes(context, attrs);
	}

	protected void initFromAttributes(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeightedLayout);

		mWidthWeightSum = a.getFloat(R.styleable.WeightedLayout_widthWeightSum, 1.0f);
		mHeightWeightSum = a.getFloat(R.styleable.WeightedLayout_heightWeightSum, 1.0f);

		float paddingWeight = a.getFloat(R.styleable.WeightedLayout_paddingWeight, -1.0f);

		if (paddingWeight > 0f) {
			mPaddingLeftWeight = paddingWeight;
			mPaddingTopWeight = paddingWeight;
			mPaddingRightWeight = paddingWeight;
			mPaddingBottomWeight = paddingWeight;
		} else {
			float paddingWidthWeight = a.getFloat(R.styleable.WeightedLayout_paddingWidthWeight, -1.0f);
			float paddingHeightWeight = a.getFloat(R.styleable.WeightedLayout_paddingHeightWeight, -1.0f);

			if (paddingWidthWeight > 0f) {
				mPaddingLeftWeight = paddingWidthWeight;
				mPaddingRightWeight = paddingWidthWeight;
			} else {
				mPaddingLeftWeight = a.getFloat(R.styleable.WeightedLayout_paddingLeftWeight, -1.0f);
				mPaddingRightWeight = a.getFloat(R.styleable.WeightedLayout_paddingRightWeight, -1.0f);
			}

			if (paddingHeightWeight > 0f) {
				mPaddingTopWeight = paddingHeightWeight;
				mPaddingBottomWeight = paddingHeightWeight;
			} else {
				mPaddingTopWeight = a.getFloat(R.styleable.WeightedLayout_paddingTopWeight, -1.0f);
				mPaddingBottomWeight = a.getFloat(R.styleable.WeightedLayout_paddingBottomWeight, -1.0f);
			}
		}

		a.recycle();
	}

	public float getWidthWeightSum() {
		return mWidthWeightSum;
	}

	public void setWidthWeightSum(float widthWeightSum) {
		mWidthWeightSum = widthWeightSum;
	}

	public float getHeightWeightSum() {
		return mHeightWeightSum;
	}

	public void setHeightWeightSum(float heightWeightSum) {
		mHeightWeightSum = heightWeightSum;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode != MeasureSpec.UNSPECIFIED) {
			myWidth = widthSize;
		} else {
			myWidth = -1;
		}

		if (heightMode != MeasureSpec.UNSPECIFIED) {
			myHeight = heightSize;
		} else {
			myHeight = -1;
		}

        myPaddingLeft = (int)(mPaddingLeftWeight > 0f ? (myWidth / mWidthWeightSum * mPaddingLeftWeight) : getPaddingLeft());
        myPaddingTop = (int)(mPaddingTopWeight > 0f ? (myHeight / mHeightWeightSum * mPaddingTopWeight) : getPaddingTop());
        myPaddingRight = (int)(mPaddingRightWeight > 0f ? (myWidth / mWidthWeightSum * mPaddingRightWeight) : getPaddingRight());
        myPaddingBottom = (int)(mPaddingBottomWeight > 0f ? (myHeight / mHeightWeightSum * mPaddingBottomWeight) : getPaddingBottom());

		setMeasuredDimension(resolveSize(myWidth, widthMeasureSpec), resolveSize(myHeight, heightMeasureSpec));
	}

	protected void myApplyLayoutParams(View child, WeightedLayoutParams lp) {
		float textSizeWeight = lp.getTextSizeWeight();

		if (textSizeWeight > 0f && (child instanceof TextView)) {
			((TextView)child).setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)myHeight / mHeightWeightSum * textSizeWeight);
		}

		lp.clearAppliedParams();
	}

	public interface WeightedLayoutParams {
		float getTextSizeWeight();
		void clearAppliedParams();
	}

	public static class LayoutParams extends ViewGroup.LayoutParams implements WeightedLayoutParams {
		public float widthWeight;
		public float heightWeight;
		public float textSizeWeight;

		public float getTextSizeWeight() {
			return textSizeWeight;
		}

		public void clearAppliedParams() {
			textSizeWeight = 0f;
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.WeightedLayout_Layout);
			this.widthWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_widthWeight, 0);
			this.heightWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_heightWeight, 0);
			this.textSizeWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_textSizeWeight, 0);
			a.recycle();
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	public static class MarginLayoutParams extends ViewGroup.MarginLayoutParams implements WeightedLayoutParams {
		public float widthWeight;
		public float heightWeight;
		public float textSizeWeight;

		public float getTextSizeWeight() {
			return textSizeWeight;
		}

		public void clearAppliedParams() {
			textSizeWeight = 0f;
		}

		public MarginLayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

			TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.WeightedLayout_Layout);
			this.widthWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_widthWeight, 0);
			this.heightWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_heightWeight, 0);
			this.textSizeWeight = a.getFloat(R.styleable.WeightedLayout_Layout_layout_textSizeWeight, 0);
			a.recycle();
		}

		public MarginLayoutParams(int width, int height) {
			super(width, height);
		}

		public MarginLayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public MarginLayoutParams(ViewGroup.MarginLayoutParams source) {
			super(source);
		}
	}
}
