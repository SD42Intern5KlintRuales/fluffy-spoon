package com.example.excelvalidator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RuleExecutorRegistry {

    private final Map<String, RuleExecutor<?>> executors;

    @Autowired
    public RuleExecutorRegistry(List<RuleExecutor<?>> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(RuleExecutor::getKey, Function.identity()));
    }

    public Optional<RuleExecutor<?>> get(String key) {
        return Optional.ofNullable(executors.get(key));
    }
}
