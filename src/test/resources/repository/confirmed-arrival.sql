INSERT INTO confirmed_arrival (id, prison_number, movement_id, timestamp, prison_id, booking_id, arrival_date, arrival_type) VALUES
(1,'prison number', 'movement id', to_timestamp('2020-01-01 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id', 123, '2020-01-01', 'NEW_TO_PRISON'),
(2,'prison number 1', 'movement id 1',to_timestamp('2020-01-02 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 1', 123, '2020-01-02','NEW_TO_PRISON'),
(3,'prison number 1', 'movement id 1',to_timestamp('2020-01-03 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 1', 123, '2020-01-03', 'NEW_TO_PRISON'),
(4,'prison number 2', 'movement id 1',to_timestamp('2020-01-02 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 2', 124, '2020-01-02', 'NEW_TO_PRISON'),
(5,'prison number 3', 'movement id 2',to_timestamp('2020-01-04 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 3', 124, '2020-01-04', 'NEW_TO_PRISON'),
(6,'prison number 4', 'movement id 2',to_timestamp('2020-01-05 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 4', 128, '2020-01-05', 'NEW_TO_PRISON'),
(7,'prison number 5', 'movement id 2',to_timestamp('2020-01-06 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 5', 126, '2020-01-06', 'NEW_TO_PRISON'),
(8,'prison number 6', 'movement id 3',to_timestamp('2020-01-07 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prison id 6', 127, '2020-01-07', 'NEW_TO_PRISON');
