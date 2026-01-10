-- Discussion Forum Tables

-- Discussion Topics Table
CREATE TABLE discussion_topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_solved BOOLEAN DEFAULT FALSE,
    views_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
);

-- Discussion Replies Table
CREATE TABLE discussion_replies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_solution BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES discussion_topics(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_topic_id (topic_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- Discussion Likes Table (for upvoting)
CREATE TABLE discussion_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT,
    reply_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES discussion_topics(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_id) REFERENCES discussion_replies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_topic_like (topic_id, user_id),
    UNIQUE KEY unique_reply_like (reply_id, user_id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_reply_id (reply_id),
    INDEX idx_user_id (user_id)
);

-- Discussion Attachments Table
CREATE TABLE discussion_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT,
    reply_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES discussion_topics(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_id) REFERENCES discussion_replies(id) ON DELETE CASCADE,
    INDEX idx_topic_id (topic_id),
    INDEX idx_reply_id (reply_id)
);

-- Add some sample data (optional)
-- INSERT INTO discussion_topics (class_id, created_by, title, content, is_pinned) 
-- VALUES (1, 1, 'Welcome to the Discussion Forum!', 'Feel free to ask questions and help each other.', TRUE);
