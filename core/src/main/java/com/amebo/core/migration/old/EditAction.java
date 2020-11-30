package com.amebo.core.migration.old;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAction {

    private static final Map<Type, Integer> DRAWABLES = new HashMap<>();

    static {
//        DRAWABLES.put(Type.PREVIEW, -1);

        DRAWABLES.put(Type.ATTACH_FILE, -1);
        DRAWABLES.put(Type.INSERT_LINK, -1);
        DRAWABLES.put(Type.INSERT_IMAGE, -1);

        DRAWABLES.put(Type.QUOTE, -1);
        DRAWABLES.put(Type.BOLD, -1);
        DRAWABLES.put(Type.ITALIC, -1);
        DRAWABLES.put(Type.STRIKE_THROUGH, -1);

        DRAWABLES.put(Type.TEXT_COLOR, -1);
        DRAWABLES.put(Type.FONT_SIZE, -1);
        DRAWABLES.put(Type.FONT, -1);
        DRAWABLES.put(Type.HR, -1);

        DRAWABLES.put(Type.CODE, -1);
        DRAWABLES.put(Type.SUB_SCRIPT, -1);
        DRAWABLES.put(Type.SUPER_SCRIPT, -1);
        DRAWABLES.put(Type.ALIGN_LEFT, -1);
        DRAWABLES.put(Type.ALIGN_RIGHT, -1);
        DRAWABLES.put(Type.ALIGN_CENTER, -1);

        DRAWABLES.put(Type.SETTINGS, -1);
    }

    private final Type type;
    private final String name;

    private EditAction(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public static List<EditAction> defaultActions(boolean hideSettings) {
        List<EditAction> list = new ArrayList<>();
//        list.add(new EditAction(Type.PREVIEW, "Preview"));

        list.add(new EditAction(Type.ATTACH_FILE, "Attach file"));
        list.add(new EditAction(Type.INSERT_LINK, "Insert link"));
        list.add(new EditAction(Type.INSERT_IMAGE, "Insert image"));


        list.add(new EditAction(Type.QUOTE, "Quote"));
        list.add(new EditAction(Type.BOLD, "Bold"));
        list.add(new EditAction(Type.ITALIC, "Italic"));
        list.add(new EditAction(Type.STRIKE_THROUGH, "Strikethrough"));

        list.add(new EditAction(Type.TEXT_COLOR, "Text color"));
        list.add(new EditAction(Type.FONT_SIZE, "Font size"));
        list.add(new EditAction(Type.FONT, "Font face"));
        list.add(new EditAction(Type.HR, "Horizontal rule"));

        list.add(new EditAction(Type.CODE, "Code"));
        list.add(new EditAction(Type.SUB_SCRIPT, "Subscript"));
        list.add(new EditAction(Type.SUPER_SCRIPT, "Superscript"));
        list.add(new EditAction(Type.ALIGN_LEFT, "Align left"));
        list.add(new EditAction(Type.ALIGN_RIGHT, "Align right"));
        list.add(new EditAction(Type.ALIGN_CENTER, "Align center"));

        if (!hideSettings)
            list.add(new EditAction(Type.SETTINGS, "Settings"));
        return list;
    }

    public static List<EditAction> defaultActions() {
        return defaultActions(false);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @SuppressWarnings("ConstantConditions")
    public int getDrawable() {
        return DRAWABLES.get(getType());
    }

    public enum Type {
        PREVIEW(null),
        ALIGN_LEFT("left"),
        ALIGN_RIGHT("right"),
        ALIGN_CENTER("center"),
        QUOTE("quote"),
        TEXT_COLOR("color"),
        INSERT_LINK("url=", "url"),
        INSERT_IMAGE("img"),
        BOLD("b"),
        ITALIC("i"),
        STRIKE_THROUGH("s"),
        SUPER_SCRIPT("sup"),
        SUB_SCRIPT("sub"),
        FONT("font=Lucida Sans Unicode", "font"),
        FONT_SIZE("size=8pt", "size"),
        CODE("code"),
        HR("hr"),
        ATTACH_FILE(null),
        SETTINGS(null),
        EMOTICON(null);

        private final String start;
        private final String end;


        Type(String start, String end) {
            this.start = start;
            this.end = end;
        }

        Type(String start) {
            this.start = start;
            this.end = start;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }
}