package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {


    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper , FileService fileService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.fileService = fileService;
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        productDTO.setProductId(null);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category" , "categoryId" , categoryId));
        boolean isProductNotPresent  = true;
        List<Product> products = category.getProducts();
        for (Product value : products){
            if (value.getProductName().trim().toLowerCase().equals(productDTO.getProductName().toLowerCase().trim())){
                isProductNotPresent = false;
                break;
            }
        }
        if(isProductNotPresent){
            Product product = modelMapper.map(productDTO , Product.class);
            product.setCategory(category);
            double specialPrice = product.getPrice() -
                    (product.getDiscount() * 0.01) * product.getPrice();
            product.setSpecialPrice(specialPrice);
            product.setImage("default.png");
            Product savedProduct = productRepository.save(product);
            return  modelMapper.map(savedProduct , ProductDTO.class);
        }else {
            throw new APIException("Product Already exists !!");
        }

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of( pageNumber , pageSize , sortByAndOrder);
        Page<Product>  pageProducts = productRepository.findAll(pageDetails);
        List<Product> products =  pageProducts.getContent();

       List<ProductDTO> productDTOS  = products.stream()
               .map(product -> modelMapper.map(product , ProductDTO.class)).toList();
        if (productDTOS.isEmpty()){
            throw new APIException("No products Exists !");
        }
        ProductResponse  productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

       return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                ()-> new ResourceNotFoundException("Category" , "cateogryId" , categoryId)
        );
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of( pageNumber , pageSize , sortByAndOrder);
        Page<Product>  pageProducts = productRepository.findByCategoryOrderByPriceAsc(category , pageDetails);
        List<Product> products =  pageProducts.getContent();

        List<ProductDTO> productDTOS  = products.stream()
                .map(product -> modelMapper.map(product , ProductDTO.class)).toList();
        if(products.size() == 0){
            throw new APIException(category.getCategoryName() +" category does not have any products");
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;

    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of( pageNumber , pageSize , sortByAndOrder);
        Page<Product>  pageProducts = productRepository.findByProductNameLikeIgnoreCase('%'+ keyword + '%'  , pageDetails);
        List<Product> products =  pageProducts.getContent();

        List<ProductDTO> productDTOS  = products.stream()
                .map(product -> modelMapper.map(product , ProductDTO.class)).toList();

        if(products.size() == 0){
            throw new APIException("Products not found with Keyword " + keyword);
        }
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;

    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //  Get the existing peoduct from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product" ,"productId" , productId));

        Product product = modelMapper.map(productDTO, Product.class);
        //  Update the product info with user shared in request body
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //  Save to DataBase
        Product savedProduct =  productRepository.save(productFromDb);
        return modelMapper.map(savedProduct , ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product  = productRepository.findById(productId).orElseThrow(
                ()->{
                    throw new ResourceNotFoundException("Product" , "ProductId" , productId);
                });
        productRepository.delete(product);
        return modelMapper.map(product , ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //  Get the Products from DB
        Product productFromDb = productRepository.findById(productId).orElseThrow(
                ()->{throw new ResourceNotFoundException("Product" , "ProductId" , productId);}
        );
        // Upload image to server
        //  Get the file name of uploaded image
        String fileName = fileService.uploadImage(path , image);
        //  Updating the new file name to the product
        productFromDb.setImage(fileName);
        //  Save updated Product
        Product updatedProduct = productRepository.save(productFromDb);
        // return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct , ProductDTO.class);
    }


}
