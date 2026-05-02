-- search-test-data.sql

DELETE FROM application_status;
DELETE FROM student_courses;
DELETE FROM students;

INSERT INTO students (id, name, furigana, nickname, email, area, age, gender, remarks, is_deleted)
VALUES
    (1, '山田 太郎', 'ヤマダ タロウ', 'たろう', 'taro.yamada@example.com', '東京都', 25, '男性', '', 0),
    (2, '佐藤 花子', 'サトウ ハナコ', 'はなちゃん', 'hanako.sato@example.com', '大阪府', 28, '女性', '', 0),
    (3, '鈴木 一郎', 'スズキ イチロウ', 'いっちー', 'ichiro.suzuki@example.com', '福岡県', 30, '男性', '', 0),
    (4, '高橋 美咲', 'タカハシ ミサキ', 'みーちゃん', 'misaki.takahashi@example.com', '北海道', 22, '女性', '', 0),
    (5, '田中 恒一', 'タナカ コウイチ', 'こうちゃん', 'koichi.tanaka@example.com', '愛知県', 27, '男性', '', 0),
    (6, '山本 直樹', 'ヤマモト ナオキ', 'なおき', 'naoki.yamamoto@example.com', '東京都', 35, '男性', '', 0),
    (7, '田辺 彩', 'タナベ アヤ', 'あや', 'aya.tanabe@example.com', '大阪府', 24, '女性', '', 0);

INSERT INTO student_courses (id, student_id, course_name, course_start_at, course_end_at)
VALUES
    (1, 1, 'Javaコース', '2025-01-01 10:00:00', '2026-01-01 10:00:00'),
    (2, 2, 'AWSコース', '2025-02-01 10:00:00', '2026-02-01 10:00:00'),
    (3, 3, 'Javaコース', '2025-03-01 10:00:00', '2026-03-01 10:00:00'),
    (4, 4, 'Webデザインコース', '2025-04-01 10:00:00', '2026-04-01 10:00:00'),
    (5, 5, 'Javaコース', '2025-05-01 10:00:00', '2026-05-01 10:00:00'),
    (6, 6, 'AWSコース', '2025-06-01 10:00:00', '2026-06-01 10:00:00'),
    (7, 7, 'Webデザインコース', '2025-07-01 10:00:00', '2026-07-01 10:00:00');

INSERT INTO application_status (id, student_course_id, status, is_deleted)
VALUES
    (1, 1, '仮申込', 0),
    (2, 2, '本申込', 0),
    (3, 3, '受講中', 0),
    (4, 4, '終了', 0),
    (5, 5, '仮申込', 0),
    (6, 6, '受講中', 0);

-- student_course_id = 7 は application_status なし
-- LEFT JOIN で status が NULL のケース確認用