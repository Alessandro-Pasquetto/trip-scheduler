CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE trip_plan (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,

    CONSTRAINT fk_trip_plan_user
       FOREIGN KEY(user_id)
           REFERENCES user_account(id)
           ON DELETE CASCADE
);

CREATE TYPE activity_category AS ENUM (
    'Activity',
    'Transport',
    'Other',
);

CREATE TABLE activity (
    id SERIAL PRIMARY KEY,
    trip_plan_id INTEGER NOT NULL,
    day DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category activity_category NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CONSTRAINT fk_activity_trip_plan
        FOREIGN KEY (trip_plan_id)
        REFERENCES trip_plan(id)
        ON DELETE CASCADE
);