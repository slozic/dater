INSERT INTO users (id, username, email, "password", enabled, created_at, firstname, lastname, birthday, date_list_gender_filter)
VALUES ('aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5', 'clara', 'clara@gmail.com',
        '$2a$10$gvFbrJnQn.wKTCXcTO4CTeJeHqcTin/PO8vssGbHQAQSvqRWDIYb6', true, now(), 'Clara', 'Schmidt',
        '2000-07-28 00:00:00', 'ALL'),
       ('6c49abd4-0e82-47f6-bb0c-558c9a890bd4', 'tom.h', 'tom@gmail.com',
        '$2a$10$iSywFVmkJ00tT70gAn1LhOzWh6TTgOW811lrj/FgAken3ldC0Mw1G', true, now(), 'Tom', 'Hanks',
        '1972-03-03 00:00:00', 'ALL'),
       ('d70e2eca-5e2d-4fd6-b517-e09c8a2afe0d', 'sam.s', 'sam@gmail.com',
        '$2a$10$AADq92Dsjc2mcOuDucNP4eGCqsoQAqopH9Ild6dcf63ZDTuVhO2Oi', true, now(), 'Sam', 'Stone',
        '1993-04-12 00:00:00', 'ALL');

INSERT INTO dates (id, description, location, enabled, scheduled_time, created_by, created_at, title)
VALUES ('be62daa9-6cda-45ea-8b0b-4ea15f735e53', 'Perfect afternoon date in the alps', 'Alps', true,
        now() + interval '1 day', 'aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5', now(), 'Date in the Alps');

INSERT INTO date_attendees (attendee_id, date_id, status, time_added)
VALUES ('aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5', 'be62daa9-6cda-45ea-8b0b-4ea15f735e53', 'ACCEPTED', now()),
       ('6c49abd4-0e82-47f6-bb0c-558c9a890bd4', 'be62daa9-6cda-45ea-8b0b-4ea15f735e53', 'REJECTED', now()),
       ('d70e2eca-5e2d-4fd6-b517-e09c8a2afe0d', 'be62daa9-6cda-45ea-8b0b-4ea15f735e53', 'ACCEPTED', now());

INSERT INTO chat_messages (id, date_id, sender_id, recipient_id, participant_user_id, message, created_at)
VALUES ('2f2627f3-f01d-4af8-9bb3-9b7a4d8f1432', 'be62daa9-6cda-45ea-8b0b-4ea15f735e53',
        'aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5', '6c49abd4-0e82-47f6-bb0c-558c9a890bd4',
        '6c49abd4-0e82-47f6-bb0c-558c9a890bd4', 'old thread hello', now() - interval '2 hour'),
       ('89e8435a-34c8-4718-b73e-d157ff40f1da', 'be62daa9-6cda-45ea-8b0b-4ea15f735e53',
        'aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5', 'd70e2eca-5e2d-4fd6-b517-e09c8a2afe0d',
        'd70e2eca-5e2d-4fd6-b517-e09c8a2afe0d', 'new thread hello', now() - interval '1 hour');
