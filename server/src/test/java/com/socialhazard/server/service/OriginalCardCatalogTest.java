package com.socialhazard.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OriginalCardCatalogTest {

    @Test
    void loadsDeckPacksFromJsonCatalog() {
        OriginalCardCatalog catalog = new OriginalCardCatalog();

        assertEquals(78, catalog.promptCards().size());
        assertEquals(499, catalog.answerCards().size());
        assertTrue(catalog.promptCards().stream().allMatch(card -> card.promptId().startsWith("docs_prompt_")));
        assertTrue(catalog.answerCards().stream().allMatch(card -> card.cardId().startsWith("docs_answer_")));
        assertTrue(catalog.promptCards().stream().anyMatch(card -> card.pickCount() == 2));
    }
}
