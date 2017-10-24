package com.jayhsugo.orderlunchbox;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Administrator on 2017/10/24.
 */

public class MaxTextLengthWatcher implements TextWatcher {

    private int maxLength; // 儲存最大的字串長度
    private int currentEnd = 0; // 儲存目前字串改變的結束位置，例如：abcdefg變成abcd1234efg，變化的結束位置就在索引8

    public MaxTextLengthWatcher(final int maxLength) {
        setMaxLength(maxLength);
    }

    public final void setMaxLength(final int maxLength) {
        if (maxLength >= 0) {
            this.maxLength = maxLength;
        } else {
            this.maxLength = 0;
        }
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        currentEnd = start + count; // 取得變化的結束位置
    }

    @Override
    public void afterTextChanged(final Editable s) {
        while (calculateLength(s) > maxLength) { // 若變化後的長度超過最大長度
            // 刪除最後變化的字元
            currentEnd--;
            s.delete(currentEnd, currentEnd + 1);
        }
    }

    // 字算字串的字元長度
    protected int calculateLength(final CharSequence c) {
        int len = 0;
        final int l = c.length();
        for (int i = 0; i < l; i++) {
            final char tmp = c.charAt(i);
            if (tmp >= 0x20 && tmp <= 0x7E) {
                // 字元值 32~126 是 ASCII 半形字元的範圍
                len++;
            } else {
                // 非半形字元
                len += 2;
            }
        }
        return len;
    }

}
