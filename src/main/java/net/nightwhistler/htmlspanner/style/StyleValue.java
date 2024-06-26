package net.nightwhistler.htmlspanner.style;

import android.util.Log;
import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.ParsePatcher;

/**
 * Created with IntelliJ IDEA.
 * User: alex
 * Date: 6/29/13
 * Time: 9:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class StyleValue {

    public static enum Unit { PX, EM, PERCENTAGE };

    private Integer intValue;
    private Float floatValue;

    private Unit unit;

    public static StyleValue parse(String value, ParsePatcher parsePatcher) {

        if ( value.equals("0") ) {
            return new StyleValue(0f, Unit.EM);
        }

        if ( value.endsWith("px") ) {

            try {
                final Integer intValue = parsePatcher.parsePxValue(value);
                return new StyleValue(intValue);
            } catch (NumberFormatException nfe ) {
                Log.e("StyleValue", "Can't parse value: " + value );
                return null;
            }
        }

        if ( value.endsWith("%") ) {
            Log.d("StyleValue", "translating percentage " + value );
            try {
                final int percentage = Integer.parseInt( value.substring(0, value.length() -1 ) );
                final float floatValue = percentage / 100f;

                return new StyleValue(floatValue, Unit.PERCENTAGE);
            } catch ( NumberFormatException nfe ) {
                Log.e("StyleValue", "Can't parse font-size: " + value );
                return null;
            }
        }

        if ( value.endsWith("em") ) {
            try {
                final Float number = Float.parseFloat(value.substring(0, value.length() - 2));
                return new StyleValue(number, Unit.EM);
            } catch ( NumberFormatException nfe ) {
                Log.e("CSSCompiler", "Can't parse value: " + value );
                return null;
            }
        }

        if ( value.equals("small") ) {
            return new StyleValue(0.8F, Unit.PERCENTAGE);
        }

        if ( value.equals("big") ) {
            return new StyleValue(1.25F, Unit.PERCENTAGE);
        }

        return null;
    }

    public StyleValue( int intValue ) {
        this.unit = Unit.PX;
        this.intValue = intValue;
    }

    public StyleValue( float floatValue, Unit unit ) {
        this.floatValue = floatValue;
        this.unit = unit;
    }

    public int getIntValue() {
        return this.intValue;
    }

    public float getFloatValue() {
        return this.floatValue;
    }

    public Unit getUnit() {
        return this.unit;
    }

    @Override
    public String toString() {
        if ( intValue != null ) {
            return "" + intValue + this.unit;
        } else {
            return "" + floatValue + this.unit;
        }
    }
}
