CREATE TABLE "booking"
(
    id serial NOT NULL constraint booking_pk PRIMARY KEY,
    prison_id VARCHAR(255) NOT NULL,
    movement_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    prisoner_id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL,
    booking_date DATE NOT NULL,
    move_type VARCHAR(255) NOT NULL
);

CREATE INDEX id_idx ON "booking"(id);
