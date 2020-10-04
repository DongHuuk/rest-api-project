package org.kuroneko.restapiproject.community.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CommunityForm {

    @NotEmpty
    private String title;

    @NotEmpty
    private String manager;

}
