package com.project.revhive.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummary {
    private Long id;
    private String username;
    private String profilePicture;
}
