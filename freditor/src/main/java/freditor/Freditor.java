package freditor;

import freditor.ephemeral.IntStack;
import freditor.ephemeral.IntGapBuffer;

import java.io.*;
import java.util.ArrayDeque;
import java.util.function.IntConsumer;

public final class Freditor extends CharZipper {
    private IntStack lineBreaksBefore;
    private IntStack lineBreaksAfter;

    private IntGapBuffer flexerStates;

    public final Flexer flexer;
    public final Indenter indenter;

    public Freditor(Flexer flexer, Indenter indenter) {
        lineBreaksBefore = new IntStack();
        lineBreaksAfter = new IntStack();

        flexerStates = new IntGapBuffer();

        this.flexer = flexer;
        this.indenter = indenter;
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

            refreshBookkeeping();
        }
    }

    private void refreshBookkeeping() {
        refreshLineBreaks();
        refreshFlexerStates();
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

    // LINE BREAKS

    private void refreshLineBreaks() {
        refreshLineBreaks(before(), lineBreaksBefore);
        refreshLineBreaks(after(), lineBreaksAfter);
    }

    private static void refreshLineBreaks(CharSequence text, IntStack lineBreaks) {
        lineBreaks.clear();
        final int len = text.length();
        for (int i = 0; i < len; ++i) {
            if (text.charAt(i) == '\n') {
                lineBreaks.push(i);
            }
        }
    }

    private int numberOfLineBreaks() {
        return lineBreaksBefore.size() + lineBreaksAfter.size();
    }

    public int rows() {
        return numberOfLineBreaks() + 1;
    }

    public int lengthOfRow(int row) {
        return endPositionOfRow(row) - homePositionOfRow(row);
    }

    public int homePositionOfRow(int row) {
        if (row == 0) return 0;
        --row;
        if (row < lineBreaksBefore.size()) return lineBreaksBefore.get(row) + 1;
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.get(n - 1 - row) + 1;
        return length();
    }

    public int endPositionOfRow(int row) {
        if (row < lineBreaksBefore.size()) return lineBreaksBefore.get(row);
        final int n = numberOfLineBreaks();
        if (row < n) return length() - 1 - lineBreaksAfter.get(n - 1 - row);
        return length();
    }

    public int rowOfPosition(int position) {
        if (position < before().length()) {
            return lineBreaksBefore.binarySearch(position);
        } else {
            return numberOfLineBreaks() - lineBreaksAfter.binarySearch(length() - position);
        }
    }

    public int homePositionOf(int position) {
        return homePositionOfRow(rowOfPosition(position));
    }

    public int endPositionOf(int position) {
        return endPositionOfRow(rowOfPosition(position));
    }

    public int columnOfPosition(int position) {
        return position - homePositionOf(position);
    }

    // FLEXER

    public int stateAt(int index) {
        return (0 <= index) && (index < length()) ? flexerStates.get(index) : Flexer.END;
    }

    private void refreshFlexerStates() {
        flexerStates.clear();
        int state = Flexer.END;
        final int len = length();
        for (int i = 0; i < len; ++i) {
            char x = charAt(i);
            state = flexer.nextState(state, x);
            flexerStates.add(state);
        }
    }

    private void fixFlexerStatesFrom(int index) {
        int state = stateAt(index - 1);
        final int len = length();
        for (int i = index; i < len; ++i) {
            char x = charAt(i);
            state = flexer.nextState(state, x);
            if (flexerStates.set(i, state) == state) return;
        }
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

    private boolean lexemeStartsAt(int index) {
        return stateAt(index) <= 0;
    }

    public void findOpeningParen(int start, IntConsumer present, Runnable missing) {
        int nesting = 0;
        for (int i = cursor - 1; i >= start; --i) {
            switch (stateAt(i)) {
                case Flexer.CLOSING_PAREN:
                case Flexer.CLOSING_BRACKET:
                case Flexer.CLOSING_BRACE:
                    --nesting;
                    break;

                case Flexer.OPENING_PAREN:
                case Flexer.OPENING_BRACKET:
                case Flexer.OPENING_BRACE:
                    if (nesting == 0) {
                        present.accept(i);
                        return;
                    }
                    ++nesting;
            }
        }
        missing.run();
    }

    public void findClosingParen(int end, IntConsumer present, Runnable missing) {
        int nesting = 0;
        for (int i = cursor; i < end; ++i) {
            switch (stateAt(i)) {
                case Flexer.OPENING_PAREN:
                case Flexer.OPENING_BRACKET:
                case Flexer.OPENING_BRACE:
                    ++nesting;
                    break;

                case Flexer.CLOSING_PAREN:
                case Flexer.CLOSING_BRACKET:
                case Flexer.CLOSING_BRACE:
                    if (nesting == 0) {
                        present.accept(i);
                        return;
                    }
                    --nesting;
            }
        }
        missing.run();
    }

    // CHARZIPPER OVERRIDES

    @Override
    public void clear() {
        lineBreaksBefore.clear();
        lineBreaksAfter.clear();

        flexerStates.clear();

        super.clear();
    }

    @Override
    protected void focusOn(int index) {
        super.focusOn(index);
        final int len = length();
        mirrorLineBreaks(lineBreaksBefore, lineBreaksAfter, index, len - 1);
        mirrorLineBreaks(lineBreaksAfter, lineBreaksBefore, len - index, len - 1);
    }

    private void mirrorLineBreaks(IntStack src, IntStack dst, int threshold, int mirror) {
        while (!src.isEmpty() && src.top() >= threshold) {
            dst.push(mirror - src.pop());
        }
    }

    @Override
    public void insertAt(int index, char x) {
        super.insertAt(index, x);
        if (x == '\n') {
            lineBreaksBefore.push(index);
        }
        flexerStates.add(index, Integer.MIN_VALUE);
        fixFlexerStatesFrom(index);
    }

    @Override
    public void insertAt(int index, CharSequence s) {
        super.insertAt(index, s);
        final CharSequence before = before();
        final int end = before.length();
        for (int i = index; i < end; ++i) {
            if (before.charAt(i) == '\n') {
                lineBreaksBefore.push(i);
            }
            flexerStates.add(i, Integer.MIN_VALUE);
        }
        fixFlexerStatesFrom(index);
    }

    private void insertAt(int index, char x, CharSequence s) {
        super.insertAt(index, x);
        if (x == '\n') {
            lineBreaksBefore.push(index);
        }
        flexerStates.add(index, Integer.MIN_VALUE);

        final int start = after().length();
        insertAfterFocus(s);
        final CharSequence after = after();
        final int end = after.length();
        for (int i = start; i < end; ++i) {
            if (after.charAt(i) == '\n') {
                lineBreaksAfter.push(i);
            }
            flexerStates.add(index + 1, Integer.MIN_VALUE);
        }
        fixFlexerStatesFrom(index);
    }

    @Override
    public char deleteLeftOf(int index) {
        char deleted = super.deleteLeftOf(index);
        if (deleted == '\n') {
            lineBreaksBefore.pop();
        }
        flexerStates.remove(index - 1);
        fixFlexerStatesFrom(index - 1);
        return deleted;
    }

    @Override
    public char deleteRightOf(int index) {
        char deleted = super.deleteRightOf(index);
        if (deleted == '\n') {
            lineBreaksAfter.pop();
        }
        flexerStates.remove(index);
        fixFlexerStatesFrom(index);
        return deleted;
    }

    @Override
    public String deleteRange(int start, int end) {
        String result = super.deleteRange(start, end);
        int firstObsoleteLineBreak = lineBreaksBefore.binarySearch(start);
        lineBreaksBefore.shrinkToSize(firstObsoleteLineBreak);
        flexerStates.remove(start, end);
        fixFlexerStatesFrom(start);
        return result;
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

    public void setCursorTo(int row, int column) {
        cursor = Math.min(homePositionOfRow(row) + column, endPositionOfRow(row));
        desiredColumn = column;
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
        cursor = Math.min(length(), endOfLexeme(cursor));
        forgetDesiredColumn();
    }

    public String lexemeAtCursor() {
        return lexemeAt(cursor);
    }

    private String lexemeAt(int index) {
        int start = startOfLexeme(index);
        int end = Math.min(length(), endOfLexeme(index));
        return subSequence(start, end);
    }

    public String symbolNearCursor(int symbolFirst) {
        if (stateAt(cursor) <= symbolFirst) {
            return lexemeAt(cursor);
        } else if (cursor >= 1) {
            return lexemeAt(cursor - 1);
        } else {
            return "";
        }
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

        SystemClipboard.set(subSequence(selectionStart(), selectionEnd()));
        lastAction = EditorAction.OTHER;
    }

    public void cut() {
        if (selectionIsEmpty()) return;

        commit();
        SystemClipboard.set(deleteRange(selectionStart(), selectionEnd()));
        cursor = origin = selectionStart();
        lastAction = EditorAction.OTHER;
    }

    public void paste() {
        insert(SystemClipboard.getVisibleLatin1());
    }

    public void insertCharacter(char c) {
        deleteSelection();
        if (lastAction != EditorAction.SINGLE_INSERT || cursor != lastCursor || c == ' ') {
            commit();
            lastAction = EditorAction.SINGLE_INSERT;
        }

        insertWithSynthAt(cursor++, c);
        lastCursor = cursor;
        forgetDesiredColumn();
        adjustOrigin();
    }

    private void insertWithSynthAt(int index, char x) {
        final int oldState = stateAt(index);
        final int newState = flexer.nextState(stateAt(index - 1), x);
        if (newState == oldState && flexer.preventInsertion(newState)) return;

        String synth = flexer.synthesizeOnInsert(newState, oldState);
        if (synth.isEmpty()) {
            insertAt(index, x);
        } else {
            insertAt(index, x, synth);
        }
    }

    public void insert(CharSequence s) {
        deleteSelection();
        commit();

        insertAt(cursor, s);
        cursor += s.length();
        forgetDesiredColumn();
        adjustOrigin();
        lastAction = EditorAction.OTHER;
    }

    public void onEnter(char previousCharTyped) {
        deleteSelection();
        commit();

        String synth = indenter.synthesizeOnEnterAfter(previousCharTyped);
        if (synth.isEmpty()) {
            insertAt(cursor++, '\n');
        } else {
            insertAt(cursor++, '\n', synth);
        }
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
        while (cursor > 0) {
            cursor = startOfLexeme(cursor - 1);
            int state = stateAt(cursor);
            if (state == Flexer.NEWLINE || state == Flexer.FIRST_SPACE) continue;

            forgetDesiredColumn();
            break;
        }
    }

    public void moveCursorBeforePreviousOpeningParen(boolean isShiftDown) {
        if (isShiftDown) {
            findClosingParen(length(), closing -> origin = closing + 1, () -> origin = length());
        }
        findOpeningParen(0, opening -> cursor = opening, () -> cursor = 0);
        forgetDesiredColumn();
    }

    public void moveCursorRight() {
        if (cursor < length()) {
            ++cursor;
            forgetDesiredColumn();
        }
    }

    public void moveCursorToNextLexeme() {
        while (cursor < length()) {
            cursor = endOfLexeme(cursor);
            int state = stateAt(cursor);
            if (state == Flexer.NEWLINE || state == Flexer.FIRST_SPACE) continue;

            forgetDesiredColumn();
            break;
        }
    }

    public void moveCursorAfterNextClosingParen(boolean isShiftDown) {
        if (isShiftDown) {
            findOpeningParen(0, opening -> origin = opening, () -> origin = 0);
        }
        findClosingParen(length(), closing -> cursor = closing + 1, () -> cursor = length());
        forgetDesiredColumn();
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

            int destination = endPositionOf(selectionEndForLineMovement());
            insertAt(destination, '\n');
            insertAt(destination + 1, line);
        }
    }

    private int selectionEndForLineMovement() {
        // When multiple lines are selected, the last line should not be moved
        // if the cursor is at the beginning of the line.
        return selectionIsEmpty() ? selectionEnd() : selectionEnd() - 1;
    }

    public void moveSelectedLinesDown() {
        int below = rowOfPosition(selectionEndForLineMovement()) + 1;
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
        int[] corrections = indenter.corrections(this);
        for (int row = corrections.length - 1; row >= 0; --row) {
            correct(row, corrections[row]);
        }
        setRowAndColumn(oldRow, leadingSpaces(homePositionOfRow(oldRow)));
        adjustOrigin();
        forgetDesiredColumn();
    }

    private void correct(int row, int correction) {
        int start = homePositionOfRow(row);
        if (correction > 0) {
            insertAt(start, SpaceSequence.of(correction));
        } else if (correction < 0) {
            deleteRange(start, start - correction);
        }
    }

    public int leadingSpaces(int index) {
        int start = index;
        final int len = length();
        if (index < len && stateAt(index) == Flexer.FIRST_SPACE) {
            ++index;
            while (index < len && stateAt(index) == Flexer.NEXT_SPACE) {
                ++index;
            }
        }
        return index - start;
    }

    // PERSISTENCE

    public void loadFromFile(String pathname) throws IOException {
        loadFromReader(new FileReader(pathname));
    }

    public void loadFromString(String program) {
        try {
            loadFromReader(new StringReader(program));
        } catch (IOException impossible) {
            impossible.printStackTrace();
        }
    }

    private void loadFromReader(Reader reader) throws IOException {
        try (BufferedReader in = new BufferedReader(reader)) {
            String line = in.readLine();
            if (line != null) {
                super.clear();
                insertBeforeFocus(line);
                while ((line = in.readLine()) != null) {
                    insertBeforeFocus("\n");
                    insertBeforeFocus(line);
                }
                refreshBookkeeping();
                if (cursor >= length()) {
                    cursor = length();
                }
                adjustOrigin();
                forgetDesiredColumn();
            }
        }
    }

    public void saveToFile(String pathname) throws IOException {
        String text = toString();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(pathname))) {
            for (int i = 0; i < rows(); ++i) {
                int start = homePositionOfRow(i);
                int end = endPositionOfRow(i);
                out.write(text, start, end - start);
                out.newLine();
            }
        }
    }
}