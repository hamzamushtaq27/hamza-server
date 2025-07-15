-- Sample hospitals data
INSERT INTO hospitals (id, name, address, latitude, longitude, phone, specialization, rating, created_at, updated_at)
VALUES
    (1, '서울대학교병원', '서울특별시 종로구 대학로 101', 37.5799, 126.9985, '02-2072-2114', '정신건강의학과', 4.5, NOW(), NOW()),
    (2, '삼성서울병원', '서울특별시 강남구 일원로 81', 37.4883, 127.0851, '02-3410-2114', '정신건강의학과', 4.3, NOW(), NOW()),
    (3, '세브란스병원', '서울특별시 서대문구 연세로 50-1', 37.5642, 126.9401, '02-2228-5800', '정신건강의학과', 4.4, NOW(), NOW()),
    (4, '아산병원', '서울특별시 송파구 올림픽로 43길 88', 37.5266, 127.1086, '02-3010-3114', '정신건강의학과', 4.2, NOW(), NOW()),
    (5, '강북삼성병원', '서울특별시 종로구 새문안로 29', 37.5735, 126.9681, '02-2001-2001', '정신건강의학과', 4.1, NOW(), NOW());

-- Insert sample treatments
INSERT INTO treatments (id, name, description, type, duration_minutes, difficulty_level, created_at, updated_at) VALUES
(1, '인지행동치료 (CBT)', '부정적 사고 패턴을 인식하고 변화시키는 치료법', 'CBT', 20, 'BEGINNER', NOW(), NOW()),
(2, '마음챙김 명상', '현재 순간에 집중하여 마음의 평온을 찾는 명상', 'MEDITATION', 10, 'BEGINNER', NOW(), NOW()),
(3, '점진적 근육 이완법', '몸의 긴장을 풀어주는 이완 기법', 'RELAXATION', 15, 'BEGINNER', NOW(), NOW()),
(4, '호흡 조절 운동', '깊은 호흡을 통한 스트레스 완화', 'RELAXATION', 5, 'BEGINNER', NOW(), NOW()),
(5, '가벼운 운동 요법', '우울증 완화를 위한 운동 프로그램', 'EXERCISE', 30, 'INTERMEDIATE', NOW(), NOW());

-- Insert sample hospitals
INSERT INTO hospitals (id, name, address, phone, latitude, longitude, department, rating, created_at, updated_at) VALUES
(1, '서울대학교병원 정신건강의학과', '서울특별시 종로구 대학로 101', '02-2072-2972', 37.5800, 126.9999, '정신건강의학과', 4.5, NOW(), NOW()),
(2, '강남세브란스병원 정신건강의학과', '서울특별시 강남구 언주로 211', '02-2019-3341', 37.5196, 127.0234, '정신건강의학과', 4.3, NOW(), NOW()),
(3, '삼성서울병원 정신건강의학과', '서울특별시 강남구 일원로 81', '02-3410-3583', 37.4881, 127.0854, '정신건강의학과', 4.4, NOW(), NOW()),
(4, '아산의료원 정신건강의학과', '서울특별시 송파구 올림픽로43길 88', '02-3010-3740', 37.5262, 127.1083, '정신건강의학과', 4.2, NOW(), NOW()),
(5, '연세대학교 의과대학 세브란스병원', '서울특별시 서대문구 연세로 50-1', '02-2228-5926', 37.5665, 126.9388, '정신건강의학과', 4.6, NOW(), NOW());

-- Insert sample treatment programs
INSERT INTO treatment_programs (id, name, description, duration_weeks, sessions_per_week, target_condition, created_at, updated_at) VALUES
(1, '우울증 극복 프로그램', '단계별 CBT 기법을 활용한 우울증 치료 프로그램', 8, 3, 'DEPRESSION', NOW(), NOW()),
(2, '불안 관리 프로그램', '불안증상 완화를 위한 종합적 관리 프로그램', 6, 4, 'ANXIETY', NOW(), NOW()),
(3, '스트레스 완화 프로그램', '일상 스트레스 관리를 위한 실용적 프로그램', 4, 5, 'STRESS', NOW(), NOW()),
(4, '수면 개선 프로그램', '수면 패턴 개선을 위한 전문 프로그램', 6, 2, 'SLEEP_DISORDER', NOW(), NOW()),
(5, '자존감 향상 프로그램', '자기 효능감과 자존감 향상을 위한 프로그램', 12, 2, 'SELF_ESTEEM', NOW(), NOW());

