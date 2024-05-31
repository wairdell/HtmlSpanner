package net.nightwhistler.htmlspanner.spans;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.LineBackgroundSpan;
import android.util.Log;

import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.style.Style;
import net.nightwhistler.htmlspanner.style.StyleValue;

/**
 * Created with IntelliJ IDEA.
 * User: alex
 * Date: 6/23/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class BorderSpan implements LineBackgroundSpan {

    private int start;
    private int end;

    private Style style;

    private HtmlSpanner spanner;

    public BorderSpan(Style style, int start, int end, HtmlSpanner spanner) {
        this.start = start;
        this.end = end;

        this.style = style;
        this.spanner = spanner;
    }

    private int edgeLeft;
    private int edgeRight;

    private int edgeTop;

    private int edgeBottom;

    private void clearEdge() {
        edgeLeft = Integer.MAX_VALUE;
        edgeRight = Integer.MIN_VALUE;
        edgeTop = Integer.MAX_VALUE;
        edgeBottom = Integer.MIN_VALUE;
    }


    @Override
    public void drawBackground(Canvas c, Paint p,
                               int left, int right,
                               int top, int baseline, int bottom,
                               CharSequence text, int start, int end,
                               int lnum) {
        int baseMargin = 0;

        if (style.getMarginLeft() != null) {
            StyleValue styleValue = style.getMarginLeft();

            if (styleValue.getUnit() == StyleValue.Unit.PX) {
                if (styleValue.getIntValue() > 0) {
                    baseMargin = styleValue.getIntValue();
                }
            } else if (styleValue.getFloatValue() > 0f) {
                baseMargin = (int) (styleValue.getFloatValue() * HtmlSpanner.HORIZONTAL_EM_WIDTH);
            }

            //Leave a little bit of room
            baseMargin--;
        }

        if (baseMargin > 0) {
            left = left + baseMargin;
        }

        int originalColor = p.getColor();
        float originalStrokeWidth = p.getStrokeWidth();
        Paint.Style originalPaintStyle = p.getStyle();

        StyleValue borderRadius = style.getBorderRadius();

        if (start == this.start) {
            clearEdge();
        }

        if (borderRadius != null && end - start == 1 && text.charAt(start) == '\n') {
            if (start == this.start) return;
        }

        if (borderRadius != null) {
            left = edgeLeft = Math.min(edgeLeft, left);
            right = edgeRight = Math.max(edgeRight, right);
            top = edgeTop = Math.min(edgeTop, top);
            bottom = edgeBottom = Math.max(edgeBottom, bottom);
            if (text.charAt(this.end - 1) != '\n' && end < this.end) {
                return;
            } else if (end < this.end - 1) {
                return;
            }
        }

        Integer backgroundColor = spanner.getContrastPatcher().patchBackgroundColor(style);
        if (spanner.isUseColoursFromStyle() && backgroundColor != null) {
            p.setColor(backgroundColor);
            p.setStyle(Paint.Style.FILL);
            if (borderRadius != null) {
                int r = borderRadius.getIntValue();
                c.drawRoundRect(left, top, right, bottom, r, r, p);
            } else {
                c.drawRect(left, top, right, bottom, p);
            }
        }

        if (spanner.isUseColoursFromStyle() && style.getBorderColor() != null) {
            p.setColor(style.getBorderColor());
        }

        int strokeWidth;

        if (style.getBorderWidth() != null && style.getBorderWidth().getUnit() == StyleValue.Unit.PX) {
            strokeWidth = style.getBorderWidth().getIntValue();
        } else {
            strokeWidth = 1;
        }

        p.setStrokeWidth(strokeWidth);
        p.setStyle(Paint.Style.STROKE);
        if (borderRadius != null) {
            int r = borderRadius.getIntValue();
            c.drawRoundRect(left, top, right, bottom, r, r, p);
        } else {
            c.drawRect(left, top, right, bottom, p);
        }
        /*right -= strokeWidth;

        p.setStyle(Paint.Style.STROKE);

        if (start <= this.start) {
            Log.d("BorderSpan", "Drawing first line");
            c.drawLine(left, top, right, top, p);
        }

        if (end >= this.end) {
            Log.d("BorderSpan", "Drawing last line");
            c.drawLine(left, bottom, right, bottom, p);
        }

        c.drawLine(left, top, left, bottom, p);
        c.drawLine(right, top, right, bottom, p);*/

        p.setColor(originalColor);
        p.setStrokeWidth(originalStrokeWidth);
        p.setStyle(originalPaintStyle);
    }


}
