package org.kuroneko.restapiproject.domain;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class AccountForm {

    @Column(unique = true)
    @NotEmpty
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{2,10}$", message = "can't be used characters yours. Please check your username.")
    private String username;

    @Email(message = "Please check your email. is not Email Type")
    @NotEmpty
    @Column(unique = true)
    private String email;

    @NotEmpty
    @Length(min = 8, max = 12, message = "can't be used characters yours. Please check your password.")
    private String password;

    @NotEmpty
    @Length(min = 8, max = 12, message = "can't be used characters yours. Please check your password.")
    private String checkingPassword;

    public boolean checkedPassword() {
        return this.password.equals(checkingPassword);
    }

}
