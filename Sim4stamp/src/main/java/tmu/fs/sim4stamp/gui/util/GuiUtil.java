/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2018  Keiichi Tsumuta
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.gui.util;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.scene.control.TextFormatter;

/**
 * 画面にに関するユーティリティクラス
 *
 * @author Keiichi Tsumuta
 */
public class GuiUtil {

    private static final Pattern NUMBER = Pattern.compile("[^0-9]");
    private static final Pattern NUMBER_D = Pattern.compile("[^0-9.]");

    public static TextFormatter<String> getIntTextFormater() {
        UnaryOperator<TextFormatter.Change> filter = (TextFormatter.Change t) -> {
            if (t.isReplaced()) {
                if(NUMBER.matcher(t.getText()).matches()){
                    t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));
                }
            }
            if (t.isAdded()) {
                if(NUMBER.matcher(t.getText()).matches()){   
                    t.setText("");
                }
            }
            return t;
        };
        return new TextFormatter<>(filter);
    }

    public static TextFormatter<String> getDecimalTextFormater() {
        UnaryOperator<TextFormatter.Change> filter = (TextFormatter.Change t) -> {
            if (t.isReplaced()) {
                if(NUMBER.matcher(t.getText()).matches()){
                    t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));
                }
            }
            if (t.isAdded()) {
                if (t.getControlText().contains(".")) {
                    if(NUMBER.matcher(t.getText()).matches()){
                        t.setText("");
                    }
                }else if(NUMBER_D.matcher(t.getText()).matches()){   
                    t.setText("");
                }
            }
            return t;
        };
        return new TextFormatter<>(filter);
    }

}
