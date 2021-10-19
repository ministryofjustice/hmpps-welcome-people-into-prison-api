CREATE TABLE "bookings"
(
    id serial NOT NULL constraint bookings_pk PRIMARY KEY,
    prison_id VARCHAR(255) NOT NULL,
    movement_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    prisoner_id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL
);

CREATE INDEX id_idx ON "bookings"(id);
