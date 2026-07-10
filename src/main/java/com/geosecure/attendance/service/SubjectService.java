package com.geosecure.attendance.service;

import com.geosecure.attendance.dto.request.SubjectRequest;
import com.geosecure.attendance.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    List<SubjectResponse> findAll();

    SubjectResponse findById(Integer id);

    SubjectResponse create(SubjectRequest request);

    SubjectResponse update(Integer id, SubjectRequest request);

    void delete(Integer id);
}
