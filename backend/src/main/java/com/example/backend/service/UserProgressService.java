package com.example.backend.service;

import com.example.backend.model.UserProgress;
import com.example.backend.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    public List<UserProgress> findProgressByUserId(Long userId) {
        return userProgressRepository.findByUsersUserId(userId);
    }

    public Optional<UserProgress> findProgressById(Long id) {
        return userProgressRepository.findById(id);
    }

    public void saveProgress(UserProgress progress) {
        userProgressRepository.save(progress);
    }
}
