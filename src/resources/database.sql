-- users - utenti registrati
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    first_name TEXT,
    last_name TEXT,
    email TEXT,
    is_admin INTEGER DEFAULT 0 CHECK(is_admin IN (0, 1)),
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- game_sessions - singole sessioni di gioco
CREATE TABLE IF NOT EXISTS game_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    score REAL NOT NULL,
    difficulty TEXT NOT NULL,
    language TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,

    -- vincoli di integrità inter-referenziale
    FOREIGN KEY(user_id) REFERENCES users(id)
);

-- answers - risposte date dagli utenti alle domande generate
CREATE TABLE IF NOT EXISTS answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    chosen_answer TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    is_correct INTEGER NOT NULL CHECK(is_correct IN (0, 1)),

    -- vincoli di integrità inter-referenziale
    FOREIGN KEY(session_id) REFERENCES game_sessions(id)
);
