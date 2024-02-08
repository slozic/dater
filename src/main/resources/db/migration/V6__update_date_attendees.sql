ALTER TABLE date_attendees
DROP CONSTRAINT date_attendees_attendee_id_fkey,
ADD CONSTRAINT date_attendees_attendee_id_fkey
   FOREIGN KEY (attendee_id)
   REFERENCES users(id)
   ON DELETE CASCADE;