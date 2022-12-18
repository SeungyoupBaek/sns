package com.youp.sns.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youp.sns.controller.request.PostCommentRequest;
import com.youp.sns.controller.request.PostCreateRequest;
import com.youp.sns.controller.request.PostModifyRequest;
import com.youp.sns.exception.ErrorCode;
import com.youp.sns.exception.SnsApplicationException;
import com.youp.sns.fixture.PostEntityFixture;
import com.youp.sns.model.Post;
import com.youp.sns.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    @WithMockUser
    @DisplayName("포스트작성")
    void postCreate() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("포스트작성_로그인하지않은경우")
    void postCreate_notLoginState() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCreateRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트수정")
    void postUpdate() throws Exception {
        String title = "title";
        String body = "body";

        when(postService.modify(eq(title), eq(body), any(), any())).thenReturn(
                Post.fromEntity(PostEntityFixture.get("userName", 1, 1)));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("포스트수정시_로그인하지않은경우")
    void postUpdate_notLoginState() throws Exception {
        String title = "title";
        String body = "body";

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트수정시_본인이_작성한_글이_아니라면_에러발생")
    void postUpdate_notMyPost() throws Exception {
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService)
                .modify(eq(title), eq(body), any(), eq(1));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트수정시_수정하려는_글이_없는경우_에러발생")
    void postUpdate_notExist() throws Exception {
        String title = "title";
        String body = "body";

        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService)
                .modify(eq(title), eq(body), any(), eq(1));

        mockMvc.perform(put("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostModifyRequest(title, body)))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트삭제")
    void postDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("포스트삭제시_로그인하지_않은_경우")
    void postDelete_notLoginState() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트삭제시_작성자와_삭제요청자가_다를경우")
    void postDelete_notMyPost() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.INVALID_PERMISSION)).when(postService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("포스트삭제시_삭제하려는_포스트가_존재하지_않은_경우")
    void postDelete_notExist() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).delete(any(), any());

        mockMvc.perform(delete("/api/v1/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("피드목록")
    void feedList() throws Exception {
        when(postService.list(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("피드목록요청시_로그인하지_않은_경우")
    void feedList_notLoginState() throws Exception {
        when(postService.list(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("내피드목록")
    void myFeedList() throws Exception {
        when(postService.my(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("내피드목록요청시_로그인하지_않은_경우")
    void myFeedList_notLoginState() throws Exception {
        when(postService.my(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요기능")
    void like() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("좋아요버튼클릭시_로그인하지_않은경우")
    void like_notLoginState() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("좋아요버튼클릭시_게시물이_없는경우")
    void like_notExist() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).like(any(), any());
        mockMvc.perform(post("/api/v1/posts/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글기능")
    void reply() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("댓글작성시_로그인하지_않은경우")
    void reply_notLoginState() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글작성시_게시물이_없는경우")
    void reply_notExist() throws Exception {
        doThrow(new SnsApplicationException(ErrorCode.POST_NOT_FOUND)).when(postService).comment(any(), any(), any());
        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostCommentRequest("comment")))
                ).andDo(print())
                .andExpect(status().isNotFound());
    }
}
