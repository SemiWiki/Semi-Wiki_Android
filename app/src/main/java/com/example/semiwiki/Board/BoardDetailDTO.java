package com.example.semiwiki.Board;

import java.util.List;

public class BoardDetailDTO {

    private String title;
    private String createdAt;
    private String modficatedAt;
    private List<UserPreview> users;
    private List<String> categories;

    private List<HeaderDTO> noticeBoardHeaders;

    public String getTitle() { return title; }
    public String getCreatedAt() { return createdAt; }
    public String getModficatedAt() { return modficatedAt; }
    public List<UserPreview> getUsers() { return users; }
    public List<String> getCategories() { return categories; }
    public List<HeaderDTO> getNoticeBoardHeaders() { return noticeBoardHeaders; }



    public static class UserPreview {
        private long id;
        private String accountId;
        private Integer studentNum;
        private String username;
        private String role;

        public long getId() { return id; }
        public String getAccountId() { return accountId; }
        public Integer getStudentNum() { return studentNum; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }

    public static class HeaderDTO {
        private long id;
        private String title;

        private String headerNumber;

        private int number;

        private int level;

        private String contents;

        private List<HeaderDTO> children;

        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getHeaderNumber() { return headerNumber; }
        public int getNumber() { return number; }
        public int getLevel() { return level; }
        public String getContents() { return contents; }
        public List<HeaderDTO> getChildren() { return children; }
    }
}
