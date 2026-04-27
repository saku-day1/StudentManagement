INSERT INTO students (name, furigana, nickname, email, area, age, gender, remarks, is_deleted)
VALUES
    ('山田 太郎', 'ヤマダ タロウ', 'たろう', 'taro.yamada@example.com', '東京都', 25, '男性', '', 0),
    ('佐藤 花子', 'サトウ ハナコ', 'はなちゃん', 'hanako.sato@example.com', '大阪府', 28, '女性', '', 0),
    ('鈴木 一郎', 'スズキ イチロウ', 'いっちー', 'ichiro.suzuki@example.com', '福岡県', 30, '男性', '', 0),
    ('高橋 美咲', 'タカハシ ミサキ', 'みーちゃん', 'misaki.takahashi@example.com', '北海道', 22, '女性', '', 0),
    ('田中 恒一', 'タナカ コウイチ', 'こうちゃん', 'koichi.tanaka@example.com', '愛知県', 27, '男性', '', 0);

INSERT INTO students_courses (student_id, course_name, course_start_at, course_end_at)
VALUES
    (1, 'Javaコース', '2026-04-01 00:00:00', '2027-04-01 00:00:00'),
    (2, 'AWSコース', '2026-04-05 00:00:00', '2027-04-05 00:00:00'),
    (3, 'Webデザインコース', '2026-04-10 00:00:00', '2027-04-10 00:00:00'),
    (4, 'Javaコース', '2026-04-15 00:00:00', '2027-04-15 00:00:00'),
    (5, 'AWSコース', '2026-04-20 00:00:00', '2027-04-20 00:00:00');

INSERT INTO application_statuses (
    student_course_id, status, is_deleted, deleted_at
)
VALUES
    -- 6か月より古い削除済み：物理削除される想定
    (1, '仮申込', 1, '2025-09-01 00:00:00'),

    -- 6か月以内の削除済み：残る想定
    (2, '本申込', 1, '2026-02-01 00:00:00'),

    -- 未削除：残る想定
    (3, '受講中', 0, NULL),

    -- 未削除：残る想定
    (4, '仮申込', 0, NULL),

    -- 6か月より古い削除済み：物理削除される想定
    (5, '本申込', 1, '2025-08-01 00:00:00');