package com.networq.dto;

import com.networq.entity.Blogs;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BlogCursorResponse {

    private List<Blogs> blogs;
    private String nextCursor;
    private boolean hasMore;
}