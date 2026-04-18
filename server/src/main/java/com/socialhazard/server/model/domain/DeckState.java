package com.socialhazard.server.model.domain;

import com.socialhazard.server.exception.GameException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class DeckState<T> {

    private final Deque<T> drawPile = new ArrayDeque<>();
    private final List<T> discardPile = new ArrayList<>();
    private final Random random;

    public DeckState(List<T> cards, Random random) {
        this.random = random;
        List<T> shuffled = new ArrayList<>(cards);
        shuffle(shuffled);
        drawPile.addAll(shuffled);
    }

    public T draw() {
        refillIfNeeded();
        if (drawPile.isEmpty()) {
            throw new GameException("DECK_EXHAUSTED", "Unable to draw another card from the deck.");
        }
        return drawPile.removeFirst();
    }

    public void discard(T card) {
        if (card != null) {
            discardPile.add(card);
        }
    }

    public int remainingDrawCount() {
        return drawPile.size();
    }

    private void refillIfNeeded() {
        if (!drawPile.isEmpty() || discardPile.isEmpty()) {
            return;
        }
        List<T> shuffled = new ArrayList<>(discardPile);
        discardPile.clear();
        shuffle(shuffled);
        drawPile.addAll(shuffled);
    }

    private void shuffle(List<T> cards) {
        for (int index = cards.size() - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            T value = cards.get(index);
            cards.set(index, cards.get(swapIndex));
            cards.set(swapIndex, value);
        }
    }
}
