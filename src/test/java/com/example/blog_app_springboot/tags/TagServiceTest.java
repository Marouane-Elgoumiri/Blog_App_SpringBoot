package com.example.blog_app_springboot.tags;

import com.example.blog_app_springboot.common.exceptions.DuplicateResourceException;
import com.example.blog_app_springboot.common.exceptions.ResourceNotFoundException;
import com.example.blog_app_springboot.tags.dtos.TagResponse;
import com.example.blog_app_springboot.tags.entity.TagEntity;
import com.example.blog_app_springboot.tags.repository.TagRepository;
import com.example.blog_app_springboot.tags.service.TagService;
import com.example.blog_app_springboot.common.utils.SlugGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private SlugGenerator slugGenerator;

    @InjectMocks
    private TagService tagService;

    private TagEntity sampleTag;

    @BeforeEach
    void setUp() {
        sampleTag = TagEntity.builder()
                .id(1L)
                .name("java")
                .slug("java")
                .build();
    }

    @Test
    void can_get_all_tags() {
        when(tagRepository.findAll()).thenReturn(List.of(sampleTag));

        List<TagResponse> tags = tagService.getAllTags();

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("java");
    }

    @Test
    void can_get_tag_by_slug() {
        when(tagRepository.findBySlug("java")).thenReturn(Optional.of(sampleTag));

        TagResponse response = tagService.getTagBySlug("java");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("java");
    }

    @Test
    void throws_when_tag_not_found_by_slug() {
        when(tagRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagBySlug("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void can_create_tag() {
        when(tagRepository.existsByName("python")).thenReturn(false);
        when(slugGenerator.generate("python")).thenReturn("python");
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(invocation -> {
            TagEntity saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TagResponse response = tagService.createTag("python");

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("python");
        assertThat(response.getSlug()).isEqualTo("python");
        verify(tagRepository).save(any(TagEntity.class));
    }

    @Test
    void throws_on_duplicate_tag() {
        when(tagRepository.existsByName("java")).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag("java"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tag already exists");
    }

    @Test
    void can_delete_tag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(sampleTag));

        tagService.deleteTag(1L);

        verify(tagRepository).delete(sampleTag);
    }

    @Test
    void throws_when_deleting_nonexistent_tag() {
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTag(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag not found");
    }

    @Test
    void find_or_create_tags_returns_existing_and_creates_new() {
        TagEntity existing = TagEntity.builder()
                .id(1L)
                .name("java")
                .slug("java")
                .build();

        when(tagRepository.findByNameInIgnoreCase(List.of("java", "python")))
                .thenReturn(List.of(existing));
        when(slugGenerator.generate("python")).thenReturn("python");
        when(tagRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = tagService.findOrCreateTags(List.of("java", "python"));

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(TagEntity::getName))
                .containsExactlyInAnyOrder("java", "python");
    }

    @Test
    void find_or_create_tags_returns_empty_for_null_input() {
        var result = tagService.findOrCreateTags(null);
        assertThat(result).isEmpty();
    }

    @Test
    void find_or_create_tags_returns_empty_for_empty_input() {
        var result = tagService.findOrCreateTags(List.of());
        assertThat(result).isEmpty();
    }
}
