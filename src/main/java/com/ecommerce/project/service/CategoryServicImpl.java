package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.hibernate.boot.model.source.spi.Sortable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServicImpl implements  CategoryService {


    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;


    @Autowired
    public  CategoryServicImpl(CategoryRepository categoryRepository , ModelMapper modelMapper){
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public CategoryResponse getAllCategory(Integer pageNumber , Integer pageSize , String sortBy , String sortOrder) {
       Sort  sortByAndOrder  = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber , pageSize , sortByAndOrder );
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();
        if (categories.isEmpty()){
            throw new APIException("No category created till now !");
        }
        List<CategoryDTO> categoryDTOS = categories.stream().
                map(category -> modelMapper.map(category , CategoryDTO.class)).
                toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalpages(categoryPage.getTotalPages());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }


    @Override
    public CategoryDTO  createCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO , Category.class);
        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if(categoryFromDb !=null){
            throw new APIException("Category with the name "+ category.getCategoryName() +" already exists");
        }
        Category savedCategory =   categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory , CategoryDTO.class);
        return savedCategoryDTO;
    }


    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
       Category category = categoryRepository.findById(categoryId)
               .orElseThrow(()-> new ResourceNotFoundException("Category" , "categoryId" , categoryId));
       categoryRepository.delete(category);
       CategoryDTO deletedCategoryDTO = modelMapper.map(category, CategoryDTO.class);
        return deletedCategoryDTO;
    }


    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category savedCategory =  categoryRepository.findById(categoryId).
                orElseThrow(()-> new ResourceNotFoundException("Category" , "categoryId" , categoryId));
        categoryDTO.setCategoryId(categoryId);
        Category category = modelMapper.map(categoryDTO, Category.class);
        savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}
