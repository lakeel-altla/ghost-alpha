package com.lakeel.altla.ghost.alpha.mock.lib.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextContextMenuEditText extends android.support.v7.widget.AppCompatEditText {

    public interface TextContextMenuListener {

        void onCut();

        void onPaste(@Nullable Editable editable);

        void onCopy();
    }

    private final List<TextContextMenuListener> listeners = Collections.synchronizedList(new ArrayList<TextContextMenuListener>());

    public TextContextMenuEditText(@NonNull Context context) {
        super(context);
    }

    public TextContextMenuEditText(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public TextContextMenuEditText(@NonNull Context context, @NonNull AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id) {
            case android.R.id.cut:
                onCut();
                break;
            case android.R.id.paste:
                onPaste();
                break;
            case android.R.id.copy:
                onCopy();
        }
        return consumed;
    }

    public void addTextContextMenuListener(@NonNull TextContextMenuListener listener) {
        listeners.add(listener);
    }

    public void removeTextContextMenuListener(@NonNull TextContextMenuListener listener) {
        listeners.remove(listener);
    }

    private void onCut() {
        for (TextContextMenuListener listener : listeners) {
            listener.onCut();
        }
    }

    private void onPaste() {
        for (TextContextMenuListener listener : listeners) {
            listener.onPaste(getEditableText());
        }
    }

    private void onCopy() {
        for (TextContextMenuListener listener : listeners) {
            listener.onCopy();
        }
    }
}