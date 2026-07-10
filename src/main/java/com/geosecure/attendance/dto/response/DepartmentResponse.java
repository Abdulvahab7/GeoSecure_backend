package com.geosecure.attendance.dto.response;

import com.geosecure.attendance.entity.Department;

public class DepartmentResponse {

    private Integer id;
    private String name;
    private String code;

    public static DepartmentResponse from(Department d) {
        DepartmentResponse r = new DepartmentResponse();
        r.id = d.getId();
        r.name = d.getName();
        r.code = d.getCode();
        return r;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
