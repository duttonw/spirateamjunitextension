package com.inflectra.spiratest.addons.junitextension;


import org.apache.commons.lang.StringUtils;

public class SpiraServerConfiguration {
    private String url = null;
    private String login = null;
    private String password = null;
    private Integer projectId = -1;
    private Integer releaseId = -1;

    public boolean isEmpty() {
        return StringUtils.isBlank(url) || StringUtils.isBlank(login) || StringUtils.isBlank(password)
                || (projectId == -1);
    }

    public static SpiraServerConfiguration create(SpiraServerConfiguration copy){
        return new SpiraServerConfiguration().withLogin(copy.getLogin())
                .withPassword(copy.getPassword())
                .withProjectId(copy.getProjectId()).withReleaseId(copy.getReleaseId()).withUrl(copy.getUrl());
    }

    public SpiraServerConfiguration() {
    }



    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public void setReleaseId(Integer releaseId) {
        this.releaseId = releaseId;
    }

    public SpiraServerConfiguration withUrl(String url) {
        this.url = url;
        return this;
    }

    public SpiraServerConfiguration withLogin(String login) {
        this.login = login;
        return this;
    }

    public SpiraServerConfiguration withPassword(String password) {
        this.password = password;
        return this;
    }

    public SpiraServerConfiguration withProjectId(Integer projectId) {
        this.projectId = projectId;
        return this;
    }

    public SpiraServerConfiguration withReleaseId(Integer releaseId) {
        this.releaseId = releaseId;
        return this;
    }
}
