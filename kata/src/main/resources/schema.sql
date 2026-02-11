DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS event;

CREATE TABLE event (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       date_time TIMESTAMP NOT NULL
);

CREATE TABLE seat (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      event_id BIGINT NOT NULL,
                      seat_number VARCHAR(10) NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES event(id)
);