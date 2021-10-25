CREATE TABLE "confirmed_arrival"
(
    id serial NOT NULL constraint confirmed_arrival_pk PRIMARY KEY,
    prison_number VARCHAR(255) NOT NULL,
    movement_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    prisoner_id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL,
    booking_date DATE NOT NULL,
    arrival_type VARCHAR(255) NOT NULL
);

CREATE INDEX id_idx ON "confirmed_arrival"(id);
CREATE INDEX booking_date_idx ON "confirmed_arrival"(booking_date);
CREATE INDEX prison_number_idx ON "confirmed_arrival"(prison_number);
