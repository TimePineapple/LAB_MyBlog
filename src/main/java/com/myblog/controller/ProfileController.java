package com.myblog.controller;

import com.myblog.model.MeResponse;
import com.myblog.model.PersonalProfile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {
    // 统一维护资料，让 /me 和 /profile 保持一致。请替换为自己的信息。
    private final PersonalProfile personalProfile = createProfile();

    @GetMapping("/profile")
    public PersonalProfile profile() {
        return personalProfile;
    }

    @GetMapping("/me")
    public MeResponse me() {
        MeResponse response = new MeResponse();
        response.name = personalProfile.blogName;
        response.owner = personalProfile.owner;
        response.introduction = personalProfile.introduction;
        return response;
    }

    private static PersonalProfile createProfile() {
        PersonalProfile profile = new PersonalProfile();
        profile.blogName = "MyBlog";
        profile.owner = "时光菠萝";
        profile.introduction = "发烧友";
        profile.interests = "滑雪";
        profile.vision = "个人展示页";
        return profile;
    }
}
