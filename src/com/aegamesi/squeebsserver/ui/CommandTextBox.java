package com.aegamesi.squeebsserver.ui;

import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public class CommandTextBox extends TextBox {
    public Runnable command = null;

    @Override
    public Result handleKeyStroke(KeyStroke keyStroke) {
        if (keyStroke.getKeyType() == KeyType.Enter && command != null)
            command.run();

        return super.handleKeyStroke(keyStroke);
    }
}
