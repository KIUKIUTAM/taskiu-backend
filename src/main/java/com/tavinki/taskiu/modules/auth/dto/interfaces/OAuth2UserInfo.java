package com.tavinki.taskiu.modules.auth.dto.interfaces;

public interface OAuth2UserInfo {
    String getEmail();

    String getName();

    String getSub();

    String getPicture();

    void setPicture(String picture);

}
