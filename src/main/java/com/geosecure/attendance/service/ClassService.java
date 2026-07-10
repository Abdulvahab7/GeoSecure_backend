package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.ClassRequest;
import com.geosecure.attendance.dto.response.ClassResponse;

import java.util.List;

public interface ClassService {

    List<ClassResponse> findAll();

    ClassResponse findById(Integer id);

    ClassResponse create(ClassRequest request);

    ClassResponse update(Integer id, ClassRequest request);
}
