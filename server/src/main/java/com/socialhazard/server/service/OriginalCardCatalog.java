package com.socialhazard.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialhazard.server.model.domain.AnswerCard;
import com.socialhazard.server.model.domain.PromptCard;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OriginalCardCatalog {

    private static final String JSON_RESOURCE_PATH = "cards/social-hazard-decks.json";
    private static final String BLACK_CARDS_FILENAME = "black_Cards.txt";
    private static final String BLUE_CARDS_FILENAME = "blue_Cards.txt";
    private static final Pattern NUMBERED_CARD_PATTERN = Pattern.compile(
            "(?ms)^\\s*\\d+\\.\\s*(.*?)(?=^\\s*\\d+\\.|\\z)"
    );
    private static final Pattern BLANK_PATTERN = Pattern.compile("_{3,}");

    private final List<PromptCard> promptCards;
    private final List<AnswerCard> answerCards;

    public OriginalCardCatalog() {
        this(new ObjectMapper());
    }

    public OriginalCardCatalog(ObjectMapper objectMapper) {
        CardDeckSource cardDeckSource = loadFromDocs().orElseGet(() -> loadFromJson(objectMapper));
        validateCards(cardDeckSource.prompts(), cardDeckSource.answers());
        this.promptCards = List.copyOf(cardDeckSource.prompts());
        this.answerCards = List.copyOf(cardDeckSource.answers());
    }

    public List<PromptCard> promptCards() {
        return promptCards;
    }

    public List<AnswerCard> answerCards() {
        return answerCards;
    }

    private java.util.Optional<CardDeckSource> loadFromDocs() {
        Path blackCardsPath = resolveDocsPath(BLACK_CARDS_FILENAME);
        Path blueCardsPath = resolveDocsPath(BLUE_CARDS_FILENAME);
        if (blackCardsPath == null || blueCardsPath == null) {
            return java.util.Optional.empty();
        }

        try {
            List<String> promptTexts = extractCardTexts(Files.readString(blackCardsPath, StandardCharsets.UTF_8));
            List<String> answerTexts = extractCardTexts(Files.readString(blueCardsPath, StandardCharsets.UTF_8));

            List<PromptCard> prompts = new ArrayList<>();
            for (int index = 0; index < promptTexts.size(); index++) {
                String promptText = normalizeCardText(promptTexts.get(index));
                prompts.add(new PromptCard(
                        "docs_prompt_" + String.format("%03d", index + 1),
                        stripPickTwoSuffix(promptText),
                        detectPickCount(promptText)
                ));
            }

            List<AnswerCard> answers = new ArrayList<>();
            for (int index = 0; index < answerTexts.size(); index++) {
                answers.add(new AnswerCard(
                        "docs_answer_" + String.format("%03d", index + 1),
                        normalizeCardText(answerTexts.get(index))
                ));
            }
            return java.util.Optional.of(new CardDeckSource(prompts, answers));
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load card text files from docs.", exception);
        }
    }

    private CardDeckSource loadFromJson(ObjectMapper objectMapper) {
        DeckCatalogFile catalogFile = loadCatalog(objectMapper);
        validateCatalog(catalogFile);
        return new CardDeckSource(flattenPrompts(catalogFile), flattenAnswers(catalogFile));
    }

    private DeckCatalogFile loadCatalog(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource(JSON_RESOURCE_PATH).getInputStream()) {
            return objectMapper.readValue(inputStream, DeckCatalogFile.class);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load Social Hazard deck catalog from " + JSON_RESOURCE_PATH + ".", exception);
        }
    }

    private void validateCatalog(DeckCatalogFile catalogFile) {
        if (catalogFile == null || catalogFile.packs() == null || catalogFile.packs().isEmpty()) {
            throw new IllegalStateException("Deck catalog must define at least one pack.");
        }

        Set<String> packIds = new LinkedHashSet<>();
        for (DeckPackFile pack : catalogFile.packs()) {
            if (pack == null) {
                throw new IllegalStateException("Deck catalog contains a null pack entry.");
            }
            if (isBlank(pack.packId())) {
                throw new IllegalStateException("Every deck pack must have a non-empty packId.");
            }
            if (!packIds.add(pack.packId())) {
                throw new IllegalStateException("Deck catalog contains duplicate packId: " + pack.packId());
            }
            if (isBlank(pack.displayName())) {
                throw new IllegalStateException("Deck pack " + pack.packId() + " is missing a displayName.");
            }
            if (pack.prompts() == null || pack.prompts().isEmpty()) {
                throw new IllegalStateException("Deck pack " + pack.packId() + " must contain prompt cards.");
            }
            if (pack.answers() == null || pack.answers().isEmpty()) {
                throw new IllegalStateException("Deck pack " + pack.packId() + " must contain answer cards.");
            }
        }
    }

    private void validateCards(List<PromptCard> prompts, List<AnswerCard> answers) {
        if (prompts.size() < 60) {
            throw new IllegalStateException("Deck catalog should contain at least 60 prompt cards for replayability.");
        }
        if (answers.size() < 120) {
            throw new IllegalStateException("Deck catalog should contain at least 120 answer cards for replayability.");
        }

        Set<String> uniquePrompts = new LinkedHashSet<>();
        for (PromptCard prompt : prompts) {
            if (prompt == null || isBlank(prompt.promptId()) || isBlank(prompt.text())) {
                throw new IllegalStateException("Prompt cards must have ids and text.");
            }
            if (prompt.pickCount() < 1 || prompt.pickCount() > 2) {
                throw new IllegalStateException("Prompt card " + prompt.promptId() + " has an unsupported pick count.");
            }
            if (!uniquePrompts.add(prompt.text())) {
                throw new IllegalStateException("Duplicate prompt card found: " + prompt.text());
            }
        }

        Set<String> uniqueAnswers = new LinkedHashSet<>();
        for (AnswerCard answer : answers) {
            if (answer == null || isBlank(answer.cardId()) || isBlank(answer.text())) {
                throw new IllegalStateException("Answer cards must have ids and text.");
            }
            if (!uniqueAnswers.add(answer.text())) {
                throw new IllegalStateException("Duplicate answer card found: " + answer.text());
            }
        }
    }

    private List<PromptCard> flattenPrompts(DeckCatalogFile catalogFile) {
        List<PromptCard> cards = new ArrayList<>();
        for (DeckPackFile pack : catalogFile.packs()) {
            for (int index = 0; index < pack.prompts().size(); index++) {
                String promptText = pack.prompts().get(index);
                cards.add(new PromptCard(
                        pack.packId() + "_prompt_" + String.format("%03d", index + 1),
                        stripPickTwoSuffix(promptText),
                        detectPickCount(promptText)
                ));
            }
        }
        return cards;
    }

    private List<AnswerCard> flattenAnswers(DeckCatalogFile catalogFile) {
        List<AnswerCard> cards = new ArrayList<>();
        for (DeckPackFile pack : catalogFile.packs()) {
            for (int index = 0; index < pack.answers().size(); index++) {
                cards.add(new AnswerCard(
                        pack.packId() + "_answer_" + String.format("%03d", index + 1),
                        normalizeCardText(pack.answers().get(index))
                ));
            }
        }
        return cards;
    }

    private List<String> extractCardTexts(String rawText) {
        List<String> cards = new ArrayList<>();
        Matcher matcher = NUMBERED_CARD_PATTERN.matcher(rawText);
        while (matcher.find()) {
            cards.add(matcher.group(1));
        }
        return cards;
    }

    private Path resolveDocsPath(String fileName) {
        Path workspacePath = Path.of("docs", fileName);
        if (Files.exists(workspacePath)) {
            return workspacePath;
        }
        Path serverModulePath = Path.of("..", "docs", fileName);
        if (Files.exists(serverModulePath)) {
            return serverModulePath.normalize();
        }
        return null;
    }

    private String normalizeCardText(String rawText) {
        if (rawText == null) {
            return "";
        }
        String normalized = rawText
                .replace("\r", "\n")
                .replaceAll("\\s*\n\\s*", " ")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s+([,.;!?])", "$1")
                .trim();
        return normalized;
    }

    private int detectPickCount(String rawText) {
        if (rawText == null) {
            return 1;
        }
        if (rawText.toUpperCase().contains("PICK 2")) {
            return 2;
        }
        return BLANK_PATTERN.matcher(rawText).results().count() >= 2 ? 2 : 1;
    }

    private String stripPickTwoSuffix(String rawText) {
        return normalizeCardText(rawText)
                .replaceAll("(?i)\\s*PICK\\s*2\\s*$", "")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record CardDeckSource(List<PromptCard> prompts, List<AnswerCard> answers) {
    }

    private record DeckCatalogFile(List<DeckPackFile> packs) {
    }

    private record DeckPackFile(String packId, String displayName, List<String> prompts, List<String> answers) {
    }
}
