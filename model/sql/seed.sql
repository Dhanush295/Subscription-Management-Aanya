-- Static seed data. Re-run on every server boot after schema.sql.

INSERT INTO plans (code, name, monthly_inr, yearly_inr, screens, quality, trial_days, yearly_discount_pct) VALUES
  ('BASIC',   'Basic',   199,  1999, 1, '480p',  7,  15),
  ('PRO',     'Pro',     499,  4999, 2, '1080p', 7,  17),
  ('PREMIUM', 'Premium', 799,  7999, 4, '4K',   14, 17);

INSERT INTO movies (id, title, language, genre, year, rating, min_plan) VALUES
  ('m01', 'KGF: Chapter 2',              'Kannada', 'Action',   2022, 8.4, 'BASIC'),
  ('m02', 'Kantara',                     'Kannada', 'Drama',    2022, 8.5, 'BASIC'),
  ('m03', 'U-Turn',                      'Kannada', 'Thriller', 2016, 7.8, 'PRO'),
  ('m04', 'SU from SO ',                 'Kannada', 'Comedy',   2013, 6.9, 'BASIC'),
  ('m05', 'Mungaru Male',                'Kannada', 'Romance',  2006, 8.2, 'BASIC'),
  ('m06', '777 Charlie',                 'Kannada', 'Drama',    2022, 8.7, 'PRO'),
  ('m07', 'Vikrant Rona',                'Kannada', 'Action',   2022, 7.1, 'PRO'),
  ('m08', 'The Dark Knight',             'English', 'Action',   2008, 9.0, 'BASIC'),
  ('m09', 'The Shawshank Redemption',    'English', 'Drama',    1994, 9.3, 'BASIC'),
  ('m10', 'Se7en',                       'English', 'Thriller', 1995, 8.6, 'PRO'),
  ('m11', 'The Grand Budapest Hotel',    'English', 'Comedy',   2014, 8.1, 'PRO'),
  ('m12', 'La La Land',                  'English', 'Romance',  2016, 8.0, 'BASIC'),
  ('m13', 'Inception',                   'English', 'Thriller', 2010, 8.8, 'PREMIUM'),
  ('m14', 'Interstellar',                'English', 'Drama',    2014, 8.7, 'PREMIUM'),
  ('m15', 'John Wick',                   'English', 'Action',   2014, 7.4, 'PRO');

-- Built-in admin account. Password hash mirrors AuthStore.hash("admin@123").
-- Formula: Integer.toHexString(("admin@123" + "netflix-salt").hashCode())
-- Computed below at boot by Schema.java; placeholder here so foreign keys work if
-- someone inserts manually. Schema.java overwrites this row with the real hash.
INSERT INTO accounts (user_id, email, password_hash, role) VALUES
  ('admin000', 'admin@gmail.com', 'PLACEHOLDER', 'ADMIN');
INSERT INTO profiles (user_id, first_name, last_name, age, email) VALUES
  ('admin000', 'Admin', 'User', 0, 'admin@gmail.com');
