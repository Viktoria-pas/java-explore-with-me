package ru.practicum.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Music");
        testCategoryDto = new CategoryDto(1L, "Music");
        newCategoryDto = new NewCategoryDto("Music");
    }

    @Test
    void getCategories_ShouldReturnCategoryList() {
        Page<Category> categoryPage = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);
        when(categoryMapper.toCategoryDto(testCategory)).thenReturn(testCategoryDto);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategoryDto, result.get(0));
    }

    @Test
    void createCategory_WithValidData_ShouldReturnCategoryDto() {
        when(categoryRepository.existsByName("Music")).thenReturn(false);
        when(categoryMapper.toCategory(newCategoryDto)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toCategoryDto(testCategory)).thenReturn(testCategoryDto);

        CategoryDto result = categoryService.createCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals(testCategoryDto, result);
        verify(categoryRepository).existsByName("Music");
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void createCategory_WithExistingName_ShouldThrowConflictException() {
        when(categoryRepository.existsByName("Music")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(newCategoryDto));
        verify(categoryRepository).existsByName("Music");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WithValidData_ShouldReturnUpdatedCategory() {
        CategoryDto updateDto = new CategoryDto(1L, "Updated Music");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Music")).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toCategoryDto(testCategory)).thenReturn(updateDto);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Music", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(testCategory);
    }
}


