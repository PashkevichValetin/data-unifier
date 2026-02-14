package com.pashcevich.data_unifier.service;

public interface DataUnificationService {
    void processAllData();
    void processUserData();
    void processOrderData();
    void processUserById(Long userId);
    long getProcessedCount();
}