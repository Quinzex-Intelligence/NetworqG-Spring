package com.networq.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse
{
    private boolean authenticated;
    private String id;
    private String googleSub;
    private String email;
    private String name;
    private String picture;
}
