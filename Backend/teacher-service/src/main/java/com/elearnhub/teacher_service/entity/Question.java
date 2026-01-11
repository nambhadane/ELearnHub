package com.elearnhub.teacher_service.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(nullable = false)
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    
    @Column(nullable = false)
    private Integer marks;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String correctAnswer; // For SHORT_ANSWER and TRUE_FALSE
    
    @Column(columnDefinition = "TEXT")
    private String explanation; // Optional explanation shown after submission
    
    // Constructors
    public Question() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Quiz getQuiz() {
        return quiz;
    }
    
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public Integer getMarks() {
        return marks;
    }
    
    public void setMarks(Integer marks) {
        this.marks = marks;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public List<QuestionOption> getOptions() {
        return options;
    }
    
    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
