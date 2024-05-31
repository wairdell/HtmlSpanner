package net.nightwhistler.htmlspanner.handlers;

import android.text.SpannableStringBuilder;
import android.util.Log;
import net.nightwhistler.htmlspanner.SpanStack;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.nightwhistler.htmlspanner.css.CSSCompiler;
import net.nightwhistler.htmlspanner.spans.*;
import net.nightwhistler.htmlspanner.style.Style;
import net.nightwhistler.htmlspanner.style.StyleCallback;
import net.nightwhistler.htmlspanner.style.StyleValue;

import org.htmlcleaner.BaseToken;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.TagNode;

import java.util.List;

/**
 * TagNodeHandler for any type of text that may be styled using CSS.
 *
 * @author Alex Kuiper
 */
public class StyledTextHandler extends TagNodeHandler {

    private Style style;

    public StyledTextHandler() {
        this.style = new Style();
    }

    public StyledTextHandler(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return style;
    }

    @Override
    public void beforeChildren(TagNode node, SpannableStringBuilder builder, SpanStack spanStack) {

        Style useStyle = spanStack.getStyle( node, getStyle() );

        String styleAttr = node.getAttributeByName("style");
        if ( getSpanner().isAllowStyling() && styleAttr != null ) {
            String[] pairs = styleAttr.split(";");
            for ( String pair: pairs ) {
                String[] keyVal = pair.split(":");
                if (keyVal.length != 2) continue;
                String key = keyVal[0].toLowerCase().trim();
                if (!"margin-top".equals(key)) continue;
                String value = keyVal[1].toLowerCase().trim();
                CSSCompiler.StyleUpdater updater = CSSCompiler.getStyleUpdater(key, value, getSpanner());
                if ( updater != null ) {
                    useStyle = updater.updateStyle(useStyle, getSpanner());
                }
                break;
            }
        }

        if (builder.length() > 0 &&  useStyle.getDisplayStyle() == Style.DisplayStyle.BLOCK ) {

            if ( builder.charAt(builder.length() -1) != '\n' ) {
                builder.append('\n');
            }
        }

        //If we have a top margin, we insert an extra newline. We'll manipulate the line height
        //of this newline to create the margin.
        if ( useStyle.getMarginTop() != null ) {

            StyleValue styleValue = useStyle.getMarginTop();

            if ( styleValue.getUnit() == StyleValue.Unit.PX ) {
                if ( styleValue.getIntValue() > 0 ) {
                    if ( appendNewLine(builder) ) {
                        spanStack.pushSpan( new VerticalMarginSpan( styleValue.getIntValue() ),
                            builder.length() -1, builder.length() );
                    }
                }
            } else {
                if ( styleValue.getFloatValue() > 0f ) {
                    if ( appendNewLine(builder) ) {
                        spanStack.pushSpan( new VerticalMarginSpan( styleValue.getFloatValue() ),
                            builder.length() -1, builder.length() );
                    }
                }
            }

        }


    }

    public final void handleTagNode(TagNode node, SpannableStringBuilder builder,
                                    int start, int end, SpanStack spanStack) {
        Style styleFromCSS = spanStack.getStyle( node, getStyle() );
        handleTagNode(node, builder, start, end, styleFromCSS, spanStack);
    }

    public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, Style useStyle, SpanStack stack ) {

        if ( useStyle.getDisplayStyle() == Style.DisplayStyle.BLOCK ) {
            TagNode parent = node.getParent();
            if (parent == null) {
                appendNewLine(builder);
            } else {
                int index = parent.getChildIndex(node) + 1;
                List<? extends BaseToken> allChildren = parent.getAllChildren();
                for (int i = index; i < allChildren.size(); i++) {
                    BaseToken children = allChildren.get(i);
                    if (children instanceof TagNode) {
                        appendNewLine(builder);
                        break;
                    } else if (children instanceof ContentNode && !"\n".equals(((ContentNode) children).getContent())) {
                        appendNewLine(builder);
                        break;
                    }
                }
            }

            //If we have a bottom margin, we insert an extra newline. We'll manipulate the line height
            //of this newline to create the margin.
            if ( useStyle.getMarginBottom() != null ) {

                StyleValue styleValue = useStyle.getMarginBottom();

                if ( styleValue.getUnit() == StyleValue.Unit.PX ) {
                    if ( styleValue.getIntValue() > 0 ) {
                        appendNewLine(builder);
                        stack.pushSpan( new VerticalMarginSpan( styleValue.getIntValue() ),
                            builder.length() -1, builder.length() );
                    }
                } else {
                    if ( styleValue.getFloatValue() > 0f ) {
                        appendNewLine(builder);

                        stack.pushSpan( new VerticalMarginSpan( styleValue.getFloatValue() ),
                            builder.length() -1, builder.length() );
                    }
                }

            }
        }

        if ( builder.length() > start ) {
            stack.pushSpan(new StyleCallback(getSpanner().getFontResolver()
                .getDefaultFont(), useStyle, start, builder.length() ));
        } else {
            Log.d( "StyledTextHandler", "Refusing to push span of length " + ( builder.length() - start ));
        }
    }

}
