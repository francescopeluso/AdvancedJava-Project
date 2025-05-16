-- LANGUAGE
CREATE TABLE IF NOT EXISTS Language (
    code TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

-- WORDS
CREATE TABLE IF NOT EXISTS Words (
    id INTEGER PRIMARY KEY,
    word TEXT NOT NULL,
    language_code TEXT NOT NULL,
    FOREIGN KEY (language_code) REFERENCES Language(code)
);

-- DOCUMENT
CREATE TABLE IF NOT EXISTS Document (
    id INTEGER PRIMARY KEY,
    path TEXT NOT NULL,
    language_code TEXT NOT NULL,
    FOREIGN KEY (language_code) REFERENCES Language(code)
);

-- DOCUMENT_WORD: Document-Word Matrix con frequenza
CREATE TABLE IF NOT EXISTS document_word (
    document_id INTEGER NOT NULL,
    word_id INTEGER NOT NULL,
    frequenza INTEGER NOT NULL,
    PRIMARY KEY (document_id, word_id),
    FOREIGN KEY (document_id) REFERENCES Document(id),
    FOREIGN KEY (word_id) REFERENCES Words(id)
);

-- PLAYER
CREATE TABLE IF NOT EXISTS Player (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    cognome TEXT NOT NULL
);

-- GAME con difficoltà e durata
CREATE TABLE IF NOT EXISTS Game (
    id INTEGER PRIMARY KEY,
    data TEXT NOT NULL, -- formato 'YYYY-MM-DD'
    difficolta TEXT NOT NULL CHECK (difficolta IN ('facile', 'medio', 'difficile')),
    durata_minuti INTEGER NOT NULL CHECK (durata_minuti >= 0)
);

-- PLAYER_GAME: relazione tra Player e Game
CREATE TABLE IF NOT EXISTS player_game (
    game_id INTEGER NOT NULL,
    player_username TEXT NOT NULL,
    PRIMARY KEY (game_id, player_username),
    FOREIGN KEY (game_id) REFERENCES Game(id),
    FOREIGN KEY (player_username) REFERENCES Player(username)
);

-- GAME_DOCUMENT: relazione tra Game e Document
CREATE TABLE IF NOT EXISTS game_document (
    game_id INTEGER NOT NULL,
    document_id INTEGER NOT NULL,
    PRIMARY KEY (game_id, document_id),
    FOREIGN KEY (game_id) REFERENCES Game(id),
    FOREIGN KEY (document_id) REFERENCES Document(id)
);

-- QUESTION
CREATE TABLE IF NOT EXISTS Question (
    id INTEGER PRIMARY KEY,
    text TEXT NOT NULL
);

-- ANSWERS
CREATE TABLE IF NOT EXISTS Answers (
    id INTEGER PRIMARY KEY,
    value TEXT NOT NULL,
    correct BOOLEAN NOT NULL
);

-- QUESTION_ANSWER: relazione tra Question e Answers
CREATE TABLE IF NOT EXISTS question_answer (
    question_id INTEGER NOT NULL,
    answer_id INTEGER NOT NULL,
    PRIMARY KEY (question_id, answer_id),
    FOREIGN KEY (question_id) REFERENCES Question(id),
    FOREIGN KEY (answer_id) REFERENCES Answers(id)
);

-- GAME_ANSWER: relazione tra Game e Answers
CREATE TABLE IF NOT EXISTS game_answer (
    game_id INTEGER NOT NULL,
    answer_id INTEGER NOT NULL,
    PRIMARY KEY (game_id, answer_id),
    FOREIGN KEY (game_id) REFERENCES Game(id),
    FOREIGN KEY (answer_id) REFERENCES Answers(id)
);

-- GAME_QUESTION: relazione tra Game e Question
CREATE TABLE IF NOT EXISTS game_question (
    game_id INTEGER NOT NULL,
    question_id INTEGER NOT NULL,
    PRIMARY KEY (game_id, question_id),
    FOREIGN KEY (game_id) REFERENCES Game(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
);