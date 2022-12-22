package io.github.adainish.clandorus.util;

import io.github.adainish.clandorus.Clandorus;
import net.minecraft.util.text.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    private static final Pattern HEXPATTERN = Pattern.compile("\\{(#[a-fA-F0-9]{6})}");
    private static final String SPLITPATTERN = "((?=\\{#[a-fA-F0-9]{6}}))";

    public static ITextComponent parseHexCodes(String text, boolean removeItalics) {
        if(text == null)
            return null;
        StringTextComponent comp = new StringTextComponent("");

        String[] temp = text.split(SPLITPATTERN);
        Arrays.stream(temp).forEach(s -> {
            Matcher m = HEXPATTERN.matcher(s);
            if(m.find()) {
                Color color = Color.fromHex(m.group(1));
                s = m.replaceAll("");
                if(removeItalics)
                    comp.appendSibling(new StringTextComponent(s).setStyle(Style.EMPTY.setColor(color).setItalic(false)));
                else
                    comp.appendSibling(new StringTextComponent(s).setStyle(Style.EMPTY.setColor(color)));
            } else {
                comp.appendSibling(new StringTextComponent(s));
            }
        });

        return comp;
    }

    public static String noPermission = "&c(&4!&c) &eYou lack the permission to use this";


    public static String toString(String[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(" ");
        }
    }

    public static final Color BLUE = Color.fromHex("#00AFFC");
    public static final Color ORANGE = Color.fromHex("#FF6700");
    public static final String BLUE_PARSE = "{#00AFFC}";
    public static final String ORANGE_PARSE = "{#FF6700}";
    public static final Color INFOTEXT = Color.fromTextFormatting(TextFormatting.GRAY);

    public static final String VERSION = Clandorus.VERSION;

    private static final IFormattableTextComponent PLUGIN_PREFIX = new StringTextComponent(Util.formattedString("&6[&bClandorus&6]")).setStyle(Style.EMPTY.setColor(BLUE));

    private static final IFormattableTextComponent MESSAGE_PREFIX = getPluginPrefix().appendSibling(new StringTextComponent(" &e» ").setStyle(Style.EMPTY.setColor(ORANGE)));

    /**
     * @return a copy of the coloured TextComponent
     */
    public static IFormattableTextComponent getPluginPrefix() {
        return PLUGIN_PREFIX.deepCopy();
    }

    /**
     * @return a copy of the coloured prefix
     */
    public static IFormattableTextComponent getMessagePrefix() {
        return MESSAGE_PREFIX.deepCopy();
    }
}
