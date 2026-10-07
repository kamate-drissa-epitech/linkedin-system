package com.linkedin.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Document for post search
 * Index when post is created
 * Enable full text search across Post document
 */
@Document(indexName = "posts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private String authorId;

    private String imageUrl;




    private LocalDateTime createdAt;
}
