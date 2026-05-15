-- route-service/src/main/resources/data.sql
-- Pune Metro sample seed data for MetroSewa Route Service

DELETE FROM line_stations;
DELETE FROM stations;
DELETE FROM metro_lines;

-- ================= METRO LINES =================
INSERT INTO metro_lines (id, line_number, line_name, color_code, start_station, end_station, active) VALUES
(1, 'Line 1', 'Purple Line', '#800080', 'PCMC Bhavan', 'Swargate', true),
(2, 'Line 2', 'Aqua Line', '#00BFFF', 'Vanaz', 'Ramwadi', true),
(3, 'Line 3', 'Red Line', '#FF0000', 'Megapolis Circle', 'Civil Court', true);

-- ================= STATIONS =================
INSERT INTO stations (id, station_name, station_code, interchange, active) VALUES
-- Purple Line stations
(1, 'PCMC Bhavan', 'PCMC', false, true),
(2, 'Sant Tukaram Nagar', 'STN', false, true),
(3, 'Nashik Phata', 'NSP', false, true),
(4, 'Kasarwadi', 'KSW', false, true),
(5, 'Phugewadi', 'PHG', false, true),
(6, 'Dapodi', 'DAP', false, true),
(7, 'Bopodi', 'BOP', false, true),
(8, 'Khadki', 'KHD', false, true),
(9, 'Range Hills', 'RHL', false, true),
(10, 'Shivaji Nagar', 'SHV', true, true),
(11, 'Civil Court', 'CIVIL', true, true),
(12, 'Kasba Peth', 'KSP', false, true),
(13, 'Mandai', 'MAN', false, true),
(14, 'Swargate', 'SWG', false, true),

-- Aqua Line stations
(15, 'Vanaz', 'VNZ', false, true),
(16, 'Anand Nagar', 'ANG', false, true),
(17, 'Ideal Colony', 'IDC', false, true),
(18, 'Nal Stop', 'NAL', false, true),
(19, 'Garware College', 'GWC', false, true),
(20, 'Deccan Gymkhana', 'DEC', false, true),
(21, 'Chhatrapati Sambhaji Udyan', 'CSU', false, true),
(22, 'PMC Bhavan', 'PMC', false, true),
(23, 'Mangalwar Peth', 'MNP', false, true),
(24, 'Pune Railway Station', 'PRS', false, true),
(25, 'Ruby Hall Clinic', 'RUBY', false, true),
(26, 'Bund Garden', 'BDG', false, true),
(27, 'Yerwada', 'YER', false, true),
(28, 'Kalyani Nagar', 'KLY', false, true),
(29, 'Ramwadi', 'RMW', false, true),

-- Red Line stations
(30, 'Megapolis Circle', 'MEGA', false, true),
(31, 'Quadron', 'QDR', false, true),
(32, 'Infosys Phase II', 'INF2', false, true),
(33, 'Dohler', 'DOH', false, true),
(34, 'Wipro Technologies', 'WIP', false, true),
(35, 'Pall India', 'PAL', false, true),
(36, 'Shivaji Chowk', 'SVC', false, true),
(37, 'Hinjewadi', 'HIN', false, true),
(38, 'Wakad Chowk', 'WKC', false, true),
(39, 'Balewadi Stadium', 'BWS', false, true),
(40, 'NICMAR', 'NIC', false, true),
(41, 'Ramnagar', 'RAMN', false, true),
(42, 'Laxmi Nagar', 'LXN', false, true),
(43, 'Balewadi Phata', 'BWP', false, true),
(44, 'Baner Gaon', 'BNG', false, true),
(45, 'Baner', 'BAN', false, true),
(46, 'Indian Agricultural Research Institute', 'IARI', false, true),
(47, 'Sakal Nagar', 'SKN', false, true),
(48, 'Savitribai Phule Pune University', 'SPPU', false, true),
(49, 'Reserve Bank of India', 'RBI', false, true),
(50, 'Agriculture College', 'AGC', false, true);

-- ================= LINE-STATION MAPPING =================
-- Line 1: Purple Line - PCMC Bhavan to Swargate
INSERT INTO line_stations (id, line_id, station_id, station_order, distance_from_start) VALUES
(1, 1, 1, 1, 0.0),
(2, 1, 2, 2, 1.5),
(3, 1, 3, 3, 3.0),
(4, 1, 4, 4, 4.5),
(5, 1, 5, 5, 6.0),
(6, 1, 6, 6, 7.5),
(7, 1, 7, 7, 9.0),
(8, 1, 8, 8, 10.5),
(9, 1, 9, 9, 12.0),
(10, 1, 10, 10, 13.5),
(11, 1, 11, 11, 15.0),
(12, 1, 12, 12, 16.5),
(13, 1, 13, 13, 18.0),
(14, 1, 14, 14, 19.5),

-- Line 2: Aqua Line - Vanaz to Ramwadi
(15, 2, 15, 1, 0.0),
(16, 2, 16, 2, 1.5),
(17, 2, 17, 3, 3.0),
(18, 2, 18, 4, 4.5),
(19, 2, 19, 5, 6.0),
(20, 2, 20, 6, 7.5),
(21, 2, 21, 7, 9.0),
(22, 2, 22, 8, 10.5),
(23, 2, 11, 9, 12.0),
(24, 2, 23, 10, 13.5),
(25, 2, 24, 11, 15.0),
(26, 2, 25, 12, 16.5),
(27, 2, 26, 13, 18.0),
(28, 2, 27, 14, 19.5),
(29, 2, 28, 15, 21.0),
(30, 2, 29, 16, 22.5),

-- Line 3: Red Line - Megapolis Circle to Civil Court
(31, 3, 30, 1, 0.0),
(32, 3, 31, 2, 1.5),
(33, 3, 32, 3, 3.0),
(34, 3, 33, 4, 4.5),
(35, 3, 34, 5, 6.0),
(36, 3, 35, 6, 7.5),
(37, 3, 36, 7, 9.0),
(38, 3, 37, 8, 10.5),
(39, 3, 38, 9, 12.0),
(40, 3, 39, 10, 13.5),
(41, 3, 40, 11, 15.0),
(42, 3, 41, 12, 16.5),
(43, 3, 42, 13, 18.0),
(44, 3, 43, 14, 19.5),
(45, 3, 44, 15, 21.0),
(46, 3, 45, 16, 22.5),
(47, 3, 46, 17, 24.0),
(48, 3, 47, 18, 25.5),
(49, 3, 48, 19, 27.0),
(50, 3, 49, 20, 28.5),
(51, 3, 50, 21, 30.0),
(52, 3, 10, 22, 31.5),
(53, 3, 11, 23, 33.0);
