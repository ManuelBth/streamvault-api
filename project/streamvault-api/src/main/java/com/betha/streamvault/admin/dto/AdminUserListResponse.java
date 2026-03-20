package com.betha.streamvault.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {

    private List<AdminUserResponse> users;
    private long total;
    private int page;
    private int size;
}
