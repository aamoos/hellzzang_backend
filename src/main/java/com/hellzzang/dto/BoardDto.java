package com.hellzzang.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * packageName    : com.hellzzang.dto
 * fileName       : BoardDto
 * author         : hj
 * date           : 2023-06-14
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-14        hj       최초 생성
 */
@Data
public class BoardDto {

    @NotNull
    private Long id; //postId

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private int likes;

    @NotNull
    private String nickname;

    private Long comments;

    @QueryProjection
    public BoardDto(Long id, String title, String content, int likes, String nickname, Long comments){
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.nickname = nickname;
        this.comments = comments;
    }
}