-- Insert sample treatment contents
INSERT INTO treatment_contents (id, treatment_id, title, content, content_type, sequence_order, created_at, updated_at) VALUES
(1, 1, 'CBT 기초 이해하기', '인지행동치료의 기본 개념과 원리를 학습합니다.', 'TEXT', 1, NOW(), NOW()),
(2, 1, '부정적 사고 패턴 인식하기', '자신의 부정적 사고 패턴을 인식하고 기록하는 방법을 배웁니다.', 'TEXT', 2, NOW(), NOW()),
(3, 2, '마음챙김 명상 기초', '기본적인 마음챙김 명상 기법을 배우고 실습합니다.', 'AUDIO', 1, NOW(), NOW()),
(4, 2, '호흡 중심 명상', '호흡에 집중하는 명상 기법을 연습합니다.', 'AUDIO', 2, NOW(), NOW()),
(5, 3, '점진적 근육 이완 기법', '몸의 각 부위를 순차적으로 이완시키는 방법을 배웁니다.', 'AUDIO', 1, NOW(), NOW());

-- Insert sample chatbot responses
INSERT INTO chatbot_responses (id, trigger_keyword, response_text, response_type, priority, created_at, updated_at) VALUES
(1, '우울', '우울한 기분이 드시는군요. 지금 어떤 상황에서 그런 감정을 느끼시는지 말씀해 주실 수 있나요?', 'EMPATHY', 1, NOW(), NOW()),
(2, '불안', '불안하신 마음 충분히 이해해요. 지금 가장 걱정되는 것이 무엇인지 이야기해 주세요.', 'EMPATHY', 1, NOW(), NOW()),
(3, '스트레스', '스트레스를 받고 계시는군요. 잠시 깊게 숨을 들이마시고 천천히 내쉬어 보세요.', 'GUIDANCE', 1, NOW(), NOW()),
(4, '자살', '지금 매우 힘든 시간을 보내고 계시는 것 같아요. 전문가와 상담하는 것이 좋겠어요. 생명의전화 1588-9191에 연락해 보세요.', 'CRISIS', 10, NOW(), NOW()),
(5, '죽고싶어', '정말 힘드시겠어요. 혼자 견디지 마시고 도움을 요청하세요. 즉시 전문가와 상담받을 수 있도록 연결해 드리겠습니다.', 'CRISIS', 10, NOW(), NOW());

-- Insert sample user (for testing, password is 'password123')
INSERT INTO users (id, email, password, nickname, provider, provider_id, role, created_at, updated_at) VALUES
(1, 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe8FRmjSP4OtKBpzXOxzFm', '테스트유저', 'LOCAL', null, 'USER', NOW(), NOW()),
(2, 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe8FRmjSP4OtKBpzXOxzFm', '관리자', 'LOCAL', null, 'ADMIN', NOW(), NOW());

-- Insert sample diagnosis for test user
INSERT INTO diagnoses (id, user_id, total_score, severity, diagnosis_date, created_at, updated_at) VALUES
(1, 1, 12, 'MODERATE', NOW(), NOW(), NOW()),
(2, 1, 8, 'MILD', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Insert sample diagnosis answers
INSERT INTO diagnosis_answers (id, diagnosis_id, question_id, answer_score, created_at, updated_at) VALUES
(1, 1, 1, 2, NOW(), NOW()),
(2, 1, 2, 3, NOW(), NOW()),
(3, 1, 3, 2, NOW(), NOW()),
(4, 1, 4, 3, NOW(), NOW()),
(5, 1, 5, 2, NOW(), NOW());

-- Insert sample recommendations
INSERT INTO recommendations (id, user_id, diagnosis_id, treatment_id, recommended_date, status, created_at, updated_at) VALUES
(1, 1, 1, 1, NOW(), 'ACTIVE', NOW(), NOW()),
(2, 1, 1, 2, NOW(), 'ACTIVE', NOW(), NOW()),
(3, 1, 1, 3, NOW(), 'PENDING', NOW(), NOW());

-- Insert sample user progress
INSERT INTO user_progress (id, user_id, treatment_id, completed_sessions, total_sessions, last_session_date, progress_percentage, created_at, updated_at) VALUES
(1, 1, 1, 5, 24, NOW(), 20.8, NOW(), NOW()),
(2, 1, 2, 12, 24, NOW(), 50.0, NOW(), NOW());