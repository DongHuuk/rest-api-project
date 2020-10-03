package org.kuroneko.restapiproject.account.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountPasswordForm {

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
