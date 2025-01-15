package com.ai.android.checkbox3;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.CompoundButton;

public class CheckBox3 extends CompoundButton {
    private static final String TAG = CheckBox3.class.getSimpleName();
    private static final int[] INDETERMINATE_STATE_SET = {
            R.attr.state_indeterminate
    };
    private static final int[] DEFAULT_CYCLE = {
            1, 0, 1, 0
    };
    private boolean mBroadcasting;
    private boolean mIndeterminate;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
    private int[] mCycle = DEFAULT_CYCLE;

    public CheckBox3(final Context context) {
        this(context, null);
    }

    public CheckBox3(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.ai_checkbox3_style);
    }

    public CheckBox3(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBox3, defStyleAttr, R.style.ai_Widget_Checkbox3);

        try {

            if (a.hasValue(R.styleable.CheckBox3_ai_checkbox3_checkableCycle)) {
                int cycleRes = a.getResourceId(R.styleable.CheckBox3_ai_checkbox3_checkableCycle, 0);
                if (cycleRes != 0) {
                    setCycle(getResources().getIntArray(cycleRes));
                }
            }

            final boolean indeterminate = a.getBoolean(R.styleable.CheckBox3_ai_checkbox3_indeterminate, false);
            final boolean checked = a.getBoolean(R.styleable.CheckBox3_android_checked, false);

            Log.v(TAG, String.format("checked: %b, indeterminate: %b", checked, indeterminate));

            mBroadcasting = true;

            int index = getStateIndex(checked, indeterminate);
            if (isStateIndexInvalid(index)) {
                Log.e(TAG, String.format("Invalid state index (%d). Not allowed", index));
                index = getFirstValidStateIndex(index);
                Log.e(TAG, String.format("using next valid state: %d", index));
            }
            moveToState(index);

        } finally {
            a.recycle();
        }
    }

    public void setCycle(final int cycleRes) {
        setCycle(getResources().getIntArray(cycleRes));
    }

    /**
     * Change the cycle used to determine the next state of the checkbox when the user
     * click the component, or when the {@link #toggle()} is called.<br />
     * The value passed is an array of 4 integer elements, representing the state of the checkbox.<br />
     * The value of each element can be 0 or 1, with 0 to disable the state and 1 to enable the state.<br />
     * The states are (in order):
     * <ul>
     * <li>Checked</li>
     * <li>Indeterminate (checked)</li>
     * <li>Unchecked</li>
     * <li>Indeterminate (unchecked)</li>
     * </ul>
     * So if a cycle like this one is used:
     * <code>int[] cycle = new int[]{1,0,1,0}</code>
     * the checkbox will only cycle between checked and unchecked state.
     * The default cycle is: <code>{1,0,1,0}</code>
     * <p>
     * <p>The method throws an exception if the array is not valid. A valid array must match the following rules:
     * <ul>
     * <li>If not null, must have 4 elements</li>
     * <li>At least 2 elements must be not equal to 0</li>
     * </ul></p>
     *
     * @param cycle change the cycle
     */
    public void setCycle(final int[] cycle) {
        validateCycle(cycle);

        if (BuildConfig.DEBUG) {
            StringBuilder builder = new StringBuilder("[");
            for (int i : cycle) {
                builder.append(i);
                builder.append(", ");
            }
            builder.append("]");
            Log.i(TAG, String.format("setCycle(%s)", builder));
        }

        mCycle = cycle == null ? DEFAULT_CYCLE : cycle;
    }

    /**
     * Move to the next state of the checkbox
     */
    @Override
    public void toggle() {
        if (null == mCycle) return;

        Log.i(TAG, "toggle()");
        final int currentIndex = getCurrentStateIndex();
        moveToNextState(currentIndex);
    }

    @Override
    public void setChecked(boolean checked) {
        if (null == mCycle) return;

        Log.i(TAG, String.format("setChecked(%b)", checked));

        int index = getStateIndex(checked, isIndeterminate());
        if (isStateIndexInvalid(index)) {
            Log.e(TAG, String.format("Invalid state index (%d). Not allowed", index));
            return;
        }

        super.setChecked(checked);
    }

    public void setChecked(boolean checked, boolean indeterminate) {
        if (null == mCycle) return;

        if (isChecked() == checked) {
            setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
        setChecked(checked);
    }

    @ViewDebug.ExportedProperty
    public boolean isIndeterminate() {
        return mIndeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        if (null == mCycle) return;

        Log.i(TAG, String.format("setIndeterminate(%b)", indeterminate));

        int index = getStateIndex(isChecked(), indeterminate);
        if (isStateIndexInvalid(index)) {
            Log.e(TAG, String.format("Invalid state index (%d). Not allowed", index));
            return;
        }

        setIndeterminateImpl(indeterminate);
    }

    private void moveToNextState(final int index) {
        Log.i(TAG, String.format("moveToNextState(current: %d)", index));
        int nextIndex = getNextValidIndex(index);
        Log.v(TAG, String.format("nextIndex: %d", nextIndex));
        boolean checked = nextIndex < 2;
        mIndeterminate = nextIndex == 1 || nextIndex == 3;
        setChecked(checked);
    }

    private void moveToState(final int nextIndex) {
        if (isStateIndexInvalid(nextIndex)) {
            Log.e(TAG, String.format("Invalid state index (%d). Not allowed", nextIndex));
        }
        boolean checked = nextIndex < 2;
        mIndeterminate = nextIndex == 1 || nextIndex == 3;
        setChecked(checked);
    }


    private void validateCycle(final int[] cycle) {
        if (null == cycle) {
            return;
        }
        if (cycle.length != 4) {
            throw new IllegalArgumentException("Invalid cycle length. Expected 4 an array or 4 int");
        }

        int total = 0;
        for (final int i1 : cycle) {
            if (i1 != 0) {
                total++;
            }
        }
        if (total < 2) {
            throw new IllegalArgumentException("Invalid cycle. At least 2 elements must be positive");
        }
    }

    private int getCurrentStateIndex() {
        return getStateIndex(isChecked(), isIndeterminate());
    }

    private int getStateIndex(boolean checked, boolean indeterminate) {
        int index = 0;
        if (checked) {
            index += indeterminate ? 1 : 0;
        } else {
            index = indeterminate ? 3 : 2;
        }
        return index;
    }

    private int getFirstValidStateIndex(int index) {
        int i = index;
        while (i >= 0) {
            if (mCycle[i] != 0) return i;
            i--;
        }

        i = index;
        while (i < mCycle.length) {
            if (mCycle[i] != 0) return i;
            i++;
        }

        return -1;
    }

    private boolean isStateIndexInvalid(int index) {
        return mCycle[index] == 0;
    }

    private int getNextValidIndex(int index) {
        int nextIndex = index + 1 >= mCycle.length ? 0 : index + 1;
        if (mCycle[nextIndex] != 0) {
            return nextIndex;
        }
        return getNextValidIndex(nextIndex);
    }

    @Override
    public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener(listener);
        mOnCheckedChangeListener = listener;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isIndeterminate()) {
            mergeDrawableStates(drawableState, INDETERMINATE_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
    }

    private void setIndeterminateImpl(boolean indeterminate) {
        if (mIndeterminate != indeterminate) {
            mIndeterminate = indeterminate;
            refreshDrawableState();
            notifyStateListener();
        }
    }

    private void notifyStateListener() {
        if (mBroadcasting) {
            return;
        }

        mBroadcasting = true;

        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, isChecked());
        }
        mBroadcasting = false;

    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return CheckBox3.class.getName();
    }
}