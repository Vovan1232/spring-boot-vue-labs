package ua.chytalnia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleInput(
    @NotBlank(message = "Вкажіть назву статті.") @Size(max = 120, message = "Назва може містити до 120 символів.") String title,
    @NotBlank(message = "Вкажіть автора.") @Size(max = 80, message = "Ім’я автора може містити до 80 символів.") String author,
    @NotBlank(message = "Додайте короткий опис.") @Size(max = 1000, message = "Опис може містити до 1000 символів.") String summary
) {}
