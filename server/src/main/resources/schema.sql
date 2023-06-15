
CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(100),
    description  VARCHAR(250),
    is_available BOOLEAN,
    owner_id     BIGINT,
    request_id   BIGINT,
    CONSTRAINT fk_items_to_users FOREIGN KEY(owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    item_id    BIGINT,
    booker_id  BIGINT,
    status     VARCHAR(15),
     CONSTRAINT fk_bookings_to_users FOREIGN KEY(booker_id) REFERENCES users(id),
     CONSTRAINT fk_bookings_to_items FOREIGN KEY(item_id) REFERENCES items(id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description  VARCHAR(250),
    requestor_id BIGINT,
    created_time TIMESTAMP,
    CONSTRAINT fk_requests_to_users FOREIGN KEY(requestor_id) REFERENCES users(id)
);


CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text      VARCHAR(250),
    item_id   BIGINT,
    author_id BIGINT,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_comments_to_users FOREIGN KEY(author_id) REFERENCES users(id),
    CONSTRAINT fk_comments_to_items FOREIGN KEY(item_id) REFERENCES items(id)
);
