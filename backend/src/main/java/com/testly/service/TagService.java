package com.testly.service;

import com.testly.dto.TagDtos.*;
import com.testly.entity.Tag;
import com.testly.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> listAll() {
        return tagRepository.findAll().stream()
                .map(t -> new TagResponse(t.getId(), t.getName()))
                .collect(Collectors.toList());
    }

    public TagResponse create(TagRequest request) {
        Tag tag = tagRepository.findByNameIgnoreCase(request.getName().trim())
                .orElseGet(() -> tagRepository.save(Tag.builder().name(request.getName().trim()).build()));
        return new TagResponse(tag.getId(), tag.getName());
    }

    /**
     * Resolves a list of tag names (as typed by the teacher, optional) into Tag entities,
     * creating any that don't exist yet. Returns an empty set if names is null/empty --
     * tags are optional, a question can have zero tags.
     */
    public Set<Tag> resolveOrCreate(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }
        return names.stream()
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .map(name -> tagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build())))
                .collect(Collectors.toSet());
    }
}
