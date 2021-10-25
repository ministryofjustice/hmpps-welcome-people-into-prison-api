INSERT INTO confirmed_arrival (id, prison_number, movement_id, timestamp, prisoner_id, booking_id, booking_date, arrival_type) VALUES
(1, 'prison id', 'movement id',to_timestamp('2020-01-01 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id', 'booking id', to_date('2020-01-01', 'yyyy-mm-dd'), 'NEW_TO_PRISON'),
(2,'prison id 1', 'movement id 1',to_timestamp('2020-01-02 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 1', 'booking id 1', '2020-01-02','NEW_TO_PRISON'),
(3,'prison id 1', 'movement id 1',to_timestamp('2020-01-03 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 1', 'booking id 1', '2020-01-03', 'NEW_TO_PRISON'),
(4,'prison id 2', 'movement id 1',to_timestamp('2020-01-02 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 2', 'booking id 2', '2020-01-02', 'NEW_TO_PRISON'),
(5,'prison id 3', 'movement id 2',to_timestamp('2020-01-04 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 3', 'booking id 3', '2020-01-04', 'NEW_TO_PRISON'),
(6,'prison id 4', 'movement id 2',to_timestamp('2020-01-05 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 4', 'booking id 4', '2020-01-05', 'NEW_TO_PRISON'),
(7,'prison id 5', 'movement id 2',to_timestamp('2020-01-06 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 5', 'booking id 5', '2020-01-06', 'NEW_TO_PRISON'),
(8,'prison id 6', 'movement id 3',to_timestamp('2020-01-07 01:01:01', 'YYYY-MM-DD HH:MI:SS'), 'prisoner id 6', 'booking id 6', '2020-01-07', 'NEW_TO_PRISON');
