package com.avaclone.utils.forms.fields;

import com.avaclone.utils.forms.ValidableField;
import com.avaclone.utils.forms.ValidationFailedException;

/**
 * Created by jedzej on 11.05.2017.
 */

public class NonEmptyValidator implements ValidableField.Validable<String> {

    @Override
    public void validate(String data) throws ValidationFailedException {
        if (data.isEmpty())
            throw new ValidationFailedException("Field required");
    }
}
