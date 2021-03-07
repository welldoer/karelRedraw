package freditor;

import java.util.ArrayDeque;

public final class Freditor extends CharZipper {
    public static final int INDENT_BY = 4;

    private static String clipboard = "";

    public Freditor(Flexer flexer) {
        super(flexer);
    }

    private int origin;
    private int cursor;
    private int desiredColumn;

    private class Memento extends CharZipper.Memento {
        private final int origin = Freditor.this.origin;
        private final int cursor = Freditor.this.cursor;
        private final int desiredColumn = Freditor.this.desiredColumn;

        @Override
        public void restore() {
            super.restore();
            Freditor.this.origin = origin;
            Freditor.this.cursor = cursor;
            Freditor.this.desiredColumn = desiredColumn;
        }
    }

    private void forgetDesiredColumn() {
        desiredColumn = -1;
    }

    private int rememberColumn() {
        if (desiredColumn == -1) {
            desiredColumn = column();
        }
        return desiredColumn;
    }

    public String getLineUntilCursor() {
        return subSequence(homePositionOf(cursor), cursor);
    }

    // CURSOR

    public int cursor() {
        return cursor;
    }

    public int row() {
        return rowOfPosition(cursor);
    }

    public int column() {
        return columnOfPosition(cursor);
    }

    public void adjustOrigin() {
        origin = cursor;
    }

    public boolean selectionIsEmpty() {
        return origin == cursor;
    }

    public int selectionStart() {
        return origin < cursor ? origin : cursor;
    }

    public int selectionEnd() {
        return origin > cursor ? origin : cursor;
    }

    public void setRowAndColumn(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        desiredColumn = column;
    }

    public void setCursorTo(int position) {
        cursor = position;
        forgetDesiredColumn();
        adjustOrigin();
    }

    public void setCursorTo(String prefix) {
        // TODO optimize
        int index = toString().indexOf(prefix);
        if (index != -1) {
            setCursorTo(index);
        }
    }

    public void selectLexemeAtCursor() {
        origin = startOfLexeme(cursor);
        if (cursor < length()) {
            cursor = endOfLexeme(cursor);
        }
        forgetDesiredColumn();
    }

    private boolean lexemeStartsAt(int index) {
        return index == length() || intAt(index) < 0;
    }

    public int startOfLexeme(int index) {
        while (!lexemeStartsAt(index)) {
            --index;
        }
        return index;
    }

    public int endOfLexeme(int index) {
        do {
            ++index;
        } while (!lexemeStartsAt(index));
        return index;
    }

    // TEXT MANIPULATION

    private final ArrayDeque<Memento> past = new ArrayDeque<>();
    private final ArrayDeque<Memento> future = new ArrayDeque<>();

    private int lastCursor = -1;
    private EditorAction lastAction = EditorAction.OTHER;

    private void commit() {
        past.push(new Memento());
        future.clear();
    }

    public void undo() {
        if (past.isEmpty()) return;

        future.push(new Memento());
        past.pop().restore();
        lastAction = EditorAction.OTHER;
    }

    public void redo() {
        if (future.isEmpty()) return;

        past.push(new Memento());
        future.pop().restore();
        lastAction = EditorAction.OTHER;
    }

    private boolean deleteSelection() {
        if (selectionIsEmpty()) return false;

        commit();
        deleteRange(selectionStart(), selectionEnd());
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
        return true;
    }

    public void copy() {
        if (selectionIsEmpty()) return;

        clipboard = subSequence(selectionStart(), selectionEnd());
        lastAction = EditorAction.OTHER;
    }

    public void cut() {
        if (selectionIsEmpty()) return;

        commit();
        clipboard = deleteRange(selectionStart(), selectionEnd());
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
    }

    public void paste() {
        insertString(clipboard);
    }

    public void insertCharacter(char c) {
        deleteSelection();
        if (lastAction != EditorAction.SINGLE_INSERT || cursor != lastCursor || c == ' ') {
            commit();
            lastAction = EditorAction.SINGLE_INSERT;
        }

        insertAt(cursor++, c);
        lastCursor = cursor;
        forgetDesiredColumn();
        adjustOrigin();
    }

