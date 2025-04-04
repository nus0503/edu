
DROP TABLE school_level;
create table school_level
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR2(10) NOT NULL,
    CONSTRAINT school_level_pk primary key (id)
);


DROP TABLE grade;
create table grade
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    school_level_id NUMBER(4) NOT NULL,
    name VARCHAR2(10) NOT NULL,
    CONSTRAINT grade_pk PRIMARY KEY (id),
    CONSTRAINT grade_fk FOREIGN KEY (school_level_id) REFERENCES school_level(id)
)

DROP TABLE subject;
CREATE TABLE subject
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR2(10) NOT NULL ,
    CONSTRAINT subject_pk PRIMARY KEY (id)
)


DROP TABLE unit;
CREATE TABLE unit
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    subject_id NUMBER(4) NOT NULL,
    grade_id NUMBER(4) NOT NULL,
    name VARCHAR2(30) NOT NULL,
    sequence NUMBER(4) NOT NULL, --null로 변경할 것--
    CONSTRAINT unit_pk PRIMARY KEY (id),
    CONSTRAINT unit_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT unit_grade_fk FOREIGN KEY (grade_id) REFERENCES grade(id)
)


DROP TABLE sub_unit;
CREATE TABLE sub_unit
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    unit_id NUMBER(4) NOT NULL,
    name VARCHAR2(30) NOT NULL,
    sequence NUMBER(4),
    CONSTRAINT sub_unit_pk PRIMARY KEY (id),
    CONSTRAINT sub_unit_unit_fk FOREIGN KEY (unit_id) REFERENCES unit(id)
)



DROP TABLE problem;
CREATE TABLE problem --문제
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    sub_unit_id NUMBER(4) NOT NULL,
    difficulty NUMBER(2) NOT NULL,
    problem_type VARCHAR2(10) NOT NULL,
    created_at DATE,
    updated_at DATE,
    CONSTRAINT problem_pk PRIMARY KEY (id),
    CONSTRAINT problem_sub_unit_fk FOREIGN KEY (sub_unit_id) REFERENCES sub_unit(id)
);



DROP TABLE media_asset;
-- 이미지/도형 저장소
CREATE TABLE media_asset
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    width NUMBER(10),
    height NUMBER(10),
    alt_text VARCHAR(255),
    CONSTRAINT media_asset_pk PRIMARY KEY (id)
);
DROP TABLE Problem_Segment;
-- 문제 내용 조각 (텍스트/수식/이미지)
CREATE TABLE Problem_Segment
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    problem_id NUMBER(4) NOT NULL,
    order_num NUMBER(4) NOT NULL,
    content_type VARCHAR2(20) NOT NULL,
    content VARCHAR2(1000),
    alignment VARCHAR2(20) DEFAULT 'inline',
    media_id NUMBER(4),
    CONSTRAINT problem_segment_pk PRIMARY KEY (id),
    CONSTRAINT problem_segment_problem_fk FOREIGN KEY (problem_id) REFERENCES Problem(id),
    CONSTRAINT problem_segment_media_fk FOREIGN KEY (media_id) REFERENCES media_asset(id)
);

-- 객관식 선택지
DROP TABLE Problem_Option;
CREATE TABLE Problem_Option
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    problem_id NUMBER(4) NOT NULL,
    option_number NUMBER(2) NOT NULL,
    is_correct NUMBER(1) NOT NULL,
    CONSTRAINT problem_option_pk PRIMARY KEY (id),
    CONSTRAINT problem_option_problem_fk FOREIGN KEY (problem_id) REFERENCES Problem(id)
);


-- 선택지 내용 조각
DROP TABLE Option_Segment;
CREATE TABLE Option_Segment
(
    id NUMBER(4) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    option_id NUMBER(4) NOT NULL,
    order_num NUMBER(4) NOT NULL,
    content_type VARCHAR2(10) NOT NULL,
    content VARCHAR2(500),
    alignment VARCHAR2(10),
    media_id NUMBER(4),
    CONSTRAINT option_segment_pk PRIMARY KEY (id),
    CONSTRAINT option_segment_fk1 FOREIGN KEY (option_id) REFERENCES Problem_Option(id),
    CONSTRAINT option_segment_fk2 FOREIGN KEY (media_id) REFERENCES Media_Asset(id)
);