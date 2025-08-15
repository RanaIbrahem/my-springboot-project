package com.ecommerce.project.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Schema(description = "Category Id for a particular category" ,example = "123")
    private Long categoryId;
    @NotBlank(message = "Category must not be NULL")
    @Schema(description = "Category Name for category u wish to create")
    @Size(min = 5, message = "Category Name must contain at least 5 characters")
    private String categoryName;
}