    public void insertString(String s) {
        deleteSelection();
        commit();

        insertAt(cursor, s);
        cursor += s.length();
        forgetDesiredColumn();
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    public void onEnter(String synthesize) {
        deleteSelection();
        commit();

        insertAt(cursor, synthesize);
        insertAt(cursor++, '\n');
        adjustOrigin();
        indent();
        lastAction = EditorAction.OTHER;
    }

    public void deleteLeft() {
        if (deleteSelection()) return;

        if (cursor > 0) {
            if (lastAction != EditorAction.SINGLE_DELETE || cursor != lastCursor) {
                commit();
                lastAction = EditorAction.SINGLE_DELETE;
            }
            deleteLeftOf(cursor--);
            lastCursor = cursor;
            forgetDesiredColumn();
            adjustOrigin();
        }
    }

    public void deleteRight() {
        if (deleteSelection()) return;

        if (cursor < length()) {
            if (lastAction != EditorAction.SINGLE_DELETE || cursor != lastCursor) {
                commit();
                lastAction = EditorAction.SINGLE_DELETE;
            }
            deleteRightOf(cursor);
            lastCursor = cursor;
            forgetDesiredColumn();
            adjustOrigin();
        }
    }

    public void deleteCurrentLine() {
        commit();
        rememberColumn();
        int row = row();
        deleteRange(homePositionOfRow(row), homePositionOfRow(row + 1));
        setRowAndColumn(row, desiredColumn);
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    // NAVIGATION

    public void moveCursorLeft() {
        if (cursor > 0) {
            --cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToPreviousLexeme() {
        if (cursor > 0) {
            cursor = startOfLexeme(cursor - 1);
            forgetDesiredColumn();
        }
    }

    public void moveCursorRight() {
        if (cursor < length()) {
            ++cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToNextLexeme() {
        if (cursor < length()) {
            cursor = endOfLexeme(cursor);
            forgetDesiredColumn();
        }
    }

    public void moveCursorUp() {
        int row = row() - 1;
        if (row >= 0) {
            setRowAndColumn(row, rememberColumn());
        }
    }

    public void moveCursorUp(int rows) {
        int row = Math.max(0, row() - rows);
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorDown() {
        int row = row() + 1;
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorDown(int rows) {
        int row = row() + rows;
        setRowAndColumn(row, rememberColumn());
    }

    public void moveCursorStart() {
        cursor = homePositionOf(cursor);
        forgetDesiredColumn();
    }

    public void moveCursorEnd() {
        cursor = endPositionOf(cursor);
        forgetDesiredColumn();
    }

    public void moveCursorTop() {
        cursor = 0;
        forgetDesiredColumn();
    }

    public void moveCursorBottom() {
        cursor = length();
        forgetDesiredColumn();
    }

    public void moveSelectedLinesUp() {
        int above = rowOfPosition(selectionStart()) - 1;
        if (above >= 0) {
            if (lastAction != EditorAction.LINE_MOVE) {
                commit();
                lastAction = EditorAction.LINE_MOVE;
            }
            int home = homePositionOfRow(above);
            int end = endPositionOfRow(above);
            int len = end - home + 1;
            deleteRightOf(end);
            String line = deleteRange(home, end);

            cursor -= len;
            origin -= len;

            int destination = endPositionOf(selectionEnd());
            insertAt(destination, '\n');
            insertAt(destination + 1, line);
        }
    }

    public void moveSelectedLinesDown() {
        int below = rowOfPosition(selectionEnd()) + 1;
        if (below < rows()) {
            if (lastAction != EditorAction.LINE_MOVE) {
                commit();
                lastAction = EditorAction.LINE_MOVE;
            }
            int home = homePositionOfRow(below);
            int end = endPositionOfRow(below);
            int len = end - home + 1;
            String line = deleteRange(home, end);
            deleteLeftOf(home);

            int destination = homePositionOf(selectionStart());
            insertAt(destination, line);
            insertAt(destination + len - 1, '\n');

            cursor += len;
            origin += len;
        }
    }

    // INDENTATION

    public void indent() {

        final int oldRow = row();
        final int len = rows();
        int[] indentation = new int[len];
        int reference = 0;
        int minimum = 0;
        for (int row = 0; row < len; ++row) {
            int line = homePositionOfRow(row);
            indentation[row] = reference - leadingClosers(line);
            reference = reference + openersMinusClosers(line);
            minimum = Math.min(minimum, reference);
        }
        for (int row = len - 1; row >= 0; --row) {
            indent(row, INDENT_BY * (indentation[row] - minimum));
        }
        setRowAndColumn(oldRow, leadingSpaces(homePositionOfRow(oldRow)));
        adjustOrigin();
        forgetDesiredColumn();
    }

    private int leadingClosers(int i) {
        int n = 0;
        final int len = length();
        for (; i < len; ++i) {
            int x = intAt(i);
            if (flexer.indentationDelta(x >> 16) < 0) {
                ++n;
            } else if ((char) x != ' ') {
                return n;
            }
        }
        return n;
    }

    private int openersMinusClosers(int i) {
        int difference = 0;
        final int len = length();
        for (; i < len; ++i) {
            int x = intAt(i);
            if ((char) x == '\n') {
                return difference;
            }
            difference += flexer.indentationDelta(x >> 16);
        }
        return difference;
    }

    private void indent(int row, int indentation) {
        int start = homePositionOfRow(row);
        indentation -= leadingSpaces(start);
        if (indentation > 0) {
            insertSpacesAt(start, indentation);
        } else if (indentation < 0) {
            deleteSpacesAt(start, -indentation);
        }
    }

    private int leadingSpaces(int i) {
        int start = i;
        final int len = length();
        while (i < len && charAt(i) == ' ') {
            ++i;
        }
        return i - start;
    }
}