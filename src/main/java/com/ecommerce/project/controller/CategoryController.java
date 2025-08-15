package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CategoryController {


    private CategoryService categoryService;


    @Autowired
    public  CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }


     @GetMapping("/public/categories")
    //@RequestMapping(value = "/public/categories" , method = RequestMethod.GET)
    //@Tag(name = "Category API" , description = "APIs For managing products")
     public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber" , defaultValue = AppConstants.PAGE_NUMBER , required = false) Integer pageNumber ,
            @RequestParam(name = "pageSize" , defaultValue = AppConstants.PAGE_SIZE , required = false) Integer pageSize ,
            @RequestParam(name = "sortBy" , defaultValue = AppConstants.SORT_CATEGORY_BY , required = false) String sortBy ,
            @RequestParam(name = "sortOrder"  , defaultValue = AppConstants.SORT_DIR , required = false) String sortOrder){
        CategoryResponse categoryResponse = categoryService.getAllCategory(pageNumber , pageSize , sortBy , sortOrder );
        return  new ResponseEntity<>(categoryResponse , HttpStatus.OK);
    }


    @PostMapping("/public/categories")
    @ApiResponses({
            @ApiResponse(responseCode = "201" , description ="Category is created Successfully" ),
            @ApiResponse(responseCode = "400" , description ="Invalid Input" , content = @Content),
            @ApiResponse(responseCode = "500" , description ="Internal server error" , content = @Content),
    })
    @Operation(summary = "Create Category", description = "API to create a new category")
    public  ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO){
       CategoryDTO savedCategoryDTO=  categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }


    @DeleteMapping("/admin/categories/{categoryId}")
    public  ResponseEntity<CategoryDTO> deleteCategory(@Parameter(description = "ID of Category that u wish to delete")
            @PathVariable Long categoryId){
        CategoryDTO deletedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deletedCategory , HttpStatus.OK);
        //  return  ResponseEntity.status(HttpStatus.OK).body(status);
        //return  ResponseEntity.ok(status);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO ,  @PathVariable Long categoryId){
            CategoryDTO saveCategoryDTO = categoryService.updateCategory(categoryDTO , categoryId);
            return new  ResponseEntity<>(saveCategoryDTO  , HttpStatus.OK);
    }
}
