-- 학교 급 데이터
INSERT INTO School_Level (name) VALUES ('중학교');

-- 학년 데이터
INSERT INTO Grade (SCHOOL_LEVEL_ID, name)
VALUES (1, '1학년');

-- 교과 데이터
INSERT INTO Subject (name) VALUES ('수학');

-- 단원 데이터
INSERT INTO Unit (subject_id, grade_id, name, sequence)
VALUES (1, 1, '기하', 1);

-- 소단원 데이터
INSERT INTO Sub_Unit (unit_id, name, sequence)
VALUES (1, '삼각형의 넓이', 1);

-- 문제 기본 정보
INSERT INTO Problem (sub_unit_id, difficulty, problem_type)
VALUES (1, 3, '객관식');

-- 이미지 저장
-- INSERT INTO Media_Asset (file_path, file_type, width, height, alt_text)
-- VALUES
--     ('/images/triangle.png', 'image/png', 300, 200, '삼각형 도식도'),
-- ('/images/formula1.png', 'image/png', 150, 50, '넓이 공식');

-- 문제 내용
INSERT INTO Problem_Segment (problem_id, order_num, content_type, content, alignment, media_id)
VALUES
(1, 1, 'text', '다음 삼각형의 넓이는?', 'left', NULL);
INSERT INTO Problem_Segment (problem_id, order_num, content_type, content, alignment, media_id)
VALUES
(1, 2, 'formula', 'S = \frac{1}{2}bh', 'center', NULL);


-- 객관식 선택지
INSERT INTO Problem_Option (problem_id, option_number, is_correct)
VALUES
    (1, 1, 0);
INSERT INTO Problem_Option (problem_id, option_number, is_correct)
VALUES
(1, 2, 1);
INSERT INTO Problem_Option (problem_id, option_number, is_correct)
VALUES
(1, 3, 0);
INSERT INTO Problem_Option (problem_id, option_number, is_correct)
VALUES
(1, 4, 0);


-- 선택지 내용
INSERT INTO Option_Segment (option_id, order_num, content_type, content, alignment, media_id)
VALUES
    (1, 1, 'text', '30cm²', 'center', NULL);
INSERT INTO Option_Segment (option_id, order_num, content_type, content, alignment, media_id)
VALUES
(1, 2, 'image', NULL, 'center', null);
INSERT INTO Option_Segment (option_id, order_num, content_type, content, alignment, media_id)
VALUES
(1, 3, 'formula', 'S = bh', 'center', NULL);
INSERT INTO Option_Segment (option_id, order_num, content_type, content, alignment, media_id)
VALUES
(1, 4, 'text', '45cm²', 'center', NULL);