/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2016  Keiichi Tsumuta
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
package tmu.fs.sim4stamp.util;

/**
 *
 * @author Keiichi Tsumuta
 */
public enum Deviation {

    NORMAL("正常", 0),
    NOT_PROVIDING("Not Providing", 10),
    PROVIDING_MORE("Providing More", 21),
    PROVIDING_LESS("Providing Less", 22),
    TOO_EARLY("Too Early", 31),
    TOO_LATE("Too Late", 32),
    WRONG_ORDER("Wrong Order", 40),
    STOPPING_TOO_SOON("Stopping Too Soon", 51),
    APPLYING_TOO_LONG("Applying Too Long", 52);

    private final String name;
    private final int id;

    private Deviation(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return name;
    }

}
